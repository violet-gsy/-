package com.jc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.vo.WebSocketMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/ws")
public class WebSocketServer {

    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
    }

    // 通用推送方法：指定类型 + 内容
    public static void sendMsg(MessageTypeEnum type, String data,Integer status) {
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
