package com.yechan.client;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}
