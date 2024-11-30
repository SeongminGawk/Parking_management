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

    @Autowired 
    public LoginController(MemberLoginService memberLoginService, RSAKeyService rsaKeyService) {
        this.memberLoginService = memberLoginService;
        this.rsaKeyService = rsaKeyService;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        String publicKey = rsaKeyService.getPublicKey(); 
        model.addAttribute("publicKey", publicKey);
        return "login";
    }

    @PostMapping("/perform_login")
    @ResponseBody 
    public Map<String, Object> performLogin(@RequestParam("encryptedUserid") String encryptedUserid, @RequestParam("encryptedPassword") String encryptedPassword, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (memberLoginService.validateLogin(encryptedUserid, encryptedPassword)) {
                String userid = memberLoginService.decryptWithRSA(encryptedUserid);
                session.setAttribute("userid", userid);
                response.put("success", true); 
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
            session.invalidate(); 
        }
        return "redirect:/login";
    }
}

