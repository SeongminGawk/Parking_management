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
    List<Member> findAllByName(String name); 
    Optional<Member> findByUseridAndName(String userId, String name); 
}
