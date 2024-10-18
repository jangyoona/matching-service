$(function() {
    // DOM 요소를 변수에 저장
    const chatbotToggler = $('.chatbot-toggler');
    const closeBtn = $(".close-btn");
    const chatbox = $(".chatbox");
    const chatInput = $(".chat-input textarea");
    const sendChatBtn = $(".chat-input span");
    const chatListBtn = $('.chat-list-btn');
    const chatList = $('.chat-list');
    const recordBtn = $('.record-btn');
    
    let userId = $('#currentUserId').val();
    

    let userMessage = null;
    const inputInitHeight = chatInput[0].scrollHeight;
    let chatRooms = [];
    let currentIndex = 0;
    let mediaRecorder;
    let audioChunks = [];
    let audioBlob = null;
    let chatCurrentRoomId = null;

    // 새로운 채팅 메시지 요소를 생성하는 함수
    const createChatLi = (message, className) => {
        const chatLi = $("<li></li>");
        chatLi.addClass(["chat", className]);
        let chatContent = className === "outgoing" ? `<p></p>` : `<span class="material-symbols-outlined">smart_toy</span><p></p>`;
        chatLi.html(chatContent);
        chatLi.find("p").text(message);
        return chatLi;
    }

    // 응답 메시지를 생성하는 함수
    const generateResponse = (chatElement, message) => {
        const messageElement = chatElement.find("p");
        messageElement.text(message);
        chatbox.scrollTop(chatbox[0].scrollHeight);
    }

    // 서버 요청을 처리하는 함수
    const fetchData = async (url, options) => {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error(`HTTP error! status : ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Fetch error:', error);
        }
    }

    // 채팅을 처리하는 함수
    const handleChat = async () => {
        userMessage = chatInput.val().trim();
        if (!userMessage) return;
        chatInput.val("");
        chatInput.css('height', `${inputInitHeight}px`);
        chatbox.append(createChatLi(userMessage, "outgoing"));
        chatbox.scrollTop(chatbox[0].scrollHeight);

        let conversationHistory = [];
        chatbox.find("li.chat").each(function() {
            const role = $(this).hasClass("outgoing") ? "user" : "assistant";
            const content = $(this).find("p").text();
            conversationHistory.push({ role, content });
        });
        conversationHistory.push({ role: 'user', content: userMessage });
        if (conversationHistory.length > 2) {
            conversationHistory.pop();
        }
        console.log(conversationHistory);
        const response_data = await fetchData(`http://192.168.0.15:9999/chatbot/chat-text?userId=${userId}`, {
            method: "POST",
            cache: "no-cache",
            headers: { "Content-Type": "application/json;charset=utf-8" },
            body: JSON.stringify({ conversation: conversationHistory })
        });

        if (response_data) {
            const response_message = response_data.message;
            const incomingChatLi = createChatLi("Thinking...", "incoming");
            chatbox.append(incomingChatLi);
            chatbox.scrollTop(chatbox[0].scrollHeight);
            generateResponse(incomingChatLi, response_message);


            if (!chatCurrentRoomId || chatCurrentRoomId === 1) {
                const maxChatRoomId = chatRooms.length > 0 ? Math.max(...chatRooms.map(room => room.GPTTALKID)) : 0;
                chatCurrentRoomId = maxChatRoomId + 1;
                fetchData(`http://192.168.0.15:9999/chatbot/chat-history-save?chatCurrentRoomId=${chatCurrentRoomId}&userId=${userId}`, {
                    method: "POST",
                    cache: "no-cache",
                    headers: { "Content-Type": "application/json;charset=utf-8" },
                    body: JSON.stringify({ conversation: conversationHistory })
                });
                console.log('chatCurrentRoomId:', chatCurrentRoomId, "userId:", userId);
            }

            fetchData(`http://192.168.0.15:9999/chatbot/chat-detail-history-save`, {
                method: "POST",
                cache: "no-cache",
                headers: { "Content-Type": "application/json;charset=utf-8" },
                body: JSON.stringify({ 
                    chatCurrentRoomId: chatCurrentRoomId, 
                    responseData: response_message, 
                    userMessage: userMessage,
                    userId: userId // userId 추가
                })
            });
            console.log("chatCurrentRoomId:", chatCurrentRoomId, "responseData:", response_message, "userMessage:", userMessage, "userId:", userId); // userId 추가
        }
    }

    // 입력 텍스트 영역의 내용이 변경될 때 실행
    chatInput.on("input", () => {
        chatInput.css('height', `${inputInitHeight}px`);
        chatInput.css('height', `${chatInput[0].scrollHeight}px`);
    });

    // 입력 텍스트 영역에서 키를 눌렀을 때 실행
    chatInput.on("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey && window.innerWidth > 800) {
            e.preventDefault();
            handleChat();
        }
    });

    // 채팅 전송 버튼 클릭 시 채팅 처리 함수 실행
    sendChatBtn.on("click", handleChat);

    // 챗봇 닫기 버튼 클릭 시 챗봇 창 닫기
    closeBtn.on("click", () => $('body').removeClass("show-chatbot"));

    // 챗봇 토글 버튼 클릭 시 챗봇 창 열기/닫기
    chatbotToggler.on('click', function(event){
        $('body').toggleClass('show-chatbot');
        fetchChatRooms();
    });

    // 채팅 목록 버튼 클릭 시 채팅 목록 토글
    chatListBtn.on('click', function(event){
        fetchChatRooms();
        switch ($(this).text()) {
            case "list":
                $(this).text('menu_open');
                chatList.toggle();
                break;
            case "menu_open":
                $(this).text('list');
                chatList.toggle();
                break;
        }
    });

    // 채팅 목록을 서버에서 가져오는 함수
    function fetchChatRooms() {
        fetchData(`http://192.168.0.15:9999/chatbot/chat-list?userId=${userId}`, {
            method: "GET",
            cache: "no-cache",
            headers: { "Content-Type": "application/json;charset=utf-8" }
        }).then(data => {
            if (data) {
                chatRooms = data;
                $('.chat-list ul').empty();
                $('.chat-room').remove();
                loadChatRooms();
            }
        });
    }

    // 채팅방을 로드하는 함수
    function loadChatRooms() {
        chatRooms.forEach(room => {
            const roomElement = $(`
                <div class="chat-room" data-listid="${room.GPTTALKID}" id="chat-room-${room.GPTTALKID}">
                    <h5>지난 채팅 ${room.GPTTALKID}</h5>
                    <button class="delete-button" data-id="${room.GPTTALKID}">
                        <span class="material-symbols-outlined">delete</span>
                    </button>
                    <ul>
                        <li><strong>마지막 질문:</strong> ${room.GPTREQUEST}</li>
                    </ul>
                </div>
            `);
            chatList.append(roomElement);
        });

        // 삭제 버튼 클릭 시 채팅방 삭제
        $('.delete-button').on('click', function(event) {
            const talkId = $(this).data('id');
            fetchData(`http://192.168.0.15:9999/chatbot/chat-list-delete?gptTalkId=${talkId}`, {
                method: "GET",
                cache: "no-cache",
                headers: { "Content-Type": "application/json;charset=utf-8" }
            }).then(() => fetchChatRooms());
        });

        // 채팅방 클릭 시 채팅 기록 로드
        $('.chat-room').on('click', function(event) {
            const talkId = $(this).data('listid');
            fetchData(`http://192.168.0.15:9999/chatbot/chat-history?gptTalkId=${talkId}&userId=${userId}`, {
                method: "GET",
                cache: "no-cache",
                headers: { "Content-Type": "application/json;charset=utf-8" }
            }).then(chat_list => {
                if (chat_list) {
                    chatbox.find('li.incoming, li.outgoing').remove();
                    chat_list.forEach(chat => {
                        const userMessage = chat.GPTREQUEST;
                        const botResponse = chat.GPTRESPONSE;
                        chatbox.append(createChatLi(userMessage, "outgoing"));
                        chatbox.scrollTop(chatbox[0].scrollHeight);
                        chatbox.append(createChatLi(botResponse, "incoming"));
                        chatbox.scrollTop(chatbox[0].scrollHeight);
                        chatCurrentRoomId = talkId;
                    });
                }
            }).catch(error => console.error('Error fetching chat history:', error));
        });
    }

    // 새채팅 버튼 클릭 시 새 채팅방 생성
    $('#newChat').on('click', function(event) {
        chatbox.find('li.incoming, li.outgoing').remove();
        $('.chat-list').hide();
        chatCurrentRoomId = null;
    });

    // 녹음 버튼 클릭 시 녹음 시작/중지
    recordBtn.on('click', function(event) {
        switch ($(this).text()) {
            case "mic":
                $(this).text('stop_circle');
                navigator.mediaDevices.getUserMedia({ audio: true })
                    .then(stream => {
                        mediaRecorder = new MediaRecorder(stream);
                        mediaRecorder.start();
                        console.log('Recording started...');
                        mediaRecorder.ondataavailable = event => {
                            audioChunks.push(event.data);
                        };
                    })
                    .catch(error => {
                        console.error("Error accessing microphone:", error);
                    });
                break;
            case "stop_circle":
                $(this).text('mic');
                mediaRecorder.stop();
                mediaRecorder.onstop = () => {
                    audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                    audioChunks = [];
                    sendAudioToServer();
                };
                break;
        }
    });

    // 오디오 데이터를 서버로 전송하는 함수
    function sendAudioToServer() {
        const formData = new FormData();
        formData.append('audio', audioBlob, 'recording.wav');
        fetch('http://192.168.0.15:9999/chatbot/audio-to-text', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            console.log('Server response:', data);
            chatInput.val(data);
        })
        .catch(error => {
            console.error('Error sending audio:', error);
        });
    }
});