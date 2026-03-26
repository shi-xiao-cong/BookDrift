package com.book.system.controller;

import com.book.system.entity.User;
import com.book.system.mapper.UserMapper;
import com.book.system.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MedalService medalService;

    // 用户登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            System.out.println("登录请求: " + username);

            // 根据学号查询用户
            User user = userMapper.findByStudentId(username);

            if (user != null && password.equals(user.getPassword())) {
                // 生成token
                String token = UUID.randomUUID().toString().replace("-", "");

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("studentId", user.getStudentId());
                userInfo.put("name", user.getName());
                userInfo.put("phone", user.getPhone());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole());
                userInfo.put("borrowedCount", user.getBorrowedCount());

                result.put("success", true);
                result.put("message", "登录成功");
                result.put("token", token);
                result.put("user", userInfo);
            } else {
                result.put("success", false);
                result.put("message", "学号或密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "登录失败：" + e.getMessage());
        }

        return result;
    }

    // 用户注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("注册请求: " + user);

            // 检查学号是否已存在
            User existingUser = userMapper.findByStudentId(user.getStudentId());
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "学号已存在");
                return result;
            }

            // 设置默认值
            user.setRole("user");
            user.setBorrowedCount(0);

            // 保存用户
            int insertResult = userMapper.insert(user);
            System.out.println("用户插入结果: " + insertResult + ", 生成ID: " + user.getId());

            // 初始化用户勋章和统计
            medalService.initUserMedals(user.getId());

            result.put("success", true);
            result.put("message", "注册成功");
            result.put("userId", user.getId());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "注册失败：" + e.getMessage());
        }

        return result;
    }

    // 退出登录
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出成功");
        return result;
    }
}