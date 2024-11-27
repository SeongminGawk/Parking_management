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

    public void save(ParkingLot parkingLot) {
        // 중복 확인 후 저장
        if (!parkingRepository.existsByPrkLotId(parkingLot.getPrkLotId())) {
            parkingRepository.save(parkingLot);
        }
    }
    // 주차장 정보를 저장하는 메서드입니다. 저장하기 전에 주차장 ID의 중복 여부를 확인합니다.
    
    public Integer getNextParkingLotSeq() {
        return (int) (parkingRepository.count() + 1);
    }
    // 다음 주차장 순번을 반환하는 메서드입니다. 현재 저장된 주차장 개수에 1을 더한 값을 반환합니다.
    
    public ParkingLot findLatestParkingLotByUserId(String userId) {
        return parkingRepository.findTopByRgtrIdOrderByRegDtDesc(userId);
    }
    // 주어진 사용자 ID로 가장 최근에 등록된 주차장 정보를 반환하는 메서드입니다.
    
    public Page<ParkingLot> findAll(int page, int size) {
        return parkingRepository.findAll(PageRequest.of(page, size));
    }
    // 주차장 정보를 페이지네이션하여 반환하는 메서드입니다. 페이지 번호와 페이지 크기를 인자로 받습니다.
    
    public boolean existsByPrkLotId(String prkLotId) {
        return parkingRepository.existsByPrkLotId(prkLotId);
    }
    // 주차장 ID의 존재 여부를 확인하는 메서드입니다.
    
    public Optional<ParkingLot> findById(Integer id) {
        return parkingRepository.findById(id);
    }
    // 주차장 ID로 주차장 정보를 조회하는 메서드입니다.
}


