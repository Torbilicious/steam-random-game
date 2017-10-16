package de.torbilicious.steam

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App

class SteamApp : App(GamesView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false
    }
}

fun main(args: Array<String>) {
    Application.launch(SteamApp::class.java, *args)
}
