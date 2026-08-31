package com.davyd.integration.repository;

import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.models.User;
import com.davyd.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private void flushAndClearPersistenceContext(){
        entityManager.flush();
        entityManager.clear();
    }

    private User createUserForTest(){
        String email = "dav@gmail.com";
        String name = "Davyd";

        return new User(name, email);
    }

    @Test
    void shouldSaveAndGet(){
        User user = createUserForTest();

        userRepository.save(user);

        flushAndClearPersistenceContext();

        Optional<User> savedUser = userRepository.findByEmail(user.getEmail());

        assertTrue(savedUser.isPresent());
        assertNotNull(savedUser.get().getId());
        assertEquals(user.getName(), savedUser.get().getName());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
    }

    @Test
    void shouldExistByEmail(){
        User user = createUserForTest();

        userRepository.save(user);

        flushAndClearPersistenceContext();

        assertTrue(userRepository.existsByEmail(user.getEmail()));
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        assertFalse(userRepository.existsByEmail("unknown@gmail.com"));
    }

    @Test
    void shouldDeleteUser(){
        User user = createUserForTest();

        userRepository.save(user);

        flushAndClearPersistenceContext();

        userRepository.delete(user);

        flushAndClearPersistenceContext();

        Optional<User> savedUser = userRepository.findByEmail(user.getEmail());

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void shouldThrowWhenSavingUserWithSameEmail(){
        User user = createUserForTest();

        User otherUser = new User("adri", user.getEmail());

        userRepository.save(user);

        flushAndClearPersistenceContext();

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(otherUser));
    }

    @Test
    void shouldSaveAndGetManyUsers(){
        String name = "Same";
        String email = "1@gmail.com";
        int userCount = 10;

        for (int i = 0; i < userCount; i++){
            userRepository.save(new User(name, i+email));
        }

        flushAndClearPersistenceContext();

        List<User> users = userRepository.findAll();

        assertEquals(userCount, users.size());
    }

    @Test
    void shouldDeleteManyUsers(){
        String name = "Same";
        String email = "1@gmail.com";
        int userCount = 10;

        for (int i = 0; i < userCount; i++){
            userRepository.save(new User(name, i+email));
        }

        flushAndClearPersistenceContext();

        List<User> users = userRepository.findAll();

        assertEquals(userCount, users.size());

        userRepository.deleteById(users.get(0).getId());
        userRepository.delete(users.get(1));
        userRepository.delete(users.get(5));

        assertEquals(userCount - 3, userRepository.findAll().size());
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentUser(){
        assertDoesNotThrow(() -> userRepository.deleteById(1L));
    }
}
