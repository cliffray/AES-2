package gui.javafx.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.InputFile;
import model.JInputFile;

public class JInputFileTableView extends TableView<JInputFile>{
    public JInputFileTableView(){
        setMinWidth(700.0);
        getColumns().addAll(getTableColumns());
        setStyle("-fx-text-alignment: left;");
    }

    public static ObservableList<TableColumn<JInputFile, ?>> getTableColumns() {
        TableColumn<JInputFile, String> fileNameColumn          = new TableColumn<>("檔案名稱");
        TableColumn<JInputFile, String> completeFilenameColumn  = new TableColumn<>("檔案路徑");
        TableColumn<JInputFile, String> encryptStatusColumn     = new TableColumn<>("加密狀態");
        TableColumn<JInputFile, String> decryptStatusColumn     = new TableColumn<>("解密狀態");

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<JInputFile, String>(InputFile.FILE_NAME));
        completeFilenameColumn.setCellValueFactory(new PropertyValueFactory<JInputFile, String>(InputFile.COMPLETE_FILE_NAME));
        encryptStatusColumn.setCellValueFactory(new PropertyValueFactory<JInputFile, String>(InputFile.ENCRYPT_STATUS));
        decryptStatusColumn.setCellValueFactory(new PropertyValueFactory<JInputFile, String>(InputFile.DECRYPT_STATUS));

        ObservableList<TableColumn<JInputFile, ?>> tableColumns = FXCollections.observableArrayList();
        tableColumns.addAll(fileNameColumn, completeFilenameColumn, encryptStatusColumn, decryptStatusColumn);

        return tableColumns;
    }
}
