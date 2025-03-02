package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientResponseDto;
import rs.banka4.user_service.dto.EmployeeResponseDto;

public interface ClientService {
    ResponseEntity<ClientResponseDto> getMe(String authorization);
}
