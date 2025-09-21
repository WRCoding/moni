package com.longjunwang.moni.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HtmlAgent implements MoniAgent<String, String> {

    @Value("classpath:prompts/html.st")
    private Resource htmlPrompt;

    @Autowired
    private ChatClient openAIChatClient;

    @Override
    public String submitAgent(String s) {
        SystemMessage sysMsg = new SystemMessage("你是一名MarkDown转换HTML的智能助手");
        return openAIChatClient.prompt(new Prompt(List.of(sysMsg, userMessage(s))))
                .call()
                .content();
    }

    @Override
    public void executeAgent(String s) {

    }

    private UserMessage userMessage(String content){
        PromptTemplate promptTemplate = PromptTemplate.builder().resource(htmlPrompt).build();
        Message message = promptTemplate.createMessage(Map.of("markdown_input", content));
        return (UserMessage) message;
    }
}
