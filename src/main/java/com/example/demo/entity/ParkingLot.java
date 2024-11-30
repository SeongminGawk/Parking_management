package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_parking_lot")
@Getter
@Setter
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prk_lot_seq", nullable = false)
    private Integer id;

    @Column(name = "prk_lot_nm", nullable = false, length = 50)
    private String prkLotNm;
    
    @Column(name = "zipcd", nullable = false, length = 5)
    private String zipcd;

    @Column(name = "bas_addr", nullable = false, length = 100)
    private String basAddr;

    @Column(name = "dtl_addr", length = 100)
    private String dtlAddr;

    @Column(name = "prk_lot_id", nullable = false, length = 20)
    private String prkLotId;

    @Column(name = "inst_dt", nullable = false)
    private LocalDate instDt;

    @Column(name = "latitude", nullable = false, length = 24)
    private String latitude;

    @Column(name = "longitude", nullable = false, length = 24)
    private String longitude;

    @Column(name = "prk_cmprt_co", nullable = false, columnDefinition = "numeric(3,0)")
    private int prkCmprtCo;

    @Column(name = "opertn_day", nullable = false, length = 24)
    private String opertnDay;

    @Column(name = "prk_chrg_mthd", nullable = false, length = 2)
    private String prkChrgMthd;

    @Column(name = "image_path", nullable = false, length = 255)
    private String imagePath;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "rgtr_id", nullable = false, length = 24)
    private String rgtrId;
}
