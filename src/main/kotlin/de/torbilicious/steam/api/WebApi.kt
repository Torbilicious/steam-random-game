package de.torbilicious.steam.api

import com.google.gson.Gson
import khttp.get
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


val baseUrl = "https://api.steampowered.com"

val headers = mapOf("Accept" to "application/json")
val apiKey: String? = System.getenv("API_KEY")

val gson = Gson()

enum class SteamInterface {
    IPlayerService,
    ISteamApps,
    ISteamUser
}

class WebApi {

    private var steamId: SteamId
    private var friends: SteamFriends = listOf()

    init {
        if (apiKey == null || apiKey == "") {
            error("No Apikey provided.")
        }

        steamId = loadId(76561198031026305).first
    }

    fun loadId(id: Long): Pair<SteamId, SteamFriends> {
        @Suppress("UNNECESSARY_SAFE_CALL")
        if (id == steamId?.id) return Pair(steamId, friends)

        val friendListResponseString = getSteamResponse(SteamInterface.ISteamUser,
                "GetFriendList",
                mapOf("steamid" to id.toString()),
                1)

        val friendListResponse = gson.fromJson(friendListResponseString, FriendListResponse::class.java)

        val friendsDto = friendListResponse.friendslist.friends

        friends = loadExcludingFriends(friendsDto.map { it.steamid.toLong() })

        steamId = loadExcludingFriends(listOf(id)).first()

        return Pair(steamId, friends)
    }

    private fun loadExcludingFriends(ids: List<Long>): List<SteamId> {
        println("Loading ${ids.size} ids")

        val deferred = ids.map {
            async(CommonPool) {
                val gamesResponseString = getSteamResponse(SteamInterface.IPlayerService,
                        "GetOwnedGames",
                        mapOf("include_appinfo" to "1",
                                "include_played_free_games" to "1",
                                "steamid" to it.toString()))

                Pair(it, gson.fromJson(gamesResponseString, GameResponse::class.java))
            }
        }

        val games = runBlocking {
            deferred.map { it.await() }
        }

        val summaryResponseString = getSteamResponse(SteamInterface.ISteamUser,
                "GetPlayerSummaries",
                mapOf("steamids" to ids.joinToString(",")),
                2)

        val summaryResponse = gson.fromJson(summaryResponseString, IdSummaryResponse::class.java)

        return summaryResponse.response.players.map { player ->
            SteamId(player.steamid.toLong(),
                    games
                            .find { it.first == player.steamid.toLong() }
                            ?.second?.response?.games ?: listOf(),
                    player.personaname,
                    player.avatar)
        }

    }

    private fun getSteamResponse(steamInterface: SteamInterface,
                                 method: String,
                                 parameters: Map<String, String>,
                                 version: Int = 1): String {
        val sb = StringBuilder()

        sb.append("$baseUrl/$steamInterface/$method/v$version/?key=$apiKey")

        parameters.forEach { k, v ->
            sb.append("&$k=$v")
        }

        return get(sb.toString(), headers).text
    }
}

data class SteamId(val id: Long,
                   val games: Games,
                   val nickname: String,
                   val avatarUrl: String)

typealias SteamFriends = List<SteamId>


//data class AllGamesRespone(val applist: AllSteamGamesWrapper)
//data class AllSteamGamesWrapper(val apps: AllSteamGames)
//typealias AllSteamGames = Array<AllSteamGamesGame>
//data class AllSteamGamesGame(val appid: Number, val name: String)

data class GameResponse(val response: PlayerGames)
data class PlayerGames(val game_count: Number, val games: Games?)
typealias Games = List<Game>
data class Game(val appid: Number, val playtime_forever: String, val name: String, val img_logo_url: String) {
    fun getLogoUrl(): String = "http://media.steampowered.com/steamcommunity/public/images/apps/$appid/$img_logo_url.jpg"

}

data class IdSummaryResponse(val response: IdSummary)
data class IdSummary(val players: PlayerSummaries)
typealias PlayerSummaries = List<PlayerSummary>
data class PlayerSummary(val personaname: String, val avatar: String, val steamid: String)

data class FriendListResponse(val friendslist: Friendslist)
data class Friendslist(val friends: Friends)
typealias Friends = List<Friend>
data class Friend(val steamid: String)
