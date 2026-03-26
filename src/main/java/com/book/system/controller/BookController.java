package com.book.system.controller;

import com.book.system.entity.Book;
import com.book.system.service.BookService;
import com.book.system.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    // 获取可借阅书籍列表
    @GetMapping("/available")
    public List<Book> getAvailableBooks() {
        System.out.println("获取可借阅书籍列表");
        return bookService.getAvailableBooks();
    }

    // 根据ID查询书籍详情
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Integer id) {
        System.out.println("查询书籍详情，ID=" + id);
        return bookService.findById(id);
    }

    // 获取书籍漂流轨迹
    @GetMapping("/{id}/trajectory")
    public List<Map<String, Object>> getBookTrajectory(@PathVariable Integer id) {
        return borrowService.getBookTrajectory(id);
    }

    // 根据二维码标识查询书籍
    @GetMapping("/qr/{identifier}")
    public Book getBookByIdentifier(@PathVariable String identifier) {
        System.out.println("收到请求：二维码查询，标识=" + identifier);

        try {
            Integer id = Integer.parseInt(identifier);
            Book book = bookService.findById(id);
            if (book != null) {
                System.out.println("通过数字ID查询成功：id=" + id);
                return book;
            }
        } catch (NumberFormatException e) {
            System.out.println("标识不是数字，尝试用book_id查询");
        }

        Book book = bookService.findByBookId(identifier);
        if (book != null) {
            System.out.println("通过book_id查询成功：book_id=" + identifier);
        } else {
            System.out.println("未找到书籍，标识=" + identifier);
        }
        return book;
    }

    // 捐赠书籍 - 修改接收参数
    @PostMapping("/donate")
    public Map<String, Object> donateBook(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("收到捐赠请求: " + params);

            // 从参数中提取书籍信息
            Book book = new Book();
            book.setTitle((String) params.get("title"));
            book.setAuthor((String) params.get("author"));
            book.setDonorName((String) params.get("donorName"));
            book.setCondition((String) params.get("condition"));
            book.setIsbn((String) params.get("isbn"));
            book.setPublisher((String) params.get("publisher"));
            book.setDescription((String) params.get("description"));
            book.setCoverUrl((String) params.get("coverUrl"));

            // 获取用户ID
            Integer userId = null;
            if (params.get("userId") != null) {
                userId = Integer.parseInt(params.get("userId").toString());
            }

            System.out.println("捐赠人用户ID: " + userId);

            // 参数验证
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "书名不能为空");
                return result;
            }
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "作者不能为空");
                return result;
            }
            if (book.getDonorName() == null || book.getDonorName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "捐赠人姓名不能为空");
                return result;
            }

            // 调用service保存书籍，传入userId
            Book savedBook = bookService.donateBook(book, userId);

            result.put("success", true);
            result.put("message", "捐赠成功");
            result.put("bookId", savedBook.getBookId());
            result.put("qrCodeData", savedBook.getQrCodeData());

        } catch (NumberFormatException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "用户ID格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "捐赠失败：" + e.getMessage());
        }

        return result;
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取所有书籍（包括已借出）
     */
    @GetMapping("/all")
    public Map<String, Object> getAllBooks() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Book> books = bookService.findAll();
            result.put("success", true);
            result.put("data", books);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除书籍
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteBook(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = bookService.deleteBook(id);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "书籍不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新书籍信息
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateBook(@PathVariable Integer id, @RequestBody Book book) {
        Map<String, Object> result = new HashMap<>();
        try {
            book.setId(id);
            int rows = bookService.updateBookInfo(book);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败，书籍不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 搜索书籍（管理员用）
     */
    @GetMapping("/search")
    public Map<String, Object> searchBooks(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Book> books = bookService.searchBooks(keyword);
            result.put("success", true);
            result.put("data", books);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "搜索失败：" + e.getMessage());
        }
        return result;
    }
}