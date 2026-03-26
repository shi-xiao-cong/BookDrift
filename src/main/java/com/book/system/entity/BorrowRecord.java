package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class BorrowRecord {
    private Integer id;
    private String borrowNo;
    private Integer bookId;
    private String bookTitle;
    private String bookAuthor;      // 书籍作者
    private String bookCover;       // 书籍封面
    private String bookIsbn;        // 书籍ISBN
    private String bookPublisher;   // 书籍出版社
    private Integer userId;
    private String userName;
    private String studentId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private Integer period;
    private String pickupMethod;
    private String contact;
    private String remarks;
    private Boolean isOverdue;
    private Integer overdueDays;
    private String status;  // borrowing/returned/overdue
    private Date createdAt;
}