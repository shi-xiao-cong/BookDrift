package com.book.system.controller;

import com.book.system.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medals")
@CrossOrigin(origins = "*")
public class MedalController {

    @Autowired
    private MedalService medalService;

    // 获取用户勋章
    @GetMapping("/{userId}")
    public Map<String, Object> getUserMedals(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> medals = medalService.getUserMedals(userId);
            result.put("success", true);
            result.put("data", medals);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}