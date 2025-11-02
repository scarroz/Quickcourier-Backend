package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.UpdateUserRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.UserResponseDTO;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.model.User;
import co.edu.unbosque.quickcourier.repository.UserRepository;
import co.edu.unbosque.quickcourier.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de usuarios
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final DataMapper dataMapper;

    public UserServiceImpl(UserRepository userRepository, DataMapper dataMapper) {
        this.userRepository = userRepository;
        this.dataMapper = dataMapper;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        logger.debug("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return dataMapper.toUserResponseDTO(user);
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserResponseDTO getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return dataMapper.toUserResponseDTO(user);
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserResponseDTO getCurrentUser(Long userId) {
        logger.debug("Fetching current user: {}", userId);
        return getUserById(userId);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        logger.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Actualizar campos si están presentes
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        logger.info("User {} updated successfully", id);

        return dataMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deactivateUser(Long id) {
        logger.info("Deactivating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        logger.info("User {} deactivated", id);
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        return users.map(dataMapper::toUserResponseDTO);
    }
}