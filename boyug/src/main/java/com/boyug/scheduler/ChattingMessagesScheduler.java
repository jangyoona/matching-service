package com.boyug.scheduler;

import com.boyug.entity.ChatMessageEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChattingMessagesScheduler {

    private final RedisService redisService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

    private final TransactionTemplate transactionTemplate;


    @Scheduled(fixedRate = 20000) // 20초 마다
    public void saveMessagesToDB() {
        // 트랙잭션 활성화
        transactionTemplate.execute(status -> {

            // 트랜잭션 활성화 확인
//            System.out.println(TransactionSynchronizationManager.isActualTransactionActive()); // true 출력

            Set<String> keys = redisService.findAllMessages();

            for (String key : keys) {
                Long size = redisService.getMessagesSize(key);

                // 개수 기준 ex) 5개 이상 일 때
                if (size != null && size >= 5) {
                    List<String> messages = redisService.findMessagesByChatRoomId(key);

                    if (messages != null && !messages.isEmpty()) {
                        // 메세지 Entity 로 변환
                        List<ChatMessageEntity> entity = convertRedisMessagesToEntities(messages);

                        // DB 일괄 저장
                        chatMessageRepository.saveAll(entity);

                        // 저장 후 Redis 삭제
                        redisService.deleteMessagesByChatRoomId(key);
                    }
                }
            }
            return null; // 트랜잭션 종료 및 커밋
        });
    }


    @NotNull
    private List<ChatMessageEntity> convertRedisMessagesToEntities(List<String> messages) {
        return messages.stream().map(msg -> {
            String[] parts = msg.split("\\|");

            return new ChatMessageEntity(
                    chatRoomRepository.findById(Integer.parseInt(parts[0])), // chatRoomId
                    parts[1],                                                // chatContent
                    Timestamp.valueOf(parts[2]),                             // chatSendTime
                    accountRepository.findById(Integer.parseInt(parts[3])),  // fromUser
                    accountRepository.findById(Integer.parseInt(parts[4])),  // toUser
                    false                                                    // toIsRead
            );
        }).collect(Collectors.toList());
    }

}
