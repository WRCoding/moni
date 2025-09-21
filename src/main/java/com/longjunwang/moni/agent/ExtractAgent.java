package com.longjunwang.moni.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Service
@Slf4j
public class ExtractAgent implements MoniAgent<String, String>{


    @Autowired
    private ChatClient extractChatClient;

    @Override
    public String submitAgent(String s) {
        FileSystemResource resource = new FileSystemResource(s);
        var mimeType = MimeTypeUtils.IMAGE_JPEG;
        if (s.toLowerCase().endsWith("png")){
            mimeType = MimeTypeUtils.IMAGE_PNG;
        }
        Media media = new Media(mimeType, resource);
        UserMessage userMessage = UserMessage.builder().text("请你提取图片主要区域里的金额,时间,商家等支付相关的信息")
                .media(media)
                .build();
        String content = extractChatClient.prompt(new Prompt(List.of(userMessage)))
                .call().content();
        log.info("extractImage : {}", content);
        return content;
    }

    @Override
    public void executeAgent(String s) {

    }
}
