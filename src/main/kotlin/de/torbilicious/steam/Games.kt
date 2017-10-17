package de.torbilicious.steam

import com.google.gson.Gson
import de.torbilicious.steam.SteamInterface.IPlayerService
import khttp.get
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


val baseUrl = "https://api.steampowered.com"
val storeUrl = "https://store.steampowered.com/app"

val genresRegEx = "<div class=\"details_block\">\\s*<b>Title:</b>[^<]*<br>\\s*<b>Genre:</b>\\s*(<a[^>]*>([^<]+)</a>,?\\s*)+\\s*<br>".toRegex(RegexOption.IGNORE_CASE)
val tagsRegEx = "<a[^>]*class=\"app_tag\"[^>]*>([^<]*)</a>".toRegex(RegexOption.IGNORE_CASE)
val flagsRegEx = "<a href=\"http://store.steampowered.com/search/\\?category2=[0-9]+\" class=\"name\">([^<]*)</a>".toRegex(RegexOption.IGNORE_CASE)

val headers = mapOf("Accept" to "application/json")
val apiKey = "D7581B32F8F56F62CB1D7F272E50F7D1"
val defaultId = 76561198031026305

val gson = Gson()

enum class SteamInterface {
    IPlayerService,
    ISteamApps
}


fun main(args: Array<String>) {
    val gamesResponseString = getSteamResponse(IPlayerService, "GetOwnedGames")

    val gamesRespone = gson.fromJson(gamesResponseString, GameResponse::class.java)
    val appIds = gamesRespone.response.games.sortedBy { it.name }//.take(5)

    val deferred = appIds.map {
        async(CommonPool) {
            println("${it.appid} - ${it.name}")
            println("Meta: \n${getMetaFor(it.appid)}")
            println()
            it
        }
    }

    runBlocking {
        deferred.forEach {
            it.await()
        }
    }

//    appIds.forEach {
//        println("${it.appid} - ${it.name}")
//        println("Meta: \n${getMetaFor(it.appid)}")
//        println()
//    }

}

fun getSteamResponse(steamInterface: SteamInterface, method: String, version: Int = 1): String {
    return get("$baseUrl/$steamInterface/$method/v$version/" +
            "?key=$apiKey&steamid=$defaultId&include_appinfo=1", headers).text
}

fun getMetaFor(appid: Number): String {
    val html = get("$storeUrl/$appid").text

    val genreMatches = genresRegEx.find(html)
    val genre = genreMatches?.groups?.get(2)?.value

    val tagMatches = tagsRegEx.find(html)
    val tags = tagMatches?.groups?.drop(1)?.map { it?.value?.trim() }?.joinToString("\n", prefix = "    ")

    val flagsMatches = flagsRegEx.find(html)
    val flags = flagsMatches?.groups?.drop(1)?.map { it?.value?.trim() }?.joinToString("\n", prefix = "    ")

    return "Genre:\n    $genre\n" +
            "Tags:\n$tags" +
            "Flags:\n$flags"
}


//data class AllGamesRespone(val applist: AllSteamGamesWrapper)
//data class AllSteamGamesWrapper(val apps: AllSteamGames)
//typealias AllSteamGames = Array<AllSteamGamesGame>
//data class AllSteamGamesGame(val appid: Number, val name: String)


data class GameResponse(val response: PlayerGames)
data class PlayerGames(val game_count: Number, val games: Games)
typealias Games = Array<Game>
data class Game(val appid: Number, val playtime_forever: String, val name: String)
