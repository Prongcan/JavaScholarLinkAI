package com.scholarlink.repository;

import com.scholarlink.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setInterest("AI");

        User saved = userRepository.save(user);

        assertNotNull(saved.getUserId());
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testExistsByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testUniqueUsername() {
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("pass1");
        userRepository.save(user1);

        // 尝试创建同名用户应该失败（由数据库约束处理）
        User user2 = new User();
        user2.setUsername("testuser");
        user2.setPassword("pass2");

        // 这应该抛出异常或由数据库约束处理
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            userRepository.flush();
        });
    }
}

