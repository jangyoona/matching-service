$(function(){
    // 아이디 포커스
    $('#userPhone').focus();

    // 인풋창 포커스
    $('#userPhone').on('keyup', function(){
        if($('#userPhone').val().length == 11) {
            $('#userPw').focus();
        } else {
            $('#userPhone').focus();
        }

    });

    // 아이디 저장
    $('#id-save-checkbox').change(function(){
        if($('#id-save-checkbox').is(':checked')) {
            $('#id-save-checkbox').val('true');
        }
    });


    // 로그인
    $('.submit-btn').on('click', (e) => {

        e.preventDefault();
        e.stopPropagation();

        const id = $('#userPhone').val();
        const pw = $('#userPw').val();

        if(id.length == 0) {
            alert('아이디를 입력해 주세요');
            $('#userPhone').focus();
            return;
        } else if(id.length < 11) {
            alert('아이디 11자리를 입력해 주세요');
            $('#userPhone').focus();
            return;
        }

        if(pw.length == 0) {
            alert('비밀번호를 입력해 주세요');
            $('#userPw').focus();
        } else if(pw.length < 8) {
            alert('비밀번호는 8자~20자 내로 입력해 주세요');
            $('#userPw').focus();
        } else {
            isSocial(id, function(isSocialUser) {
                if (!isSocialUser) {
                    $('#login-form').submit();
                }
            });
        }
    });

    $('#kakao-btn').on('click', function(e) {

        if (!confirm('개인회원만 소셜 로그인이 가능합니다. 진행하시겠습니까?')){
            e.preventDefault();
            e.stopPropagation();
        }
    });

    function isSocial(id, callback) {
        $.ajax({
            url: '/userView/account/socialId-check',
            data: { userPhone : id },
            success: function(resp) {
                if (resp === true) {
                    location.href = '/userView/account/login?social';
                    callback(true); // 소셜 로그인 회원
                } else {
                    callback(false); // 일반 회원
                }
            },
            error: function(error) {
                console.error('로그인 실패');
                alert('로그인 실패. 다시 시도해 주세요');
            }
        });
    }
});
//$('#kakao-logout').on('click', function(e) {
//
//    $.ajax({
//        url : "logout/oauth/code/kakao",
//        data : 1,
//        success : function(result){
//            alert("test: 200 로그아웃 성공 / 401 카톡로그인 안한사람")
//            location.href = "/home";
//        },
//        error : function(err) {
//            alert('로그아웃이 실패하였습니다');
//        }
//    });
//});

//function wsOpen() {
//    const sessionId = localStorage.getItem('sessionId');
//    if (sessionId) {
//        const ws = new WebSocket("ws://" + location.host + "/chatting/" + 1);
////        wsEvt(ws); // 웹 소켓 이벤트 핸들러 호출
//    }
//}