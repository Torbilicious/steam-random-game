package de.torbilicious

import javafx.scene.layout.GridPane
import tornadofx.*


class SteamLoginWebView : View() {
    override val root = GridPane()

    init {
        with(root) {
            row {
                webview {
                    engine.load("https://steinberg.net")
                }
            }
        }
    }
}
