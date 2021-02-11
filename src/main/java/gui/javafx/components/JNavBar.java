package gui.javafx.components;

import javafx.scene.layout.VBox;

/**
 * 自訂的導覽列，放置加解密操作相關的元件
 */
public class JNavBar extends VBox {
    public JNavBar(){
        super(5.0);
        setMaxWidth(600.0);
        setStyle("-fx-padding: 10px");
    }
}
