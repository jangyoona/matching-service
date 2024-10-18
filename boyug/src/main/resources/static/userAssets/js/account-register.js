// 생년월일 인풋창 하나로 - 자동으로 입력하게끔 해서 하면 좋을거 같은뎅 아래 블로그 참고
// https://velog.io/@hi_young/input-%EB%82%A0%EC%A7%9C%EC%9E%90%EB%8F%99-%ED%95%98%EC%9D%B4%ED%94%88-%EC%88%AB%EC%9E%90%EB%A7%8C-%EC%9E%85%EB%A0%A5

$('.container-fluid #nav-container-box').load('/navbar');
$('#privacy-container').load('/userView/account/privacy-policy');

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

// 주소 api
// $('#zipp_btn').on('click', function(e) {
function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 팝업을 통한 검색 결과 항목 클릭 시 실행
            let addr = ''; // 주소_결과값이 없을 경우 공백
            let extraAddr = ''; // 참고항목

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소를 선택
                addr = data.roadAddress;
            } else { // 지번 주소를 선택
                addr = data.jibunAddress;
            }

            if(data.userSelectedType === 'R'){
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
            } else {
              $('UserAdd1').val('');
            }
          // 선택된 우편번호와 주소 정보를 input 박스에 넣는다.
            $('#zipp_code_id').val(data.zonecode);
            $('#UserAdd1').val(addr);
            $("#UserAdd1").val(function(index, value) {
                    return value + extraAddr;
            });
            $('#UserAdd2').focus(); // 우편번호 + 주소 입력이 완료되었음으로 상세주소로 포커스 이동
        }
    }).open();
}
// });

$('#userPhone').focus();

// 이메일 고르면 인풋창에 옮겨주기
$('#email-domain').on('change', function() {

    $('#domain').val('');
    
    const domain = $('#email-domain').val();
    $('#domain').val(domain);

    if($('#domain').val() != '') {
        $('#domain').attr('readonly', true);
    } else {
        $('#domain').attr('readonly', false);
    }
});

// 아이디 중복검사
$('#id-check-btn').on('click', function(e) {
    e.preventDefault();
    e.stopPropagation();

    const userPhone = $('#userPhone').val().trim();

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
        if(result == false)  {
            alert('사용 가능한 아이디입니다');
            $('#id-check-btn').val('true');
            sendSms(userPhone);
//                $('#userPw').focus();
            // $('#userPhone').css('border', '2px solid green');
        } else {
            alert('중복된 아이디입니다');
            $('#userPhone').val('');
            $('#userPhone').focus();
        }
    });
});

