package com.example.demo.controller;

import com.example.demo.service.MemberRegisterService;
import com.example.demo.service.FindInfoService;  
import com.example.demo.service.RSAKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MemberController {

    @Autowired
    private MemberRegisterService memberRegisterService;

    @Autowired
    private FindInfoService findInfoService; 

    @Autowired
    private RSAKeyService rsaKeyService;

    // 회원가입 페이지
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("publicKey", rsaKeyService.getPublicKey());
        return "register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerMember(
        @RequestParam String encryptedUserid,
        @RequestParam String encryptedUsername, 
        @RequestParam String encryptedPassword,
        @RequestParam String encryptedPhoneNumber,
        셋
    @PostMapping("/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userId = requestData.get("userId");
            String name = requestData.get("name");
            String newPassword = requestData.get("newPassword");

            boolean resetSuccess = findInfoService.resetPassword(userId, name, newPassword);

            if (resetSuccess) {
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "아이디와 이름이 일치하지 않습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

}
