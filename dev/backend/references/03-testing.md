# 测试规范

> 通用测试原则参考 `~/.kiro/steering/testing-standards.md`

## 单元测试（JUnit 5 + Mockito）

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("创建用户 - 成功")
    void testCreate_Success() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("test");
        request.setPassword("Test123456");
        request.setEmail("test@example.com");

        // Act
        Long userId = userService.create(request);

        // Assert
        assertNotNull(userId);
        assertTrue(userId > 0);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void testCreate_UsernameExists() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");

        when(userRepository.findByUsername("existing"))
            .thenReturn(new User());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            userService.create(request);
        });
    }
}
```

## 集成测试（TestContainers）

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        // 保存用户
        User user = new User();
        user.setUsername("test");
        userRepository.save(user);

        // 查询用户
        User found = userRepository.findByUsername("test");
        assertNotNull(found);
        assertEquals("test", found.getUsername());
    }
}
```

## 测试命名

- 格式：`should_[预期行为]_when_[条件]`
- 或：`[方法名]_[场景]_[预期]`

## Mock 原则

- 外部依赖（数据库、HTTP、文件系统）必须 Mock
- 不要测试框架生成的代码、纯数据类、第三方库
