// ❌ 反例：分层越界
// 问题：Controller 直接操作 Repository

package com.example.user.controller;

import com.example.user.repository.UserRepository;
import com.example.user.entity.UserPO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // ❌ 错误：Controller 直接注入 Repository
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    // ❌ 错误：直接返回 PO 给前端
    public UserPO getUser(@PathVariable Long id) {
        // ❌ 错误：Controller 直接查询数据库
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    // ❌ 错误：直接保存 DTO，未转换为 PO
    public void createUser(@RequestBody UserCreateRequest request) {
        // 应该转换为 PO 再保存
        userRepository.save(request);  // 类型不匹配！
    }
}

// 正确做法见 good/service-layer.java
