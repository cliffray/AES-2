package model;

import enums.JFileStatusEnum;
import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 專案自訂File，存放加解密狀態
 * @property filename SimpleStringProperty 檔案名稱
 * @property completeFilename SimpleStringProperty 完整檔案名稱(包含路徑)
 * @property encryptStatus SimpleStringProperty 加密狀態
 * @property decryptStatus SimpleStringProperty 解密狀態
 */
public class JInputFile extends File{
    private SimpleStringProperty filename, completeFilename, encryptStatus, decryptStatus;

    public static final String FILE_NAME = "filename";
    public static final String COMPLETE_FILE_NAME = "completeFilename";
    public static final String ENCRYPT_STATUS = "encryptStatus";
    public static final String DECRYPT_STATUS = "decryptStatus";

    public JInputFile(@NotNull String completeFilename) {
        this(completeFilename, JFileStatusEnum.UNPROCESSED, JFileStatusEnum.UNPROCESSED);
    }

    public JInputFile(@NotNull String completeFilename, JFileStatusEnum encryptStatus, JFileStatusEnum decryptStatus){
        super(completeFilename);
        this.filename = new SimpleStringProperty(getName());
        this.completeFilename = new SimpleStringProperty(completeFilename);
        this.encryptStatus = new SimpleStringProperty(encryptStatus.displayText);
        this.decryptStatus = new SimpleStringProperty(decryptStatus.displayText);
    }

    public String getFilename(){
        return filename.get();
    }

    public void setFilename(String filename) {
        this.filename.setValue(filename);
    }

    public String getCompleteFilename(){
        return completeFilename.get();
    }

    public void setCompleteFilename(String completeFilename) {
        this.completeFilename.setValue(completeFilename);
    }

    public String  getEncryptStatus(){
        return encryptStatus.get();
    }

    public void setEncryptStatus(JFileStatusEnum encryptStatus) {
        this.encryptStatus.setValue(encryptStatus.displayText);
    }

    public String  getDecryptStatus(){
        return decryptStatus.get();
    }

    public void setDecryptStatus(JFileStatusEnum decryptStatus) {
        this.decryptStatus.setValue(decryptStatus.displayText);
    }
}
