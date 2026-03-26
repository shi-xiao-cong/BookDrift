package com.book.system.mapper;

import com.book.system.entity.BorrowRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BorrowRecordMapper {

    // 查询用户当前借阅
    @Select("SELECT * FROM borrow_record WHERE user_id = #{userId} AND return_date IS NULL")
    List<BorrowRecord> findCurrentBorrowByUserId(Integer userId);

    // 查询书籍当前借阅记录
    @Select("SELECT * FROM borrow_record WHERE book_id = #{bookId} AND return_date IS NULL")
    BorrowRecord findCurrentByBookId(Integer bookId);

    // 查询书籍的所有借阅记录（用于漂流轨迹）
    @Select("SELECT * FROM borrow_record WHERE book_id = #{bookId} ORDER BY borrow_date ASC")
    List<BorrowRecord> findByBookId(Integer bookId);

    // 插入借阅记录（借书）
    @Insert("INSERT INTO borrow_record(borrow_no, book_id, book_title, user_id, user_name, student_id, " +
            "borrow_date, due_date, period, pickup_method, contact, remarks, status, is_overdue, overdue_days) " +
            "VALUES(#{borrowNo}, #{bookId}, #{bookTitle}, #{userId}, #{userName}, #{studentId}, " +
            "#{borrowDate}, #{dueDate}, #{period}, #{pickupMethod}, #{contact}, #{remarks}, #{status}, #{isOverdue}, #{overdueDays})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BorrowRecord record);

    // 更新归还信息（还书）
    @Update("UPDATE borrow_record SET return_date = #{returnDate}, status = #{status}, " +
            "is_overdue = #{isOverdue}, overdue_days = #{overdueDays} WHERE id = #{id}")
    int updateReturn(BorrowRecord record);

    // 统计总借阅次数
    @Select("SELECT COUNT(*) FROM borrow_record")
    int countTotalBorrows();

    // 统计当前借阅数量
    @Select("SELECT COUNT(*) FROM borrow_record WHERE return_date IS NULL")
    int countCurrentBorrows();

    // 统计逾期数量
    @Select("SELECT COUNT(*) FROM borrow_record WHERE return_date IS NULL AND due_date < CURDATE()")
    int countOverdue();
}