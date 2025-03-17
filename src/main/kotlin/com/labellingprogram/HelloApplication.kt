package com.labellingprogram

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Screen

class App : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(App::class.java.getResource("/com/labellingprogram/main-view.fxml"))
        val scene = Scene(fxmlLoader.load())


        scene.stylesheets.add(App::class.java.getResource("/com/labellingprogram/styles/styles.css")!!.toExternalForm())


        stage.title = "Label Manager"

        val screenBounds = Screen.getPrimary().visualBounds

        stage.isMaximized = false

        stage.isResizable = true
        stage.height = screenBounds.height

        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(App::class.java)
}
