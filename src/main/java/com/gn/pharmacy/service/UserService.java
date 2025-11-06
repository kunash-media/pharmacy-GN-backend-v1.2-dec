package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.UserDTO;
import com.gn.pharmacy.dto.request.UserRequestDto;
import com.gn.pharmacy.dto.response.UserResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long userId, UserRequestDto userRequestDto);
    void deleteUser(Long userId);

    public UserResponseDto patchUser(@PathVariable Long userId,
                                                     @RequestBody UserRequestDto userRequestDto);

    boolean isEmailExists(String email);

    UserDTO authenticateUser(String mobile, String password);

}

