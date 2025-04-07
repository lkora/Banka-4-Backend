package rs.banka4.user_service.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.user_service.exceptions.user.*;
import rs.banka4.user_service.exceptions.user.client.NotActivated;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.security.PreAuthBankUserAuthentication;
import rs.banka4.user_service.security.UnauthenticatedBankUserPrincipal;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.utils.specification.EmployeeSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public LoginResponseDto login(LoginDto loginDto) {
        final var principal =
            new UnauthenticatedBankUserPrincipal(UserType.EMPLOYEE, loginDto.email());
        AuthenticatedBankUserAuthentication token;
        try {
            token =
                (AuthenticatedBankUserAuthentication) authenticationManager.authenticate(
                    new PreAuthBankUserAuthentication(principal, loginDto.password())
                );
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            LOGGER.debug("Login for {} failed", loginDto, e);
            throw new IncorrectCredentials();
        }

        Employee employee =
            employeeRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));
        if (!employee.isActive() || employee.getPassword() == null) {
            LOGGER.debug("Login for {} failed: not activated", loginDto);
            throw new NotActivated();
        }

        String accessToken = jwtService.generateAccessToken(employee);
        String refreshToken = jwtService.generateRefreshToken(token.getPrincipal());

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public EmployeeResponseDto getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        UUID employeeId = jwtService.extractUserId(token);

        if (jwtService.isTokenExpired(token)) throw new NotAuthenticated();

        return EmployeeMapper.INSTANCE.toResponseDto(
            employeeRepository.findById(employeeId)
                .orElseThrow(NotAuthenticated::new)
        );
    }

    @Override
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        List<String> privileges =
            Stream.of(Privilege.values())
                .map(Privilege::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PrivilegesDto(privileges));
    }

    public void createEmployee(CreateEmployeeDto dto) {
        if (userService.existsByEmail(dto.email())) {
            throw new DuplicateEmail(dto.email());
        }

        if (!userService.isPhoneNumberValid(dto.phone())) {
            throw new InvalidPhoneNumber();
        }

        if (employeeRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsername(dto.username());
        }
        Employee employee = EmployeeMapper.INSTANCE.toEntity(dto);
        employeeRepository.save(employee);

        userService.sendVerificationEmail(employee.getFirstName(), employee.getEmail());
    }

    public ResponseEntity<Page<EmployeeDto>> getAll(
        String firstName,
        String lastName,
        String email,
        String position,
        PageRequest pageRequest
    ) {
        SpecificationCombinator<Employee> combinator = new SpecificationCombinator<>();

        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(EmployeeSpecification.hasFirstName(firstName));
        }
        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(EmployeeSpecification.hasLastName(lastName));
        }
        if (email != null && !email.isEmpty()) {
            combinator.and(EmployeeSpecification.hasEmail(email));
        }
        if (position != null && !position.isEmpty()) {
            combinator.and(EmployeeSpecification.hasPosition(position));
        }

        Page<Employee> employees = employeeRepository.findAll(combinator.build(), pageRequest);
        Page<EmployeeDto> dtos =
            employees.map(
                employee -> new EmployeeDto(
                    employee.getId(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getDateOfBirth(),
                    employee.getGender(),
                    employee.getEmail(),
                    employee.getPhone(),
                    employee.getAddress(),
                    employee.getUsername(),
                    employee.getPosition(),
                    employee.getDepartment(),
                    employee.isActive()
                )
            );

        return ResponseEntity.ok(dtos);
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Optional<Employee> findEmployeeById(UUID id) {
        return employeeRepository.findById(id);
    }

    @Override
    public void activateEmployeeAccount(Employee employee, String password) {
        employee.setEnabled(true);
        employee.setActive(true);
        employee.setPassword(passwordEncoder.encode(password));
        employeeRepository.save(employee);
    }

    public void updateEmployee(UUID id, UpdateEmployeeDto updateEmployeeDto) {
        Employee employee =
            employeeRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id.toString()));

        if (userService.existsByEmail(updateEmployeeDto.email())) {
            throw new DuplicateEmail(updateEmployeeDto.email());
        }

        if (!userService.isPhoneNumberValid(updateEmployeeDto.phoneNumber())) {
            throw new InvalidPhoneNumber();
        }

        if (employeeRepository.existsByUsername(updateEmployeeDto.username())) {
            throw new DuplicateUsername(updateEmployeeDto.username());
        }

        EmployeeMapper.INSTANCE.fromUpdate(employee, updateEmployeeDto);
        if (updateEmployeeDto.privilege() != null) {
            employee.setPrivileges(updateEmployeeDto.privilege());
        }
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee employee =
            employeeRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id.toString()));
        return EmployeeMapper.INSTANCE.toResponseDto(employee);
    }
}
