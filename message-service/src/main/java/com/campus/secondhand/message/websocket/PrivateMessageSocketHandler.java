package com.campus.secondhand.message.websocket;

import com.campus.secondhand.common.security.jwt.JwtTokenService;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class PrivateMessageSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_KEY = "userId";
    private final JwtTokenService jwtTokenService;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public PrivateMessageSocketHandler(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = queryParam(session.getUri(), "token");
        if (token == null || token.trim().isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        try {
            Claims claims = jwtTokenService.parseToken(token);
            Object userIdValue = claims.get(USER_ID_KEY);
            if (userIdValue == null) {
                session.close(CloseStatus.BAD_DATA);
                return;
            }
            Long userId = Long.valueOf(String.valueOf(userIdValue));
            session.getAttributes().put(USER_ID_KEY, userId);
            sessions.put(userId, session);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object userIdValue = session.getAttributes().get(USER_ID_KEY);
        if (userIdValue != null) {
            sessions.remove(Long.valueOf(String.valueOf(userIdValue)));
        }
    }

    public void pushPrivateMessage(Long userId, String payload) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException ignored) {
        }
    }

    private String queryParam(URI uri, String key) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
