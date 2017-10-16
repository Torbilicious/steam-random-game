package de.torbilicious

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.View

fun <T> Iterable<T>.random(): T? {
    return when {
        count() == 0 -> null
        else -> {
            val n = (Math.random() * count()).toInt()
            this.drop(kotlin.comparisons.maxOf(n-1, 0))
                    .take(1)
                    .single()
        }
    }
}

fun View.popup() {
    val dialog = Stage(StageStyle.UTILITY)
    dialog.scene = Scene(this.root)
    dialog.title = this.title

    with(dialog) {
        initModality(Modality.APPLICATION_MODAL)
        height = 300.0
        width = 300.0
        showAndWait()
    }
}
