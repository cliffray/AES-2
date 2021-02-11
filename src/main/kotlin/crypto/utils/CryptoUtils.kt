package crypto.utils

import model.CipherTransformation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import java.util.Base64.getEncoder
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具
 */
class CryptoUtils {

    companion object {
        private const val BLOCK_SIZE = 16

        /**
         * 完整加密過程，解密出來的檔案為encryptedFile（附檔名為env）
         * @param cipherTransformation CipherTransformation
         * @param keySize Int
         * @param ivSize Int
         * @param originalCompleteFilename String 相對路徑(絕對路徑) + 檔名
         * @param encodedKey String
         * @param ivString String
         * @return Unit
         */
        @JvmStatic fun encrypt(cipherTransformation: CipherTransformation, keySize: Int, ivSize: Int, originalCompleteFilename: String, encodedKey: String, ivString: String) {
            val keyGen = KeyGenerator.getInstance(cipherTransformation.algorithm)
            keyGen.init(keySize, SecureRandom())
            val secretKey = if (encodedKey.isEmpty()) keyGen.generateKey() else convertStringToSecretKey(cipherTransformation.algorithm, encodedKey)
            var iv = ByteArray(ivSize)

            if (ivString.isEmpty()) {
                val prng = SecureRandom()
                prng.nextBytes(iv)
            } else {
                iv = ivString.split(',').map { it.toByte() }.toByteArray()
            }

            val originalFile = File(originalCompleteFilename)
            val encryptedFile = File("src/main/resources/output/${originalFile.name}.enc")

            // 將iv輸出
            if (ivString.isEmpty()) {
                val file = File("src/main/resources/output/iv-${Date().time}.txt")
                val outputIvString = iv.joinToString(",")
                val outStream = FileOutputStream(file)
                outStream.write(outputIvString.toByteArray())
            }

            // 將key輸出
            if (encodedKey.isEmpty()) {
                val file = File("src/main/resources/output/key-${Date().time}.txt")
                val outputKey = convertSecretKeyToString(secretKey)
                val outStream = FileOutputStream(file)
                outStream.write(outputKey.toByteArray())
            }

            this.encrypt(cipherTransformation, secretKey, iv, originalFile, encryptedFile)
        }


        /**
         * 加密
         * @param cipherTransformation CipherTransformation
         * @param secretKey SecretKey
         * @param iv ByteArray
         * @param originFile File 輸入檔案 (明文)
         * @param encryptedFile File 輸出檔案 (密文)
         * @return Unit
         * @throws Exception
         */
        @Throws(Exception::class)
        fun encrypt(cipherTransformation: CipherTransformation, secretKey: SecretKey, iv: ByteArray, originFile: File, encryptedFile: File)  {
            val cipher = Cipher.getInstance(cipherTransformation.getText())

            if (cipherTransformation.isNeedIv()) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            }

            val buf = ByteArray(4096)

            val inStream = FileInputStream(originFile)
            val outStream = FileOutputStream(encryptedFile)

            var readBytes = inStream.read(buf);
            while(readBytes > 0) {
                val cipherBytes = cipher.update(buf, 0 , readBytes)
                outStream.write(cipherBytes)
                readBytes = inStream.read(buf)
            }
            cipher.doFinal()
        }


        /**
         * 完整解密過程
         * @param cipherTransformation CipherTransformation
         * @param keySize Int
         * @param ivSize Int
         * @param originalCompleteFilename String 相對路徑(絕對路徑) + 檔名(格式為.原始副檔名.enc)
         * @param encodedKey String
         * @param ivString String
         * @return Unit
         */
        @JvmStatic fun decrypt(cipherTransformation: CipherTransformation, keySize: Int, ivSize: Int, originalCompleteFilename: String, encodedKey: String, ivString: String) {
            val keyGen = KeyGenerator.getInstance(cipherTransformation.algorithm)
            keyGen.init(keySize, SecureRandom())
            val secretKey = if (encodedKey.isEmpty()) keyGen.generateKey() else convertStringToSecretKey(cipherTransformation.algorithm, encodedKey)
            var iv = ByteArray(ivSize)

            if (ivString.isEmpty()) {
                val prng = SecureRandom()
                prng.nextBytes(iv)
            } else {
                iv = ivString.split(',').map { it.toByte() }.toByteArray()
            }

            val originalFile = File(originalCompleteFilename)

            val outputFilename = "src/main/resources/output/${originalFile.name}"
            val encryptedCompleteFilename = outputFilename.subSequence(0, outputFilename.length - 4).toString() // 去除.enc
            val encryptedFile = File(encryptedCompleteFilename)

            println("${cipherTransformation.getText()} / $keySize / $ivSize / $iv")

            this.decrypt(cipherTransformation, secretKey, iv, originalFile, encryptedFile)
        }

        /**
         * 解密
         * @param cipherTransformation CipherTransformation
         * @param secretKey SecretKey
         * @param iv ByteArray
         * @param cipherTextFile File
         * @param decryptedFile File
         * @return Unit
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic fun decrypt(cipherTransformation: CipherTransformation, secretKey: SecretKey, iv: ByteArray, cipherTextFile: File, decryptedFile: File) {
            val cipher = Cipher.getInstance(cipherTransformation.getText())

            if (cipherTransformation.isNeedIv()) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }

            if(!decryptedFile.exists()){
                decryptedFile.createNewFile() //: Here, it may be fail if ...
            }

            val buf = ByteArray(4096)

            val inStream = FileInputStream(cipherTextFile)
            val outStream = FileOutputStream(decryptedFile)

            var readBytes = inStream.read(buf)

            while(readBytes > 0){
                val decryptedBytes = cipher.update(buf, 0 , readBytes)
                outStream.write(decryptedBytes)
                readBytes = inStream.read(buf)
            }

            cipher.doFinal();
        }

        /**
         * 參考網址: [key/string轉換](https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa)
         * @param secretKey SecretKey
         * @return String
         */
        private fun convertSecretKeyToString(secretKey: SecretKey): String {
            // get base64 encoded version of the key
            return Base64.getEncoder().encodeToString(secretKey.encoded) // encodedKey
        }

