package model

import enums.FileStatusEnum
import javafx.beans.property.SimpleStringProperty
import java.io.File

/**
 * 專案自訂File，存放加解密狀態
 * @property filename SimpleStringProperty 檔案名稱
 * @property completeFilename SimpleStringProperty 完整檔案名稱(包含路徑)
 * @property encryptStatus SimpleStringProperty 加密狀態
 * @property decryptStatus SimpleStringProperty 解密狀態
 * @constructor
 */
class InputFile(completeFilename: String,
                encryptStatus: FileStatusEnum = FileStatusEnum.UNPROCESSED,
                decryptStatus: FileStatusEnum = FileStatusEnum.UNPROCESSED) : File(completeFilename) {

    companion object {
        // 填入property名稱，務必和class的var要一樣
        const val FILE_NAME = "filename"
        const val COMPLETE_FILE_NAME = "completeFilename"
        const val ENCRYPT_STATUS = "encryptStatus"
        const val DECRYPT_STATUS = "decryptStatus"
    }

    private var filename            = SimpleStringProperty(name)
    private var completeFilename    = SimpleStringProperty(completeFilename)
    private var encryptStatus       = SimpleStringProperty(encryptStatus.displayText)
    private var decryptStatus       = SimpleStringProperty(decryptStatus.displayText)

    fun getFilename() =  filename.get()

    fun setFilename(filename: String) {
        this.filename.value = filename
    }

    fun getCompleteFilename() = completeFilename.get()

    fun setCompleteFilename(completeFilename: String) {
        this.completeFilename.value = completeFilename
    }

    fun getEncryptStatus() = encryptStatus.get()

    fun setEncryptStatus(encryptStatus: FileStatusEnum) {
        this.encryptStatus.value = encryptStatus.displayText
    }

    fun getDecryptStatus() = decryptStatus.get()

    fun setDecryptStatus(decryptStatus: FileStatusEnum) {
        this.decryptStatus.value = decryptStatus.displayText
    }

}