$(function() {
    $('#chatting').focus();

    var ws;
    function wsOpen() {
        if (ws && ws.readyState === 1) {
            // 이미 WebSocket 연결이 존재하면 새로 연결하지 않음
            console.log("WebSocket is already open.");
            return;
        }

        ws = new WebSocket("ws://" + location.host + "/chatting/" + $("#roomNumber").val());
        wsEvt();
    }

    function wsEvt() {
        ws.onopen = function(data) {
            // 소켓이 열리면 동작
            let loginUser = $('#login-user').val();
            if(loginUser.userCategory == 2 || loginUser.userCategory == 3) {
                const welcomeMessage = `[Welcome]\n안녕하세요 문의사항을 남겨주시면 확인 후 답변 도와드리겠습니다.`
                // welcome message 작성하세요
                const otherUserImg = $('.other-image').last().attr('src');
                $(".chat_wrap .inner").append(
                                              '<div class="item yourmsg" style="margin-left:-9px;">' +
                                              '<div class="testimonial-img p-1" style="width:4rem; display:inline-block">' +
                                              '<img src="' + otherUserImg + '" class="img-fluid rounded-circle" alt="Image">' +
                                              '</div>' +
                                              '<div class="box" style="margin-left: 0.5rem; margin-top:1.5rem; position:relative; top:0.6rem;">' +
                                              '<p class="msg" style="height:auto;">' + welcomeMessage + '</p><span class="time">' + currentTime() + '</span>' +
                                              '</div></div>'
                );
            }
        };

        ws.onmessage = function(data) {

            var msg = data.data;

            if (msg != null && msg.trim() != '') {
                var d = JSON.parse(msg);
                if (d.type == "getId") {
                    var si = d.sessionId != null ? d.sessionId : "";
                    if (si != '') {
                        $("#sessionId").val(si);
                    }
                } else if (d.type == "message") {
                    if (d.sessionId == $("#sessionId").val()) {
                        // 내가 보낸 메세지
                        $(".chat_wrap .inner").append('<div class="item mymsg"><div class="box"><p class="msg" style="height:auto;">' +
                                                        d.msg + '</p><span class="time">' + currentTime() + '</span></div></div>'
                                                     );
                        const currentUrl = encodeURIComponent(window.location.href);
                        $.ajax({
                            url: "sendMessage",
                            data:
                                {
                                    message: d.msg,
                                    roomNumber: d.roomNumber,
                                    fromUserId : $('#fromUserId').val(),
                                    uri : currentUrl
                                },
                            contentType: "application/json; charset=UTF-8",
                            success: function(res, status, xhr) {
                                if(res === 'success') {
                                    console.log('메세지 저장 완료');
                                }
                            },
                            error: function(status, xhr, err) {
                                alert('메세지 전송 에러');
                            }
                        });
                    } else {
                        const otherUserImg = $('.other-image').last().attr('src');
                        // 상대방이 보낸 메세지
                        $(".chat_wrap .inner").append(
                                  '<div class="item yourmsg" style="margin-left:-9px;">' +
                                  '<div class="testimonial-img p-1" style="width:4rem; display:inline-block">' +
                                  '<img src="' + otherUserImg + '" class="img-fluid rounded-circle" alt="Image" style="background-color:white;">' +
                                  '</div>' +
                                  '<div class="box" style="margin-left: 0.5rem; margin-top:1.5rem; position:relative; top:0.6rem;">' +
                                  '<p class="msg" style="height:auto;">' + d.msg + '</p><span class="time">' + currentTime() + '</span>' +
                                  '</div></div>'
                        );
                    }
                    var lastItem = $(".chat_wrap .inner").find(".item:last");
                    setTimeout(function() {
                        lastItem.addClass("on");
                    }, 10);

                    var position = lastItem.position().top + $(".chat_wrap .inner").scrollTop();
                    $(".chat_wrap .inner").stop().animate({scrollTop: position}, 500);
                } else {
                    console.warn("unknown type!");
                }
            }
        };
    }

    // 채팅 목록의 안읽은 메세지 계산
    $('.new-message-count').each(function() {
        var $chatRoom = $(this).closest('tr');
        var $toIsReadStatuses = $chatRoom.find('.toIsReadStatus');

        // 읽지 않은 메시지의 개수를 계산
        var unreadCount = $toIsReadStatuses.toArray().filter(function(input) {
            return $(input).val() === 'false';
        }).length;

        // 읽지 않은 메시지가 있으면 개수 표시 및 display 설정
        if (unreadCount > 0) {
            $(this).text(unreadCount).css('display', 'inline');
        }
    });
    // enter + send-btn click event
    $(document).on("keypress", "#chatting", function(e) {
        if (e.which === 13) { // Enter key
            send();
        }
    });
    $('.btn-primary').on('click', function() {
        send();
    });

    function send() {
        var message = $("#chatting").val().trim();
        if (message === "") {
            return;
        }
        var option = {
            type: "message",
            roomNumber: $("#roomNumber").val(),
            sessionId: $("#sessionId").val(),
            fromUserId: $("#fromUserId").val(),
            toUserId: $('#toUserId').val(),
            msg: message
        };
        ws.send(JSON.stringify(option));
        $('#chatting').val("");
    }

    function currentTime() {
        let date = new Date();
        let hh = date.getHours();
        let mm = date.getMinutes();
        let apm = hh >= 12 ? "오후" : "오전";
        hh = hh % 12;
        hh = hh ? hh : 12;
        return apm + " " + hh + ":" + (mm < 10 ? '0' : '') + mm; // 두 자리 설정
    }

    // WebSocket 연결 열기
    wsOpen();


    // 이동 경로를 안전하게 붙이는 함수
    function appendPath(baseUri, path) {
      // 경로가 '/'로 시작하지 않으면 '/'를 추가
      return `${baseUri}${path.startsWith('/') ? path : '/' + path}`;
    }

    // chatting 접속
    $('.go-chatting').on('click', function(e) {
        e.preventDefault();
        const chatRoomId = $(this).data("chatroom-id");

        // 현재 페이지의 URL을 가져옵니다.
        const fullUrl = window.location.href;

        // URL 객체를 생성하여 URL의 구성 요소를 분석합니다.
        const url = new URL(fullUrl);

        // origin 속성을 사용하여 프로토콜, 호스트 및 포트를 가져옵니다.
        const baseUri = url.origin;

        // 이동할 URI를 생성합니다.
        const moveUri = appendPath(baseUri, "moveChatting?roomNumber=" + chatRoomId);

        $.ajax({
            url : "message-read",
            type: "GET",
            data: {
                    chatRoomId : chatRoomId,
                    toUserId : $('#fromUserId').val() // 로그인 유저 아이디
                  },
            success: function(resp, status, xhr) {
                location.href = moveUri;
            },
            error: function(xhr, status, err) {
                console.error('에러 발생:', err);
                console.log(err);
                alert('요청이 실패했습니다. 다시 시도해 주세요.');
            }
        });
//        location.href = moveUri;
    });

    // 편집 버튼 눌렀을 때 select-box show
    $('#edit-btn').on('click', function(e) {
        let status = $('#edit-btn').text();
        if(status == '편집') {
            $('.select-box').css('display', 'inline');
            $('#getOut-chatroom, #check-span').css('display', 'block');
            $('.go-chatting').css('pointer-events', 'none');
            $('#edit-btn').text('취소');
        } else {
            e.preventDefault();
            $('#all-check').prop('checked', false);
            $('.select-box').prop('checked', false);
            $('.select-box, #getOut-chatroom, #check-span').css('display', 'none');
            $('.go-chatting').css('pointer-events', 'auto');
            $('#edit-btn').text('편집');
        }
    });

    // 전체 선택 버튼
    $('#all-check').on('change', function(e) {
        const chk = $('.select-box');

        if($(this).is(':checked')) {
              chk.prop('checked', true);
          } else {
              chk.prop('checked', false);
          }
    });

    // 채팅방 나가기 (checked)
    $('#getOut-chatroom').on('click', function(e) {
        e.preventDefault();

        let selectedValues = [];
        $('input[name="select-box"]:checked').each(function() {
            selectedValues.push($(this).data('chatroom-id'));
        });

        if(selectedValues.length === 0) {
            return;
        }

        $.ajax({
            url : "out-chatroom",
            type: "post",
            data: {selectedValues : selectedValues},
            success: function(resp, status, xhr) {
                alert('채팅방 나가기 완료되었습니다');

                // 현재 있는 채팅방을 나가기 했다면
                const urlParams = new URLSearchParams(window.location.search);
                const roomNumber = parseInt(urlParams.get('roomNumber')); // 'roomNumber' 파라미터 값을 정수로 변환
                $.each(selectedValues, function(idx, value) {
                    if(value == roomNumber){
                        window.close();
                        return;
                    }
                });

                location.reload();
            },
            error: function(xhr, status, err) {
                console.error('에러 발생:', err);
                console.log(err);
                alert('요청이 실패했습니다. 다시 시도해 주세요.');
            }
        });
    });

});
