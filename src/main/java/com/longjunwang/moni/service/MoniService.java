package com.longjunwang.moni.service;

import com.longjunwang.moni.agent.*;
import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.MoniMsg;
import com.longjunwang.moni.enums.IntentEnum;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MoniService {


    @Autowired
    private ExpenseAgent expenseAgent;

    @Autowired
    private AnalyseAgent analyseAgent;

    @Autowired
    private ClassifyIntentAgent classifyIntentAgent;

    @Autowired
    private HtmlAgent htmlAgent;

    @Autowired
    private ExtractAgent extractAgent;

    @Autowired
    private MailService mailService;

    @Value("${spring.mail.username}")
    private String to;

    private static final String OTHER_MSG = "当前内容不属于消费交易相关,不进行处理";

    public String handleMessage(MoniMsg message) throws MessagingException {
        AgentContext agentContext = classifyIntent(message);
        String intent = agentContext.getIntent();
        log.info("intent {}", agentContext);
        if (IntentEnum.INSERT.name().equals(intent)) {
            return expenseAgent.submitAgent(agentContext);
        }

        if (IntentEnum.ANALYZE.name().equals(intent)) {
            String s = htmlAgent.submitAgent(analyseAgent.submitAgent(agentContext));
            mailService.sendMimeMail(to, "分析", s);
            return s;
        }

        return OTHER_MSG;

    }

    private AgentContext classifyIntent(MoniMsg message) {
        String content = message.content();
        if (message.isFile()){
            content = extractImage(content);
        }
        return classifyIntentAgent.submitAgent(content);
    }

    private String extractImage(String path) {
        return extractAgent.submitAgent(path);
    }



}
