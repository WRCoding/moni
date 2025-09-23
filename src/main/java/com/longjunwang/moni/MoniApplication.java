package com.longjunwang.moni;

import com.longjunwang.moni.service.mcp.MoniMcpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoniApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoniApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider moniMcpTools(MoniMcpService moniMcpService) {
        return MethodToolCallbackProvider.builder().toolObjects(moniMcpService).build();
    }
}
