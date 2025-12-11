package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.exception.UsernameAlreadyExistsException;
import com.luisfelipe.simplecarapi.repository.UserRepository;
import com.luisfelipe.simplecarapi.utils.UserUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    @InjectMocks
    private UserService service;
    @Mock
    private UserRepository repository;
    private List<User> userList;
    @InjectMocks
    private UserUtils userUtils;

    @BeforeEach
    void init(){
        userList = userUtils.newUsersList();
    }

    @Order(1)
    @Test
    @DisplayName("Save creates a new user when successful")
    void save_CreatesUser_WhenSuccessful(){
        User userToSave = userUtils.newUserToSave();
        BDDMockito.given(repository.findByUsername(userToSave.getUsername())).willReturn(Optional.empty());
        BDDMockito.given(repository.save(userToSave)).willReturn(userToSave);

        User savedUser = service.save(userToSave);

        Assertions.assertThat(savedUser).isNotNull().isEqualTo(userToSave).hasNoNullFieldsOrProperties();
        Assertions.assertThat(savedUser.getId()).isNotNull();
    }

    @Order(2)
    @Test
    @DisplayName("Save throws UsernameAlreadyExistsException when username already exists.")
    void save_ThrowsUsernameAlreadyExistsException_WhenUsernameExists(){
        User userToSave = userUtils.newUsersList().getFirst();
        BDDMockito.given(repository.findByUsername(userToSave.getUsername())).willReturn(Optional.of(userToSave));

        Assertions.assertThatThrownBy(() -> service.save(userToSave))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }



}