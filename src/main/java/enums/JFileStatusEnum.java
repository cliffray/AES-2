package enums;

// TODO: "font color" will be added
/**
 * 檔案加解密狀態
 * @property displayText String 顯示「狀態」文字
 * @constructor
 */
public enum JFileStatusEnum {
    UNPROCESSED ("未處理"),
    PROCESSING  ("處理中"),
    FINISH      ("完成"),
    FAILED      ("失敗");

    public String displayText;

    JFileStatusEnum(String displayText){
        this.displayText = displayText;
    }
}