        /**
         *
         * @param algorithm String
         * @param encodedKey String
         * @return SecretKey
         */
        private fun convertStringToSecretKey(algorithm: String, encodedKey: String): SecretKey {
            // decode the base64 encoded string
            val decodedKey = Base64.getDecoder().decode(encodedKey)
            // rebuild key using SecretKeySpec
            return SecretKeySpec(decodedKey, 0, decodedKey.size, algorithm) // originalKey
        }

        /**
         * 部分加密
         * @param secretKey SecretKey
         * @param iv ByteArray
         * @param cipherTextFile File
         * @param blockIndex Int
         * @param blockCount Int
         * @return ByteArray
         * @throws Exception
         */
        @Deprecated("測試用")
        @Throws(Exception::class)
        fun decryptPartial(secretKey: SecretKey, iv: ByteArray, cipherTextFile: File, blockIndex: Int, blockCount: Int) : ByteArray {
            val offset = blockIndex * BLOCK_SIZE;
            val bufSize = blockCount * BLOCK_SIZE;

            val cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, calculateIVForBlock(IvParameterSpec(iv), blockIndex.toLong()));

            var decryptedBytes = ByteArray(bufSize)

            val inStream = FileInputStream(cipherTextFile)

            val inputBuf = ByteArray(bufSize)
            inStream.skip(offset.toLong())

            val readBytes = inStream.read(inputBuf)
            decryptedBytes = cipher.update(inputBuf, 0, readBytes)

            return decryptedBytes;
        }

        /**
         * 計算 block 的 iv
         * @param iv IvParameterSpec
         * @param blockIndex Long
         * @return IvParameterSpec
         */
        @Deprecated("包含在decryptPartial")
        private fun calculateIVForBlock(iv: IvParameterSpec, blockIndex: Long): IvParameterSpec {
            val biginIV = BigInteger(1, iv.iv)
            val blockIV = biginIV.add(BigInteger.valueOf(blockIndex))
            val blockIVBytes = blockIV.toByteArray()

            // Normalize the blockIVBytes as 16 bytes for IV
            return when {
                blockIVBytes.size == BLOCK_SIZE -> {
                    IvParameterSpec(blockIVBytes)
                }
                blockIVBytes.size > BLOCK_SIZE -> {
                    // For example: if the blockIVBytes length is 18, blockIVBytes is [0],[1],...[16],[17]
                    // We have to remove [0],[1] , so we change the offset = 2
                    val offset = blockIVBytes.size - BLOCK_SIZE
                    IvParameterSpec(blockIVBytes, offset, BLOCK_SIZE)
                }
                else -> {
                    // For example: if the blockIVBytes length is 14, blockIVBytes is [0],[1],...[12],[13]
                    // We have to insert 2 bytes at head
                    val newBlockIV = ByteArray(BLOCK_SIZE) //: default set to 0 for 16 bytes
                    val offset = blockIVBytes.size - BLOCK_SIZE
                    System.arraycopy(blockIVBytes, 0, newBlockIV, offset, blockIVBytes.size)
                    IvParameterSpec(newBlockIV)
                }
            }
        }

        /**
         * 建立測試檔案
         * @param path String 檔案路徑
         * @return Unit
         * @throws Exception
         */
        @Deprecated("測試用")
        @Throws(Exception::class)
        private fun createTestFile(path: String) {
            val test = File(path)
            val out = FileOutputStream(test)

            val buf = StringBuffer(16);

            val blockCount = 100000

            for (i in 0..blockCount) {
                buf.append(i);
                val size = buf.length

                for (j in 0..(14-size)) {
                    buf.append('#')
                }

                out.write(buf.toString().toByteArray());
                out.write("\r\n".toByteArray());
                buf.delete(0, 16);
            }

        }

        /**
         * 測試加解密過程
         * @return Unit
         * @throws Exception
         * @throws NoSuchAlgorithmException
         */
        @Throws(Exception::class, NoSuchAlgorithmException::class)
        fun test() {
            val keyGen = KeyGenerator.getInstance("AES") // NoSuchAlgorithmException
            keyGen.init(256, SecureRandom() )
            val secretKey = keyGen.generateKey()
            val iv = ByteArray(128 / 8)
            val prng = SecureRandom()
            prng.nextBytes(iv);

            run {
                val originalFile = "src/main/resources/MoAstray_icon.png"
                val encryptedFile = "src/main/resources/output/CipherText.enc"
                val decryptedFile = "src/main/resources/output/Decrypted.mov"

                val cipherTransformation = CipherTransformation("ARS", "CTR", "PKCS5PADDING")

                this.encrypt(cipherTransformation, secretKey, iv, File(originalFile), File(encryptedFile))
                this.decrypt(cipherTransformation, secretKey, iv, File(encryptedFile), File(decryptedFile))
            }
        }

    } // companion object end

}