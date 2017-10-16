package de.torbilicious.steam

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException
import com.github.koraktor.steamcondenser.steam.community.SteamGame
import com.github.koraktor.steamcondenser.steam.community.SteamId
import de.torbilicious.popup
import de.torbilicious.random
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import tornadofx.*


class GamesView : View("Steam lib") {

    companion object {
        private val defaultId = 76561198031026305
    }

    override val root = GridPane()
    private var userInput: TextField by singleAssign()
    private var gamesBox: VBox by singleAssign()
    private var games = mutableListOf<SteamGame>()

    private var steamId: SteamId? = null

    init {
        steamId = initUser(defaultId)

        with(root) {
            paddingAll = 3

            prefHeight = 500.0
            prefWidth = 300.0

            row {
                hbox {
                    userInput = textfield(defaultId.toString())

                    button("load") {
                        setOnAction {
                            try {
                                steamId = initUser(userInput.text.toLong())
                                updateGames()
                            } catch (e: NumberFormatException) {
                                alert(Alert.AlertType.ERROR, "Error parsing ID", "Could not get id from input!")
                            }
                        }
                    }

                    button("random") {
                        setOnAction {
                            if (!games.isEmpty()) {
                                val randomGame = games.random()
                                alert(Alert.AlertType.INFORMATION, "Random Game", randomGame?.name)
                            }
                        }
                    }

                    button("friends") {
                        setOnAction {
                            createFriendsView().popup()
                        }
                    }
                }
            }

            row {
                scrollpane {
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    gamesBox = vbox(spacing = 5)
                    updateGames()
                }
            }
        }
    }

    private fun updateGames() {
        games = steamId?.games?.values
                ?.sortedBy { it.name }
//                ?.take(5)
                ?.toMutableList() ?: mutableListOf()

        with(gamesBox) {
            games.forEach {
                hbox(spacing = 10) {
                    imageview(it.logoThumbnailUrl)
                    text(it.name)
                }
            }
        }
    }

    private fun initUser(id: Long): SteamId? {
        val steamId = try {
            SteamId.create(id)
        } catch (e: SteamCondenserException) {
            null
        }

        steamId?.fetchData()

        title = "Steam lib(${steamId?.nickname})"
        println("Loaded: ${steamId?.nickname}")
        setStageIcon(Image(steamId?.avatarIconUrl))

        return steamId
    }

    private fun createFriendsView(): FriendsView {
        return FriendsView(steamId
                ?.friends
                ?.toList()
                ?: emptyList())
    }
}
