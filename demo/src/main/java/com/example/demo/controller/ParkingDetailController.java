package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.ParkingLot;
import com.example.demo.service.ParkingService;

import javax.servlet.http.HttpSession;

@Controller
public class ParkingDetailController {

    @Autowired
    private ParkingService parkingService;

    @GetMapping("/parking-details")
    public String parkingDetails(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userid");

        // 마지막으로 등록된 주차장 정보 가져오기
        ParkingLot parkingLot = parkingService.findLatestParkingLotByUserId(userId);
        
        model.addAttribute("parking", parkingLot);

        return "parking-details";
    }
}


