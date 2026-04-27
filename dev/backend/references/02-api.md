<!-- @format -->

# API 设计规范

## RESTful 规范

- URL 使用 kebab-case：`/api/v1/user-orders`
- 资源命名使用复数：`/users`、`/orders`
- HTTP 方法语义：`GET`（查）、`POST`（增）、`PUT`（改）、`DELETE`（删）

## 统一响应封装

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private String traceId;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }
}
```

## API 契约层规范

```java
/**
 * 用户服务 API 契约
 */
public interface UserFacade {

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<UserResponse> getById(Long id);

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户ID
     */
    Result<Long> create(UserCreateRequest request);

    /**
     * 分页查询用户
     *
     * @param request 查询条件
     * @return 用户列表
     */
    Result<PageResult<UserResponse>> page(UserPageRequest request);
}
```

## Controller 层规范

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserFacade {

    private final UserService userService;

    @Override
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Override
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }
}
```

## 版本控制

- URL 版本前缀：`/api/v1/`
- 破坏性变更时升级版本号
