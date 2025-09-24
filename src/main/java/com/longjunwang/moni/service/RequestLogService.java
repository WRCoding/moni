package com.longjunwang.moni.service;

import com.longjunwang.moni.entity.RequestLog;
import com.longjunwang.moni.mapper.RequestLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLogService {

    private final RequestLogMapper requestLogMapper;

    public void record(RequestLog requestLog) {
        if (requestLog == null) {
            return;
        }
        try {
            requestLogMapper.insertSelective(requestLog);
        } catch (Exception ex) {
            log.warn("Failed to persist request log: {}", ex.getMessage());
        }
    }
}
