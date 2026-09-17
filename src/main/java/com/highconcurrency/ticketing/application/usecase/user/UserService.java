package com.highconcurrency.ticketing.application.usecase.user;

import com.highconcurrency.ticketing.application.common.ErrorCode;
import com.highconcurrency.ticketing.application.common.HighConcurrencyTicketingException;
import com.highconcurrency.ticketing.domain.user.User;
import com.highconcurrency.ticketing.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long createUser(UserCreateRequest userCreateRequest) {
        User user = User.create(
                userCreateRequest.email(),
                userCreateRequest.name(),
                passwordEncoder.encode(userCreateRequest.password())
        );

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new HighConcurrencyTicketingException(ErrorCode.NOT_FOUND, "해당 사용자가 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new HighConcurrencyTicketingException(ErrorCode.NOT_FOUND, "해당 사용자가 없습니다."));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = getUser(userId);
        user.updateProfile(request.name(), passwordEncoder.encode(request.password()));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.delete(getUser(userId));
    }
}
