package gui.javafx.components

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import model.InputFile
import javax.swing.text.TabExpander

/**
 * 檔案列表
 */
class InputFileTableView : TableView<InputFile>() {

    init {
        minWidth = 700.0
        columns.addAll(getTableColumns())
        style = "-fx-text-alignment: left;"
    }

    companion object {
        /**
         * 取得InputFile的欄位資訊
         * @return ObservableList<TableColumn<InputFile, *>>
         */
        fun getTableColumns(): ObservableList<TableColumn<InputFile, *>> {
            val fileNameColumn          = TableColumn<InputFile, String>("檔案名稱")
            val completeFilenameColumn  = TableColumn<InputFile, String>("檔案路徑")
            val encryptStatusColumn     = TableColumn<InputFile, String>("加密狀態")
            val decryptStatusColumn     = TableColumn<InputFile, String>("解密狀態")

            fileNameColumn.cellValueFactory         = PropertyValueFactory<InputFile, String>(InputFile.FILE_NAME)
            completeFilenameColumn.cellValueFactory = PropertyValueFactory<InputFile, String>(InputFile.COMPLETE_FILE_NAME)
            encryptStatusColumn.cellValueFactory    = PropertyValueFactory<InputFile, String>(InputFile.ENCRYPT_STATUS)
            decryptStatusColumn.cellValueFactory    = PropertyValueFactory<InputFile, String>(InputFile.DECRYPT_STATUS)

            val tableColumns = FXCollections.observableArrayList<TableColumn<InputFile, *>>()
            tableColumns.addAll(fileNameColumn, completeFilenameColumn, encryptStatusColumn, decryptStatusColumn)

            return tableColumns
        }
    }

}