package com.book.system.service;

import com.book.system.entity.Note;
import com.book.system.mapper.NoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private MedalService medalService;

    // 获取书籍笔记
    public List<Note> getBookNotes(Integer bookId, Integer currentUserId) {
        List<Note> notes = noteMapper.findByBookId(bookId);

        // 检查当前用户是否点赞
        if (currentUserId != null) {
            for (Note note : notes) {
                boolean liked = noteMapper.checkLike(note.getId(), currentUserId) > 0;
                // 可以在返回时添加 liked 字段，但Note实体没有这个字段
                // 这里可以在返回时转换为Map，或者修改Note实体
                note.setLiked(liked);
            }
        }

        return notes;
    }

    // 添加笔记
    @Transactional
    public Note addNote(Integer bookId, Integer userId, String userName, String content) {
        Note note = new Note();
        note.setBookId(bookId);
        note.setUserId(userId);
        note.setUserName(userName);
        note.setContent(content);

        noteMapper.insert(note);

        // 更新勋章进度（笔记相关）
        medalService.updateProgress(userId, "note", 1);
        // 返回时获取完整笔记信息（包含头像）
        note = noteMapper.findById(note.getId());

        // 设置当前用户点赞状态（自己的笔记，未点赞）
        if (note != null && userId != null) {
            note.setLiked(false);
        }
        return note;
    }

    @Transactional
    public Note updateNote(Integer id, Integer userId, String content) {
        // 检查笔记是否存在
        Note existingNote = noteMapper.findById(id);
        if (existingNote == null) {
            throw new RuntimeException("笔记不存在");
        }

        // 检查权限（只有笔记作者才能修改）
        if (!existingNote.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改他人的笔记");
        }

        // 更新内容
        existingNote.setContent(content);
        noteMapper.update(existingNote);

        return existingNote;
    }
    @Transactional
    public void deleteNote(Integer id, Integer userId) {
        // 检查笔记是否存在
        Note existingNote = noteMapper.findById(id);
        if (existingNote == null) {
            throw new RuntimeException("笔记不存在");
        }

        // 检查权限（只有笔记作者才能删除）
        if (!existingNote.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的笔记");
        }

        // 删除笔记
        noteMapper.delete(id, userId);
    }

    // 点赞/取消点赞
    @Transactional
    public boolean toggleLike(Integer noteId, Integer userId) {
        boolean liked = noteMapper.checkLike(noteId, userId) > 0;

        if (liked) {
            noteMapper.removeLike(noteId, userId);
            noteMapper.updateLikes(noteId, -1);
            return false;
        } else {
            noteMapper.addLike(noteId, userId);
            noteMapper.updateLikes(noteId, 1);
            return true;
        }
    }
}