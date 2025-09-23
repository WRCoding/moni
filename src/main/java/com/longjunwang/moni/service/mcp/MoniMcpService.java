package com.longjunwang.moni.service.mcp;

import com.longjunwang.moni.entity.Expense;
import com.longjunwang.moni.mapper.ExpenseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MoniMcpService {

    @Autowired
    private ExpenseMapper expenseMapper;

    @Tool(description = "获取指定范围的花费记录,如果没有明确的范围,默认为最近一个星期")
    public List<Expense> getExpense(@ToolParam(description = "范围的起始时间,包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-01") String startTime,
                                    @ToolParam(description = "范围的终止时间,不包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-10") String endTime) {
        log.info("MoniMcpService getExpense, startTime={}, endTime={}", startTime, endTime);
        return expenseMapper.selectByRangeDate(startTime, endTime);
    }
}
