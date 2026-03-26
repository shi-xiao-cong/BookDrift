package com.book.system.controller;

import com.book.system.entity.Note;
import com.book.system.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteService noteService;

    // 获取书籍笔记
    @GetMapping("/book/{bookId}")
    public Map<String, Object> getBookNotes(@PathVariable Integer bookId,
                                            @RequestParam(required = false) Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Note> notes = noteService.getBookNotes(bookId, userId);
            result.put("success", true);
            result.put("data", notes);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 添加笔记
    @PostMapping
    public Map<String, Object> addNote(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer bookId = Integer.parseInt(params.get("bookId").toString());
            Integer userId = Integer.parseInt(params.get("userId").toString());
            String userName = (String) params.get("userName");
            String content = (String) params.get("content");

            Note note = noteService.addNote(bookId, userId, userName, content);

            result.put("success", true);
            result.put("data", note);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateNote(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer userId = Integer.parseInt(params.get("userId").toString());
            String content = (String) params.get("content");

            Note note = noteService.updateNote(id, userId, content);

            result.put("success", true);
            result.put("data", note);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 【新增】删除笔记
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNote(@PathVariable Integer id,
                                          @RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            noteService.deleteNote(id, userId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 点赞/取消点赞
    @PostMapping("/like")
    public Map<String, Object> toggleLike(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer noteId = Integer.parseInt(params.get("noteId").toString());
            Integer userId = Integer.parseInt(params.get("userId").toString());

            boolean liked = noteService.toggleLike(noteId, userId);

            result.put("success", true);
            result.put("liked", liked);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}