package com.longjunwang.moni.agent;

import com.longjunwang.moni.entity.AgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClassifyIntentAgent implements MoniAgent<String, AgentContext>{

    @Value("classpath:prompts/intent.st")
    private Resource intentPrompt;

    @Autowired
    private ChatClient intentChatClient;


    @Override
    public AgentContext submitAgent(String content) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(intentPrompt);
        Message sysMsg = systemPromptTemplate.createMessage();
        UserMessage userMessage = UserMessage.builder().text(content).build();
        String intent = intentChatClient.prompt(new Prompt(List.of(sysMsg, userMessage)))
                .call().content();
        return AgentContext.builder().intent(intent).content(content).build();
    }

    @Override
    public void executeAgent(String s) {

    }
}
