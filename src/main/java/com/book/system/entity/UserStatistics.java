package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserStatistics {
    private Integer userId;
    private Integer donationCount;
    private Integer borrowCount;
    private Integer noteCount;
    private Date updatedAt;
}