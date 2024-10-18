package com.boyug.common;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.MessageListRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MessageListResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
public class SmsApi {

    @Value("${coolsms.api-key}")
    private String smsApiKey;

    @Value("${coolsms.api-secret}")
    private String smsApiSecret;

    @Value("${coolsms.api-sendernumber}")
    private String senderNumber;

    DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(smsApiKey, smsApiSecret, "https://api.coolsms.co.kr");
    }

    // 인증 문자 발송
    public String sendMessage(String to) {

        String code = String.valueOf((int)(Math.random() * 8999) + 1000);

        SingleMessageSentResponse response = null;
        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(to);
        message.setText("[Together] 인증번호는 [" + code + "] 입니다.");
        try {
            response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            // 방금 발송된 메세지 아이디
//            result = getMessageList(response.getMessageId());
//            System.out.println("메세지 아이디 추출: " + response.getMessageId());

        } catch (Exception e) {
            System.out.println("메세지 전송 실패");
            System.out.println(e.getMessage());
        }

        // 발송 메세지 ID return
//        return response.getMessageId();
        return code;
    }

    // 인증 문자 발송
    public void notificationSend(String to, String sendMessage) {

        SingleMessageSentResponse response = null;
        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(to);
        message.setText(sendMessage);
        try {
            response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            // 방금 발송된 메세지 아이디
//            result = getMessageList(response.getMessageId());
//            System.out.println("메세지 아이디 추출: " + response.getMessageId());

        } catch (Exception e) {
            System.out.println("메세지 전송 실패");
            System.out.println(e.getMessage());
        }
    }

    public int getMessageStatusCode(String messageId) {
        MessageListRequest request = new MessageListRequest();
        MessageListResponse response = this.messageService.getMessageList(request);

        System.out.println("상태 코드: " + response.getMessageList().get(messageId).getStatusCode());

        return Integer.parseInt(response.getMessageList().get(messageId).getStatusCode());
    }

    // 남은 잔액 조회 - show 컨트롤러, html 따로 만들긴 해야함
    @GetMapping("/sms-balance")
    @ResponseBody
    public Balance getBalance() {
        Balance balance = this.messageService.getBalance();
        System.out.println(balance);

        // 2025 : 잔액 차감 실패
        // 2230 : 잔액 부족
        // 3010 : 수신번호 형식 오류

        return balance;
    }






}
