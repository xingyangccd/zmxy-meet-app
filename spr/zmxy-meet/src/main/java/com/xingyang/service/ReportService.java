package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.Report;

public interface ReportService extends IService<Report> {
    /**
     * 创建举报
     */
    void createReport(Long reporterId, String reportedType, Long reportedId, 
                     String reason, String description);
}
