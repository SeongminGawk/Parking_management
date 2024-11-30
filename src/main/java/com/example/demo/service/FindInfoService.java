package com.example.demo.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

@Service
public class FindInfoService {

    @Autowired
    private MemberRepository memberRepository;

    // 이름으로 모든 아이디 찾기
    public List<String> findIdsByName(String name) {
        List<Member> members = memberRepository.findAllByName(name);
        return members.stream()
                .map(Member::getUserid)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean resetPassword(String userId, String name, String newPassword) throws Exception {
        // 이름과 아이디가 일치하는 회원 조회
        Optional<Member> optionalMember = findByUseridAndName(userId, name);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            // 새 비밀번호 
            String salt = generateSalt(); 
            String hashedPassword = hashWithSHA256(newPassword + salt); 

            // 정보 업데이트
            member.setPassword(hashedPassword);
            member.setSalt(salt);
            member.setCreatedAt(LocalDateTime.now()); 
            memberRepository.save(member);
            return true;
        }
        return false; 
    }

    // 이름과 아이디로 회원 조회
    public Optional<Member> findByUseridAndName(String userId, String name) {
        return memberRepository.findByUseridAndName(userId, name);
    }

    private String hashWithSHA256(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
