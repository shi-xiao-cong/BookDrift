package com.book.system.mapper;

import com.book.system.entity.UserStatistics;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserStatisticsMapper {

    // 根据用户ID查询统计信息
    @Select("SELECT * FROM user_statistics WHERE user_id = #{userId}")
    UserStatistics findByUserId(@Param("userId") Integer userId);

    // 初始化用户统计记录
    @Insert("INSERT INTO user_statistics (user_id, donation_count, borrow_count, note_count) " +
            "VALUES (#{userId}, 0, 0, 0)")
    int initUserStatistics(@Param("userId") Integer userId);

    // 更新捐赠数量
    @Update("UPDATE user_statistics SET donation_count = donation_count + #{increment}, updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int updateDonationCount(@Param("userId") Integer userId, @Param("increment") int increment);

    // 更新借阅数量
    @Update("UPDATE user_statistics SET borrow_count = borrow_count + #{increment}, updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int updateBorrowCount(@Param("userId") Integer userId, @Param("increment") int increment);

    // 更新笔记数量
    @Update("UPDATE user_statistics SET note_count = note_count + #{increment}, updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int updateNoteCount(@Param("userId") Integer userId, @Param("increment") int increment);
}