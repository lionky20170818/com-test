package com.ligl.common.feignClient;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * 功能: AES加密器
 * 创建: liguoliang
 * 日期: 2017/7/4 0004 18:19
 * 版本: V1.0
 */
public class AesUtil {

    /**
     * 密钥
     */
    private String key;

    /**
     * 字符集
     */
    private String encoding = "utf-8";

    /**
     * 加密
     *
     * @param content 待加密内容
     * @return
     */
    public String encrypt(String content) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(key.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        byte[] byteContent = content.getBytes(encoding);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] byteRresult = cipher.doFinal(byteContent);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteRresult.length; i++) {
            String hex = Integer.toHexString(byteRresult[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @return
     */
    public String decrypt(String content) throws Exception {
        if (content.length() < 1)
            return null;
        byte[] byteRresult = new byte[content.length() / 2];
        for (int i = 0; i < content.length() / 2; i++) {
            int high = Integer.parseInt(content.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(content.substring(i * 2 + 1, i * 2 + 2), 16);
            byteRresult[i] = (byte) (high * 16 + low);
        }

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(key.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] result = cipher.doFinal(byteRresult);
        return new String(result, encoding);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public static void main(String[] args) throws Exception {
        AesUtil aesUtil = new AesUtil();
        aesUtil.setKey("e#DxeTyUTNu@XJpU");
        //String str = aesUtil.code("{ \"productCode\" : \"123456\"}");
        String str = aesUtil.decrypt("C1E80DF273F6182BF7E208DA83EF6A52E466080501ED75C727D2C337C5BAFECD683C74DE5E065F419C8A9300C7D37E4B23A6190065B1859901F5FBCC77F5BC34BF07DEB7CAB0FA600C6B1F82D6BFDF8A0EBCE86A19F7DB267A7C035D9A44C594");
        System.out.println(str);
    }
}
