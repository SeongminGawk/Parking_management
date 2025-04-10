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
        @RequestParam String encryptedEmail,
        Model model) {
            try {
                memberRegisterService.registerMember(
                    encryptedUserid, 
                    encryptedUsername, 
                    encryptedPassword, 
                    encryptedPhoneNumber, 
                    encryptedEmail
                );
                return "redirect:/login";
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
                return "register";
            } catch (Exception e) {
                model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
                return "register";
            }
    }

    // 아이디/비밀번호 찾기 페이지
    @GetMapping("/find-info")
    public String showFindInfoPage() {
        return "find-info";  
    }

    // RSA 공개 키 제공
    @GetMapping("/getPublicKey")
    @ResponseBody
    public Map<String, String> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", rsaKeyService.getPublicKey());
        return response;
    }

    // 아이디 중복 체크
    @GetMapping(value = "/checkUserid", produces = "application/json")
    @ResponseBody
    public Map<String, Object> checkUserid(@RequestParam String userid) {
        boolean exists = memberRegisterService.checkUseridExists(userid);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "이미 사용 중인 아이디입니다" : "사용 가능한 아이디입니다");
        return response;
    }

    // 이름으로 아이디 찾기
    @PostMapping("/find-id")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = requestData.get("name"); // 이름 추출
            List<String> userId = findInfoService.findIdsByName(name); // 서비스 호출

            response.put("success", true);
            response.put("userId", userId);
            response.put("message", "아이디 찾기가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userId = requestData.get("userId");
            String name = requestData.get("name");
            String newPassword = requestData.get("newPassword");

            // 비밀번호 재설정 로직 호출
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
