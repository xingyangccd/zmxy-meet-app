package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.entity.Circle;
import com.xingyang.service.CircleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/circles")
public class CircleController {
    
    private final CircleService circleService;
    
    public CircleController(CircleService circleService) {
        this.circleService = circleService;
    }
    
    /**
     * 获取圈子列表
     */
    @GetMapping
    public Result<List<Circle>> getCircles() {
        List<Circle> circles = circleService.list();
        return Result.success(circles);
    }
    
    /**
     * 获取圈子详情
     */
    @GetMapping("/{id}")
    public Result<Circle> getCircle(@PathVariable Long id) {
        Circle circle = circleService.getById(id);
        if (circle == null) {
            return Result.error("圈子不存在");
        }
        return Result.success(circle);
    }
}
