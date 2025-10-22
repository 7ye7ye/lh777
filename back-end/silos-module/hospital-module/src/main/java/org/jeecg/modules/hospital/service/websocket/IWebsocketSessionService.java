//package org.jeecg.modules.m2.service.websocket;
//
//import jakarta.websocket.Session;
//import org.jeecg.modules.m2.entity.openapi.OpenApiLLMMessage;
//import org.jeecg.modules.m2.entity.openapi.OpenApiVLMMessage;
//import org.jeecg.modules.m2.entity.websocket.WebsocketSessionPack;
//
//public interface IWebsocketSessionService {
//    void setupSession(String userId, String sessionId, Session session);
//    void onMessage(WebsocketSessionPack pack);
//    void onClose();
//    void sessionStart();
//    void sessionStop();
//    void processLLMMessage(OpenApiLLMMessage message);
//    void processVLMMessage(OpenApiVLMMessage message);
//}
