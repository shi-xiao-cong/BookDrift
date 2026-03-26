package com.book.system.controller;

import com.book.system.entity.User;
import com.book.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;  // 【新增】用于文件上传
import java.io.File;  // 【新增】用于文件操作
import java.io.IOException;
import java.util.UUID;  // 【新增】用于生成唯一文件名
import java.util.Map;
import java.util.HashMap;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 根据学号查询用户（对应BorrowBook.vue的扫码验证）
    @GetMapping("/student/{studentId}")
    public User getUserByStudentId(@PathVariable("studentId") String studentId) {
        System.out.println("根据学号查询用户: " + studentId);
        User user = userMapper.findByStudentId(studentId);
        System.out.println("查询结果: " + user);
        return user;
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") Integer id) {
        return userMapper.findById(id);
    }

    // 用户注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("收到注册请求: " + user);

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
            System.out.println("插入结果: " + insertResult + ", 生成ID: " + user.getId());

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

    // 用户登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            System.out.println("收到登录请求: " + username);

            // 根据学号查询用户
            User user = userMapper.findByStudentId(username);

            // 验证用户是否存在且密码正确
            if (user != null && password.equals(user.getPassword())) {
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("userId", user.getId());
                result.put("username", user.getName());
                result.put("studentId", user.getStudentId());
                result.put("phone", user.getPhone());
                result.put("email", user.getEmail());
                result.put("role", user.getRole());
                result.put("borrowedCount", user.getBorrowedCount());
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

    // ==================== 兼容前端的 /auth 路径 ====================

    /**
     * 兼容前端的 /auth/login 接口
     */
    @PostMapping("/auth/login")
    public Map<String, Object> authLogin(@RequestBody Map<String, String> loginData) {
        System.out.println("收到/auth/login请求: " + loginData);
        return login(loginData);
    }

    /**
     * 兼容前端的 /auth/register 接口
     */
    @PostMapping("/auth/register")
    public Map<String, Object> authRegister(@RequestBody User user) {
        System.out.println("收到/auth/register请求: " + user);
        return register(user);
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable("id") Integer id, @RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        user.setId(id);
        userMapper.update(user);
        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    // 获取所有用户（管理员功能）
    @GetMapping
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            userMapper.deleteById(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // 更新用户角色
    @PutMapping("/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = params.get("role");
            // 需要添加 updateRole 方法到 UserMapper
            // userMapper.updateRole(id, role);
            result.put("success", true);
            result.put("message", "更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    // 搜索用户
    @GetMapping("/search")
    public Map<String, Object> searchUsers(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userMapper.findByKeyword("%" + keyword + "%");
            result.put("success", true);
            result.put("data", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索失败：" + e.getMessage());
        }
        return result;

    }

    // ==================== 新增头像相关接口 ====================

    // 上传头像
    @PostMapping("/avatar/{userId}")
    public Map<String, Object> uploadAvatar(@PathVariable Integer userId,
                                            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("收到头像上传请求，用户ID: " + userId);

            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只能上传图片文件");
                return result;
            }

            // 检查文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "图片大小不能超过2MB");
                return result;
            }
            String uploadDir = new File("uploads/avatars").getAbsolutePath() + File.separator;
            System.out.println("上传目录: " + uploadDir);

            // 创建上传目录（如果不存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("创建目录结果: " + created);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = userId + "_" + UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadDir + fileName;

            // 保存文件
            File dest = new File(filePath);
            file.transferTo(dest);
            System.out.println("文件保存成功: " + filePath);

            // 生成访问URL
            String avatarUrl = "http://localhost:10011/uploads/avatars/" + fileName;

            // 更新数据库中的用户头像
            userMapper.updateAvatar(userId, avatarUrl);

            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("avatarUrl", avatarUrl);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }

        return result;
    }

    // 获取头像
    @GetMapping("/avatar/{userId}")
    public Map<String, Object> getAvatar(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("获取头像，用户ID: " + userId);

            // 从数据库获取用户信息
            User user = userMapper.findById(userId);

            if (user != null && user.getAvatar() != null) {
                result.put("success", true);
                result.put("avatarUrl", user.getAvatar());
            } else {
                // 如果没有头像，返回默认头像
                result.put("success", true);
                result.put("avatarUrl", "https://via.placeholder.com/120");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @PutMapping("/info/{userId}")
    public Map<String, Object> updateUserInfo(@PathVariable Integer userId,
                                              @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String name = params.get("name");
            String phone = params.get("phone");
            String email = params.get("email");

            // 验证姓名
            if (name == null || name.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "姓名不能为空");
                return result;
            }
            if (name.length() < 2) {
                result.put("success", false);
                result.put("message", "姓名至少2个字符");
                return result;
            }

            // 验证手机号格式
            if (phone != null && !phone.isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    result.put("success", false);
                    result.put("message", "手机号格式不正确");
                    return result;
                }
            }

            // 验证邮箱格式
            if (email != null && !email.isEmpty()) {
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    result.put("success", false);
                    result.put("message", "邮箱格式不正确");
                    return result;
                }
            }

            // 更新用户信息
            userMapper.updateUserInfo(userId, name, phone, email);

            // 获取更新后的用户信息
            User updatedUser = userMapper.findById(userId);

            result.put("success", true);
            result.put("message", "修改成功");
            result.put("user", updatedUser);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "修改失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 修改密码
     */
    @PutMapping("/password/{userId}")
    public Map<String, Object> updatePassword(@PathVariable Integer userId,
                                              @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            // 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 验证旧密码
            if (!oldPassword.equals(user.getPassword())) {
                result.put("success", false);
                result.put("message", "原密码错误");
                return result;
            }

            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "新密码长度至少6位");
                return result;
            }

            // 更新密码
            userMapper.updatePassword(userId, newPassword);

            result.put("success", true);
            result.put("message", "密码修改成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "修改失败：" + e.getMessage());
        }
        return result;
    }

}