package com.book.system.mapper;

import com.book.system.entity.Note;
import com.book.system.entity.NoteLike;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NoteMapper {

    // 查询书籍的笔记
    @Select("SELECT n.*, u.name as user_name,u.avatar as user_avatar FROM note n " +
            "LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE n.book_id = #{bookId} ORDER BY n.created_at DESC")
    @Results({
            @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Note> findByBookId(Integer bookId);

    // 根据ID查询笔记
    @Select("SELECT n.id, n.book_id, n.user_id, n.user_name, n.content, n.likes, n.created_at, n.updated_at, u.avatar as user_avatar " +
            "FROM note n " +
            "LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE n.id = #{id}")
    @Results({
            @Result(property = "userAvatar", column = "user_avatar")
    })
    Note findById(@Param("id") Integer id);

    // 插入笔记
    @Insert("INSERT INTO note(book_id, user_id, user_name, content, likes, created_at) " +
            "VALUES(#{bookId}, #{userId}, #{userName}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Note note);

    // 更新笔记
    @Update("UPDATE note SET content = #{content}, updated_at = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int update(Note note);

    // 删除笔记
    @Delete("DELETE FROM note WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Integer id, @Param("userId") Integer userId);

    // 查询用户是否点赞
    @Select("SELECT COUNT(*) FROM note_like WHERE note_id = #{noteId} AND user_id = #{userId}")
    int checkLike(@Param("noteId") Integer noteId, @Param("userId") Integer userId);

    // 添加点赞
    @Insert("INSERT INTO note_like(note_id, user_id) VALUES(#{noteId}, #{userId})")
    int addLike(@Param("noteId") Integer noteId, @Param("userId") Integer userId);

    // 取消点赞
    @Delete("DELETE FROM note_like WHERE note_id = #{noteId} AND user_id = #{userId}")
    int removeLike(@Param("noteId") Integer noteId, @Param("userId") Integer userId);

    // 更新笔记点赞数
    @Update("UPDATE note SET likes = likes + #{delta} WHERE id = #{noteId}")
    int updateLikes(@Param("noteId") Integer noteId, @Param("delta") int delta);
}