package com.book.system.mapper;

import com.book.system.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    // ==================== 原有方法 ====================

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Integer id);

    @Select("SELECT * FROM user WHERE student_id = #{studentId}")
    User findByStudentId(@Param("studentId") String studentId);

    // 【新增】根据姓名查询用户
    @Select("SELECT * FROM user WHERE name = #{name}")
    User findByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM user")
    Integer countTotal();

    @Select("SELECT COUNT(*) FROM book WHERE donor_name = (SELECT name FROM user WHERE id = #{userId})")
    Integer countUserDonations(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId}")
    Integer countUserBorrows(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM note WHERE user_id = #{userId}")
    Integer countUserNotes(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM user_statistics WHERE user_id = #{userId}")
    boolean checkUserStatisticsExists(@Param("userId") Integer userId);

    @Insert("INSERT INTO user_statistics (user_id, donation_count, borrow_count, note_count) VALUES (#{userId}, 0, 0, 0)")
    void initUserStatistics(@Param("userId") Integer userId);

    // ==================== 原有新增方法 ====================

    // 更新用户借阅数量（借书时+1，还书时-1）
    @Update("UPDATE user SET borrowed_count = borrowed_count + #{increment} WHERE id = #{userId}")
    void updateBorrowedCount(@Param("userId") Integer userId, @Param("increment") int increment);

    // 更新用户统计表中的借阅数量
    @Update("UPDATE user_statistics SET borrow_count = borrow_count + #{increment}, updated_at = NOW() WHERE user_id = #{userId}")
    void updateUserBorrows(@Param("userId") Integer userId, @Param("increment") Integer increment);

    // 更新用户统计表中的捐赠数量
    @Update("UPDATE user_statistics SET donation_count = donation_count + #{increment}, updated_at = NOW() WHERE user_id = #{userId}")
    void updateUserDonations(@Param("userId") Integer userId, @Param("increment") Integer increment);

    // 更新用户统计表中的笔记数量
    @Update("UPDATE user_statistics SET note_count = note_count + #{increment}, updated_at = NOW() WHERE user_id = #{userId}")
    void updateUserNotes(@Param("userId") Integer userId, @Param("increment") Integer increment);

    // 插入新用户
    @Insert("INSERT INTO user (student_id, name, password, phone, email, role, borrowed_count, created_at) " +
            "VALUES (#{studentId}, #{name}, #{password}, #{phone}, #{email}, #{role}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    // 更新用户信息
    @Update("UPDATE user SET name = #{name}, phone = #{phone}, email = #{email} WHERE id = #{id}")
    int update(User user);

    // 删除用户
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 根据关键字搜索用户
    @Select("SELECT * FROM user WHERE student_id LIKE #{keyword} OR name LIKE #{keyword}")
    List<User> findByKeyword(@Param("keyword") String keyword);

    // 更新用户角色
    @Update("UPDATE user SET role = #{role} WHERE id = #{id}")
    int updateRole(@Param("id") Integer id, @Param("role") String role);

    // 更新用户头像
    @Update("UPDATE user SET avatar = #{avatarUrl} WHERE id = #{userId}")
    void updateAvatar(@Param("userId") Integer userId, @Param("avatarUrl") String avatarUrl);

    // 获取用户头像
    @Select("SELECT avatar FROM user WHERE id = #{userId}")
    String getAvatar(@Param("userId") Integer userId);


    /**
     * 更新用户信息（姓名、手机号、邮箱）
     */
    @Update("UPDATE user SET name = #{name}, phone = #{phone}, email = #{email} WHERE id = #{userId}")
    int updateUserInfo(@Param("userId") Integer userId,
                       @Param("name") String name,
                       @Param("phone") String phone,
                       @Param("email") String email);

    /**
     * 更新密码
     */
    @Update("UPDATE user SET password = #{password} WHERE id = #{userId}")
    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);
}
