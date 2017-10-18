package de.torbilicious.steam.api

import com.google.gson.Gson
import khttp.get


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

    init {
        steamId = loadId(76561198031026305)
    }

    fun loadId(id: Long): SteamId {
        val gamesResponseString = getSteamResponse(SteamInterface.IPlayerService,
                "GetOwnedGames",
                mapOf("include_appinfo" to "1"),
                id)

        val gamesRespone = gson.fromJson(gamesResponseString, GameResponse::class.java)

        val summaryResponseString = getSteamResponse(SteamInterface.ISteamUser,
                "GetPlayerSummaries",
                mapOf("steamids" to id.toString()),
                id,
                2)

        val summaryRespone = gson.fromJson(summaryResponseString, IdSummaryResponse::class.java)
        val player = summaryRespone.response.players.first()

        steamId = SteamId(id,
                gamesRespone.response.games,
                player.personaname,
                player.avatar)

        return steamId

//        val appIds = gamesRespone.response.games.sortedBy { it.name }//.take(5)

//        appIds.forEach { println("${it.appid} - ${it.name}") }
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


//data class AllGamesRespone(val applist: AllSteamGamesWrapper)
//data class AllSteamGamesWrapper(val apps: AllSteamGames)
//typealias AllSteamGames = Array<AllSteamGamesGame>
//data class AllSteamGamesGame(val appid: Number, val name: String)

data class GameResponse(val response: PlayerGames)
data class PlayerGames(val game_count: Number, val games: Games)
typealias Games = List<Game>
data class Game(val appid: Number, val playtime_forever: String, val name: String, val img_logo_url: String) {
    fun getLogoUrl(): String = "http://media.steampowered.com/steamcommunity/public/images/apps/$appid/$img_logo_url.jpg"

}

data class IdSummaryResponse(val response: IdSummary)
data class IdSummary(val players: PlayerSummaries)
typealias PlayerSummaries = List<PlayerSummary>
data class PlayerSummary(val personaname: String, val avatar: String)

