package com.boyug.service;

import com.boyug.dto.*;
import com.boyug.entity.ChatMessageEntity;
import com.boyug.entity.ChatRoomEntity;
import com.boyug.repository.ChatMessageRepository;
import com.boyug.repository.ChatRoomRepository;
import com.boyug.security.WebUserDetails;
import com.boyug.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChattingServiceImpl implements ChattingService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    // NotificationService 와 순환 참조로 인해 이벤트 객체 생성
    private ApplicationEventPublisher eventPublisher;

    public ChattingServiceImpl(ApplicationEventPublisher eventPublisher, JwtUtil jwtUtil, AccountService accountService, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository) {
        this.eventPublisher = eventPublisher;
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }


    private static final String WELCOME_MESSAGE = "안녕하세요 문의사항을 남겨주시면 확인 후 답변드리겠습니다.";


    @Override
    public int getNextChatRoomId() {
        return chatRoomRepository.findNextChatRoomId();
    }


    /*
    * chatRoom 생성
    * */
    @Override
    public List<ChatRoomDto> createOrGetChatRoom(WebUserDetails userDetails, int toUserId) {
        List<ChatRoomDto> roomList = new ArrayList<>();
        Integer existingRoom = getRoomDupCheck(userDetails.getUser(), toUserId);

        if (existingRoom == null) {
            ChatRoomDto newRoom = createNewChatRoom(userDetails, toUserId);
            roomList.add(newRoom);
            sendWelcomeMessage(newRoom, userDetails, toUserId);
        } else {
            ChatRoomDto existingChatRoom = getChatRoom(existingRoom);
            existingChatRoom.setRoomNumber(existingRoom);
            roomList.add(existingChatRoom);
        }
        return roomList;
    }


    public Integer getRoomDupCheck(UserDto loginUser, int otherUserId) {
        Integer isChatRoom = 0;
        if(loginUser.getUserCategory() == 2) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndBoyugType(loginUser.getUserId(), otherUserId);
        } else if(loginUser.getUserCategory() == 3) {
            isChatRoom = chatMessageRepository.findChatRoomByUserIdsAndUserType(loginUser.getUserId(), otherUserId);
        }

        return isChatRoom;
    }

    private ChatRoomDto createNewChatRoom(WebUserDetails userDetails, int toUserId) {
        ChatRoomDto room = new ChatRoomDto();
        ChatRoomEntity savedEntity = chatRoomRepository.save(room.toEntity());
        int savedId = savedEntity.getChatRoomId();

        room.setChatRoomId(savedId);
        room.setRoomNumber(savedId);
        room.setToUserId(toUserId);
        room.setFromUserId(userDetails.getUser().getUserId());

        return room;
    }

    private void sendWelcomeMessage(ChatRoomDto chatRoom, WebUserDetails fromUser, int toUserId) {
        UserDto toUser = accountService.getUserInfo(toUserId);

        ChatMessageDto sendMessage = new ChatMessageDto();
        sendMessage.setChatRoomId(chatRoom);
        sendMessage.setToUserId(fromUser.getUser());
        sendMessage.setFromUserId(toUser);
        sendMessage.setChatContent(WELCOME_MESSAGE);

        insertChatMessage(sendMessage);
    }


    @Override
    public ChatRoomDto getChatRoom(int roomNumber) {
        return chatRoomRepository.findById(roomNumber)
                .map(ChatRoomDto::of)
                .orElse(null);
    }



    // start_sendMessage
    @Override
    public void sendMessage(WebUserDetails userDetails, String roomNumber, String message, int toUserId) {
        UserDto toUser = accountService.getUserInfo(toUserId);

        ChatRoomDto chatRoom = getChatRoom(Integer.parseInt(roomNumber));
        ChatMessageDto sendMessage = new ChatMessageDto();
        sendMessage.setChatRoomId(chatRoom);
        sendMessage.setToUserId(toUser);
        sendMessage.setFromUserId(userDetails.getUser());
        sendMessage.setChatContent(message);

        // 메세지 저장
        insertChatMessage(sendMessage);

        // 알림 메세지 생성 + 발송
        newNotificationMessage(userDetails, toUser, chatRoom, message);

    }

    public void newNotificationMessage(WebUserDetails userDetails, UserDto toUser,
                                       ChatRoomDto chatRoom, String message) {
        // 알림 메세지 생성
        String notificationMessage;

        if(userDetails.getUser().getUserCategory() == 2) {
            BoyugUserDto boyugUser = accountService.getBoyugUserInfo(userDetails.getUser().getUserId());
            notificationMessage = String.format("%s님이 메세지를 보냈습니다.", boyugUser.getBoyugUserName());
        } else {
            notificationMessage = String.format("%s님이 메세지를 보냈습니다.", userDetails.getUser().getUserName());
        }

        // 알림발송 -> 순환참조 때문에 이벤트 만듬
        eventPublisher.publishEvent(new ChattingEvent(userDetails.getUser(), toUser, chatRoom, notificationMessage, message));
    }
    // end_sendMessage


    @Override
    public void insertChatMessage(ChatMessageDto sendMessage) {
        ChatMessageEntity entity = sendMessage.toEntity();
        chatMessageRepository.save(entity);
    }

    @Override
    public List<ChatMessageDto> getMessagesByRoomId(int chatRoomId) {
        List<ChatMessageEntity> entities = chatMessageRepository.findByChatRoomChatRoomIdOrderByChatSendTimeAsc(chatRoomId);
        return entities.stream()
                .map(ChatMessageDto::of)
                .collect(Collectors.toList());
    }

    @Override
    public void updateChatMessageIsRead(int chatRoomId, int toUserId) {
        chatMessageRepository.updateToIsRead(chatRoomId, toUserId);
    }



    // start_moveChatting
    @Override
    public ModelAndView prepareChatView(int roomNumber, WebUserDetails userDetails, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("chat");
        ChatRoomDto isChatRoom = getChatRoom(roomNumber);

        if (isChatRoom == null) {
            mv.setViewName("home?chaterror");
            return mv;
        }

        // 로그인 유저 카테고리 추출 (welcome message 유무 체크)
        UserDto loginUser = userDetails.getUser();
        mv.addObject("loginUserId", loginUser.getUserId());
        mv.addObject("loginUserName", loginUser.getUserName());
        mv.addObject("loginUserCategory", loginUser.getUserCategory());

        // 상대방 채팅방 헤더 name 추출
        UserDto otherUser = getOtherUser(isChatRoom, loginUser);
         mv.addObject("otherUserName", getOtherUserName(otherUser));

        // 채팅방 목록 profileImage url
        List<ProfileImageDto> profileImage = accountService.getUserProfileImage(otherUser);
        String profileUrl = (profileImage != null && !profileImage.isEmpty()) ? profileImage.get(0).getImgSavedName() : null;
        mv.addObject("profileUrl", profileUrl);

        // 채팅방 목록 안 각 메세지별 프로필 사진 설정 (나 or 상대방)
        List<ChatRoomDto> userRoomList = getChatRoomByUserId(loginUser);
        setUserProfileImagesForChatRooms(userRoomList, loginUser);
//        setUserProfileImagesForChatRooms(userRoomList, loginUser, otherUser);
        mv.addObject("userRoomList", userRoomList);

        // 기존 채팅 불러오기
        List<ChatMessageDto> messages = getMessagesByRoomId(roomNumber);
        setProfileImagesForMessages(messages, loginUser, profileImage);
        mv.addObject("messages", messages);

//        int toUserId = getToUserId(roomNumber, session, messages, loginUser, mv);
        mv.addObject("toUserId", otherUser.getUserId());
        mv.addObject("roomNumber", roomNumber);

        return mv;
    }

    /*
    * 1. otherUser 추출
    * */
    private UserDto getOtherUser(ChatRoomDto isChatRoom, UserDto loginUser) {
        UserDto otherUser = null;

        if(loginUser.getUserId() == isChatRoom.getChatMessages().get(0).getToUserId().getUserId()) {
            otherUser = accountService.getUserInfo(isChatRoom.getChatMessages().get(0).getFromUserId().getUserId());
        } else {
            otherUser = accountService.getUserInfo(isChatRoom.getChatMessages().get(0).getToUserId().getUserId());
        }
        return otherUser;
    }

    /*
     * 2. 채팅방 상대방 name 추출
     * */
    private String getOtherUserName(UserDto otherUser) {
        String otherUserName = "";

        if (otherUser.getUserCategory() == 2) {
            BoyugUserDto boyugUser = accountService.getBoyugUserInfo(otherUser.getUserId());
            otherUserName = boyugUser.getBoyugUserName();
        } else if (otherUser.getUserCategory() == 3) {
            otherUserName = otherUser.getUserName();
        }
        return otherUserName;
    }

    /*
    * 3. 왼쪽 채팅목록 프로필 사진 메세지에서 추출
    * */
    private void setUserProfileImagesForChatRooms(List<ChatRoomDto> userRoomList, UserDto loginUser) {
//    private void setUserProfileImagesForChatRooms(List<ChatRoomDto> userRoomList, UserDto loginUser, UserDto otherUser) {
        int userCategory = loginUser.getUserCategory();

        List<ProfileImageDto> profileImages = null;
        for (ChatRoomDto chatRoomDto : userRoomList) {
            if((userCategory == 2 && chatRoomDto.isBoyugChatActive()) || (userCategory == 3 && chatRoomDto.isUserChatActive())) {
                if (chatRoomDto.getChatMessages().get(0).getToUserId().getUserId() != loginUser.getUserId()) {
                    profileImages = accountService.getUserProfileImage(chatRoomDto.getChatMessages().get(0).getToUserId());
                    chatRoomDto.getChatMessages().get(0).getToUserId().setImages(profileImages);
                } else {
                    profileImages = accountService.getUserProfileImage(chatRoomDto.getChatMessages().get(0).getFromUserId());
                    chatRoomDto.getChatMessages().get(0).getFromUserId().setImages(profileImages);
                }
            }
        }
    }

    /*
    * 4. 메세지별 프로필 이미지 설정
    * */
    private void setProfileImagesForMessages(List<ChatMessageDto> messages, UserDto loginUser, List<ProfileImageDto> profileImage) {
        for (ChatMessageDto message : messages) {
            if (message.getToUserId().getUserId() != loginUser.getUserId()) {
                message.getToUserId().setImages(profileImage);
            } else if (message.getFromUserId().getUserId() != loginUser.getUserId()) {
                message.getFromUserId().setImages(profileImage);
            }
        }
    }

    /*
    * 5. toUserId 추출
    * */
