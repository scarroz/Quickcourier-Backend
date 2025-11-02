package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.CreateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AddressResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.model.Address;
import co.edu.unbosque.quickcourier.model.User;
import co.edu.unbosque.quickcourier.repository.AddressRepository;
import co.edu.unbosque.quickcourier.repository.UserRepository;
import co.edu.unbosque.quickcourier.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de direcciones.
 * Maneja creación, actualización y administración de direcciones de usuario.
 */
@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AddressResponseDTO createAddress(CreateAddressRequestDTO request, Long userId) {
        logger.info("Creando dirección para usuario {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setZone(request.zone());
        address.setPostalCode(request.postalCode());

        // Si no hay direcciones, la primera será por defecto
        List<Address> existing = addressRepository.findAllByUserId(userId);
        if (existing.isEmpty()) {
            address.setIsDefault(true);
        } else {
            address.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        }

        address.setCreatedAt(LocalDateTime.now());

        Address saved = addressRepository.save(address);
        logger.info("Dirección creada con ID {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public AddressResponseDTO updateAddress(Long id, UpdateAddressRequestDTO request, Long userId) {
        logger.info("Actualizando dirección {} del usuario {}", id, userId);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("La dirección no pertenece al usuario autenticado");
        }

        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setZone(request.zone());
        address.setPostalCode(request.postalCode());

        if (request.isDefault() != null && request.isDefault()) {
            // Marcar esta como default y quitar default de las demás
            List<Address> addresses = addressRepository.findAllByUserId(userId);
            for (Address addr : addresses) {
                addr.setIsDefault(addr.getId().equals(address.getId()));
                addressRepository.save(addr);
            }
        }

        Address updated = addressRepository.save(address);
        logger.info("Dirección {} actualizada correctamente", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    public void deleteAddress(Long id, Long userId) {
        logger.info("Eliminando dirección {} del usuario {}", id, userId);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("La dirección no pertenece al usuario autenticado");
        }

        addressRepository.delete(address);
    }

    @Override
    public AddressResponseDTO getAddressById(Long id, Long userId) {
        logger.info("Obteniendo dirección {} del usuario {}", id, userId);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("La dirección no pertenece al usuario autenticado");
        }

        return mapToResponse(address);
    }

    @Override
    public List<AddressResponseDTO> getUserAddresses(Long userId) {
        logger.info("Listando direcciones del usuario {}", userId);
        List<Address> addresses = addressRepository.findAllByUserId(userId);
        return addresses.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AddressResponseDTO getDefaultAddress(Long userId) {
        logger.info("Obteniendo dirección por defecto del usuario {}", userId);

        Address address = addressRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay dirección por defecto configurada"));
        return mapToResponse(address);
    }

    @Override
    public AddressResponseDTO setDefaultAddress(Long id, Long userId) {
        logger.info("Marcando dirección {} como predeterminada para usuario {}", id, userId);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("La dirección no pertenece al usuario autenticado");
        }

        List<Address> addresses = addressRepository.findAllByUserId(userId);
        for (Address addr : addresses) {
            addr.setIsDefault(addr.getId().equals(address.getId()));
            addressRepository.save(addr);
        }

        return mapToResponse(address);
    }

    // ===================================================
    // MÉTODO AUXILIAR
    // ===================================================
    private AddressResponseDTO mapToResponse(Address address) {
        return new AddressResponseDTO(
                address.getId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getZone(),
                address.getPostalCode(),
                address.getIsDefault(),
                address.getFullAddress(),
                address.getCreatedAt()
        );
    }
}
