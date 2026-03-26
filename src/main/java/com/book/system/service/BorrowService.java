package com.book.system.service;

import com.book.system.entity.Book;
import com.book.system.entity.BorrowRecord;
import com.book.system.entity.User;
import com.book.system.mapper.BookMapper;
import com.book.system.mapper.BorrowRecordMapper;
import com.book.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MedalService medalService;

    /**
     * 获取书籍漂流轨迹
     */
    public List<Map<String, Object>> getBookTrajectory(Integer bookId) {
        List<Map<String, Object>> trajectory = new ArrayList<>();

        try {
            // 1. 查询书籍的捐赠信息
            Book book = bookMapper.findById(bookId);
            if (book != null && book.getDonateTime() != null) {
                Map<String, Object> donateRecord = new HashMap<>();
                donateRecord.put("time", new SimpleDateFormat("yyyy-MM-dd").format(book.getDonateTime()));
                donateRecord.put("action", "捐赠");
                donateRecord.put("user", book.getDonorName());
                trajectory.add(donateRecord);
            }

            // 2. 查询书籍的所有借阅记录
            List<BorrowRecord> borrowRecords = borrowRecordMapper.findByBookId(bookId);
            for (BorrowRecord record : borrowRecords) {
                // 借出记录
                if (record.getBorrowDate() != null) {
                    Map<String, Object> borrowRecord = new HashMap<>();
                    borrowRecord.put("time", new SimpleDateFormat("yyyy-MM-dd").format(record.getBorrowDate()));
                    borrowRecord.put("action", "认领");
                    borrowRecord.put("user", record.getUserName());
                    trajectory.add(borrowRecord);
                }

                // 归还记录
                if (record.getReturnDate() != null) {
                    Map<String, Object> returnRecord = new HashMap<>();
                    returnRecord.put("time", new SimpleDateFormat("yyyy-MM-dd").format(record.getReturnDate()));
                    returnRecord.put("action", "归还");
                    returnRecord.put("user", record.getUserName());
                    trajectory.add(returnRecord);
                }
            }

            // 3. 按时间排序
            trajectory.sort((a, b) -> {
                String timeA = (String) a.get("time");
                String timeB = (String) b.get("time");
                return timeA.compareTo(timeB);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return trajectory;
    }

    /**
     * 根据书籍ID查询当前借阅记录
     */
    public BorrowRecord findCurrentByBookId(Integer bookId) {
        return borrowRecordMapper.findCurrentByBookId(bookId);
    }

    /**
     * 借书
     */
    @Transactional
    public BorrowRecord borrowBook(Integer userId, Integer bookId, Integer period,
                                   String pickupMethod, String contact, String remarks) {
        // 1. 查询书籍信息
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            throw new RuntimeException("书籍不存在");
        }
        if (!"available".equals(book.getStatus())) {
            throw new RuntimeException("书籍已借出");
        }

        // 2. 查询用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getBorrowedCount() >= 3) {
            throw new RuntimeException("已达到最大借阅数量");
        }

        // 3. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setBorrowNo("BR" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        record.setBookId(bookId);
        record.setBookTitle(book.getTitle());
        record.setUserId(userId);
        record.setUserName(user.getName());
        record.setStudentId(user.getStudentId());
        record.setBorrowDate(new Date());

        // 计算应还日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, period);
        record.setDueDate(cal.getTime());

        record.setPeriod(period);
        record.setPickupMethod(pickupMethod);
        record.setContact(contact);
        record.setRemarks(remarks);
        record.setStatus("borrowing");
        record.setIsOverdue(false);
        record.setOverdueDays(0);

        // 4. 插入借阅记录
        borrowRecordMapper.insert(record);

        // 5. 更新书籍状态为已借出
        bookMapper.updateStatus(bookId, "borrowed");

        // 6. 更新用户借阅数量
        userMapper.updateBorrowedCount(userId, 1);

        // 7. 更新借阅勋章进度
        medalService.updateProgress(userId, "borrow", 1);

        // 8. 查询完整的书籍信息并设置到记录中
        Book fullBook = bookMapper.findById(bookId);
        if (fullBook != null) {
            record.setBookTitle(fullBook.getTitle());
            // 可以设置更多书籍信息
        }

        return record;
    }

    /**
     * 还书
     */
    @Transactional
    public BorrowRecord returnBook(Integer bookId) {
        // 1. 查询当前借阅记录
        BorrowRecord record = borrowRecordMapper.findCurrentByBookId(bookId);
        if (record == null) {
            throw new RuntimeException("没有找到借阅记录");
        }

        // 2. 设置归还日期
        Date now = new Date();
        record.setReturnDate(now);

        // 3. 计算是否逾期
        if (now.after(record.getDueDate())) {
            record.setIsOverdue(true);
            long diff = now.getTime() - record.getDueDate().getTime();
            long days = diff / (24 * 60 * 60 * 1000);
            record.setOverdueDays((int) days);
            record.setStatus("overdue");
        } else {
            record.setIsOverdue(false);
            record.setOverdueDays(0);
            record.setStatus("returned");
        }

        // 4. 更新借阅记录
        borrowRecordMapper.updateReturn(record);

        // 5. 更新书籍状态为可借阅
        bookMapper.updateStatus(bookId, "available");

        // 6. 更新用户借阅数量（减1）
        userMapper.updateBorrowedCount(record.getUserId(), -1);

        return record;
    }

    /**
     * 查询用户的当前借阅
     * 修改：补充书籍详细信息（作者、封面、ISBN、出版社）
     */
    public List<BorrowRecord> findCurrentByUserId(Integer userId) {
        System.out.println("查询用户当前借阅, userId: " + userId);

        // 1. 先获取借阅记录
        List<BorrowRecord> records = borrowRecordMapper.findCurrentBorrowByUserId(userId);
        System.out.println("找到借阅记录数量: " + records.size());

        // 2. 为每条记录补充书籍信息
        for (BorrowRecord record : records) {
            Book book = bookMapper.findById(record.getBookId());
            if (book != null) {
                System.out.println("为借阅记录补充书籍信息: " + book.getTitle());

                // 设置书籍详细信息
                record.setBookAuthor(book.getAuthor());
                record.setBookCover(book.getCoverUrl());
                record.setBookIsbn(book.getIsbn());
                record.setBookPublisher(book.getPublisher());

                // 如果bookTitle为空，设置一下
                if (record.getBookTitle() == null || record.getBookTitle().isEmpty()) {
                    record.setBookTitle(book.getTitle());
                }
            }
        }

        return records;
    }
}