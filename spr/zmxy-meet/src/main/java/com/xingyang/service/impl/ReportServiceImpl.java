package com.xingyang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Report;
import com.xingyang.mapper.ReportMapper;
import com.xingyang.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    
    @Override
    public void createReport(Long reporterId, String reportedType, Long reportedId, 
                           String reason, String description) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReportedType(reportedType);
        report.setReportedId(reportedId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        report.setDeleted(0);
        
        save(report);
    }
}
