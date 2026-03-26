package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class NoteLike {
    private Integer id;
    private Integer noteId;
    private Integer userId;
    private Date createdAt;
}