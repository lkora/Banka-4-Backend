package rs.banka4.user_service.unit.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.client.ClientContactNotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.ClientContactServiceImpl;

public class ClientContactServiceTests {

    @Mock
    private JwtService jwtService;
    @Mock
    private ClientContactRepository clientContactRepository;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private ClientContactServiceImpl clientContactService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateClientContactSuccess() {
        // Arrange
        String token = "validToken";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

        // Act
        clientContactService.createClientContact(token, request);

        // Assert
        verify(clientContactRepository, times(1)).save(any(ClientContact.class));
    }

    @Test
    void testCreateClientContactClientNotFound() {
        // Arrange
        String token = "validToken";
        String email = "email@test.com";
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(UUID.randomUUID());
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ClientNotFound.class,
            () -> clientContactService.createClientContact(token, request)
        );
    }

    @Test
    void testGetSpecificClientContactSuccess() {
        // Arrange
        String token = "validToken";
        UUID contactId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContact clientContact = ClientObjectMother.generateBasicClientContact();
        clientContact.setClient(client);
        clientContact.setId(contactId);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(clientContact));

        // Act
        ClientContactDto result = clientContactService.getSpecificClientContact(token, contactId);

        // Assert
        assertNotNull(result);
        assertEquals(contactId, result.id());
        assertEquals(clientContact.getNickname(), result.nickname());
        assertEquals(clientContact.getAccountNumber(), result.accountNumber());
    }


    @Test
    void testGetSpecificClientContactNotAuthenticated() {
        // Arrange
        String token = "validToken";
        UUID contactId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContact clientContact = new ClientContact();
        clientContact.setClient(new Client());

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(clientContact));

        // Act & Assert
        assertThrows(
            NotAuthenticated.class,
            () -> clientContactService.getSpecificClientContact(token, contactId)
        );
    }

    @Test
    void testGetAllClientContactsSuccess() {
        // Arrange
        String token = "validToken";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        Pageable pageable = PageRequest.of(0, 10);
        ClientContact clientContact = ClientObjectMother.generateBasicClientContact();
        clientContact.setClient(client);
        Page<ClientContact> clientContactPage = new PageImpl<>(List.of(clientContact), pageable, 1);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findAllActive(pageable, client)).thenReturn(clientContactPage);

        // Act
        Page<ClientContactDto> result = clientContactService.getAllClientContacts(token, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
            clientContact.getNickname(),
            result.getContent()
                .get(0)
                .nickname()
        );
        assertEquals(
            clientContact.getAccountNumber(),
            result.getContent()
                .get(0)
                .accountNumber()
        );
    }

    @Test
    void testGetAllClientContactsNoContactsFound() {
        // Arrange
        String token = "validToken";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClientContact> clientContactPage = new PageImpl<>(List.of(), pageable, 0);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findAllActive(pageable, client)).thenReturn(clientContactPage);

        // Act
        Page<ClientContactDto> result = clientContactService.getAllClientContacts(token, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetAllClientContactsClientNotFound() {
        // Arrange
        String token = "validToken";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtService.extractUserId(token)).thenReturn(UUID.randomUUID());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

        // Act & Assert
        assertThrows(
            ClientNotFound.class,
            () -> clientContactService.getAllClientContacts(token, pageable)
        );
    }

    @Test
    void testUpdateClientContactSuccess() {
        // Arrange
        String token = "validToken";
        UUID contactId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContact clientContact = ClientObjectMother.generateBasicClientContact();
        clientContact.setClient(client);
        clientContact.setId(contactId);
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(clientContact));

        // Act
        clientContactService.updateClientContact(token, contactId, request);

        // Assert
        verify(clientContactRepository, times(1)).save(clientContact);
        assertEquals(request.nickname(), clientContact.getNickname());
        assertEquals(request.accountNumber(), clientContact.getAccountNumber());
    }

    @Test
    void testUpdateClientContactClientNotFound() {
        // Arrange
        String token = "validToken";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);

        UUID contactId = UUID.randomUUID();
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(UUID.randomUUID());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

        // Act & Assert
        assertThrows(
            ClientNotFound.class,
            () -> clientContactService.updateClientContact(token, contactId, request)
        );
    }

    @Test
    void testUpdateClientContactNotAuthenticated() {
        // Arrange
        String token = "validToken";
        UUID contactId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContact clientContact = ClientObjectMother.generateBasicClientContact();
        clientContact.setClient(new Client());
        clientContact.setId(contactId);
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findById(contactId)).thenReturn(Optional.of(clientContact));

        // Act & Assert
        assertThrows(
            NotAuthenticated.class,
            () -> clientContactService.updateClientContact(token, contactId, request)
        );
    }

    @Test
    void testUpdateClientContactNotFound() {
        // Arrange
        String token = "validToken";
        UUID contactId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), null);
        ClientContactRequest request = ClientObjectMother.generateBasicClientContactRequest();

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientContactRepository.findById(contactId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ClientContactNotFound.class,
            () -> clientContactService.updateClientContact(token, contactId, request)
        );
    }
}
