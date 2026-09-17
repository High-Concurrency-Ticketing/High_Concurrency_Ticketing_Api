package com.highconcurrency.ticketing.presentation;

import com.highconcurrency.ticketing.application.usecase.user.UserCreateRequest;
import com.highconcurrency.ticketing.application.usecase.user.UserResponse;
import com.highconcurrency.ticketing.application.usecase.user.UserUpdateRequest;
import com.highconcurrency.ticketing.application.usecase.user.UserUseCase;
import com.highconcurrency.ticketing.presentation.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User")
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping
    @Operation(summary = "사용자 생성")
    public ResponseEntity<Long> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userUseCase.createUser(request));
    }

    @GetMapping
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<UserResponse> getUser(@CurrentUserId Long currentUserId) {
        return ResponseEntity.ok(UserResponse.from(userUseCase.getUser(currentUserId)));
    }

    @PatchMapping
    @Operation(summary = "내 정보 수정")
    public ResponseEntity<UserResponse> updateUser(@CurrentUserId Long currentUserId, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userUseCase.updateUser(currentUserId, request));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    public ResponseEntity<Void> deleteUser(@CurrentUserId Long currentUserId) {
        userUseCase.deleteUser(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
