package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.service.MemberLoginService;
import com.example.demo.service.RSAKeyService;

@Controller
public class LoginController {

    private final MemberLoginService memberLoginService;
    private final RSAKeyService rsaKeyService;

    @Autowired // 의존성 주입을 통해 MemberLoginService와 RSAKeyService 인스턴스를 생성자에서 받아 초기화합니다.
    public LoginController(MemberLoginService memberLoginService, RSAKeyService rsaKeyService) {
        this.memberLoginService = memberLoginService;
        this.rsaKeyService = rsaKeyService;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        String publicKey = rsaKeyService.getPublicKey(); // RSA 공개 키 가져오기
        model.addAttribute("publicKey", publicKey); // 모델에 키 추가
        return "login";
    }

    @PostMapping("/perform_login")
    @ResponseBody // POST 요청에 대해 /perform_login 경로를 매핑합니다. 로그인 요청을 처리하고 JSON 형식의 응답을 반환합니다.
    public Map<String, Object> performLogin(@RequestParam("encryptedUserid") String encryptedUserid, @RequestParam("encryptedPassword") String encryptedPassword, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (memberLoginService.validateLogin(encryptedUserid, encryptedPassword)) {
                String userid = memberLoginService.decryptWithRSA(encryptedUserid);
                session.setAttribute("userid", userid);
                response.put("success", true); //성공하면 아이디 저장
            } else {
                response.put("success", false);
                response.put("message", "아이디 및 비밀번호를 다시 입력해 주십시오.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "아이디 및 비밀번호를 다시 입력해 주십시오.");
        }
        return response;
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 무효화하여 로그아웃 처리
        }
        return "redirect:/login"; // 로그아웃 후 로그인 페이지로 이동
    }
}

