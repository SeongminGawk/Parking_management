package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ParkingLot;

@Repository
public interface ParkingRepository extends JpaRepository<ParkingLot, Integer> {
// 주차장 등록자 ID로 주차장 정보를 검색하며, 등록 날짜(regDt) 기준으로 가장 최근의 주차장 정보를 반환하는 메서드입니다.
    ParkingLot findTopByRgtrIdOrderByRegDtDesc(String rgtrId);
    
    // 주차장 ID가 존재하는지 여부를 확인하는 메서드입니다. 존재하면 true, 존재하지 않으면 false를 반환합니다.
    boolean existsByPrkLotId(String prkLotId);
}
