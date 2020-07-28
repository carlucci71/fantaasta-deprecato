package com.example.demo;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
public class Criptaggio {
    private  final String ALGORITHM = "AES";
    private static final byte[] keyValue = "ADBSJHJS12547896".getBytes();
    public  String encrypt(String valueToEnc, String x) throws Exception {
    	if (valueToEnc == null || "".equals(valueToEnc.trim())) return "";
        Key key = generateKey(x);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        byte[] encryptedByteValue = new Base64().encode(encValue);
        String encryptedValue = encryptedByteValue.toString();
        return encryptedValue;
    }

    public  String decrypt(String encryptedValue, String x) throws Exception {
    	if (encryptedValue == null || "".equals(encryptedValue.trim())) return "";
        Key key = generateKey(x);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] enctVal = c.doFinal(encryptedValue.getBytes());
        byte[] decordedValue = new Base64().decode(enctVal);
        return decordedValue.toString();
    }

    private  Key generateKey(String x) throws Exception {
    	x="ADBSJHJS12547896";
        byte[] keyValue = x.getBytes();
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }

}