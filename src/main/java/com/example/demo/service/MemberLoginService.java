package com.example.demo.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

@Service
public class MemberLoginService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RSAKeyService rsaKeyService;

    // 로그인 검증
    public boolean validateLogin(String encryptedUserid, String encryptedPassword) throws Exception {
        String userid = decryptWithRSA(encryptedUserid); 
        String rawPassword = decryptWithRSA(encryptedPassword); 
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("User not found with userid: " + userid));

        String salt = member.getSalt(); 
        String hashedPassword = hashWithSHA256(rawPassword + salt); 

        return hashedPassword.equals(member.getPassword()); 
    }

    // 비밀번호 재설정
    @Transactional
    public boolean resetPassword(String userid, String newPassword) throws NoSuchAlgorithmException {
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("User not found with userid: " + userid));
        String salt = generateSalt(); 
        String hashedPassword = hashWithSHA256(newPassword + salt); 
        member.setSalt(salt); 
        member.setPassword(hashedPassword); 
        memberRepository.save(member); 

        return true;
    }

    // RSA 복호화
    public String decryptWithRSA(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyService.getKeyPair().getPrivate());
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, "UTF-8");
    }

    // SHA-256 해시
    private String hashWithSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    // 솔트 생성
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}
