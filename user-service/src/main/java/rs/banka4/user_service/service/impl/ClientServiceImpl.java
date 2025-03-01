package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.ClientResponseDto;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.mapper.BasicClientMapper;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final BasicClientMapper basicClientMapper;
    private final JwtUtil jwtUtil;


    @Override
    public ResponseEntity<ClientResponseDto> getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String clinetUsername = jwtUtil.extractUsername(token);

        if(jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();
        if(jwtUtil.isTokenInvalidated(token)) throw new NotAuthenticated();

        Client client = clientRepository.findByEmail(clinetUsername).orElseThrow(NotFound::new);

        ClientResponseDto response = basicClientMapper.entityToDto(client);
        return ResponseEntity.ok(response);

    }
}
