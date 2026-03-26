package com.book.system.mapper;

import com.book.system.entity.Book;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookMapper {

    // ==================== 原有方法 ====================

    @Select("SELECT * FROM book ORDER BY donate_time DESC")
    List<Book> findAll();

    @Select("SELECT * FROM book WHERE id = #{id}")
    Book findById(@Param("id") Integer id);

    @Select("SELECT * FROM book WHERE book_id = #{bookId}")
    Book findByBookId(@Param("bookId") String bookId);

    @Select("SELECT * FROM book WHERE status = #{status} ORDER BY donate_time DESC")
    List<Book> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM book WHERE status = 'available' ORDER BY donate_time DESC")
    List<Book> findAvailableBooks();

    @Select("SELECT * FROM book WHERE donor_name = #{donorName} ORDER BY donate_time DESC")
    List<Book> findByDonor(@Param("donorName") String donorName);

    // ==================== 搜索方法 ====================

    @Select("SELECT * FROM book WHERE title LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%') ORDER BY donate_time DESC")
    List<Book> search(@Param("keyword") String keyword);

    // ==================== 统计方法 ====================

    @Select("SELECT COUNT(*) FROM book")
    Integer countTotalBooks();

    @Select("SELECT COUNT(*) FROM book WHERE status = 'available'")
    Integer countAvailableBooks();

    @Select("SELECT COUNT(*) FROM book WHERE status = 'borrowed'")
    Integer countBorrowedBooks();

    // ==================== 插入方法 ====================

    @Insert("INSERT INTO book (book_id, title, author, isbn, publisher, cover_url, `condition`, status, donor_name, donate_time, description, qr_code_data) " +
            "VALUES (#{bookId}, #{title}, #{author}, #{isbn}, #{publisher}, #{coverUrl}, #{condition}, #{status}, #{donorName}, #{donateTime}, #{description}, #{qrCodeData})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    // ==================== 更新方法 ====================

    @Update("UPDATE book SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 更新书籍信息（管理员用）
     */
    @Update("UPDATE book SET title = #{title}, author = #{author}, isbn = #{isbn}, publisher = #{publisher}, " +
            "cover_url = #{coverUrl}, `condition` = #{condition}, description = #{description}, status = #{status} WHERE id = #{id}")
    int updateBookInfo(Book book);

    // ==================== 删除方法 ====================

    @Delete("DELETE FROM book WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM book WHERE book_id = #{bookId}")
    int deleteByBookId(@Param("bookId") String bookId);
}