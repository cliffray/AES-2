package model

/**
 * 存放
 * @property algorithm String 演算法
 * @property operation String 工作模式
 * @property padding String 分組密碼的填充方式
 * @constructor
 */
class CipherTransformation(
    var algorithm: String,
    var operation: String,
    var padding: String) {

    /**
     * 字串格式，"演算法/模式/填充方式", ex: "AES/CTR/PKCS5PADDING"
     * @return String
     */
    fun getText() = "$algorithm/$operation/$padding"

    /**
     * 判斷是否需要初始向量(iv)
     * @return Boolean
     */
    fun isNeedIv() = operation != "ECB"
}