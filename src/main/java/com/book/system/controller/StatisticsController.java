package com.book.system.controller;

import com.book.system.entity.Statistics;
import com.book.system.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取系统统计（原有方法，返回 Statistics 对象）
     */
    @GetMapping("/system")
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            Statistics stats = statisticsService.getSystemStatistics();

            Map<String, Object> data = new HashMap<>();
            data.put("totalBooks", stats.getTotalBooks());
            data.put("availableBooks", stats.getAvailableBooks());
            data.put("borrowedBooks", stats.getBorrowedBooks());
            data.put("totalUsers", stats.getTotalUsers());
            data.put("totalDonations", stats.getTotalDonations());
            data.put("totalBorrows", stats.getTotalBorrows());

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取统计失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserStatistics(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> stats = statisticsService.getUserStatistics(userId);
            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 更新用户统计（前端操作时调用）
     */
    @PostMapping("/update")
    public Map<String, Object> updateUserStatistics(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer userId = (Integer) request.get("userId");
            String type = (String) request.get("type");
            Integer increment = request.get("increment") != null ?
                    (Integer) request.get("increment") : 1;

            System.out.println("更新用户统计: userId=" + userId + ", type=" + type + ", increment=" + increment);

            statisticsService.updateUserStatistics(userId, type, increment);

            // 获取更新后的统计信息
            Map<String, Object> updatedStats = statisticsService.getUserStatistics(userId);

            result.put("success", true);
            result.put("message", "更新成功");
            result.put("data", updatedStats);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}