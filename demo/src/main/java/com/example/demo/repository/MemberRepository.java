package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserid(String userid);

    boolean existsByUserid(String userid);

    List<Member> findAllByName(String name); // 이름으로 모든 회원 찾기

    Optional<Member> findByUseridAndName(String userId, String name); // 아이디와 이름으로 회원 조회
}
