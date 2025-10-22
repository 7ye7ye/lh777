//package org.jeecg.modules.m2.controller.websocket;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.ai4j.openai4j.Json;
//import jakarta.websocket.*;
//import jakarta.websocket.server.PathParam;
//import jakarta.websocket.server.ServerEndpoint;
//import lombok.Getter;
//import lombok.Setter;
//import org.jeecg.modules.m2.entity.websocket.WebsocketSessionPack;
////import org.jeecg.modules.hospital.service.impl.websocket.WebsocketSessionServiceImpl;
//import org.jeecg.modules.m2.service.websocket.IWebsocketSessionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//@ServerEndpoint("/interview-session/{userId}/{sessionId}")
//public class WebsocketSessionController {
//
//    @Getter
//    @Setter
//    private static ApplicationContext applicationContext;
//
//
//    private Session session;
//    private String sessionId;
//    private String userId;
//
//    private static CopyOnWriteArraySet<WebsocketSessionController> webSockets =new CopyOnWriteArraySet<>();
//    private static Map<String,Session> sessionPool = new HashMap<String,Session>();
//
//    private IWebsocketSessionService websocketSessionService;
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam(value="userId")String userId, @PathParam(value="sessionId")String sessionId) {
//        try {
//            this.sessionId = sessionId;
//            this.session = session;
//            this.userId = userId;
//            websocketSessionService = applicationContext.getBean(IWebsocketSessionService.class);
//            webSockets.add(this);
//            sessionPool.put(userId, session);
//            log.info("【websocket消息】有新的连接，总数为:"+webSockets.size());
//            websocketSessionService.setupSession(userId, sessionId, session);
//        } catch (Exception e) {
//            log.error("websocket连接异常", e);
//        }
//    }
//
//    @OnClose
//    public void onClose() {
//        try {
//            webSockets.remove(this);
//            sessionPool.remove(userId);
//            websocketSessionService.onClose();
//            log.info("【websocket消息】连接断开，总数为:"+webSockets.size());
//        } catch (Exception e) {
//            log.error("websocket连接断开异常", e);
//        }
//    }
//
//    @OnMessage
//    public void onMessage(String message) {
//        log.info("【websocket消息】收到客户端消息:"+message);
//        try {
//            websocketSessionService.onMessage(new ObjectMapper().readValue(message, WebsocketSessionPack.class));
//        } catch (JsonProcessingException e) {
//            log.error("websocket消息异常", e);
//        }
//    }
//
//    @OnMessage
//    public void onMessage(byte[] message) {
//        log.info("【websocket消息】收到客户端消息:"+message);
//    }
//
//    // 此为广播消息
//    public void sendAllMessage(String message) {
//        log.info("【websocket消息】广播消息:"+message);
//        for(WebsocketSessionController webSocket : webSockets) {
//            try {
//                if(webSocket.session.isOpen()) {
//                    webSocket.session.getAsyncRemote().sendText(message);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // 此为单点消息
//    public void sendOneMessage(String userId, String message) {
//        Session session = sessionPool.get(userId);
//        if (session != null&&session.isOpen()) {
//            try {
//                log.info("【websocket消息】 单点消息:"+message);
//                session.getAsyncRemote().sendText(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // 此为单点消息(多人)
//    public void sendMoreMessage(String[] userIds, String message) {
//        for(String userId:userIds) {
//            Session session = sessionPool.get(userId);
//            if (session != null&&session.isOpen()) {
//                try {
//                    log.info("【websocket消息】 单点消息:"+message);
//                    session.getAsyncRemote().sendText(message);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }
//
//}
