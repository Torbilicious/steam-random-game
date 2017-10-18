package de.torbilicious.steam.api

import khttp.get
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

val storeUrl = "https://store.steampowered.com/app"

val genresRegEx = "<div class=\"details_block\">\\s*<b>Title:</b>[^<]*<br>\\s*<b>Genre:</b>\\s*(<a[^>]*>([^<]+)</a>,?\\s*)+\\s*<br>".toRegex(RegexOption.IGNORE_CASE)
val tagsRegEx = "<a[^>]*class=\"app_tag\"[^>]*>([^<]*)</a>".toRegex(RegexOption.IGNORE_CASE)
val flagsRegEx = "<a href=\"http://store.steampowered.com/search/\\?category2=[0-9]+\" class=\"name\">([^<]*)</a>".toRegex(RegexOption.IGNORE_CASE)

fun getMetadataForGames(appIds: List<Game>) {
    val deferred = appIds.map {
        async(CommonPool) {
            val sb = StringBuilder()
            sb.append("${it.appid} - ${it.name}")
            sb.append("Meta: \n${getMetaFor(it.appid)}")
            sb.append("\n")

            println("calculated ${it.appid}")

            sb.toString()
        }
    }

    runBlocking {
        val texts = deferred.map { it.await() }

        println(texts)
    }
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
