package com.longjunwang.moni.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    private final String aiBaseUrl = System.getenv("MONI_BASE_URL");

    private final String apiKey = System.getenv("MONI_API_KEY");

    @Value("${moni.ai.claude.model}")
    private String claudeModel;

    @Value("${moni.ai.deepseek.model}")
    private String deepseekModel;

    @Value("${moni.ai.gemini.model}")
    private String geminiModel;

    @Value("${moni.ai.openai.model}")
    private String openaiModel;

    @Value("${moni.ai.intent.model}")
    private String intentModel;

    @Value("${moni.ai.extract.model}")
    private String extractModel;

    @Bean(name = "openAIChatClient")
    public ChatClient openAIChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(openaiModel).build()).build();
        return ChatClient.create(chatModel);
    }

    @Bean(name = "claudeChatClient")
    public ChatClient claudeChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(claudeModel).build()).build();
        return ChatClient.create(chatModel);
    }

    @Bean(name = "deepSeekChatClient")
    public ChatClient deepSeekChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(deepseekModel).build()).build();
        return ChatClient.create(chatModel);
    }

    @Bean(name = "geminiChatClient")
    public ChatClient geminiChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(geminiModel).build()).build();
        return ChatClient.create(chatModel);
    }

    @Bean(name = "intentChatClient")
    public ChatClient intentChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(intentModel).build()).build();
        return ChatClient.create(chatModel);
    }

    @Bean(name = "extractChatClient")
    public ChatClient extractChatClient() {
        var openAiApi = OpenAiApi.builder().baseUrl(aiBaseUrl).apiKey(apiKey).build();
        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(extractModel).build()).build();
        return ChatClient.create(chatModel);
    }
}
