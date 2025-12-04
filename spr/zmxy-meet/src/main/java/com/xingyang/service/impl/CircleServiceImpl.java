package com.xingyang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Circle;
import com.xingyang.mapper.CircleMapper;
import com.xingyang.service.CircleService;
import org.springframework.stereotype.Service;

@Service
public class CircleServiceImpl extends ServiceImpl<CircleMapper, Circle> implements CircleService {
}
