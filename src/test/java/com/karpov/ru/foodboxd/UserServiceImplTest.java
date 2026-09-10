package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.dto.UserRegistrationDto;
import com.karpov.ru.foodboxd.model.entity.Role;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.repository.RoleRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link UserServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserRegistrationDto();
        validDto.setUsername("testuser");
        validDto.setEmail("test@example.com");
        validDto.setPassword("secret123");
    }

    @Test
    void registerShouldSaveNewUserWithEncodedPassword() {
        // given
        Role roleUser = new Role(1L, "ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        User created = userService.register(validDto);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo("testuser");
        assertThat(created.getEmail()).isEqualTo("test@example.com");
        assertThat(created.getPassword()).isEqualTo("encoded");
        assertThat(created.getRoles()).containsExactly(roleUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyUsed() {
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email уже используется");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowWhenUsernameAlreadyUsed() {
        when(userRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(validDto.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя пользователя занято");
    }

    @Test
    void findByUsernameShouldReturnUserWhenFound() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");
        assertThat(result).isPresent().containsSame(user);
    }

    @Test
    void findByEmailShouldReturnEmptyWhenNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertThat(userService.findByEmail("nonexistent@example.com")).isEmpty();
    }

    @Test
    void getAllUsersShouldReturnUsersFromRepository() {
        User first = new User();
        first.setUsername("alice");
        User second = new User();
        second.setUsername("bob");
        when(userRepository.findAllByOrderByUsernameAsc()).thenReturn(List.of(first, second));

        List<User> result = userService.getAllUsers();

        assertThat(result).containsExactly(first, second);
    }

    @Test
    void searchUsersShouldReturnAllWhenQueryIsBlank() {
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findAllByOrderByUsernameAsc()).thenReturn(List.of(user));

        assertThat(userService.searchUsers("   ")).containsExactly(user);
        verify(userRepository, never())
                .findByUsernameContainingIgnoreCaseOrCityContainingIgnoreCaseOrderByUsernameAsc(any(), any());
    }

    @Test
    void searchUsersShouldTrimAndDelegateToRepository() {
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findByUsernameContainingIgnoreCaseOrCityContainingIgnoreCaseOrderByUsernameAsc("ali", "ali"))
                .thenReturn(List.of(user));

        assertThat(userService.searchUsers("  ali  ")).containsExactly(user);
    }
}