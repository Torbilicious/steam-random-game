package de.torbilicious.steam

import de.torbilicious.popup
import de.torbilicious.random
import de.torbilicious.steam.api.Game
import de.torbilicious.steam.api.SteamFriends
import de.torbilicious.steam.api.SteamId
import de.torbilicious.steam.api.WebApi
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import tornadofx.*


class GamesView : View("Steam lib") {

    companion object {
        private val defaultId = 76561198031026305
    }

    override val root = BorderPane()
    private var userInput: TextField by singleAssign()
    private var gamesBox: VBox by singleAssign()
    private var games = mutableListOf<Game>()

    private val api = WebApi()

    private var steamId: SteamId? = null
    private var friends: SteamFriends? = null

    init {
        initUser(defaultId)

        with(root) {
            paddingAll = 3

            prefHeight = 500.0
            prefWidth = 400.0

            top {
                hbox {
                    userInput = textfield(defaultId.toString())

                    button("load") {
                        setOnAction {
                            try {
                                initUser(userInput.text.toLong())
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
                            val friendsView = createFriendsView()
                            friendsView.popup()
                            friendsView.showFriends()
                        }
                    }
                }
            }

            center {
                scrollpane {
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    gamesBox = vbox(spacing = 5)
                    updateGames()
                }
            }
        }
    }

    private fun updateGames() {
        games = steamId?.games
                ?.sortedBy { it.name }
//                ?.take(5)
                ?.toMutableList() ?: mutableListOf()

        gamesBox.clear()

        with(gamesBox) {
            games.forEach {
                hbox(spacing = 10) {
                    imageview(it.getLogoUrl())
                    text(it.name)
                }
            }
        }
    }

    private fun initUser(id: Long) {

        val result = api.loadId(id)
        steamId = result.first
        friends = result.second

        title = "Steam lib(${steamId?.nickname})"
        println("Loaded: ${steamId?.nickname}")
        setStageIcon(Image(steamId?.avatarUrl))
    }

    private fun createFriendsView(): FriendsView {
        return FriendsView(friends ?: listOf())
    }
}
