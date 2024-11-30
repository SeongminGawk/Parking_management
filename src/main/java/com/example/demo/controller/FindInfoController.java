package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Member;
import com.example.demo.service.FindInfoService;

@RestController
@RequestMapping("/find-info")
public class FindInfoController {

    @Autowired
    private FindInfoService findInfoService;

    // 이름으로 아이디 찾기
    @PostMapping("/find-id")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = requestData.get("name"); 
            List<String> userIds = findInfoService.findIdsByName(name); 

            if (userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "해당 이름으로 등록된 아이디가 없습니다.");
            } else {
                response.put("success", true);
                response.put("userIds", userIds); 
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다:" + e.getMessage());
        }
        return response;
    }

    // 아이디와 이름으로 비밀번호 리셋
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
