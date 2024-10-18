$(function(){

    // ajax function
    function commonAjax(url, method, data, successCallback) {
        $.ajax({
            url: url,
            method: method,
            data: data,
            success: function(result) {
                successCallback(result);
            },
            error: function(xhr, status, err) {
                alert('실행 중 오류가 발생하였습니다');
            }
        });
    }

    $('#userPhone').focus();

    // 가입 유무 검사
    $('#id-check-btn').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const userPhone = $('#userPhone').val();

        if(userPhone.length == 0 || userPhone.length < 11) {
            alert('휴대폰 번호 11자리를 입력해 주세요');
            $('#userPhone').focus();
            return;
        }
        const phoneRegex = /^01(0|1|[6-9])[0-9]{3,4}[0-9]{4}$/;
        if(!phoneRegex.test(userPhone)) {
            alert('전화번호 형식을 확인해 주세요');
            $('#userPhone').focus();
            return;
        }

        commonAjax("dup-check", "GET", { userPhone: userPhone }, function(result) {
            if(result == true)  {
                sendSms(userPhone);
            } else {
                alert('가입되지 않은 전화번호 입니다');
                $('#userPhone').val('');
                $('#userPhone').focus();
            }
        });
    });

    // 인증 문자 발송
    function sendSms(userPhone) {

        commonAjax("send-message", "GET", { userPhone : userPhone }, function(result) {
            if(result == "success")  {
                $('#id-check-btn').val('true');
                $('.sms-container').css('display', '');
                $('#smsCode').focus();

                const time = 60 * 3; // 제한시간 3분
                smsTimer(time, $('#sms-timer'));
            } else {
                alert('해당 번호는 서비스정지 또는 없는 번호이거나,\n현재 문자를 수신할 수 없는 번호입니다.');
                $('#id-check-btn').val('false');
                $('#userPhone').val('');
                $('#userPhone').focus();
            };

        });
    };

    // 인증번호 유효 체크
    $('#sms-check-btn').on('click', function(e) {
        const number = $('#smsCode').val();
        if(number.length < 4) {
            alert('인증번호 네 자리를 입력해 주세요');
            $('#smsCode').focus();
            return;
        }

        commonAjax("check-number", "GET", {number : number}, function(result) {
            if(result == 'success') {
                $('#sms-check-btn').val('true');

                $('.sms-container').addClass('hidden');
                $('.sms-container').fadeOut(400); // 500ms duration

                $('#id-check-ok').css('display', '');
                $('.dub-check-container').css({
                    border : '3px solid green',
                    borderRadius : '14px'
                });
                $('#id-ok').html('휴대폰 인증이 완료되었습니다.');
                $('#userPhone').attr('readonly', 'true');
                $('#id').val( $('#userPhone').val());
                $('#userPw').focus();

                $('.new-passwd').css('display', '');

            } else {
                alert('인증번호가 일치하지 않습니다.\n다시 시도해 주세요');
                $('#smsCode').focus();
            }
        });
    });

    // 비밀번호, 비밀번호 확인 움직임 체크
    $('#userPw-confirm').on('keyup', function() {
        const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
         if(!regex.test($('#userPw').val())){
             $('#pw-message2').html('비밀번호는 영문자와 숫자 특수문자 중 3가지 이상으로 조합하여 8자~20자 이내로 입력해 주세요');
         } else {
             $('#pw-message2').html('');
         }

        if($('#userPw').val() != $('#userPw-confirm').val()){
            $('#userPw-confirm').css('border', '1px solid red');
            $('#pw-message').html('(!) 비밀번호를 확인해 주세요');
        } else {
            $('#userPw-confirm').css('border', '');
            $('#pw-message').html('');
        }
    });

    // submit
    $('#submit-btn').on('click', function(e){

        e.preventDefault();
        e.stopPropagation();

        const userPw = $('#userPw').val();
        const userPhone = $('#userPhone').val();

        if($('#id-check-btn').val() == 'false') {
            alert('아이디 확인을 진행해 주세요');
            $('#userPhone').focus();
            return;
        }

        if($('#sms-check-btn').val() == 'false') {
            alert('휴대폰 인증을 진행해 주세요');
            $('#userPhone').focus();
            return;
        }

        if(userPw == '' || $('#userPw-confirm').val() == '') {
            alert('비밀번호를 입력해 주세요');
            $('#userPw').focus();
            return;
        }
        if(userPw != $('#userPw-confirm').val()) {
            alert('비밀번호가 일치하지 않습니다');
            $('#userPw').focus();
            return;
        }

        const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
        if(!regex.test(userPw)){
            alert('비밀번호는 영문자와 숫자 특수문자 중 3가지 이상으로 조합하여\n8자~20자 이내로 입력해 주세요');
            $('#userPw').focus();
            return;
        }

        commonAjax("reset-passwd", "POST", { userPhone, userPw }, function(result) {
            if(result == true)  {
                alert('비밀번호 재설정 완료되었습니다');
                location.href = 'login';
            } else {
                alert('실행 중 오류가 발생하였습니다\n다시 시도해 주세요');
                location.reload();
            }
        });
    });


    function smsTimer(duration, display) {

        let timer = duration, minutes, seconds;

        const interval = setInterval(() => {

            minutes = Math.floor(timer / 60);
            seconds = timer % 60;

            // 한자리 수 일때 앞에 0 붙게 설정
            seconds = seconds < 10 ? '0' + seconds : seconds;

            display.html(minutes + ':' + seconds);

            if(--timer < 0) {
                clearInterval(interval);
                setTimeout(() => {
                    $('.sms-container').css('display', 'none');

    //                $('#id-check-btn').val('false');
    //                $('#userPhone').focus();
                }, 3000);
                $('#time-over').css('color', 'red');
                $('#time-over').html('제한 시간이 종료되었습니다.<br>다시 시도해 주세요');
            }
        }, 1000);
    }

});