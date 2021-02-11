package model;

/**
 * 存放
 * @property algorithm String 演算法
 * @property operation String 工作模式
 * @property padding String 分組密碼的填充方式
 * @constructor
 */
public class JCipherTransformation {
    public String algorithm;
    public String operation;
    public String padding;
    public final int ivSizeForBytes;

    public JCipherTransformation(String algorithm, String operation, String padding, int ivSizeForBytes){
        this.algorithm = algorithm;
        this.operation = operation;
        this.padding = padding;
        this.ivSizeForBytes = ivSizeForBytes;
    }

    public String getText(){
        System.out.println(String.format("%s/%s/%s", algorithm, operation, padding));
        return String.format("%s/%s/%s", algorithm, operation, padding);
    }

    public boolean isNeedIv(){
        return operation != "ECB";
    }
}
