package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Integer id;
    private String studentId;
    private String name;
    private String password;
    private String phone;
    private String email;
    private String role;
    private Integer borrowedCount;
    private String avatar;
    private Date createdAt;
}