package com.book.system.controller;

import com.book.system.service.BorrowService;
import com.book.system.entity.BorrowRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin(origins = "*")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    // 借书接口
    @PostMapping("/borrow")
    public Map<String, Object> borrowBook(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("收到借阅请求: " + params);

            // 安全地获取参数，处理类型转换
            Integer userId = null;
            Integer bookId = null;
            Integer period = null;
            String pickupMethod = null;
            String contact = null;
            String remarks = null;

            // 处理 userId
            Object userIdObj = params.get("userId");
            if (userIdObj != null && !userIdObj.toString().trim().isEmpty()) {
                userId = Integer.parseInt(userIdObj.toString());
            } else {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }

            // 处理 bookId
            Object bookIdObj = params.get("bookId");
            if (bookIdObj != null) {
                bookId = Integer.parseInt(bookIdObj.toString());
            } else {
                result.put("success", false);
                result.put("message", "书籍ID不能为空");
                return result;
            }

            // 处理 period
            Object periodObj = params.get("period");
            if (periodObj != null) {
                period = Integer.parseInt(periodObj.toString());
            } else {
                period = 14; // 默认14天
            }

            pickupMethod = (String) params.get("pickupMethod");
            contact = (String) params.get("contact");
            remarks = (String) params.get("remarks");

            BorrowRecord record = borrowService.borrowBook(userId, bookId, period, pickupMethod, contact, remarks);

            result.put("success", true);
            result.put("borrowNo", record.getBorrowNo());
            result.put("dueDate", record.getDueDate());
            result.put("bookId", record.getBookId());
            result.put("bookTitle", record.getBookTitle());
            result.put("userId", record.getUserId());
            result.put("userName", record.getUserName());

        } catch (NumberFormatException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "参数格式错误：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 还书接口
    @PostMapping("/return/{bookId}")
    public Map<String, Object> returnBook(@PathVariable Integer bookId) {
        Map<String, Object> result = new HashMap<>();

        try {
            BorrowRecord record = borrowService.returnBook(bookId);

            result.put("success", true);
            result.put("returnDate", record.getReturnDate());
            result.put("isOverdue", record.getIsOverdue());
            result.put("overdueDays", record.getOverdueDays());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 获取当前借阅记录接口
    @GetMapping("/current/{bookId}")
    public Map<String, Object> getCurrentBorrow(@PathVariable Integer bookId) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("查询当前借阅记录, bookId: " + bookId);

            BorrowRecord record = borrowService.findCurrentByBookId(bookId);

            if (record != null) {
                result.put("success", true);
                result.put("userName", record.getUserName());
                result.put("studentId", record.getStudentId());
                result.put("borrowDate", record.getBorrowDate());
                result.put("dueDate", record.getDueDate());
                result.put("isOverdue", record.getIsOverdue());
                result.put("overdueDays", record.getOverdueDays());
                result.put("status", record.getStatus());
            } else {
                result.put("success", false);
                result.put("message", "没有找到借阅记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 获取用户当前借阅列表
    @GetMapping("/current/user/{userId}")
    public Map<String, Object> getUserCurrentBorrows(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("查询用户当前借阅列表, userId: " + userId);

            List<BorrowRecord> records = borrowService.findCurrentByUserId(userId);

            result.put("success", true);
            result.put("data", records);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}