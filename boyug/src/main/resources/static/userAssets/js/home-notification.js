$(function() {
    function commonAjax(url, data, callback) {
        $.ajax({
            url : url,
            data: data,
            success: function(resp) {
                callback(resp);
            },
            error: function(err) {
                console.error('에러 발생:', err);
                alert('요청이 실패했습니다. 다시 시도해 주세요.');
            }
        });
    }

    // 이동 경로를 안전하게 붙이는 함수
    function appendPath(baseUri, path) {
      // 경로가 '/'로 시작하지 않으면 '/'를 추가
      return `${baseUri}${path.startsWith('/') ? path : '/' + path}`;
    }

    // 알림창 채팅방 접속
    $('#nav-container-box').on('click', '.notification', function(e) {
        e.preventDefault();
        const message = $(this).data("chat-message");
        const chatRoomId = $(this).data("chatroom-id");

        if(message.includes("메세지")) {
            // 현재 페이지의 URL을 가져옵니다.
            const fullUrl = window.location.href;

            // URL 객체를 생성하여 URL의 구성 요소를 분석합니다.
            const url = new URL(fullUrl);

            // origin 속성을 사용하여 프로토콜, 호스트 및 포트를 가져옵니다.
            const baseUri = url.origin;

            // 이동할 URI를 생성합니다.
            const moveUri = appendPath(baseUri, "moveChatting?roomNumber=" + chatRoomId);

            const notificationId = $(this).data("notification-id");
            commonAjax("/notification/chat-read", { notificationId, chatRoomId }); // 알림, 메세지 읽음으로 변경

            const options = 'width=978, height=793, top=50, left=100, scrollbars=no, location=no, resizable=no';
            window.open(moveUri, '_black', options);

        } else if(message.includes("활동")) {
            commonAjax("/notification/read", notificationId, function(resp) {
                location.href = '/personal/personalHome';
            }); // 알림 읽음으로 변경
        }
    });


    $('#nav-container-box').on('click', '.notification-delete-btn', function(e) {
        const notificationId = $(this).data("notification-id");
        const $btn = $(this);

        commonAjax("/notification/delete", { notificationId : notificationId }, function(resp){
            $('#notification-tab').addClass('show');
            $btn.closest('.noti-container').remove();

        });
    });

    $('#nav-container-box').on('click', '.all-delete-btn', function(e) {
        e.preventDefault();

        if(confirm('알림을 전체삭제 하시겠습니까?')) {
            if($('#user-id').val() != undefined && $('#user-id').val() != null) {
                commonAjax("/notification/allDelete", { userId : $('#user-id').val() }, function(resp) {
                    alert('전체삭제 완료되었습니다');
                    $('.container-fluid #nav-container-box').load('/navbar');
                });
            }
        }
    });


// SSE
    let userId = $("#currentUserId").val();
    if(userId != null) {
        const eventSource = new EventSource("/notifications/" + userId);

        eventSource.addEventListener('chatNotification', function(event) {
            const data = event.data;
            const parseData = JSON.parse(data);

            if(parseData != '연결완료') {
                showToastNotification(parseData); // toast show

                // toast-header click -> chatRoom popUp show
                $('.popup').on('click', function() {
                    const notificationId = parseData.notificationId;
                    const chatRoomId = parseData.chatRoomId;

                    commonAjax("/notification/chat-read", { notificationId, chatRoomId }); // 알림, 메세지 읽음으로 변경

                    const options = 'width=978, height=720, top=50, left=100, scrollbars=no, location=no, resizable=no';
                    window.open("/moveChatting?roomNumber=" + encodeURIComponent(chatRoomId), '_black', options);
                });
            }
        }, false);

        eventSource.onerror = function(error) {
            console.error("SSE 연결 오류= ", error);
        };
    }

    function showToastNotification(data) {
        $('.toast-container').empty();
        let toastHtml = `<div class="toast" role="alert" aria-live="assertive" aria-atomic="true">
                            <a href="javascript:" class="popup">
                                <div class="toast-header" style="background-color:var(--bs-primary); color:white;">
                                <i class="fa-solid fa-comment" style="margin-right:0.5rem;"></i>
                                <strong class="me-auto">${data.fromUserName}님의 새 메시지</strong>
                                <small>${currentTime()}</small>
                                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                                </div>
                            </a>
                            <div class="toast-body">
                                ${data.message}
                            </div>
                        </div>`;
        $('.toast-container').append(toastHtml);
        var toastElement = document.querySelector('.toast-container .toast:last-child');
        var toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 5000 });
        toast.show();
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
// /SSE

    // 페이징 (스크롤) 초기 진입시 0으로 가져와서 click시 1부터 ~ ++
    let page = 1;
    $('#more-btn').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        commonAjax("/notification/more", { page: page }, function(resp) {
            if (resp != null) {
                  const notificationList = resp.map(noti => {
                      // noti.sendDate 값을 Date 객체로 변환
                      let dateFormat = new Date(noti.sendDate);

                      // 시간 포맷을 설정
                      let options = { hour: 'numeric', minute: 'numeric', hour12: true };

                      // 날짜 포맷팅
                      let formattedDate = new Intl.DateTimeFormat('ko-KR', options).format(dateFormat);

                      return `
                          <div class="noti-container" style="display:flex; border-bottom:1px solid gray;">
                              <a class="dropdown-item notification"
                                  data-chatroom-id="${noti.chatRoom.chatRoomId}"
                                  data-notification-id="${noti.notificationId}"
                                  style="font-size:1rem; margin:10px; cursor:pointer; color:white;">
                                  <i class="fa-solid fa-comments" style="margin-left:0.5rem;"></i> ${noti.message} |
                                  <span class="send-date">${formattedDate}</span>
                                  <input type="hidden" id="toUserId" value="${noti.fromUser.userId}"> <!-- 알림창에서는 to와 from을 반대로 해줘야함 -->
                              </a>
                              <span style="align-content:center; margin-right:1rem;">
                                  <i class="fa-solid fa-square-xmark notification-delete-btn"
                                     data-notification-id="${noti.notificationId}"
                                     style="color:orange; margin-left:0.5rem; cursor:pointer;"></i>
                              </span>
                          </div>
                      `;
            }).join('');

              page++;
              $('.noti-box').append(notificationList);
              $('#notification-tab').addClass('show');
          }

          // 추가 페이지 없으면 버튼 숨기기
          if (resp.length == 0) {
              $('#more-btn').hide();
          } else {
              $('#more-btn').show();
          }
      });
    });

