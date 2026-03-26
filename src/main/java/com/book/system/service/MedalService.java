package com.book.system.service;

import com.book.system.entity.Medal;
import com.book.system.entity.UserMedal;
import com.book.system.entity.UserStatistics;
import com.book.system.mapper.MedalMapper;
import com.book.system.mapper.UserStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedalService {

    @Autowired
    private MedalMapper medalMapper;

    @Autowired
    private UserStatisticsMapper userStatisticsMapper;

    // 获取用户勋章进度
    public List<Map<String, Object>> getUserMedals(Integer userId) {
        System.out.println("获取用户勋章: userId=" + userId);

        // 1. 获取用户统计信息
        UserStatistics stats = userStatisticsMapper.findByUserId(userId);
        if (stats == null) {
            System.out.println("用户统计信息不存在，初始化...");
            stats = new UserStatistics();
            stats.setUserId(userId);
            stats.setDonationCount(0);
            stats.setBorrowCount(0);
            stats.setNoteCount(0);
            // 尝试初始化
            try {
                userStatisticsMapper.initUserStatistics(userId);
            } catch (Exception e) {
                System.out.println("初始化统计信息失败: " + e.getMessage());
            }
        }

        final UserStatistics finalStats = stats;

        System.out.println("用户统计: donation=" + finalStats.getDonationCount()
                + ", borrow=" + finalStats.getBorrowCount()
                + ", note=" + finalStats.getNoteCount());

        // 2. 获取所有勋章
        List<Medal> allMedals = medalMapper.findAll();
        System.out.println("勋章数量: " + allMedals.size());

        // 3. 获取用户已有的勋章记录
        List<UserMedal> userMedals = medalMapper.findUserMedals(userId);
        Map<Integer, UserMedal> userMedalMap = userMedals.stream()
                .collect(Collectors.toMap(UserMedal::getMedalId, um -> um));

        // 4. 组装数据
        return allMedals.stream().map(medal -> {
            Map<String, Object> result = new HashMap<>();

            // 根据勋章类型从统计表中获取当前进度
            int currentCount = 0;
            if ("donation".equals(medal.getType())) {
                currentCount = finalStats.getDonationCount() != null ? finalStats.getDonationCount() : 0;
            } else if ("borrow".equals(medal.getType())) {
                currentCount = finalStats.getBorrowCount() != null ? finalStats.getBorrowCount() : 0;
            } else if ("note".equals(medal.getType())) {
                currentCount = finalStats.getNoteCount() != null ? finalStats.getNoteCount() : 0;
            }

            // 获取用户勋章记录（如果有）
            UserMedal um = userMedalMap.get(medal.getId());

            // 判断是否解锁
            boolean isUnlocked = currentCount >= medal.getRequired();

            Map<String, Object> medalInfo = new HashMap<>();
            medalInfo.put("id", medal.getId());
            medalInfo.put("name", medal.getName());
            medalInfo.put("description", medal.getDescription());
            medalInfo.put("icon", medal.getIcon());
            medalInfo.put("type", medal.getType());
            medalInfo.put("required", medal.getRequired());

            result.put("medal", medalInfo);
            result.put("current", currentCount);
            result.put("unlocked", isUnlocked);
            result.put("unlockedAt", um != null ? um.getUnlockedAt() : null);

            return result;
        }).collect(Collectors.toList());
    }

    // 初始化用户勋章（注册时调用）
    @Transactional
    public void initUserMedals(Integer userId) {
        System.out.println("初始化用户勋章: userId=" + userId);

        // 1. 检查用户是否已有勋章记录
        List<UserMedal> existingMedals = medalMapper.findUserMedals(userId);
        if (existingMedals.isEmpty()) {
            // 初始化勋章记录
            List<Integer> medalIds = medalMapper.findAll().stream()
                    .map(Medal::getId)
                    .collect(Collectors.toList());
            if (!medalIds.isEmpty()) {
                medalMapper.initUserMedals(userId, medalIds);
                System.out.println("初始化勋章记录完成，数量: " + medalIds.size());
            }
        } else {
            System.out.println("用户已有勋章记录，跳过初始化");
        }

        // 2. 初始化统计记录
        UserStatistics stats = userStatisticsMapper.findByUserId(userId);
        if (stats == null) {
            userStatisticsMapper.initUserStatistics(userId);
            System.out.println("初始化统计记录完成");
        } else {
            System.out.println("用户已有统计记录，跳过初始化");
        }
    }

    // 更新进度
    @Transactional
    public void updateProgress(Integer userId, String type, int increment) {
        System.out.println("更新进度: userId=" + userId + ", type=" + type + ", increment=" + increment);

        // 1. 确保用户有统计记录
        UserStatistics stats = userStatisticsMapper.findByUserId(userId);
        if (stats == null) {
            userStatisticsMapper.initUserStatistics(userId);
            stats = userStatisticsMapper.findByUserId(userId);
        }

        // 2. 更新统计表
        if ("donation".equals(type)) {
            userStatisticsMapper.updateDonationCount(userId, increment);
        } else if ("borrow".equals(type)) {
            userStatisticsMapper.updateBorrowCount(userId, increment);
        } else if ("note".equals(type)) {
            userStatisticsMapper.updateNoteCount(userId, increment);
        }

        // 3. 获取更新后的统计值
        UserStatistics updatedStats = userStatisticsMapper.findByUserId(userId);
        if (updatedStats == null) {
            System.out.println("统计信息不存在");
            return;
        }

        final UserStatistics finalStats = updatedStats;

        // 4. 获取该类型的所有勋章
        List<Integer> medalIds = medalMapper.findMedalIdsByType(type);
        for (Integer medalId : medalIds) {
            // 获取勋章所需数量
            Medal medal = medalMapper.findById(medalId);
            if (medal != null) {
                // 获取对应类型的当前统计值
                int currentCount = 0;
                if ("donation".equals(type)) {
                    currentCount = finalStats.getDonationCount();
                } else if ("borrow".equals(type)) {
                    currentCount = finalStats.getBorrowCount();
                } else if ("note".equals(type)) {
                    currentCount = finalStats.getNoteCount();
                }

                // 确保用户有勋章记录
                ensureUserMedalExists(userId, medalId);

                // 更新用户勋章记录
                medalMapper.updateProgress(userId, medalId, currentCount);
                System.out.println("更新勋章: " + medal.getName() + ", 当前进度: " + currentCount + "/" + medal.getRequired());
            }
        }
    }

    // 确保用户有勋章记录
    @Transactional
    public void ensureUserMedalExists(Integer userId, Integer medalId) {
        UserMedal um = medalMapper.findUserMedal(userId, medalId);
        if (um == null) {
            // 如果没有记录，创建一条
            medalMapper.initSingleUserMedal(userId, medalId);
        }
    }
}