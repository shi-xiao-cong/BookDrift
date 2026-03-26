package com.book.system.controller;

import com.book.system.entity.Book;
import com.book.system.entity.User;
import com.book.system.mapper.UserMapper;
import com.book.system.service.BookService;
import com.book.system.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MedalService medalService;

    @Autowired
    private UserMapper userMapper;

    // 捐赠书籍
    @PostMapping
    public Map<String, Object> donateBook(@RequestBody Map<String, Object> params,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
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
}