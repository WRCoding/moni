package com.longjunwang.moni.service;

import com.alibaba.fastjson2.JSONObject;
import com.longjunwang.moni.agent.AnalyseAgent;
import com.longjunwang.moni.agent.ExpenseAgent;
import com.longjunwang.moni.agent.HtmlAgent;
import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.Expense;
import com.longjunwang.moni.entity.Recon;
import com.longjunwang.moni.enums.ReconStatus;
import com.longjunwang.moni.mapper.ExpenseMapper;
import com.longjunwang.moni.mapper.ReconMapper;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class JobService {
    @Autowired
    private AnalyseAgent analyseAgent;

    @Autowired
    private HtmlAgent htmlAgent;

    @Autowired
    private MailService mailService;

    @Autowired
    private ReconMapper reconMapper;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private ExpenseAgent expenseAgent;

    @Value("${spring.mail.username}")
    private String to;

    private static final String QUERY = "帮我分析一下昨天和前天的花销";
    private static final String ANALYSE_OBJECT = "总结";
    private static final String RECON_OBJECT = "对账总结";

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
        } finally {
            log.info("analyseTask end");
        }
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Shanghai")
    public void cronTask() {
        log.info("cronTask start");
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(formatter);
        List<Recon> recons = reconMapper.selectByStatus(ReconStatus.INIT.name());
        List<String> ids = recons.stream().map(Recon::getInsertId).toList();
        List<String> expenseId = expenseMapper.selectByIds(ids).stream().map(Expense::getId).toList();
        recons.stream().filter(item -> expenseId.contains(item.getInsertId())).forEach(item -> {
            Recon update = new Recon();
            update.setId(item.getId());
            update.setStatus(ReconStatus.SUCCESS.name());
            reconMapper.updateByPrimaryKeySelective(update);
        });
        List<Recon> needRecons = recons.stream().filter(item -> !expenseId.contains(item.getInsertId())).toList();
        int count = 0;
        int failedCount = 0;
        int successCount = 0;
        for (Recon recon : needRecons) {
            count++;
            AgentContext agentContext = JSONObject.parseObject(recon.getRaw(), AgentContext.class);
            expenseAgent.submitAgent(agentContext);
            Expense expense = expenseMapper.selectById(recon.getInsertId());
            if (Objects.isNull(expense)) {
                failedCount++;
                Recon update = new Recon();
                update.setId(recon.getId());
                update.setStatus(ReconStatus.FAILED.name());
                reconMapper.updateByPrimaryKeySelective(update);
            } else {
                successCount++;
                Recon update = new Recon();
                update.setId(recon.getId());
                update.setStatus(ReconStatus.SUCCESS.name());
                reconMapper.updateByPrimaryKeySelective(update);
            }
        }
        String sb = "日期: " +
                todayStr +
                ", 需要对账的数量： " +
                count +
                ", 成功: " + successCount + ", 失败: " + failedCount;
        mailService.sendSimpleMail(to, RECON_OBJECT, sb);
        log.info("cronTask end");
    }
}
