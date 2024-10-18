
const currentUrl = window.location.href;
let currentPath = currentUrl.replace(/^https?:\/\/[^/]+/, '');

if (currentPath == "/myPage/boyug-request") {
    $('.side-nav-item').removeClass('active');
    $('.side-nav-item').eq(3).addClass('active');
}

if (currentPath == "/myPage/boyug-response") {
    $('.side-nav-item').removeClass('active');
    $('.side-nav-item').eq(4).addClass('active');
}

if (currentPath == "/myPage/boyug-matching") {
    $('.side-nav-item').removeClass('active');
    $('.side-nav-item').eq(5).addClass('active');
}

if (currentPath == "/myPage/boyug-my-session-list") {
    $('.side-nav-item').removeClass('active');
    $('.side-nav-item').eq(2).addClass('active');
}

if (currentPath == "/myPage/boyug-file-list") {
    $('.side-nav-item').removeClass('active');
    $('.side-nav-item').eq(1).addClass('active');
}


$('.close-nav-btn').on('click', (e) => {
  $('.side-nav-container').css('left', '-20%');
  $('.open-nav-btn').css('visibility', 'visible');
  $('.main-container').css('padding', '8rem 2rem 0 10rem');
});

$('.open-nav-btn').on('click', (e) => {
  $('.side-nav-container').css('left', '2%');
  $('.open-nav-btn').css('visibility', 'hidden');
  $('.main-container').css('padding', '8rem 2rem 0 16rem');
});

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
            $('#userAddr1').val(data.zonecode);
            $('#userAddr2').val(addr);
            $("#userAdd2").val(function(index, value) {
                    return value + extraAddr;
            });
//            $('#userAddr3').focus(); // 우편번호 + 주소 입력이 완료되었음으로 상세주소로 포커스 이동
            $('#modifyInfoForm').submit();
        }
    }).open();
}

$('.info-change-btn').on('click', (e) => {
    const idx = $(e.target).parent().data('index');

    if (idx == 1) {
        $('.nav-container').css('z-index', '0');
    } else if (idx == 3) {
        execDaumPostcode();
    } else if (idx == 7) {
        $('.nav-container').css('z-index', '0');
    } else {
        $(e.target).css('display', 'none');
        $('.info-modify-btn').eq(idx).css('display', 'inline');
        $('.info-cancel-btn').eq(idx).css('display', 'inline');
        $('.modify-input').eq(idx).css('display', 'block');
        $('.boyug-info-item span').eq(idx).css('display', 'none');
    }

});

$('.info-cancel-btn').on('click', (e) => {

    const idx = $(e.target).parent().data('index');
    $('.info-modify-btn').eq(idx).css('display', 'none');
    $('.info-cancel-btn').eq(idx).css('display', 'none');
    $('.modify-input').eq(idx).css('display', 'none');
    $('.boyug-info-item span').eq(idx).css('display', 'block');
    $('.info-change-btn').eq(idx).css('display', 'inline');

});

$('.close-modal').on('click', (e) => {
    $('.nav-container').css('z-index', '9999');
});

/*** Boyug My Info Start ***/
$('.phone-number').on('input', (e) => {
    let firstNum = $('.phone-number').eq(0).val();
    let secondNum = $('.phone-number').eq(1).val();
    let thirdNum = $('.phone-number').eq(2).val();

    if (firstNum > 3) {
        $('.phone-number').eq(0).val(firstNum.slice(0, 3));
    }
    if (secondNum > 4) {
        $('.phone-number').eq(1).val(secondNum.slice(0, 4));
    }
    if (thirdNum > 4) {
        $('.phone-number').eq(2).val(thirdNum.slice(0, 4));
    }
});

$('.take-auth-btn').on('click', (e) => {
    let phoneValues = $('.phone-number');

    if($('#phoneValue').val() >= 10){
        $.ajax({
            url : '/personal/send-sms',
            method : 'POST',
            data : 'userPhone=' + $('#phoneValue').val(),
            dataType : 'text',
            success : (response, status, xhr) => {
                if (response == 'true') {
                    alert('이미 회원가입된 번호입니다');
                    phoneValues.eq(0).val('');
                    phoneValues.eq(1).val('');
                    phoneValues.eq(2).val('');
                    phoneValues.eq(0).focus();
                    e.preventDefault();
                    e.stopPropagation();
                }
                if (response == 'false') alert('문자를 전송하였습니다');
            },
            error : (xhr, status, err) => {
                alert('send sms ajax fail');
            }
        });

    } else {
        alert('연락처 번호를 입력해주세요');
        e.preventDefault();
        e.stopPropagation();
    }

});

$('.confirm-auth-btn').on('click', (e) => {
    const authNumber = $('.auth-number');

    if (authNumber.val().length > 1) {

        $.ajax({
            url : '/userView/account/check-number',
            method : 'GET',
            data : 'number=' + authNumber.val(),
            dataType : 'text',
            success : (response, status, xhr) => {
                if (response == 'success') {
                    alert('확인되었습니다');
                    $('#phoneValue').val(
                        $('.phone-number').eq(0).val() + $('.phone-number').eq(1).val() + $('.phone-number').eq(2).val()
                    );
                }
                if (response == 'error') {
                    authNumber.val('');
                    authNumber.focus();
                    infoMsg.eq(2).css('opacity', '1');
                    infoMsg.eq(3).css('opacity', '0');
                    alert('인증번호가 맞지 않습니다. 인증번호를 다시 확인해주세요');
                }
            },
            error : (xhr, status, err) => {
                alert('확인 실패');
            }
        });

    } else {
        alert('인증번호를 입력해주세요');
        e.preventDefault();
        e.stopPropagation();
    }

});

$('.take-emailAuth-btn').on('click', (e) => {
    let regex = new RegExp('[a-z0-9]+@[a-z]+\.[a-z]{2,3}');
    const emailValue = $('#emailValue').val();

    if(regex.test(emailValue)){
        $.ajax({
            url : "send-email",
            method : "POST",
            data : "userEmail=" + emailValue,
            dataType : "text",
            success : (response, status, xhr) => {
                alert('인증번호 전송 완료. 이메일을 확인해주세요.');
            },
            error : (xhr, status, err) => {
                alert('인증번호 전송 실패. 다시 시도해주세요.');
            }
        });
    } else {
        alert('이메일 형식이 맞지 않습니다');
        e.preventDefault();
        e.stopPropagation();
    }
});

$('.confirm-emailAuth-btn').on('click', (e) => {
    console.log(1);
    const emailAuth = $('.emailAuth-number').val();
    const emailValue = $('#emailValue').val();

    if (emailAuth.length > 1) {
        $.ajax({
            url : "check-email",
            method : "POST",
            data : "emailAuth=" + emailAuth + "&emailValue=" + emailValue,
            dataType : "text",
            success : (response, status, xhr) => {
                if (response == "true") {
                    alert('인증이 확인되었습니다.');
                    $('#boyugEmail').val(emailValue);
                } else {
                    alert('인증번호가 맞지 않습니다. 다시 시도해주세요.');
                }
            },
            error : (xhr, status, err) => {
                alert('인증번호 전송 실패. 다시 시도해주세요.');
            }
        });
    } else {
        alert('인증번호를 입력해주세요');
        e.preventDefault();
        e.stopPropagation();
    }
});

$('.request-modify').on('click', (e) => {

    $('#modifyInfoForm').submit();
});
/*** Boyug My Info End ***/


