package rs.banka4.user_service.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.domain.user.client.mapper.ClientContactMapper;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.client.ClientContactNotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientContactService;
import rs.banka4.user_service.service.abstraction.JwtService;

@Service
@RequiredArgsConstructor
public class ClientContactServiceImpl implements ClientContactService {

    private final JwtService jwtService;
    private final ClientContactRepository clientContactRepository;
    private final ClientRepository clientRepository;

    @Override
    public Page<ClientContactDto> getAllClientContacts(String token, Pageable pageable) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        Page<ClientContact> clientContacts =
            clientContactRepository.findAllActive(pageable, client);
        return clientContacts.map(ClientContactMapper.INSTANCE::toDto);
    }

    @Override
    public List<ClientContactDto> getClientContacts(String token) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        List<ClientContact> clientContacts = clientContactRepository.findByClient(client);
        return clientContacts.stream()
            .map(ClientContactMapper.INSTANCE::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public ClientContactDto getSpecificClientContact(String token, UUID id) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        ClientContact clientContact =
            clientContactRepository.findById(id)
                .orElseThrow(ClientContactNotFound::new);

        if (
            !clientContact.getClient()
                .equals(client)
        ) {
            throw new NotAuthenticated();
        }

        return ClientContactMapper.INSTANCE.toDto(clientContact);
    }

    @Override
    public void createClientContact(String token, ClientContactRequest request) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        ClientContact clientContact = ClientContactMapper.INSTANCE.toEntity(request);
        clientContact.setClient(client);

        clientContactRepository.save(clientContact);
    }

    @Override
    public void updateClientContact(String token, UUID id, ClientContactRequest request) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        ClientContact clientContact =
            clientContactRepository.findById(id)
                .orElseThrow(ClientContactNotFound::new);

        if (
            !clientContact.getClient()
                .equals(client)
        ) {
            throw new NotAuthenticated();
        }

        ClientContactMapper.INSTANCE.fromUpdate(clientContact, request);
        clientContactRepository.save(clientContact);
    }

    @Override
    public void deleteClientContact(String token, UUID contactId) {
        UUID clientId = jwtService.extractUserId(token);
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFound(clientId.toString()));

        ClientContact clientContact =
            clientContactRepository.findById(contactId)
                .orElseThrow(ClientContactNotFound::new);

        if (
            !clientContact.getClient()
                .equals(client)
        ) {
            throw new NotAuthenticated();
        }

        clientContact.setDeleted(true);
        clientContactRepository.save(clientContact);
    }
}
