package com.satw.demo.Util;

import static com.satw.demo.Util.StringUtil.RipeMD160;
import static com.satw.demo.Util.StringUtil.SHA256;
import static com.satw.demo.Util.StringUtil.base58Decode;
import static com.satw.demo.Util.StringUtil.base58Encode;
import static com.satw.demo.Util.StringUtil.base64Decode;
import static com.satw.demo.Util.StringUtil.base64Encode;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class KeyPairUtil {
    public static Map<String, Key> generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");   //設定ECDSA
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");     //設定SHA
            keyGen.initialize(256, random);                                 //以256位元初始化產生器
            KeyPair keyPair = keyGen.generateKeyPair();                     //產生金鑰對
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            Map<String, Key> result = new HashMap<>();
            result.put("privateKey", privateKey);
            result.put("publicKey", publicKey);
            return result;
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //取得將資料以ECDSA私鑰簽名後的結果（Byte格式）
    public static byte[] applyECDSASig(PrivateKey privateKey, String data) {
        try {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initSign(privateKey);     //以私鑰初始化簽名
            ecdsa.update(data.getBytes());  //放入資料
            return ecdsa.sign();            //簽名
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //以ECDSA公鑰驗證驗證簽名者
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);      //以公鑰初始化簽名驗證器
            ecdsaVerify.update(data.getBytes());    //放入資料
            return ecdsaVerify.verify(signature);   //驗證簽名
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Key轉String
    public static String keyToString(Key key) {
        return base64Encode(key.getEncoded());
        //return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    //String轉Key
    public static PrivateKey stringToPrivateKey(String keyString) {
        try {
            byte[] privateKeyBytes = base64Decode(keyString);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static PublicKey stringToPublicKey(String keyString) {
        try {
            byte[] publicKeyBytes = base64Decode(keyString);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Public 和 WalletAddress
    public static String getPublicKeyHash(PublicKey publicKey){
        return "00"+RipeMD160(SHA256(keyToString(publicKey)));
    }
    public static String publicKeyToAddress(PublicKey publicKey){
        String publicKeyHash = getPublicKeyHash(publicKey);
        String doubleSha256 = SHA256(SHA256(publicKeyHash));
        return base58Encode((publicKeyHash + doubleSha256.substring(0,8)).getBytes());
    }
    public static String addressToPublicKeyHash(String address){
        String addressDecode = new String(base58Decode(address));
        return addressDecode.substring(addressDecode.length()-8);
    }
}