//$(function() {
//    $('#chatting').focus();
//
//    var ws;
//    // 재연결 카운트
//    let reconnectAttempts = 0;
//    const maxReconnectAttempts = 5;
//
//    // pong 무응답 카운트
//    let pongNoAnswerCount = 0;
//
//    // ping pong 체크
//    let lastPingTime = Date.now(); // 마지막 ping 보낸 시간
//    let lastPongTime = Date.now();  // 마지막 pong 응답 시간
//    const pongTimeout = 10000;  // 10초 동안 pong 응답이 없으면 연결 종료
//    const pingInterval = 30000;  // 30초마다 ping을 보냄
//
//
//    // ping 발송 -> 응답 대기
//    async function sendPing() {
//        if (ws.readyState === WebSocket.OPEN) {
//            ws.send("ping");
//            lastPingTime = Date.now(); // 마지막 ping 시간 갱신
//            console.log('ping 발송');
//
//            // pong 응답을 기다리기 위해 10초 타이머를 설정
//            const responseReceived = await waitForPong();
//
//            if (!responseReceived) {
//                pongNoAnswerCount++;
//
//                if(pongNoAnswerCount >= maxReconnectAttempts) {
//                    console.log("[서버 오류] 최대 재연결 시도 횟수 초과. ping 발송 중단");
//                    clearTimeout(reconnectTimer); // 재연결 시도 종료
//                    clearTimeout(pingTimer);   // ping 발송 종료
//                    clearTimeout(monitorTimer);   // ping 모니터링 종료
//                    ws.close(4003, "Pong timeout_ reload");
//                    return;
//                }
//                console.log("Pong 응답없음");
//                ws.close(4002, "Pong timeout");
//            }
//        }
//    }
//
//    // ping 발송 유무 모니터링
//    function monitorPingInterval() {
//        const now = Date.now();
//        if (now - lastPingTime > pingInterval * 2) { // 60초 동안 ping 없음
//            console.warn('Ping 주기 초과. 연결 종료.');
//            ws.close(4001, "Ping timeout");
//        } else {
//            console.log("Ping 모니터링 정상");
//        }
//    }
//
//    // pong 응답을 기다리는 함수
//    function waitForPong() {
//        return new Promise((resolve) => {
//            const timeout = setTimeout(() => {
//                resolve(false);  // pong 응답이 없으면 false 반환
//            }, pongTimeout);  // 10초 기다림
//
//            // 기존 onmessage 핸들러를 덮어쓰지 않고, pong 메시지를 처리
//            const pongHandler = (event) => {
//                if (event.data === "pong") {
//                    clearTimeout(timeout);  // 타이머를 중지
//                    ws.removeEventListener('message', pongHandler);  // 이벤트 리스너 제거
//                    resolve(true);  // 응답을 받으면 true 반환
//                }
//            };
//
//            // 새로운 pong 응답을 받기 위한 이벤트 리스너 추가
//            ws.addEventListener('message', pongHandler);
//        });
//    }
//
//    // 주기적으로 ping을 보내는 타이머
//    let pingTimer = setInterval(() => {
//        sendPing();  // ping 발송
//    }, pingInterval);  // 30초마다
//
//    // ping 주기 모니터링 타이머
//    let monitorTimer = setInterval(() => {
//        monitorPingInterval(); // 모니터링
//    }, pingInterval);  // 30초마다
//
//    let reconnectTimer = null;
//    // WebSocket 재연결
//    function reconnectingWebSocket() {
//        if(reconnectAttempts < maxReconnectAttempts) {
//            // 중복방지 기존 타이머 제거
//            if(reconnectTimer) {
//                clearTimeout(reconnectTimer);
//            }
//
//            reconnectTimer = setTimeout(function() {
//                if(!ws || ws.readyState !== WebSocket.OPEN){
//                    reconnectAttempts++;
//                    wsOpen(); // 재연결 시도
//                    console.log("재연결 시도중입니다.");
//                } else {
//                    console.log("웹소켓 이미 열려있음. 재연결 중단");
//                    clearTimeout(reconnectTimer);
//                }
//            }, 5000 * reconnectAttempts); // 재연결 간격 증가
//        } else {
//            console.error("Maximum reconnect attempts reached. Please check your connection.");
//            alert("서버와의 연결이 끊어졌습니다. 다시 시도해 주세요.");
//            window.close();
//        }
//    }
//
//
//    function wsOpen() {
//        if (ws && ws.readyState === WebSocket.OPEN) {
//            // 이미 WebSocket 연결이 존재하면 새로 연결하지 않음
//            console.log("WebSocket is already open.");
//            return;
//        }
//
//        ws = new WebSocket("ws://" + location.host + "/chatting/" + $("#roomNumber").val());
//        wsEvt();
//    }
//
//
//    function wsEvt() {
//        ws.onopen = function(data) {
//            // 소켓이 열리면 동작
//            reconnectAttempts = 0;
//            clearTimeout(reconnectTimer);
//
//            const loginUserCategory = $('#loginUserCategory').val();
//            if(loginUserCategory == 2 || loginUserCategory == 3) {
//                const welcomeMessage = `[Welcome]\n안녕하세요 문의사항을 남겨주시면 확인 후 답변 도와드리겠습니다.`
//                // welcome message 작성하세요
//                const otherUserImg = $('.other-image').last().attr('src');
//                $(".chat_wrap .inner").append(
//                                              '<div class="item yourmsg" style="margin-left:-9px;">' +
//                                              '<div class="testimonial-img p-1" style="width:4rem; display:inline-block">' +
//                                              '<img src="' + otherUserImg + '" class="img-fluid rounded-circle" alt="Image">' +
//                                              '</div>' +
//                                              '<div class="box" style="margin-left: 0.5rem; margin-top:1.5rem; position:relative; top:0.6rem;">' +
//                                              '<p class="msg" style="height:auto;">' + welcomeMessage + '</p><span class="time">' + currentTime() + '</span>' +
//                                              '</div></div>'
//                );
//            }
//        };
//
//        ws.onmessage = function(data) {
//            var msg = data.data;
//
//            // ping-pong 일 경우 return
//            if(msg == 'pong') {
//                lastPongTime = Date.now();  // pong을 받으면 마지막 시간 갱신
//                console.log("pong 응답");
//                return;
//            }
//
//            if (msg != null && msg.trim() != '') {
//                var d = JSON.parse(msg);
//
//                // 웹소켓 최초 연결 시 sessionId 응답
//                if (d.type == "getId") {
//                    var si = d.sessionId != null ? d.sessionId : "";
//                    if (si != '') {
//                        $("#sessionId").val(si);
//                    }
//                } else if (d.type == "message") {
//                    if (d.sessionId == $("#sessionId").val()) {
//                        // 내가 보낸 메세지
//                        $(".chat_wrap .inner").append('<div class="item mymsg"><div class="box"><p class="msg" style="height:auto;">' +
//                                                        d.msg + '</p><span class="time">' + currentTime() + '</span></div></div>'
//                                                     );
//                        const currentUrl = encodeURIComponent(window.location.href);
//                        $.ajax({
//                            url: "sendMessage",
//                            data:
//                                {
//                                    message: d.msg,
//                                    roomNumber: d.roomNumber,
//                                    fromUserId : $('#fromUserId').val(),
//                                    toUserId : $('#toUserId').val(),
//                                    uri : currentUrl
//                                },
//                            contentType: "application/json; charset=UTF-8",
//                            success: function(res, status, xhr) {
//                                if(res) {
//                                    console.log('메세지 저장 완료');
//                                }
//                            },
//                            error: function(xhr, status, err) {
//                                if(xhr.status === 401) {
//                                    alert("로그인 세션이 만료되었습니다. 다시 로그인 해주세요.");
//                                    window.close();
//                                } else {
//                                    alert('메세지 전송 중 오류가 발생했습니다. 새로 고침 후 다시 시도해 주세요.');
//                                }
//                            }
//                        });
//                    } else {
//                        $.ajax({
//                            url: "/checkLoginStatus",
//                            success: function(resp) {
//                                if(!resp) {
//                                    alert('로그인 세션이 만료되었습니다. 다시 로그인 해주세요.');
//                                    window.close();
//                                }
//                            },
//                            error: function(error) {
//                                alert('로그인 세션이 만료되었습니다. 다시 로그인 해주세요.');
//                                if(ws) {
//                                    ws.close();
//                                }
//                                window.close();
//                            }
//                        });
//
//                        const otherUserImg = $('.other-image').last().attr('src');
//                        // 상대방이 보낸 메세지
//                        $(".chat_wrap .inner").append(
//                                  '<div class="item yourmsg" style="margin-left:-9px;">' +
//                                  '<div class="testimonial-img p-1" style="width:4rem; display:inline-block">' +
//                                  '<img src="' + otherUserImg + '" class="img-fluid rounded-circle" alt="Image" style="background-color:white; aspect-ratio: 1 / 1;">' +
//                                  '</div>' +
//                                  '<div class="box" style="margin-left: 0.5rem; margin-top:1.5rem; position:relative; top:0.6rem;">' +
//                                  '<p class="msg" style="height:auto;">' + d.msg + '</p><span class="time" style="margin-bottom:1.2rem;">' + currentTime() + '</span>' +
//                                  '</div></div>'
//                        );
//                    }
//                    var lastItem = $(".chat_wrap .inner").find(".item:last");
//                    setTimeout(function() {
//                        lastItem.addClass("on");
//                    }, 10);
//
//                    var position = lastItem.position().top + $(".chat_wrap .inner").scrollTop();
//                    $(".chat_wrap .inner").stop().animate({scrollTop: position}, 500);
//                } else {
//                    console.warn("unknown type!");
//                }
//            } else {
//                alert('잘못 수신된 메세지');
//            }
//        };
//
//        ws.onclose = function(e) {
//
//            if(e.code === 4401) {
//                alert("세션이 만료되었습니다. 다시 로그인해 주세요.");
//                window.close();
//                return;
//            }
//
//            if(e.code === 1006) {
//                alert("서버 종료 및 Access 토큰 재발급");
//            }
//
//            if(e.code === 4001) { // 클라이언트 ping 발송 실패
//                alert("네트워크가 불안정하여 채팅을 종료합니다. 다시 시도해 주세요.");
//                window.close();
//                return;
//            }
//
//            if(e.code === 4002) { // 서버 pong 발송 실패
//                // 재 연결 시도중
////                if(pongNoAnswerCount >= maxReconnectAttempts) {
////                    alert('서버 연결 문제로 새로고침 합니다.');
////                    location.reload();
////                    return;
////                }
//            }
//
//            if(e.code === 4003) {
//                // 새로고침 후 재 실행
//                alert('서버 연결 문제로 새로고침 합니다.');
//                location.reload();
//                return;
//            }
//
//            reconnectingWebSocket();
//        };
//
//        ws.onerror = function(xhr, error) {
//            if(ws && ws.readyState == WebSocket.OPEN) {
//                ws.close();
//            }
//            if(xhr.status === 401) {
//                alert("로그아웃됨");
//            }
//        };
//    }
//
//
//
//    // 채팅 목록의 안읽은 메세지 계산
//    $('.new-message-count').each(function() {
//        var $chatRoom = $(this).closest('tr');
//        var $toIsReadStatuses = $chatRoom.find('.toIsReadStatus');
//
//        // 읽지 않은 메시지의 개수를 계산
//        var unreadCount = $toIsReadStatuses.toArray().filter(function(input) {
//            return $(input).val() === 'false';
//        }).length;
//
//        // 읽지 않은 메시지가 있으면 개수 표시 및 display 설정
//        if (unreadCount > 0) {
//            $(this).text(unreadCount).css('display', 'inline');
//        }
//    });
//    // enter + send-btn click event
//    $(document).on("keypress", "#chatting", function(e) {
//        if (e.which === 13) { // Enter key
//            send();
//        }
//    });
//    $('.btn-primary').on('click', function() {
//        send();
//    });
//
//    function send() {
//        var message = $("#chatting").val().trim();
//        if (message === "") {
//            return;
//        }
//        var option = {
//            type: "message",
//            roomNumber: $("#roomNumber").val(),
//            sessionId: $("#sessionId").val(),
//            fromUserId: $("#fromUserId").val(),
//            toUserId: $('#toUserId').val(),
//            msg: message
//        };
//        ws.send(JSON.stringify(option));
//        $('#chatting').val("");
//    }
//
//    function currentTime() {
//        const date = new Date();
//        let hh = date.getHours();
//        const mm = date.getMinutes();
//        const apm = hh >= 12 ? "오후" : "오전";
//        hh = hh % 12;
//        hh = hh ? hh : 12;
//        return apm + " " + hh + ":" + (mm < 10 ? '0' : '') + mm; // 두 자리 설정
//    }
//
//    // WebSocket 연결 열기
//    wsOpen();
//
//
//    // 이동 경로를 안전하게 붙이는 함수
//    function appendPath(baseUri, path) {
//      // 경로가 '/'로 시작하지 않으면 '/'를 추가
//      return `${baseUri}${path.startsWith('/') ? path : '/' + path}`;
//    }
//
//    // chatting 접속
//    $('.go-chatting').on('click', function(e) {
//        e.preventDefault();
//        const chatRoomId = $(this).data("chatroom-id");
//
//        // 현재 페이지의 URL을 가져옵니다.
//        const fullUrl = window.location.href;
//
//        // URL 객체를 생성하여 URL의 구성 요소를 분석합니다.
//        const url = new URL(fullUrl);
//
//        // origin 속성을 사용하여 프로토콜, 호스트 및 포트를 가져옵니다.
//        const baseUri = url.origin;
//
//        // 이동할 URI를 생성합니다.
//        const moveUri = appendPath(baseUri, "moveChatting?roomNumber=" + chatRoomId);
//
//        $.ajax({
//            url : "message-read",
//            type: "GET",
//            data: {
//                    chatRoomId : chatRoomId,
//                    toUserId : $('#fromUserId').val() // 로그인 유저 아이디
//                  },
//            success: function(resp, status, xhr) {
//                location.href = moveUri;
//            },
//            error: function(xhr, status, err) {
//                console.error('에러 발생:', err);
//                console.log(err);
//                alert('요청이 실패했습니다. 다시 시도해 주세요.');
//            }
//        });
////        location.href = moveUri;
//    });
//
//    // 편집 버튼 눌렀을 때 select-box show
//    $('#edit-btn').on('click', function(e) {
//        const status = $('#edit-btn').text();
//        if(status == '편집') {
//            $('.select-box').css('display', 'inline');
//            $('#getOut-chatroom, #check-span').css('display', 'block');
//            $('.go-chatting').css('pointer-events', 'none');
//            $('#edit-btn').text('취소');
//        } else {
//            e.preventDefault();
//            $('#all-check').prop('checked', false);
//            $('.select-box').prop('checked', false);
//            $('.select-box, #getOut-chatroom, #check-span').css('display', 'none');
//            $('.go-chatting').css('pointer-events', 'auto');
//            $('#edit-btn').text('편집');
//        }
//    });
//
//    // 전체 선택 버튼
//    $('#all-check').on('change', function(e) {
//        const chk = $('.select-box');
//
//        if($(this).is(':checked')) {
//              chk.prop('checked', true);
//          } else {
//              chk.prop('checked', false);
//          }
//    });
//
//    // 채팅방 나가기 (checked)
//    $('#getOut-chatroom').on('click', function(e) {
//        e.preventDefault();
//
//        let selectedValues = [];
//        $('input[name="select-box"]:checked').each(function() {
//            selectedValues.push($(this).data('chatroom-id'));
//        });
//
//        if(selectedValues.length === 0) {
//            return;
//        }
//
//        $.ajax({
//            url : "out-chatroom",
//            type: "post",
//            data: {selectedValues : selectedValues},
//            success: function(resp, status, xhr) {
//                alert('채팅방 나가기 완료되었습니다');
//
//                // 현재 있는 채팅방을 나가기 했다면
//                const urlParams = new URLSearchParams(window.location.search);
//                const roomNumber = parseInt(urlParams.get('roomNumber')); // 'roomNumber' 파라미터 값을 정수로 변환
//                $.each(selectedValues, function(idx, value) {
//                    if(value == roomNumber){
//                        window.close();
//                        return;
//                    }
//                });
//
//                location.reload();
//            },
//            error: function(xhr, status, err) {
//
//                if(xhr.status === 401) {
//                    alert("로그인 세션이 만료되었습니다. 다시 로그인 해주세요.");
//                    location.reload();
//                    return;
//                } else if(xhr.status === 400) {
//                    alert("잘못된 요청입니다.");
//                    location.reload();
//                    return;
//                }
//                console.error('에러 발생:', err);
//                alert('요청이 실패했습니다. 다시 시도해 주세요.');
//                location.reload();
//            }
//        });
//    });
//
//});
