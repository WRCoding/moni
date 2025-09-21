package com.longjunwang.moni.agent;

import com.alibaba.fastjson2.JSON;
import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.Expense;
import com.longjunwang.moni.entity.MoniMsg;
import com.longjunwang.moni.mapper.ExpenseMapper;
import com.longjunwang.moni.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ExpenseAgent implements MoniAgent<AgentContext, String> {

    @Value("classpath:prompts/expense.st")
    private Resource expensePrompt;

    @Autowired
    private ChatClient openAIChatClient;

    @Autowired
    private ExpenseMapper expenseMapper;


    @Override
    public String submitAgent(AgentContext agentContext) {
        SystemPromptTemplate spt = new SystemPromptTemplate(expensePrompt);
        Message sysMsg = spt.createMessage(Map.of("id", idGenerate()));
        UserMessage userMsg = new UserMessage(agentContext.getContent());
        String result = openAIChatClient.prompt(new Prompt(List.of(sysMsg, userMsg)))
                .tools(this)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
        log.info("submitAgent {}", result);
        return result;
    }

    @Override
    public void executeAgent(AgentContext agentContext) {

    }

    @Tool(description = "新增消费记录")
    private Expense insertExpense(@ToolParam(description = "消费记录") Expense expense) {
        log.info("insertExpense {}", expense);
        expenseMapper.insertSelective(expense);
        return expense;
    }

    @Tool(description = "Expense的Id生成器,当需要新增消费记录的时候,通过该方法获取Id")
    private String idGenerate(){
        try {
            String id = IdGenerator.generateId();
            log.info("idGenerate {}", id);
            return id;
        } catch (IOException e) {
            log.error("idGenerate error",e);
            return UUID.randomUUID().toString().replace("-","");
        }
    }
}
