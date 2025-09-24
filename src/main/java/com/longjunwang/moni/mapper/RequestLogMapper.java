package com.longjunwang.moni.mapper;

import com.longjunwang.moni.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper {
    int insertSelective(RequestLog requestLog);
}
