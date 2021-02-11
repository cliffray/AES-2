package crypto.utils;

import model.JCipherTransformation;
import model.JInputFile;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * 加密工具
 */
public class JCryptoUtils {
    private static final int BLOCK_SIZE = 16;

    /**
     * 完整加密過程，解密出來的檔案為encryptedFile（附檔名為env）
     * @param cipherTransformation CipherTransformation
     * @param keySize Int
     * @param inputFile JInputFile 要加密的檔案
     * @param encodedKey String
     * @param ivString String
     * @return Unit
     */
    public static void encrypt(JCipherTransformation cipherTransformation, int keySize, JInputFile inputFile, String encodedKey, String ivString) throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        KeyGenerator keyGen = KeyGenerator.getInstance(cipherTransformation.algorithm);
        keyGen.init(keySize, new SecureRandom());
        SecretKey secretKey = (encodedKey.isEmpty()) ? keyGen.generateKey() : convertStringToSecretKey(cipherTransformation.algorithm, encodedKey);
        byte[] iv;

        if (ivString.isEmpty()) {
            iv = new byte[cipherTransformation.ivSizeForBytes];
            SecureRandom prng = new SecureRandom();
            prng.nextBytes(iv);
        } else {
            String[] ss = ivString.split(",");
            iv = new byte[ss.length];
            for(int i = 0;i < ss.length;i++){
                iv[i] = Byte.parseByte(ss[i]);
            }
        }

        File originalFile = new File(inputFile.getCompleteFilename());
        File encryptedFile = new File(String.format("%s/output/%s.enc", originalFile.getParentFile().getParent(), originalFile.getName()));

        // 將iv輸出
        if (ivString.isEmpty()) {
            File file = new File(String.format("%s/output/iv-%s.txt", originalFile.getParentFile().getParent(), new Date().getTime()));
            String[] ss = new String[iv.length];
            for(int i = 0;i < ss.length;i++){
                ss[i] = String.valueOf(iv[i]);
            }
            String outputIvString = String.join(",", ss);
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(outputIvString.getBytes());
        }

        // 將key輸出
        if (encodedKey.isEmpty()) {
            File file = new File(String.format("%s/output/key-%s.txt", originalFile.getParentFile().getParent(), new Date().getTime()));
            String outputKey = convertSecretKeyToString(secretKey);
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(outputKey.getBytes());
        }

        encrypt(cipherTransformation, secretKey, iv, originalFile, encryptedFile);
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
    public static void encrypt(JCipherTransformation cipherTransformation, SecretKey secretKey, byte[] iv, File originFile, File encryptedFile) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(cipherTransformation.getText());

