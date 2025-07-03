package com.example.service.impl;

import com.example.entity.Member;
import com.example.exception.ValidationException;
import com.example.repository.MemberRepository;
import com.example.validator.MemberValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberValidator memberValidator;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Іван Франко");
    }

    @Test
    void createMember_Success() {
        when(memberValidator.supports(Member.class)).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberService.createMember(testMember);

        assertNotNull(result);
        assertEquals(testMember.getName(), result.getName());
        verify(memberRepository).save(any(Member.class));
    }


    @Test
    void createMember_ValidationFails_ThrowsException() {
        when(memberValidator.supports(any())).thenReturn(true);

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("name", "error.name", "Неправильне ім'я");
            return null;
        }).when(memberValidator).validate(any(), any());

        ValidationException exception = assertThrows(ValidationException.class, () -> memberService.createMember(testMember));

        assertTrue(exception.getMessage().contains("Неправильне ім'я"));
    }


    @Test
    void getAllMembers_Success() {
        List<Member> members = Arrays.asList(testMember);
        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.getAllMembers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getMemberById_ExistingMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        Member result = memberService.getMemberById(1L);

        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
    }

    @Test
    void getMemberById_NonExistingMember_ThrowsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> memberService.getMemberById(99L));
    }

    @Test
    void updateMember_Success() {
        Member updatedMember = new Member();
        updatedMember.setName("Оновлене Ім'я");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMember);

        Member result = memberService.updateMember(1L, updatedMember);

        assertEquals(updatedMember.getName(), result.getName());
    }

    @Test
    void deleteMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        doNothing().when(memberRepository).delete(any(Member.class));

        memberService.deleteMember(1L);

        verify(memberRepository).delete(testMember);
    }

    @Test
    void findByName_Success() {
        when(memberRepository.findByName(anyString())).thenReturn(testMember);

        Optional<Member> result = memberService.findByName("Іван Франко");

        assertTrue(result.isPresent());
        assertEquals(testMember.getName(), result.get().getName());
    }
}
