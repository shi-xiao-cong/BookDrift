package com.book.system.service;

import com.book.system.entity.Statistics;
import com.book.system.entity.UserStatistics;
import com.book.system.mapper.BookMapper;
import com.book.system.mapper.BorrowRecordMapper;
import com.book.system.mapper.UserMapper;
import com.book.system.mapper.UserStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private UserStatisticsMapper userStatisticsMapper;

    /**
     * 获取系统统计信息
     */
    public Statistics getSystemStatistics() {
        Statistics stats = new Statistics();

        // 书籍统计
        Integer totalBooks = bookMapper.countTotalBooks();
        stats.setTotalBooks(totalBooks != null ? totalBooks : 0);

        Integer availableBooks = bookMapper.countAvailableBooks();
        stats.setAvailableBooks(availableBooks != null ? availableBooks : 0);

        Integer borrowedBooks = bookMapper.countBorrowedBooks();
        stats.setBorrowedBooks(borrowedBooks != null ? borrowedBooks : 0);

        // 用户统计
        Integer totalUsers = userMapper.countTotal();
        stats.setTotalUsers(totalUsers != null ? totalUsers : 0);

        // 捐赠统计（总书籍数即为总捐赠数）
        stats.setTotalDonations(totalBooks != null ? totalBooks : 0);

        // 借阅统计
        Integer totalBorrows = borrowRecordMapper.countTotalBorrows();
        stats.setTotalBorrows(totalBorrows != null ? totalBorrows : 0);

        return stats;
    }

    /**
     * 获取用户统计信息 - 从 user_statistics 表获取
     */
    public Map<String, Object> getUserStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();

        System.out.println("获取用户统计信息: userId=" + userId);

        // 从 user_statistics 表获取统计数据
        UserStatistics userStats = userStatisticsMapper.findByUserId(userId);

        if (userStats != null) {
            // 捐赠数量
            stats.put("donation_count", userStats.getDonationCount() != null ? userStats.getDonationCount() : 0);

            // 当前借阅数量（从借阅记录表实时获取）
            Integer currentBorrowCount = borrowRecordMapper.findCurrentBorrowByUserId(userId).size();
            stats.put("borrow_count", currentBorrowCount != null ? currentBorrowCount : 0);

            // 历史借阅总数
            Integer totalBorrowCount = userMapper.countUserBorrows(userId);
            stats.put("total_borrow", totalBorrowCount != null ? totalBorrowCount : 0);

            // 笔记数量
            stats.put("note_count", userStats.getNoteCount() != null ? userStats.getNoteCount() : 0);

            System.out.println("从user_statistics获取统计: donation=" + userStats.getDonationCount()
                    + ", note=" + userStats.getNoteCount());
        } else {
            // 如果没有统计记录，返回0
            stats.put("donation_count", 0);
            stats.put("borrow_count", 0);
            stats.put("total_borrow", 0);
            stats.put("note_count", 0);
            System.out.println("用户统计信息不存在，返回默认值");
        }

        return stats;
    }

    /**
     * 更新用户统计信息
     */
    @Transactional
    public void updateUserStatistics(Integer userId, String type, Integer increment) {
        System.out.println("更新用户统计: userId=" + userId + ", type=" + type + ", increment=" + increment);

        // 如果用户统计记录不存在，先创建
        if (!userMapper.checkUserStatisticsExists(userId)) {
            userStatisticsMapper.initUserStatistics(userId);
        }

        // 根据类型更新对应的统计字段
        if ("donation".equals(type)) {
            userStatisticsMapper.updateDonationCount(userId, increment);
        } else if ("borrow".equals(type)) {
            userStatisticsMapper.updateBorrowCount(userId, increment);
        } else if ("note".equals(type)) {
            userStatisticsMapper.updateNoteCount(userId, increment);
        }
    }
}