package com.example.demo.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

@Service
public class MemberRegisterService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RSAKeyService rsaKeyService;

    private SecretKey aesKey;

    public MemberRegisterService() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.aesKey = keyGen.generateKey();
    }

    @Transactional
    public Member registerMember(String encryptedUserid, String encryptedUsername, String encryptedPassword, String encryptedPhoneNumber, String encryptedEmail) throws Exception {
        String userid = rsaKeyService.decrypt(encryptedUserid);
        String name = rsaKeyService.decrypt(encryptedUsername);
        String rawPassword = rsaKeyService.decrypt(encryptedPassword);
        String phoneNumber = rsaKeyService.decrypt(encryptedPhoneNumber);
        String email = rsaKeyService.decrypt(encryptedEmail);


        if (memberRepository.existsByUserid(userid)) {
            throw new IllegalArgumentException("이미 사용 중이거나 탈퇴한 아이디입니다");
        }

        String salt = generateSalt();
        String passwordWithSalt = rawPassword + salt;
        String hashedPassword = hashWithSHA256(passwordWithSalt);

        Member member = new Member();
        member.setUserid(userid);
        member.setName(name);
        member.setPassword(hashedPassword); 
        member.setPhoneNumber(encryptWithAES(phoneNumber)); 
        member.setEmail(encryptWithAES(email)); 
        member.setCreatedAt(LocalDateTime.now());
        member.setSalt(salt);

        try {
            Member savedMember = memberRepository.save(member);
            System.out.println("Member saved successfully with ID: " + savedMember.getUserid());
            return savedMember;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving member: " + e.getMessage());
            throw e;
        }
    }

    private String encryptWithAES(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private String hashWithSHA256(String data) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding data to UTF-8", e);
        }
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public Map<String, String> getRSAKeys() {
        Map<String, String> keys = new HashMap<>();
        keys.put("publicKey", rsaKeyService.getPublicKey());
        return keys;
    }

    public boolean checkUseridExists(String userid) {
        return memberRepository.existsByUserid(userid);
    }
}
