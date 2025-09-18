//package org.jeecg.modules.m2.service.impl.websocket;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import dev.ai4j.openai4j.Json;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.ChatMessage;
//import dev.langchain4j.data.message.SystemMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.StreamingResponseHandler;
//import dev.langchain4j.model.chat.StreamingChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
//import dev.langchain4j.model.output.Response;
//import freemarker.template.Configuration;
//import freemarker.template.Template;
//import jakarta.websocket.Session;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.modules.m2.entity.*;
//import org.jeecg.modules.m2.entity.openapi.*;
//import org.jeecg.modules.m2.entity.websocket.WebsocketSessionPack;
//import org.jeecg.modules.m2.entity.websocket.WebsocketSessionPackType;
//import org.jeecg.modules.m2.service.*;
//import org.jeecg.modules.m2.service.websocket.IWebsocketSessionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.util.*;
//
//@Service
//@Slf4j
//@EnableAsync
//public class WebsocketSessionServiceImpl implements IWebsocketSessionService {
//    private String userId;
//    private String sessionId;
//    private Session session;
//    private Map<String, Object> freemarkerData;
//
//    @Autowired
//    private IM2InterviewService im2InterviewService;
//    @Autowired
//    private IM2DocumentExtractorService im2DocumentExtractorService;
//    @Autowired
//    private IM2RecruitmentService im2RecruitmentService;
//    @Autowired
//    private IM2UserApplicationsService im2UserApplicationsService;
//    @Autowired
//    private IM2UserResumeService im2UserResumeService;
//    @Autowired
//    private IM2InterviewerCharactorService im2InterviewerCharactorService;
//    @Autowired
//    private IM2CompanyService im2CompanyService;
//    @Autowired
//    private IM2InterviewScoreService im2InterviewScoreService;
//    @Autowired
//    private IM2InterviewReportService im2InterviewReportService;
//
//    @Autowired
//    private FreeMarkerConfigurer freeMarkerConfigurer;
//
//    @Value("${silos.realtime.llm.api.key}")
//    private String realtimeLlmApiKey;
//    @Value("${silos.realtime.llm.api.url}")
//    private String realtimeLlmApiUrl;
//    @Value("${silos.realtime.llm.api.model}")
//    private String realtimeLlmApiModel;
//
//    @Value("${jeecg.path.upload}")
//    private String uploadPath;
//
//    List<OpenApiVLMMessage> vlmMessageList;
//    List<ChatMessage> chatMessageList;
//
//    StreamingChatLanguageModel realtimeLlm;
//
//
//    @Override
//    public void setupSession(String userId, String sessionId, Session session) {
//        this.userId = userId;
//        this.sessionId = sessionId;
//        this.session = session;
//        vlmMessageList = new ArrayList<>();
//        chatMessageList = new ArrayList<>();
//        sessionStart();
//    }
//
//    @Override
//    public void onMessage(WebsocketSessionPack pack) {
//        if (pack.getType() == WebsocketSessionPackType.REALTIME) {
//            processLLMMessage(pack.getLlmMessage());
//        } else if (pack.getType() == WebsocketSessionPackType.OFFLINE) {
//            processVLMMessage(pack.getVlmMessage());
//        }
//    }
//
//    @Override
//    public void onClose() {
//        sessionStop();
//    }
//
//    @Override
//    public void sessionStart() {
//        String systemPrompt = "";
//        try{
//            M2Interview m2Interview = im2InterviewService.getById(sessionId);
//            if (m2Interview == null) {
//                log.error("面试记录不存在");
//                writeError("面试记录不存在");
//                session.close();
//                return;
//            }
//            M2UserApplications m2UserApplications = im2UserApplicationsService.getById(m2Interview.getApplicationId());
//            if (m2UserApplications == null) {
//                log.error("用户申请记录不存在");
//                writeError("用户申请记录不存在");
//                session.close();
//                return;
//            }
//            QueryWrapper<M2UserResume> wrapper = new QueryWrapper<M2UserResume>();
//            wrapper.eq("application_id", m2UserApplications.getId());
//            M2UserResume m2UserResume = im2UserResumeService.getOne(wrapper);
//            if (m2UserResume == null) {
//                log.error("用户简历不存在");
//                writeError("用户简历不存在");
//                session.close();
//                return;
//            }
//            String resumeContent = im2DocumentExtractorService.extractDocument(uploadPath + File.separator + m2UserResume.getResumeFile());
//
//            M2Recruitment m2Recruitment = im2RecruitmentService.getById(m2UserApplications.getRecruitmentId());
//            if (m2Recruitment == null) {
//                log.error("岗位不存在");
//                writeError("岗位不存在");
//                session.close();
//                return;
//            }
//
//            M2InterviewerCharactor m2InterviewerCharactor = im2InterviewerCharactorService.getById(m2Interview.getInterviewerId());
//            if (m2InterviewerCharactor == null) {
//                log.error("虚拟面试官不存在");
//                writeError("虚拟面试官不存在");
//                session.close();
//                return;
//            }
//            M2Company m2Company = im2CompanyService.getById(m2Recruitment.getCompanyId());
//            if (m2Company == null) {
//                log.error("公司不存在");
//                writeError("公司不存在");
//                session.close();
//                return;
//            }
//            Configuration config = freeMarkerConfigurer.getConfiguration();
//            Template template = config.getTemplate("M2RealtimeSystemPrompt.ftl");
//            Map<String, Object> data = new HashMap<>();
//            data.put("companyName", m2Company.getCompanyName());
//            data.put("companyInfo", m2Company.getCompanyDescription());
//            data.put("jobName", m2Recruitment.getRecruitmentName());
//            data.put("jobInfo", m2Recruitment.getRecruitmentDescription());
//            data.put("resumeContent", resumeContent);
//            data.put("rolePrompt", m2InterviewerCharactor.getInterviewerPrompt());
//            data.put("interviewerName", m2InterviewerCharactor.getInterviewerName());
//            freemarkerData = data;
//            StringWriter stringWriter = new StringWriter();
//            template.process(data, stringWriter);
//            systemPrompt = stringWriter.toString();
//        }catch (Exception e){
//            log.error("Error: ",e);
//            writeError("Error: " + e.getMessage());
//            return;
//        }
//
//        realtimeLlm = OpenAiStreamingChatModel.builder()
//                .apiKey(realtimeLlmApiKey)
//                .baseUrl(realtimeLlmApiUrl)
//                .modelName(realtimeLlmApiModel)
//                .build();
//        chatMessageList.clear();
//        vlmMessageList.clear();
//        chatMessageList.add(new SystemMessage(systemPrompt));
//    }
//
//    @Override
//    public void sessionStop() {
//        if(chatMessageList.isEmpty() || freemarkerData.isEmpty()){
//            return;
//        }
//        M2InterviewReport m2InterviewReport = new  M2InterviewReport();
//        m2InterviewReport.setInterviewId(sessionId);
//        im2InterviewReportService.save(m2InterviewReport);
//        M2Interview interviewData = im2InterviewService.getById(m2InterviewReport.getInterviewId());
//        interviewData.setInterviewState("pending_score");
//        im2InterviewService.updateById(interviewData);
//        im2InterviewScoreService.score(sessionId, chatMessageList, vlmMessageList, freemarkerData);
//        log.info("面试结束，面试记录ID：{}", sessionId);
//    }
//
//    @Override
//    public void processLLMMessage(OpenApiLLMMessage message) {
//        chatMessageList.add(new UserMessage(message.getContent()));
//        realtimeLlm.generate(chatMessageList, new StreamingResponseHandler<AiMessage>() {
//            @Override
//            public void onNext(String s) {
//                try {
//                    OpenApiResponseMessage openApiResponseMessage = new OpenApiResponseMessage();
//                    openApiResponseMessage.setRole("assistant");
//                    openApiResponseMessage.setContent(s);
//                    OpenApiChoice openApiChoice = new OpenApiChoice();
//                    openApiChoice.setDelta(openApiResponseMessage);
//                    OpenApiResponse openApiResponse = new OpenApiResponse();
//                    openApiResponse.setSuccess(true);
//                    openApiResponse.setChoices(List.of(openApiChoice));
//                    session.getBasicRemote().sendText(Json.toJson(openApiResponse));
//                } catch (IOException e) {
//                    log.error("发送错误消息失败", e);
//                }
//            }
//
//            @Override
//            public void onComplete(Response<AiMessage> response) {
//                chatMessageList.add(response.content());
//                StreamingResponseHandler.super.onComplete(response);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                writeError(throwable.getMessage());
//            }
//        });
//    }
//
//    @Override
//    public void processVLMMessage(OpenApiVLMMessage message) {
//        vlmMessageList.add(message);
//    }
//
//    public void writeError(String error) {
//        try {
//            log.error("错误消息：{}", error);
//            OpenApiResponseMessage openApiResponseMessage = new OpenApiResponseMessage();
//            openApiResponseMessage.setRole("assistant");
//            openApiResponseMessage.setContent(error);
//            OpenApiChoice openApiChoice = new OpenApiChoice();
//            openApiChoice.setDelta(openApiResponseMessage);
//            OpenApiResponse openApiResponse = new OpenApiResponse();
//            openApiResponse.setSuccess(false);
//            openApiResponse.setChoices(List.of(openApiChoice));
//            session.getBasicRemote().sendText(Json.toJson(openApiResponse));
//        } catch (IOException e) {
//            log.error("发送错误消息失败", e);
//        }
//    }
//
//
//}
