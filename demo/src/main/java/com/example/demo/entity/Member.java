package com.example.demo.entity;

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
@Table(name = "tb_mbr")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mbr_seq")
    private Long id;

    @Column(name = "mbr_id", nullable = false, length = 15)
    private String userid;
    
    @Column(name = "mbr_name", nullable = false, length = 50)
    private String name;

    @Column(name = "mbr_pwd", nullable = false, length = 128)
    private String password;

    @Column(name = "mbr_mphon_num", nullable = false, length = 100)
    private String phoneNumber;

    @Column(name = "mbr_email", nullable = false, length = 100)
    private String email;

    @Column(name = "salt", nullable = false, length = 32)
    private String salt;

    @Column(name = "cret_dt", nullable = false)
    private LocalDateTime createdAt;
}