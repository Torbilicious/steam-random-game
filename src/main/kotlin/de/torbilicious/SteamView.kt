package de.torbilicious

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException
import com.github.koraktor.steamcondenser.steam.community.SteamId
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.text.Text
import tornadofx.*


class SteamView : View("Steam lib") {

    companion object {
        private val defaultId = 76561198031026305
    }

    override val root = GridPane()
    private var userInput: TextField by singleAssign()
    private var gamesText: Text by singleAssign()
    private var games = mutableListOf<String>()

    private var steamId: SteamId? = null

    init {
        steamId = initUser(defaultId)

        with(root) {
            paddingAll = 3

            prefHeight = 500.0
            prefWidth = 300.0

            row {
                hbox {
                    userInput = textfield ()

                    button ("load") {
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
                                alert(Alert.AlertType.INFORMATION, "Random Game", randomGame)
                            }
                        }
                    }
                }
            }

            row {
                scrollpane {
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    gamesText = text()
                    updateGames()
                }
            }
        }
    }

    private fun updateGames() {
        games = steamId?.games?.values
                ?.map { it.name }
                ?.sorted()
                ?.toMutableList() ?: mutableListOf()

        gamesText.text = games.joinToString("\n")
    }

    private fun initUser(id: Long): SteamId? {
        val steamId = try {
            SteamId.create(id)
        } catch (e: SteamCondenserException) {
            null
        }

        title = "Steam lib(${steamId?.nickname})"
        println("Loaded: ${steamId?.nickname}")
        return steamId
    }
}


