package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Note {
    private Integer id;
    private Integer bookId;
    private Integer userId;
    private String userName;
    private String content;
    private Integer likes;
    private Date createdAt;
    private Date updatedAt;

    private Boolean liked;
    private String userAvatar;
}