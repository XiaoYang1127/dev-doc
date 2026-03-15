// ✅ 正例：分层清晰

package com.example.user.controller;

import com.example.user.api.dto.UserDTO;
import com.example.user.api.dto.UserVO;
import com.example.user.api.request.UserCreateRequest;
import com.example.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // ✅ Controller 只依赖 Service 接口
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    // ✅ 返回 VO，不暴露内部 PO
    public UserVO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    // ✅ 参数用 Request/CO，返回用 DTO/VO
    public UserDTO createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }
}

// Service 层实现业务逻辑
package com.example.user.service.impl;

import com.example.user.api.dto.UserDTO;
import com.example.user.api.dto.UserVO;
import com.example.user.api.request.UserCreateRequest;
import com.example.user.converter.UserConverter;
import com.example.user.entity.UserPO;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public UserServiceImpl(UserRepository userRepository,
                          UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public UserVO getUserById(Long id) {
        UserPO po = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        // ✅ PO 转换为 VO 再返回
        return userConverter.toVO(po);
    }

    @Override
    public UserDTO createUser(UserCreateRequest request) {
        // ✅ DTO 转换为 PO 再持久化
        UserPO po = userConverter.toPO(request);
        UserPO saved = userRepository.save(po);
        return userConverter.toDTO(saved);
    }
}
