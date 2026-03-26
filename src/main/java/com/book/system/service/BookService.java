package com.book.system.service;

import com.book.system.entity.Book;
import com.book.system.entity.BorrowRecord;
import com.book.system.entity.User;
import com.book.system.mapper.BookMapper;
import com.book.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MedalService medalService;

    // 注入 BorrowService
    @Autowired
    private BorrowService borrowService;

    /**
     * 获取所有可借阅的书籍
     */
    public List<Book> getAvailableBooks() {
        return bookMapper.findAvailableBooks();
    }

    /**
     * 根据ID查询书籍
     */
    public Book findById(Integer id) {
        return bookMapper.findById(id);
    }

    /**
     * 根据book_id查询书籍（二维码中的标识）
     */
    public Book findByBookId(String bookId) {
        return bookMapper.findByBookId(bookId);
    }

    /**
     * 捐赠书籍 - 接收userId参数
     */
    @Transactional
    public Book donateBook(Book book, Integer userId) {
        // 生成唯一书籍ID（格式：BOOK + 时间戳）
        book.setBookId("BOOK" + System.currentTimeMillis());

        // 设置初始状态为可借阅
        book.setStatus("available");

        // 设置捐赠时间
        book.setDonateTime(new Date());

        // 生成二维码数据（JSON格式）
        String qrData = String.format(
                "{\"bookId\":\"%s\",\"title\":\"%s\",\"donor\":\"%s\"}",
                book.getBookId(), book.getTitle(), book.getDonorName()
        );
        book.setQrCodeData(qrData);

        System.out.println("准备插入的书籍数据: " + book);

        try {
            // 插入数据库
            int result = bookMapper.insert(book);
            System.out.println("插入结果: " + result);
            System.out.println("生成的ID: " + book.getId());

            // 更新捐赠勋章进度 - 优先使用传入的userId
            if (userId != null) {
                medalService.updateProgress(userId, "donation", 1);
                System.out.println("更新捐赠勋章进度: userId=" + userId);
            } else {
                // 如果没有传入userId，尝试通过姓名查找
                User donor = userMapper.findByName(book.getDonorName());
                if (donor != null) {
                    medalService.updateProgress(donor.getId(), "donation", 1);
                    System.out.println("通过姓名查找更新捐赠勋章进度: userId=" + donor.getId());
                } else {
                    System.out.println("未找到捐赠人: " + book.getDonorName());
                }
            }

        } catch (Exception e) {
            System.out.println("插入失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("书籍保存失败：" + e.getMessage());
        }

        return book;
    }

    /**
     * 更新书籍状态
     */
    @Transactional
    public int updateStatus(Integer id, String status) {
        return bookMapper.updateStatus(id, status);
    }

    /**
     * 搜索书籍（按书名或作者）
     */
    public List<Book> searchBooks(String keyword) {
        return bookMapper.search(keyword);
    }

    /**
     * 获取所有书籍（管理员用）
     */
    public List<Book> findAll() {
        return bookMapper.findAll();
    }

    /**
     * 根据状态获取书籍
     */
    public List<Book> findByStatus(String status) {
        return bookMapper.findByStatus(status);
    }

    /**
     * 删除书籍（管理员用）
     */
    @Transactional
    public int deleteBook(Integer id) {
        return bookMapper.deleteById(id);
    }

    /**
     * 更新书籍信息（管理员用）
     */
    @Transactional
    public int updateBookInfo(Book book) {
        // 1. 先查询原书籍信息，获取原状态
        Book oldBook = bookMapper.findById(book.getId());
        if (oldBook == null) {
            throw new RuntimeException("书籍不存在");}
        // 2. 判断状态是否从 borrowed 变为 available
        if ("borrowed".equals(oldBook.getStatus()) && "available".equals(book.getStatus())) {
            // 查询当前是否有未归还的借阅记录
            BorrowRecord currentRecord = borrowService.findCurrentByBookId(book.getId());
            if (currentRecord != null) {
                // 调用还书逻辑（BorrowService 中已有的 returnBook 方法）
                // 注意：returnBook 方法已经包含了更新书籍状态、借阅记录、用户借阅数等操作
                // 因此此处直接调用即可
                borrowService.returnBook(book.getId());
                // 注意：returnBook 内部会将书籍状态更新为 available，所以后面无需再更新状态
                // 但其他字段（如 title, author 等）仍然需要更新，所以继续执行下面的 updateBookInfo
            }
        }
        int rows = bookMapper.updateBookInfo(book);
        return rows;

        // return bookMapper.updateBookInfo(book);
    }
}