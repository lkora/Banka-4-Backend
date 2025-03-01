package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.ClientResponseDto;
import rs.banka4.user_service.dto.EmployeeResponseDto;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientService;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;


    @Override
    public ResponseEntity<ClientResponseDto> getMe(String authorization) {
        return null;
    }
}
