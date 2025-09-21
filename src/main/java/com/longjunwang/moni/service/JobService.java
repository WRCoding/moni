package com.longjunwang.moni.service;

import com.longjunwang.moni.agent.AnalyseAgent;
import com.longjunwang.moni.agent.HtmlAgent;
import com.longjunwang.moni.entity.AgentContext;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobService {
    @Autowired
    private AnalyseAgent analyseAgent;

    @Autowired
    private HtmlAgent htmlAgent;

    @Autowired
    private MailService mailService;

    @Value("${spring.mail.username}")
    private String to;

    private static final String QUERY = "帮我分析一下昨天和前天的花销";
    private static final String ANALYSE_OBJECT = "总结";

    // CRON 表达式（Spring 风格，6 位：秒 分 时 日 月 周）
    // 每天 02:00 触发
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Shanghai")
    public void analyseTask() {
        try {
            log.info("analyseTask start");
            String answer = analyseAgent.submitAgent(AgentContext.builder().content(QUERY).build());
            String result = htmlAgent.submitAgent(answer);
            mailService.sendMimeMail(to, ANALYSE_OBJECT, result);
        } catch (MessagingException e) {
            mailService.sendSimpleMail(to, ANALYSE_OBJECT, e.getMessage());
        }finally {
            log.info("analyseTask end");
        }
    }
}
