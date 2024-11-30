package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.ParkingLot;


@Repository
public interface ParkingRepository extends JpaRepository<ParkingLot, Integer> {
    ParkingLot findTopByRgtrIdOrderByRegDtDesc(String rgtrId);
    boolean existsByPrkLotId(String prkLotId);
}