//    private int getToUserId(int roomNumber, HttpSession session, List<ChatMessageDto> messages, UserDto loginUser, ModelAndView mv) {
//        // 메세지 send시 json-option에 담길 toUserId를 위해 저장
//        int toUserId = 0;
//
////        Map<String, Object> roomUser = ChattingController.getRoomUser();
//
//        // from, to 유저 서버에 저장
////        roomUser.put(roomNumber+"f", loginUser.getUserId());
//        if(messages.isEmpty()){ // 최초 생성시 이건 잘 작동함
////            roomUser.put(roomNumber+"t", session.getAttribute("toUserId"));
//        } else {
//            if(messages.get(0).getToUserId().getUserId() == loginUser.getUserId()) {
////                roomUser.put(roomNumber+"t", messages.get(0).getFromUserId().getUserId());
//                mv.addObject("toUserName", messages.get(0).getFromUserId().getUserName());
//                toUserId = messages.get(0).getFromUserId().getUserId();
//            } else {
////                roomUser.put(roomNumber+"t", messages.get(0).getToUserId().getUserId());
//                mv.addObject("toUserName", messages.get(0).getToUserId().getUserName());
//                toUserId = messages.get(0).getToUserId().getUserId();
//            }
//        }
//        return toUserId;
//    }
    // end_moveChatting



    @Override
    public List<ChatRoomDto> getChatRoomByUserId(UserDto loginUser) {
        List<ChatRoomEntity> list = null;

        if(loginUser.getUserCategory() == 2) { // 보육유저
            list = chatRoomRepository.findAllChatRoomByBoyugUserId(loginUser.getUserId());
        } else if(loginUser.getUserCategory() == 3) { // 개인회원
            list = chatRoomRepository.findAllChatRoomByUserId(loginUser.getUserId());
        }

        if(list != null) {
            for (ChatRoomEntity chatRoomEntity : list) { // 한 행씩 꺼내서 보여줌 == 펼침.
                List<ChatMessageEntity> messages = chatMessageRepository.findByChatRoomChatRoomIdOrderByChatSendTimeAsc(chatRoomEntity.getChatRoomId());
                chatRoomEntity.setChatMessages(messages);
            }
        }
        return list != null ? list.stream()
                                  .map(ChatRoomDto::of)
                                  .collect(Collectors.toList()) : null;
    }

    @Override
    public void editChatRoomActive(WebUserDetails user, int[] selectedValues) {

        List<ChatRoomEntity> entityList = new ArrayList<>();
        for (int result : selectedValues) {
            ChatRoomEntity entity = chatRoomRepository.findById(result).orElseThrow();
            entityList.add(entity);
        }

        if(user.getUser().getUserCategory() == 2) {
            for (ChatRoomEntity chatRoomEntity : entityList) {
                chatRoomEntity.setBoyugChatActive(false);
            }

        } else if ((user.getUser().getUserCategory() == 3)) {
            for (ChatRoomEntity chatRoomEntity : entityList) {
                chatRoomEntity.setUserChatActive(false);
            }
        }

        for (ChatRoomEntity chatRoomEntity : entityList) {
            chatRoomRepository.save(chatRoomEntity);
        }
    }

}
