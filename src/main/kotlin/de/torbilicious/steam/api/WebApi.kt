package de.torbilicious.steam.api

import com.google.gson.Gson
import khttp.get
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


val baseUrl = "https://api.steampowered.com"

val headers = mapOf("Accept" to "application/json")
val apiKey = "D7581B32F8F56F62CB1D7F272E50F7D1"

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
        steamId = loadId(76561198031026305).first
    }

    fun loadId(id: Long): Pair<SteamId, SteamFriends> {

        @Suppress("UNNECESSARY_SAFE_CALL")
        if (id == steamId?.id) return Pair(steamId, friends)

        val friendListResponseString = getSteamResponse(SteamInterface.ISteamUser,
                "GetFriendList",
                mapOf("steamid" to id.toString()),
                id,
                1)

        val friendListResponse = gson.fromJson(friendListResponseString, FriendListResponse::class.java)

        val friendsDto = friendListResponse.friendslist.friends

        //TODO: load friend data via call to "GetPlayerSummaries"

        println("Loading ${friendsDto.size} friends")
        val deferred = friendsDto.map {
            async(CommonPool) {
                loadExcludingFriends(it.steamid.toLong())
            }
        }

        steamId = loadExcludingFriends(id)

        val friends = runBlocking {
            deferred.map { it.await() }
        }

        this.friends = friends

        return Pair(steamId, friends)
    }

    private fun loadExcludingFriends(id: Long): SteamId {
        println("Loading id $id")

        val gamesResponseString = getSteamResponse(SteamInterface.IPlayerService,
                "GetOwnedGames",
                mapOf("include_appinfo" to "1"),
                id)

        val gamesResponse = gson.fromJson(gamesResponseString, GameResponse::class.java)

        val summaryResponseString = getSteamResponse(SteamInterface.ISteamUser,
                "GetPlayerSummaries",
                mapOf("steamids" to id.toString()),
                id,
                2)

        val summaryResponse = gson.fromJson(summaryResponseString, IdSummaryResponse::class.java)
        val player = summaryResponse.response.players.first()

        return SteamId(id,
                gamesResponse.response.games ?: listOf(),
                player.personaname,
                player.avatar)
    }

    private fun getSteamResponse(steamInterface: SteamInterface,
                                 method: String,
                                 parameters: Map<String, String>,
                                 id: Long,
                                 version: Int = 1): String {
        val sb = StringBuilder()

        sb.append("$baseUrl/$steamInterface/$method/v$version/?key=$apiKey&steamid=$id")

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
data class PlayerSummary(val personaname: String, val avatar: String)

data class FriendListResponse(val friendslist: Friendslist)
data class Friendslist(val friends: Friends)
typealias Friends = List<Friend>
data class Friend(val steamid: String)
