package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.UpdateUserRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getUserByEmail(String email);
    UserResponseDTO getCurrentUser(Long userId);
    UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request);
    void deactivateUser(Long id);
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
}