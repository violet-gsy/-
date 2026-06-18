package com.jc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.allenum.MessageTypeEnum;
import com.jc.vo.WebSocketMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/ws")
public class WebSocketServer {

    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 超过30秒没收到心跳，判定离线
    private static final long HEART_TIMEOUT = 30 * 1000;
    // 记录每个会话最后心跳时间
    private static final Map<Session, Long> LAST_HEART_TIME = new ConcurrentHashMap<>();

    // 连接建立
    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        LAST_HEART_TIME.put(session, System.currentTimeMillis());
        log.info("新连接：{}", session.getId());
    }

    // 连接关闭
    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        LAST_HEART_TIME.remove(session);
        log.info("关闭连接：{}", session.getId());
    }

    // 异常
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("连接异常：{}", session.getId(), error);
    }

    // 接收消息 + 心跳处理
    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳：前端发 ping → 后端回 pong
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
                // 刷新心跳时间
                LAST_HEART_TIME.put(session, System.currentTimeMillis());
                return;
            } catch (IOException e) {
                log.error("心跳回复失败", e);
            }
        }
    }

    // ===================== 核心：定时检查超时会话 =====================
    @Scheduled(fixedRate = 5000) // 每5秒检查一次
    public void checkTimeout() {
        long now = System.currentTimeMillis();
        LAST_HEART_TIME.forEach((session, lastTime) -> {
            // 超过30秒没心跳
            if (now - lastTime > HEART_TIMEOUT) {
                try {
                    log.warn("心跳超时，关闭连接：{}", session.getId());
                    session.close(); // 主动关闭
                } catch (IOException e) {
                    log.error("关闭超时连接失败", e);
                }
            }
        });
    }

    // 通用推送方法：指定类型 + 内容
    public static void sendMsg(MessageTypeEnum type, String data, Integer status) {
        try {
            WebSocketMsg<Object> msg = new WebSocketMsg<>();
            msg.setType(type.getCode());
            msg.setTitle(type.getDesc());
            msg.setData(data);
            msg.setStatus(status);
            String json = OBJECT_MAPPER.writeValueAsString(msg);
            for (Session session : SESSIONS) {
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            log.error("WebSocket推送消息失败", e);
        }
    }
}
