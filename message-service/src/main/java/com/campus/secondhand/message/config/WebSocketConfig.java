package com.campus.secondhand.message.config;

import com.campus.secondhand.message.websocket.PrivateMessageSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PrivateMessageSocketHandler privateMessageSocketHandler;

    public WebSocketConfig(PrivateMessageSocketHandler privateMessageSocketHandler) {
        this.privateMessageSocketHandler = privateMessageSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(privateMessageSocketHandler, "/ws/private")
            .setAllowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://192.168.209.128", "http://192.168.209.1");
    }
}
