package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.CreateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    AddressResponseDTO createAddress(CreateAddressRequestDTO request, Long userId);
    AddressResponseDTO updateAddress(Long id, UpdateAddressRequestDTO request, Long userId);
    void deleteAddress(Long id, Long userId);
    AddressResponseDTO getAddressById(Long id, Long userId);
    List<AddressResponseDTO> getUserAddresses(Long userId);
    AddressResponseDTO getDefaultAddress(Long userId);
    AddressResponseDTO setDefaultAddress(Long id, Long userId);
}
