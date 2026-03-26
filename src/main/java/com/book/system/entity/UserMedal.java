package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserMedal {
    private Integer id;
    private Integer userId;
    private Integer medalId;
    private Integer current;  // 当前进度
    private Boolean unlocked;  // 是否已解锁
    private Date unlockedAt;   // 解锁时间
    private Date updatedAt;
}