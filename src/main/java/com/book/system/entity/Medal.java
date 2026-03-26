package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Medal {
    private Integer id;
    private String name;
    private String description;
    private String icon;
    private String type;  // donation/borrow/note
    private Integer required;  // 所需数量
    private Integer sortOrder;
    private Date createdAt;
}