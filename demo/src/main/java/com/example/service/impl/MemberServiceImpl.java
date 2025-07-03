package com.example.service.impl;

import com.example.entity.Member;
import com.example.exception.ValidationException;
import com.example.repository.MemberRepository;
import com.example.service.MemberService;
import com.example.validator.MemberValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;

    @Override
    public Member createMember(Member member) {
        DataBinder binder = new DataBinder(member);
        binder.setValidator(memberValidator);
        binder.validate();

        if (binder.getBindingResult().hasErrors()) {
            throw new ValidationException(binder.getBindingResult().getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", ")));
        }

        member.setName(member.getName().trim());
        return memberRepository.save(member);
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Учасника з ID " + id + " не знайдено"));
    }

    @Override
    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member currentMember = getMemberById(id);
        currentMember.setName(memberDetails.getName().trim());
        return memberRepository.save(currentMember);
    }

    @Override
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    @Override
    public Optional<Member> findByName(String name) {
        return Optional.ofNullable(memberRepository.findByName(name.trim()));
    }
}