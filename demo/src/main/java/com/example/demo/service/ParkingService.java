package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ParkingLot;
import com.example.demo.repository.ParkingRepository;

@Service
public class ParkingService {

    @Autowired
    private ParkingRepository parkingRepository;

    // 주차장 정보를 저장
    public void save(ParkingLot parkingLot) {
        if (!parkingRepository.existsByPrkLotId(parkingLot.getPrkLotId())) {
            parkingRepository.save(parkingLot);
        }
    }
     // 다음 주차장 순번 반환
    public Integer getNextParkingLotSeq() {
        return (int) (parkingRepository.count() + 1);
    }

     // 사용자 ID로 가장 최근에 등록된 주차장 정보를 반환
    public ParkingLot findLatestParkingLotByUserId(String userId) {
        return parkingRepository.findTopByRgtrIdOrderByRegDtDesc(userId);
    }

    // 페이지 정보 반환
    public Page<ParkingLot> findAll(int page, int size) {
        return parkingRepository.findAll(PageRequest.of(page, size));
    }
    
    public boolean existsByPrkLotId(String prkLotId) {
        return parkingRepository.existsByPrkLotId(prkLotId);
    }
    public Optional<ParkingLot> findById(Integer id) {
        return parkingRepository.findById(id);
    }
}