        if (cipherTransformation.isNeedIv()) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }

        byte[] buf = new byte[21];

        FileInputStream inStream = new FileInputStream(originFile);
        FileOutputStream outStream = new FileOutputStream(encryptedFile);

        int readBytes = inStream.read(buf);
        while(readBytes > 0) {
            byte[] cipherBytes = cipher.update(buf, 0 , readBytes);
            outStream.write(cipherBytes);
            readBytes = inStream.read(buf);
        }
        outStream.write(cipher.doFinal());

    }

    /**
     * 前置解密過程
     * @param cipherTransformation JCipherTransformation
     * @param keySize Int
     * @param inputFile JInputFile 要解密的檔案
     * @param encodedKey String
     * @param ivString String
     * @return Unit
     */
    public static void decrypt(JCipherTransformation cipherTransformation, int keySize, JInputFile inputFile, String encodedKey, String ivString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        KeyGenerator keyGen = KeyGenerator.getInstance(cipherTransformation.algorithm);
        keyGen.init(keySize, new SecureRandom());
        SecretKey secretKey = (encodedKey.isEmpty()) ? keyGen.generateKey() : convertStringToSecretKey(cipherTransformation.algorithm, encodedKey);
        byte[] iv = new byte[cipherTransformation.ivSizeForBytes];

        if (ivString.isEmpty()) {
            SecureRandom prng = new SecureRandom();
            prng.nextBytes(iv);
        } else {
            String[] ss = ivString.split(",");
            iv = new byte[ss.length];
            for(int i = 0;i < ss.length;i++)
                iv[i] = Byte.parseByte(ss[i]);
        }

        File originalFile = new File(inputFile.getCompleteFilename());

        String outputFilename = String.format("%s/output/%s", originalFile.getParentFile().getParent(), originalFile.getName());
        String encryptedCompleteFilename = outputFilename.subSequence(0, outputFilename.length() - 4).toString(); // 去除.enc
        File encryptedFile = new File(encryptedCompleteFilename);
        //debug用
        System.out.printf(
                "%s / %d / %d / %s",
                cipherTransformation.getText(),
                keySize,
                cipherTransformation.ivSizeForBytes,
                Arrays.toString(iv)
        );

        decrypt(cipherTransformation, secretKey, iv, originalFile, encryptedFile);
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
    public static void decrypt(JCipherTransformation cipherTransformation, SecretKey secretKey, byte[] iv, File cipherTextFile, File decryptedFile) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(cipherTransformation.getText());

        if (cipherTransformation.isNeedIv()) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        }

        if(!decryptedFile.exists()){
            decryptedFile.createNewFile();
        }

        byte[] buf = new byte[32];

        FileInputStream inStream = new FileInputStream(cipherTextFile);
        FileOutputStream outStream = new FileOutputStream(decryptedFile);

        int readBytes = inStream.read(buf);

        while(readBytes > 0){
            byte[] decryptedBytes = cipher.update(buf, 0 , readBytes);
            outStream.write(decryptedBytes);
            readBytes = inStream.read(buf);
        }
        outStream.write(cipher.doFinal());
    }

    /**
     * 參考網址: [key/string轉換](https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa)
     * @param secretKey SecretKey
     * @return String
     */
    private static String convertSecretKeyToString(SecretKey secretKey) {
        // get base64 encoded version of the key
        return Base64.getEncoder().encodeToString(secretKey.getEncoded()); // encodedKey
    }

    /**
     *
     * @param algorithm String
     * @param encodedKey String
     * @return SecretKey
     */
    private static SecretKey convertStringToSecretKey(String algorithm, String encodedKey) {
        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm); // originalKey
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
    //@Deprecated("測試用")
    public static byte[] decryptPartial(SecretKey secretKey, byte[] iv, File cipherTextFile, int blockIndex, int blockCount) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        int offset = blockIndex * BLOCK_SIZE;
        int bufSize = blockCount * BLOCK_SIZE;

        Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, calculateIVForBlock(new IvParameterSpec(iv), Long.valueOf(blockIndex)));

        byte[] decryptedBytes = new byte[bufSize];

        FileInputStream inStream = new FileInputStream(cipherTextFile);

        byte[] inputBuf = new byte[bufSize];
        inStream.skip(Long.valueOf(offset));

        int readBytes = inStream.read(inputBuf);
        decryptedBytes = cipher.update(inputBuf, 0, readBytes);

        return decryptedBytes;
    }

    /**
     * 計算 block 的 iv
     * @param iv IvParameterSpec
     * @param blockIndex Long
     * @return IvParameterSpec
     */
    //@Deprecated("包含在decryptPartial")
    private static IvParameterSpec calculateIVForBlock(IvParameterSpec iv, long blockIndex) {
        BigInteger biginIV = new BigInteger(1, iv.getIV());
        BigInteger blockIV = biginIV.add(BigInteger.valueOf(blockIndex));
        byte[] blockIVBytes = blockIV.toByteArray();

        // Normalize the blockIVBytes as 16 bytes for IV
        if(blockIVBytes.length == BLOCK_SIZE)
            return new IvParameterSpec(blockIVBytes);
        else if(blockIVBytes.length > BLOCK_SIZE){
            // For example: if the blockIVBytes length is 18, blockIVBytes is [0],[1],...[16],[17]
            // We have to remove [0],[1] , so we change the offset = 2
            int offset = blockIVBytes.length - BLOCK_SIZE;
            return new IvParameterSpec(blockIVBytes, offset, BLOCK_SIZE);
        }else{
            // For example: if the blockIVBytes length is 14, blockIVBytes is [0],[1],...[12],[13]
            // We have to insert 2 bytes at head
            byte[] newBlockIV = new byte[BLOCK_SIZE]; //: default set to 0 for 16 bytes
            int offset = blockIVBytes.length - BLOCK_SIZE;
            System.arraycopy(blockIVBytes, 0, newBlockIV, offset, blockIVBytes.length);
            return new IvParameterSpec(newBlockIV);
        }
    }

    /**
     * 建立測試檔案
     * @param path String 檔案路徑
     * @return Unit
     * @throws Exception
     */
    //@Deprecated("測試用")
    private static void createTestFile(String path) throws IOException {
        File test = new File(path);
        FileOutputStream out = new FileOutputStream(test);

        StringBuffer buf = new StringBuffer(16);

        int blockCount = 100000;

        for (int i = 0 ; i <= blockCount ; i++) {
            buf.append(i);
            int size = buf.length();

            for (int j = 0 ; j <= (14-size) ; j++) {
                buf.append('#');
            }

            out.write(buf.toString().getBytes());
            out.write("\r\n".getBytes());
            buf.delete(0, 16);
        }
    }

    /**
     * 測試加解密過程
     * @return Unit
     * @throws Exception
     * @throws NoSuchAlgorithmException
     */
    public static void test() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // NoSuchAlgorithmException
        keyGen.init(256, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        byte[] iv = new byte[128 / 8];
        SecureRandom prng = new SecureRandom();
        prng.nextBytes(iv);

        String originalFile = "C:\\Users\\long9\\Desktop\\AES-2-3\\build\\resources\\main\\img\\MoAstray_icon.png";
        String encryptedFile = "C:\\Users\\long9\\Desktop\\AES-2-3\\build\\resources\\main/output/MoAstray_icon.txt.enc";
        String decryptedFile = "C:\\Users\\long9\\Desktop\\AES-2-3\\build\\resources\\main\\output\\Decrypted.png";

        JCipherTransformation cipherTransformation = new JCipherTransformation("AES", "CFB8", "PKCS5PADDING", 16);

        encrypt(cipherTransformation, secretKey, iv, new File(originalFile), new File(encryptedFile));
        decrypt(cipherTransformation, secretKey, iv, new File(encryptedFile), new File(decryptedFile));
        byte[] bs = tencrypt(cipherTransformation, secretKey, iv, "走れソリよー♪風の様にー♪月見原をー♪ぱどるぱどるー♪");
        System.out.println(tdecrypt(cipherTransformation, secretKey, iv, bs));
    }

    public static byte[] tencrypt(JCipherTransformation cipherTransformation, SecretKey secretKey, byte[] iv, String s) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(cipherTransformation.getText());

        if (cipherTransformation.isNeedIv()) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }

        return cipher.doFinal(s.getBytes());
    }

    public static String tdecrypt(JCipherTransformation cipherTransformation, SecretKey secretKey, byte[] iv, byte[] bs) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(cipherTransformation.getText());

        if (cipherTransformation.isNeedIv()) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        }

        return new String(cipher.doFinal(bs));
    }
}
