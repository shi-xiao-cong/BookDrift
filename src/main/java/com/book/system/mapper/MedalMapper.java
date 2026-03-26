package com.book.system.mapper;

import com.book.system.entity.Medal;
import com.book.system.entity.UserMedal;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MedalMapper {

    // 查询所有勋章
    @Select("SELECT * FROM medal ORDER BY sort_order")
    List<Medal> findAll();

    // 根据ID查询勋章
    @Select("SELECT * FROM medal WHERE id = #{id}")
    Medal findById(@Param("id") Integer id);

    // 查询用户勋章进度
    @Select("SELECT * FROM user_medal WHERE user_id = #{userId}")
    List<UserMedal> findUserMedals(@Param("userId") Integer userId);

    // 查询单个用户勋章
    @Select("SELECT * FROM user_medal WHERE user_id = #{userId} AND medal_id = #{medalId}")
    UserMedal findUserMedal(@Param("userId") Integer userId, @Param("medalId") Integer medalId);

    // 初始化用户勋章（注册时调用）
    @Insert({
            "<script>",
            "INSERT INTO user_medal(user_id, medal_id, current, unlocked, updated_at) VALUES ",
            "<foreach collection='medalIds' item='medalId' separator=','>",
            "(#{userId}, #{medalId}, 0, false, NOW())",
            "</foreach>",
            "</script>"
    })
    int initUserMedals(@Param("userId") Integer userId, @Param("medalIds") List<Integer> medalIds);

    // 初始化单个用户勋章
    @Insert("INSERT INTO user_medal(user_id, medal_id, current, unlocked, updated_at) " +
            "VALUES (#{userId}, #{medalId}, 0, false, NOW())")
    int initSingleUserMedal(@Param("userId") Integer userId, @Param("medalId") Integer medalId);

    // 更新用户勋章进度
    @Update("UPDATE user_medal SET current = #{current}, " +
            "unlocked = CASE WHEN #{current} >= (SELECT required FROM medal WHERE id = medal_id) THEN true ELSE unlocked END, " +
            "unlocked_at = CASE WHEN #{current} >= (SELECT required FROM medal WHERE id = medal_id) AND unlocked = false THEN NOW() ELSE unlocked_at END, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId} AND medal_id = #{medalId}")
    int updateProgress(@Param("userId") Integer userId, @Param("medalId") Integer medalId, @Param("current") int current);

    // 获取勋章类型对应的ID
    @Select("SELECT id FROM medal WHERE type = #{type}")
    List<Integer> findMedalIdsByType(@Param("type") String type);
}