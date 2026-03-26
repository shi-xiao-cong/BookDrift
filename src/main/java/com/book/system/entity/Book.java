package com.book.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Book {
    private Integer id;
    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String coverUrl;
    private String condition;
    private String status;  // available/borrowed
    private String donorName;
    private Date donateTime;
    private String description;
    private String qrCodeData;
}