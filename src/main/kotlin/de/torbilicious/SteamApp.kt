package de.torbilicious

import javafx.application.Application
import tornadofx.App

class SteamApp : App(SteamView::class)

fun main(args: Array<String>) {
    Application.launch(SteamApp::class.java, *args)
}
