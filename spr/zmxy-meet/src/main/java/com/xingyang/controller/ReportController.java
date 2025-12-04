package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.dto.ReportRequest;
import com.xingyang.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private final ReportService reportService;
    
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * 提交举报
     */
    @PostMapping
    public Result<Void> createReport(@RequestBody ReportRequest request, 
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        // 验证参数
        if (request.getReportedType() == null || request.getReportedId() == null || 
            request.getReason() == null || request.getReason().trim().isEmpty()) {
            return Result.error("请填写完整的举报信息");
        }
        
        // 验证举报类型
        if (!request.getReportedType().matches("post|comment|user")) {
            return Result.error("举报类型不正确");
        }
        
        try {
            reportService.createReport(
                userId,
                request.getReportedType(),
                request.getReportedId(),
                request.getReason(),
                request.getDescription()
            );
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("举报失败: " + e.getMessage());
        }
    }
}