// 알림 세션 설정
//var ws;
//
//function wsOpen() {
//    if (ws && ws.readyState === WebSocket.OPEN) {
//        console.log("WebSocket is already open.");
//        return;
//    }
//
////        const sessionId = localStorage.getItem('sessionId');
//    const loginUser = $('#currentUserId').val();
//    if(loginUser != undefined) {
////            if(!sessionId){
//            if(ws === null || ws === undefined || ws.readyState === WebSocket.CLOSED) {
//                ws = new WebSocket("ws://" + location.host + "/chatting/" + 1);
////                    localStorage.setItem('sessionId', loginUser);
//                wsEvt();
//            }
//
//    }
//}

//    function wsEvt() {
//        ws.onopen = function() {
//            console.log('WebSocket onopen start yo');
//        };
//
//        ws.onmessage = function(data) {
//            var msg = data.data;
//
//            if (msg != null && msg.trim() != '') {
//                try {
//                    var d = JSON.parse(msg);
//    //                if (d.type == "notification" && d.toUserId === $("#currentUserId").val()) { 테스트 다 하고 이걸로 바꾸렴
//                    if (d.type == "notification") {
//                        console.log("메세지 도착: " + d.message);
//
//                        const escapeMessage = escapeHtml(d.message);
//                        const escapeFromUserName = escapeHtml(d.fromUserName);
//                         // Toast 알림
//                        $('.toast-container').empty();
//                        let toastHtml = `<div class="toast" role="alert" aria-live="assertive" aria-atomic="true">
//                                            <div class="toast-header" style="background-color:var(--bs-primary); color:white;">
//                                                <i class="fa-solid fa-comment" style="margin-right:0.5rem;"></i>
//                                                <strong class="me-auto">${escapeFromUserName}님의 새 메시지</strong>
//                                                <small>${currentTime()}</small>
//                                                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
//                                            </div>
//                                            <div class="toast-body">
//                                                ${escapeMessage}
//                                            </div>
//                                        </div>`;
//                        $('.toast-container').append(toastHtml);
//                        var toastElement = document.querySelector('.toast-container .toast:last-child');
//                        var toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 5000 });
//                        toast.show();
//                    } else {
//                        console.error("Toast element not found.");
//                    }
//                } catch(error) {
//                    alert('알림 수신 중 오류가 발생하였습니다.');
//                    console.error("json parse or dom error! " + error);
////                    location.reload();
//                }
//            }
//        };
//
//        ws.onerror = function(event) {
//            console.error("WebSocket 오류 발생:", event);
//        };
//
//        ws.onclose = function(event) {
//            console.log("WebSocket 연결이 닫혔습니다.");
//            localStorage.removeItem('sessionId');
//        };
//    }
//    wsOpen();
});