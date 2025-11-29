package com.longjunwang.moni.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ImageService {
    public static void main(String[] args) throws IOException {
        Client client = Client.builder().apiKey("sk-STCDHSGMXvzvdOxc3e551652902644618b6e961599CfEf7e").build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(Arrays.asList("TEXT", "IMAGE"))
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-pro-image-preview",
                "图片要求:\n" +
                        "创作一张手绘风格的信息图卡片，比例为9:16竖版。卡片主题鲜明，背景为带有纸质肌理的米色或米白色，整体设计体现质朴、亲切的手绘美感。\n" +
                        "卡片上方以红黑相间、对比鲜明的大号毛笔草书字体突出标题，吸引视觉焦点。文字内容均采用中文草书，整体布局分为2至4个清晰的小节，每节以简短、精炼的中文短语表达核心要点。字体保持草书流畅的韵律感，既清晰可读又富有艺术气息。\n" +
                        "卡片中点缀简单、有趣的手绘插画或图标，例如人物或象征符号，以增强视觉吸引力，引发读者思考与共鸣。整体布局注意视觉平衡，预留足够的空白空间，确保画面简洁明了，易于阅读和理解。\n" +
                        "图片内容：以英语新概念一的内容为范围，出五个单词，包含英标，以及英文例句和对应的翻译",
                config);

        for (Part part : response.parts()) {
            if (part.text().isPresent()) {
                System.out.println(part.text().get());
            } else if (part.inlineData().isPresent()) {
                var blob = part.inlineData().get();
                if (blob.data().isPresent()) {
                    Files.write(Paths.get("_01_generated_image.png"), blob.data().get());
                }
            }
        }
        client.close();
    }
}
