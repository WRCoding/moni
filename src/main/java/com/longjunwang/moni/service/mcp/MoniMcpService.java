package com.longjunwang.moni.service.mcp;

import com.alibaba.fastjson2.JSONObject;
import com.longjunwang.moni.entity.Expense;
import com.longjunwang.moni.mapper.ExpenseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MoniMcpService {

    @Autowired
    private ExpenseMapper expenseMapper;

    @Tool(description = "获取当前日期,格式为yyyy-MM-dd,如果没有指定日期,可以通过该方法获取当前日期,并以当前日期为基准")
    public String getCurrentDay(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Tool(description = "获取指定范围的花费记录,如果没有明确的范围,默认为最近一个星期")
    public JSONObject getExpense(@ToolParam(description = "范围的起始时间,包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-01") String startTime,
                                    @ToolParam(description = "范围的终止时间,不包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-10") String endTime) {
        return expenseJson(startTime,endTime);
    }


//    @Tool(description = "获取上个星期的消费记录")
//    public JSONObject getLastWeekExpense() {
//        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
//        LocalDate lastDay = LocalDate.now().minusDays(1);
//        String startTime = lastWeek.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        String endTime = lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        log.info("getLastWeekExpense start: {}, end: {}", startTime, endTime);
//        return expenseJson(startTime, endTime);
//    }

    public JSONObject expenseJson(String startTime, String endTime) {
        log.info("MoniMcpService expenseJson, startTime={}, endTime={}", startTime, endTime);
        List<Expense> expenses = expenseMapper.selectByRangeDate(startTime, endTime);
        Map<String, List<JSONObject>> typeDetailMap = expenses.stream().collect(Collectors.groupingBy(Expense::getType, Collectors.mapping(this::convert,  Collectors.toList())));
        Map<String, Double> typeAmountMap = expenses.stream().collect(Collectors.groupingBy(Expense::getType, Collectors.summingDouble(item -> Double.parseDouble(item.getAmount()))));
        JSONObject result = new JSONObject();
        result.put("每个类型的详细数据", typeDetailMap);
        result.put("每个类型的总金额", typeAmountMap);
        log.info("MoniMcpService expenseJson, result={}", result);
        return result;
    }

    private JSONObject convert(Expense item) {
        JSONObject result = new JSONObject();
        result.put("记录日期", item.getDate());
        result.put("金额", item.getAmount());
        result.put("类型", item.getType());
        result.put("备注", item.getRemark());
        result.put("额外备注", item.getSubRemark());
        return result;
    }
}
