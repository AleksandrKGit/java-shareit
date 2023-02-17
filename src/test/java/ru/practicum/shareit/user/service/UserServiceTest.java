package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.tools.configurations.AppTestConfiguration;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import java.util.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.tools.factories.UserFactory.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;
import static ru.practicum.shareit.tools.matchers.UserMatcher.equalToUser;

@SpringBootTest(classes = {UserServiceImpl.class, UserMapperImpl.class})
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceTest {
    @Autowired
    UserService service;

    @MockBean
    UserRepository repository;

    final RuntimeException emailConstraintException = new RuntimeException("UQ_USER_EMAIL constraint");

    final Long id = 1L;

    UserDtoFromClient requestUserDto;

    User createdUser;

    User updatedUser;

    User existingUser;

    @BeforeEach
    void setUp() {
        requestUserDto = createUserDtoFromClient("n1", "e1");
        createdUser = createUser(id, requestUserDto.getName(), requestUserDto.getEmail());
        updatedUser = copyOf(createdUser);
        existingUser = createUser(id, "n2", "e2");
    }

    @Test
    void create_withNotUniqueEmail_shouldThrowAlreadyExistException() {
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(repository.saveAndFlush(userArgumentCaptor.capture())).thenThrow(emailConstraintException);

        assertThrows(AlreadyExistException.class, () -> service.create(requestUserDto));
        User userToRepository = userArgumentCaptor.getValue();

        assertThat(userToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(requestUserDto.getName())),
                hasProperty("email", equalTo(requestUserDto.getEmail()))
        ));
    }

    @Test
    void create_shouldReturnCreatedEntityDto() {
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(repository.saveAndFlush(userArgumentCaptor.capture())).thenReturn(copyOf(createdUser));

        UserDtoToClient resultUserDto = service.create(requestUserDto);
        User userToRepository = userArgumentCaptor.getValue();

        assertThat(userToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(requestUserDto.getName())),
                hasProperty("email", equalTo(requestUserDto.getEmail()))
        ));

        assertThat(resultUserDto, allOf(
                hasProperty("id", equalTo(createdUser.getId())),
                hasProperty("name", equalTo(createdUser.getName())),
                hasProperty("email", equalTo(createdUser.getEmail()))
        ));
    }

    @Test
    void readAll_shouldReturnDtoListOfAllUsers() {
        when(repository.findAll()).thenReturn(List.of(copyOf(existingUser)));

        List<UserDtoToClient> resultUserDtoList = service.readAll();

        assertThat(resultUserDtoList, contains(allOf(
                hasProperty("id", equalTo(existingUser.getId())),
                hasProperty("name", equalTo(existingUser.getName())),
                hasProperty("email", equalTo(existingUser.getEmail()))
        )));
    }

    @Test
    void readById_withNotExistingId_shouldThrowNotFoundException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readById(id));
    }

    @Test
    void readById_shouldReturnSelectedUserDto() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingUser)));

        UserDtoToClient resultUserDto = service.readById(id);

        assertThat(resultUserDto, allOf(
                hasProperty("id", equalTo(existingUser.getId())),
                hasProperty("name", equalTo(existingUser.getName())),
                hasProperty("email", equalTo(existingUser.getEmail()))
        ));
    }

    @Test
    void update_withNotExistingId_shouldThrowNotFoundException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(id, requestUserDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void update_withNotUniqueEmail_shouldThrowAlreadyExistException() {
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(repository.saveAndFlush(argThat(equalToUser(updatedUser))))
                .thenThrow(emailConstraintException);

        assertThrows(AlreadyExistException.class, () -> service.update(id, requestUserDto));
    }

    @Test
    void update_shouldReturnUpdatedUserDto() {
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(repository.saveAndFlush(argThat(equalToUser(updatedUser))))
                .thenReturn(copyOf(updatedUser));

        UserDtoToClient resultUserDto = service.update(id, requestUserDto);

        assertThat(resultUserDto, allOf(
                hasProperty("id", equalTo(updatedUser.getId())),
                hasProperty("name", equalTo(updatedUser.getName())),
                hasProperty("email", equalTo(updatedUser.getEmail()))
        ));
    }

    @Test
    void delete_withNotExistingId_shouldThrowNotFoundException() {
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(id);

        assertThrows(NotFoundException.class, () -> service.delete(id));
    }

    @Test
    void delete_shouldInvokeRepositoryDeleteByIdMethodWithSelectedId() {
        service.delete(id);

        verify(repository, times(1)).deleteById(id);
    }
}