// 인증 문자 발송
let timeoutId;
function sendSms(userPhone) {

    commonAjax("send-message", "GET", { userPhone : userPhone }, function(result) {
        if(result == "success")  {
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
            // setTimeout clear
            if (timeoutId) {
                clearTimeout(timeoutId);
            }

            $('#sms-check-btn').val('true');

            // $('.sms-container').css('display', 'none');
            // $('#id-check-btn').css('display', 'none');

            $('.sms-container').addClass('hidden');
            $('.sms-container').fadeOut(400); // 500ms duration

            $('#id-check-ok').css('display', '');
            $('.dub-check-container').css({
                border : '3px solid green',
                borderRadius : '14px'
            });
            $('#id-ok').html('휴대폰 인증이 완료되었습니다.');
            $('#userPhone').attr('readonly', 'true');
            $('#userPw').focus();
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

$('#domain').on('change', function() {
    const regex = /^[a-zA-Z0-9]+\.[a-zA-Z]{2,}$/
    if(!regex.test($('#domain').val())){
        alert('유효하지 않은 이메일 형식입니다');
        $('#domain').focus();
    }
});

$('#boyugEmail').on('change', function() {
    const regex = /^[a-zA-Z0-9]+$/;
    if(!regex.test($('#boyugEmail').val())){
        alert('이메일은 영문자, 숫자만 입력해 주세요');
        $('#boyugEmail').focus();
    }
});

// 프로필 이미지 미리보기
$('#attach-profile').on('change', function(event) {
        const selectedFile = event.target.files[0];
        if (selectedFile) {
          const fileReader = new FileReader();
          fileReader.onload = function(e) {
            $('#selected-image').attr('src', e.target.result);
            // $('#selected-image')
          };
          fileReader.readAsDataURL(selectedFile);
        }
})

// 유효성 검사 실패시 해당 위치로 scroll
function targetScroll(id) {
    const result = id.attr('id');
    const element = document.querySelector('#' + result);
    const elementRect = element.getBoundingClientRect();
    const elementMidpoint = elementRect.top + window.scrollY + elementRect.height / 2;
    // 해당 위치로 스크롤
    window.scrollTo({
        top: elementMidpoint - (window.innerHeight / 2),
        behavior: 'smooth'
    });
}

// 개인정보 이용동의 체크이벤트
$('#privacy-policy').on('change', function() {
    const isChecked = $(this).is(':checked');
    $('#privacy-policy').val(isChecked); // 체크되면 true, 해제되면 false
});

// 보육유저 회원가입
$('#submit-btn').on('click', function(e){

    e.preventDefault();
    e.stopPropagation();

    if($('#id-check-btn').val() == 'false') {
        alert('아이디 중복체크를 진행해 주세요');
        $('#userPhone').focus();
        targetScroll($('#id-check-btn'));
        return;
    }

    if($('#sms-check-btn').val() == 'false') {
        alert('휴대폰 인증을 진행해 주세요');
        $('#userPhone').focus();
        targetScroll($('#userPhone'));
        return;
    }


    if($('#userPw').val() == '' || $('#userPw-confirm').val() == '') {
        alert('비밀번호를 입력해 주세요');
        $('#userPw').focus();
        targetScroll($('#userPw'));
        return;
    }
    if($('#userPw').val() != $('#userPw-confirm').val()) {
        alert('비밀번호가 일치하지 않습니다');
        $('#userPw').focus();
        targetScroll($('#userPw'));
        return;
    }

    const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
    if(!pwRegex.test($('#userPw').val())){
        alert('비밀번호는 영문자와 숫자 특수문자 중 3가지 이상으로 조합하여\n8자~20자 이내로 입력해 주세요');
        $('#userPw').focus();
        targetScroll($('#userPw'));
        return;
    }

    if($('#userName').val().length == 0) {
        alert('이름을 입력해 주세요');
        $('#userName').focus();
        targetScroll($('#userName'));
        return;
    }
    const nameRegex = /^[가-힣]+$/;
    if(!nameRegex.test($('#userName').val())) {
        alert('담당자명을 확인해 주세요');
        targetScroll($('#userName'));
        return;
    }

    if($('#boyugUserName').val().length == 0) {
        alert('센터명을 입력해 주세요');
        $('#boyugUserName').focus();
        targetScroll($('#boyugUserName'));
        return;
    }

    if($('#boyugChildNum').val() < 0) {
        alert('보육원 인원은 1 이하로 지정할 수 없습니다');
        $('#boyugChildNum').focus();
        targetScroll($('#boyugChildNum'));
        return;
    }

    if($('#zipp_code_id').val().length == 0) {
        alert('우편번호 찾기를 진행해 주세요');
        targetScroll($('#zipp_btn'));
        return;
    }

    if($('#UserAdd2').val().length == 0) {
        if(confirm('나머지 주소가 미입력 상태입니다. 진행하시겠습니까?')) {
        } else {
            $('#UserAddr2').focus();
            targetScroll($('#UserAddr2'));
            return;
        }
    }

    if($('#boyugEmail').val().length == 0 || $('#domain').val().length == 0) {
        alert('이메일을 입력해 주세요');
        $('#boyugEmail').focus();
        targetScroll($('#boyugEmail'));
        return;
    }

    const emailRegex = /^[a-zA-Z0-9]+$/;
    if(!emailRegex.test($('#boyugEmail').val())){
        alert('이메일은 영문자, 숫자만 입력해 주세요');
        $('#boyugEmail').focus();
        targetScroll($('#boyugEmail'));
        return;
    }

    const domainRegex = /^[a-zA-Z0-9]+\.[a-zA-Z]{2,}$/
    if(!domainRegex.test($('#domain').val())){
        alert('유효하지 않은 이메일 형식입니다');
        $('#domain').focus();
        targetScroll($('#domain'));
    }

    if($('#boyugChildNum').val() < 0) {
        alert('보육원 인원을 입력해 주세요');
        $('#boyugChildNum').focus();
        targetScroll($('#boyugChildNum'));
        return;
    }

    if($('#attach-profile')[0].files.length === 0) {
        alert('프로필 이미지를 선택해 주세요');
        $('#attach-profile').focus();
        targetScroll($('#attach-profile'));
        return;
    }

    if($('#attach')[0].files.length === 0) {
        alert('업체 관련 서류를 첨부해 주세요');
        $('#attach').focus();
        targetScroll($('#attach'));
        return;
    }

    if (!$('#privacy-policy').is(':checked')) {
        alert('필수 항목에 대한 수집 및 이용 동의를 거부하실 경우,\n회원가입 진행이 어렵습니다');
        return;
    }
    
    $('#register-form').submit();

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
            timeoutId = setTimeout(() => {
                $('.sms-container').css('display', 'none');
//                $('#id-check-btn').val('false');
//                $('#userPhone').val('');
//                $('#userPhone').focus('');
            }, 3000);
            $('#time-over').css('color', 'red');
            $('#time-over').html('제한 시간이 종료되었습니다.<br>다시 시도해 주세요');
        }
    }, 1000);
}