package com.longjunwang.moni.agent;

import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.Expense;
import com.longjunwang.moni.entity.MoniMsg;
import com.longjunwang.moni.mapper.ExpenseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AnalyseAgent implements MoniAgent<AgentContext, String> {

    @Value("classpath:prompts/analyse.st")
    private Resource analysePrompt;

    @Autowired
    private ChatClient openAIChatClient;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Override
    public String submitAgent(AgentContext agentContext) {
        String content = openAIChatClient.prompt(new Prompt(List.of(sysMsg(), new UserMessage(agentContext.getContent()))))
                .tools(this)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
        log.info("content:{}", content);
        return content;
    }

    @Override
    public void executeAgent(AgentContext agentContext) {

    }

    @Tool(description = "获取消费记录")
    public List<Expense> getExpense(@ToolParam(description = "范围的起始时间,包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-01") String startTime,
                                    @ToolParam(description = "范围的终止时间,不包含这天,时间格式转换为YYYY-mm-dd日 例如2025-03-10") String endTime) {
        return expenseMapper.selectByRangeDate(startTime, endTime);
    }

    private SystemMessage sysMsg(){
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(analysePrompt);
        return (SystemMessage) systemPromptTemplate.createMessage(Map.of("day", getCurrentDay()));
    }

    private String getCurrentDay(){
        LocalDate localDate = LocalDate.now();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
    }
}
