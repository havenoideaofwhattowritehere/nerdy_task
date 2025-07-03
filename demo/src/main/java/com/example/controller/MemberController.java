package com.example.controller;

import com.example.entity.Member;
import com.example.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Користувачі", description = "API для управління користувачами бібліотеки")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "Створити нового користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Користувача створено"),
            @ApiResponse(responseCode = "400", description = "Неправильні вхідні дані")
    })
    @PostMapping
    public ResponseEntity<Member> createMember(
            @Parameter(description = "Дані користувача") @RequestBody Member member
    ) {
        return new ResponseEntity<>(memberService.createMember(member), HttpStatus.CREATED);
    }

    @Operation(summary = "Отримати всіх користувачів")
    @ApiResponse(responseCode = "200", description = "Список користувачів отримано")
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @Operation(summary = "Отримати користувача за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Користувача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(
            @Parameter(description = "ID користувача") @PathVariable Long id
    ) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @Operation(summary = "Пошук користувача за ім'ям")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Користувача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @GetMapping("/search")
    public ResponseEntity<?> findByName(
            @Parameter(description = "Ім'я користувача") @RequestParam String name
    ) {
        Optional<Member> member = memberService.findByName(name);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити дані користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Дані оновлено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @Parameter(description = "ID користувача") @PathVariable Long id,
            @Parameter(description = "Оновлені дані користувача") @RequestBody Member member
    ) {
        return ResponseEntity.ok(memberService.updateMember(id, member));
    }

    @Operation(summary = "Видалити користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Користувача видалено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "ID користувача") @PathVariable Long id
    ) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
