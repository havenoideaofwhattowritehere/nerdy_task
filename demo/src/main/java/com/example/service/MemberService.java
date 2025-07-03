package com.example.service;

import com.example.entity.Member;
import java.util.List;
import java.util.Optional;

public interface MemberService {
    Member createMember(Member member);
    List<Member> getAllMembers();
    Member getMemberById(Long id);
    Member updateMember(Long id, Member member);
    void deleteMember(Long id);
    Optional<Member> findByName(String name);
}