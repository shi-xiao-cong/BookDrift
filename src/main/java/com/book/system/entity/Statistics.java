package com.book.system.entity;

import lombok.Data;

@Data
public class Statistics {
    private Integer totalBooks;
    private Integer availableBooks;
    private Integer borrowedBooks;
    private Integer totalUsers;
    private Integer totalDonations;
    private Integer totalBorrows;
}