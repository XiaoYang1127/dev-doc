<!-- @format -->

# 安全规范

## 认证授权

- 使用 JWT 进行身份认证
- Token 存储在 Redis，支持主动失效
- 敏感接口必须鉴权
- 使用 Spring Security 或 Sa-Token

## 数据安全

- 密码使用 BCrypt 加密
- 敏感信息加密存储
- API 接口使用 HTTPS
- 防止 SQL 注入、XSS 攻击

## 参数校验

```java
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
             message = "密码至少8位，包含大小写字母和数字")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

## 输入验证

- 所有外部输入必须校验（长度、类型、范围、格式）
- 不信任前端验证，后端必须重新验证

## 日志脱敏

- 敏感字段（密码、手机号、身份证）在日志中脱敏
- 使用 `***` 替代部分字符
