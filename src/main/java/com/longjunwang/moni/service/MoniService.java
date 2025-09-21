package com.longjunwang.moni.service;

import com.alibaba.fastjson2.JSONObject;
import com.longjunwang.moni.agent.*;
import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.MoniMsg;
import com.longjunwang.moni.entity.Recon;
import com.longjunwang.moni.enums.IntentEnum;
import com.longjunwang.moni.mapper.ReconMapper;
import com.longjunwang.moni.util.IdGenerator;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    @Autowired
    private ReconMapper reconMapper;

    @Value("${spring.mail.username}")
    private String to;

    private static final String OTHER_MSG = "当前内容不属于消费交易相关,不进行处理";

    public String handleMessage(MoniMsg message) throws MessagingException {
        AgentContext agentContext = classifyIntent(message);
        String intent = agentContext.getIntent();
        log.info("intent {}", agentContext);
        if (IntentEnum.INSERT.name().equals(intent)) {
            return handleInsert(agentContext);
        }

        if (IntentEnum.ANALYZE.name().equals(intent)) {
            String s = htmlAgent.submitAgent(analyseAgent.submitAgent(agentContext));
            mailService.sendMimeMail(to, "分析", s);
            return s;
        }

        return OTHER_MSG;

    }

    private String handleInsert(AgentContext agentContext) {
        try {
            String id = IdGenerator.generateId();
            agentContext.setInsertId(id);
            Recon recon = new Recon();
            recon.setInsertId(id);
            recon.setRaw(JSONObject.toJSONString(agentContext));
            reconMapper.insertSelective(recon);
            return expenseAgent.submitAgent(agentContext);
        } catch (Exception e) {
            log.error("handleInsert", e);
            return "handleInsert" + e.getMessage();
        }
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
