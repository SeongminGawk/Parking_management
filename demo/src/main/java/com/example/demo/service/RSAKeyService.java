package com.example.demo.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RSAKeyService {

    private static final Logger logger = LoggerFactory.getLogger(RSAKeyService.class);

    private final KeyPair rsaKeyPair;

    public RSAKeyService() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048); 
        this.rsaKeyPair = keyPairGen.generateKeyPair();

        logger.info("RSA Key Pair generated successfully.");
        logger.info("Public Key: {}", getPublicKey());
    }

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
    }

    public KeyPair getKeyPair() {
        return rsaKeyPair;
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

        logger.info("Data encrypted successfully.");
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String encryptedData) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            logger.info("Data decrypted successfully.");
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            logger.error("Failed to decrypt data: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid encrypted data.");
        }
    }

    public void logKeyInfo() {
        PublicKey publicKey = rsaKeyPair.getPublic();
        String keyFormat = publicKey.getFormat(); 
        String algorithm = publicKey.getAlgorithm(); 
        logger.info("Public Key Format: {}", keyFormat);
        logger.info("Public Key Algorithm: {}", algorithm);
    }
}
