package gui.javafx.components

import javafx.scene.layout.VBox

/**
 * 自訂的導覽列，放置加解密操作相關的元件
 */
class NavBar : VBox(5.0) {

    init {
        maxWidth = 600.0
        style = "-fx-padding: 10px"
    }

}