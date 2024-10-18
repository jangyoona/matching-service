// let els = document.getElementsByClassName('step');
// let steps = [];
// Array.prototype.forEach.call(els, (e) => {
//   steps.push(e);
//   e.addEventListener('click', (x) => {
//     progress(x.target.id);
//   });
// });

// function progress(stepNum) {
//   let p = stepNum * 30;
//   document.getElementsByClassName('percent')[0].style.width = `${p}%`;
//   steps.forEach((e) => {
//     if (e.id === stepNum) {
//       e.classList.add('selected');
//       e.classList.remove('completed');
//     }
//     if (e.id < stepNum) {
//       e.classList.add('completed');
//     }
//     if (e.id > stepNum) {
//       e.classList.remove('selected', 'completed');
//     }
//   });
// }
$('.container-fluid #nav-container-box').load('/navbar');
const stepOne = $('.login-stepOne-container');
const stepTwo = $('.login-stepTwo-container');
const stepThree = $('.login-stepThree-container');
const stepFour = $('.login-stepFour-container');
const stepFive = $('.login-stepFive-container');
const loginSuccess = $('.login-success-container');
const stepCircle = $('.step');
const stepCircleIcon = $('.step i');

const nextBtn = $('.login-next-btn');
const previousBtn = $('.login-previous-btn');

/* Step One */
const phoneValues = $('.phone-number-container input');
const infoMsg = $('.message-container');
const authNumber = $('.stepOne-auth-number input');
const pwValue = $('.pw-input');
const pwCheckValue = $('.pw-check-input');
/* Step One */

const warningMsg = $('.warning-text');

/* Step Two */
const infoValues = $('.stepTwo-info-container .info-item-container input');
const selectValues = $('.stepTwo-info-container .info-item-container select');
/* Step Two */

/* Step Three */
const userAddr = $('.addr');
/* Step Three */

/* Step Four */
const protectorNumber = $('.stepFive-info-container div input');
/* Step Four */

function progress(stepNum, isNext) {
  const progressBarWidth = $('.custom-progress').width();
  let p = 0;
  if (isNext === 1) {
    p = (stepNum + 1) * (progressBarWidth / 40);
    stepCircle.eq(stepNum).addClass('completed');
    stepCircle.eq(stepNum + 1).addClass('selected');
    stepCircleIcon.eq(stepNum).css('visibility', 'visible');
  } else {
    p = (stepNum - 1) * (progressBarWidth / 40);
    stepCircle.eq(stepNum).removeClass('selected');
    stepCircleIcon.eq(stepNum - 1).css('visibility', 'hidden');
    stepCircle.eq(stepNum - 1).removeClass('completed');
    stepCircle.eq(stepNum - 1).addClass('selected');
  }

  $('.custom-percent').css("width", `${p}%`);
}

function checkStepOne() {
  // 0 ~ 3
  let warningArr = [];
  if (phoneValues.eq(0).val().length < 3 ||
        phoneValues.eq(1).val().length < 4 ||
        phoneValues.eq(2).val().length < 4) {

          warningArr.push(0);
  }

  if (authNumber.val().length < 4) warningArr.push(1);
  if (pwValue.val().length < 8 ||
      !(/[\{\}\[\]\/?.,;:|\)*~`!^\-_+<>@\#$%&\\\=\(\'\"]/g.test(pwValue))) {
      warningArr.push(2);
  }
  if (pwValue.val() != pwCheckValue.val()) warningArr.push(3);

  return warningArr;
}

function checkStepTwo() {
  // 4 ~ 7
  let warningArr = [];
  if (infoValues.eq(0).val().length < 2 ||
      /[0-9]/.test(infoValues.eq(0).val())) warningArr.push(4);

  if (selectValues.eq(0).val() == null) warningArr.push(5);
  if (selectValues.eq(1).val() == null) warningArr.push(5);
  if (selectValues.eq(2).val() == null) warningArr.push(5);

  if (selectValues.eq(3).val() === '선택' ) warningArr.push(6);

  if (selectValues.eq(4).val() === '선택') warningArr.push(7);

  return warningArr;
}

function checkStepThree() {
  // 8 ~ 10
  let warningArr = [];
  if (userAddr.eq(0).val().length < 1) warningArr.push(8);

  if (userAddr.eq(1).val().length < 1) warningArr.push(9);

  if (userAddr.eq(2).val().length < 1) warningArr.push(10);

  return warningArr;
}


nextBtn.on('click', (e) => {
  const index = $(e.target).parent().data('index');

  switch (index) {
    case 0 :
      let warningArrOne = checkStepOne();
      if (warningArrOne.length != 0) {
        warningArrOne.forEach((v, i) => {
          warningMsg.eq(v).css('visibility', 'visible');
        });

        e.preventDefault();
        e.stopPropagation();
      } else {
        stepOne.css('opacity', '0');
        stepOne.css('z-index', '0');
        stepTwo.css('opacity', '1');
        progress(index, 1);
        for (let i = 0; i < 4; i++) {
          warningMsg.eq(i).css('visibility', 'hidden');
        }
      }

//       stepOne.css('opacity', '0');
//       stepOne.css('z-index', '0');
//       stepTwo.css('opacity', '1');
//       progress(index, 1);
      break;
    case 1 :
      let warningArrTwo = checkStepTwo();
      if (warningArrTwo.length != 0) {
        warningArrTwo.forEach(v => {
          // 2 ~ 5
          warningMsg.eq(v).css('visibility', 'visible');
        });
        e.preventDefault();
        e.stopPropagation();
      } else {
        stepTwo.css('opacity', '0');
        stepTwo.css('z-index', '0');
        stepThree.css('opacity', '1');
        progress(index, 1);
        for (let i = 2; i <= 5; i++) {
          warningMsg.eq(i).css('visibility', 'hidden');
        }
      }

//       stepTwo.css('opacity', '0');
//         stepTwo.css('z-index', '0');
//         stepThree.css('opacity', '1');
//         progress(index, 1);
      break;
    case 2 :
      let warningArrThree = checkStepThree();
      if (warningArrThree.length != 0) {
        warningArrThree.forEach(v => {
          warningMsg.eq(v).css('visibility', 'visible');
        });
        e.preventDefault();
        e.stopPropagation();
      } else {
        stepThree.css('opacity', '0');
        stepThree.css('z-index', '0');
        stepFour.css('opacity', '1');
        progress(index, 1);
        for (let i = 6; i <= 8; i++) {
          warningMsg.eq(i).css('visibility', 'hidden');
        }
      }

//       stepThree.css('opacity', '0');
//       stepThree.css('z-index', '0');
//       stepFour.css('opacity', '1');
//       progress(index, 1);
      break;
    case 3 :
      stepFour.css('opacity', '0');
      stepFour.css('z-index', '0');
      stepFive.css('opacity', '1');
      progress(index, 1);
      break;
  }

});

previousBtn.on('click', (e) => {
  const index = $(e.target).parent().data('index');

  switch (index) {
    case 1 :
      stepTwo.css('opacity', '0');
      stepOne.css('opacity', '1');
      stepOne.css('z-index', '6');
      progress(index, 0);
      break;
    case 2 :
      stepThree.css('opacity', '0');
      stepTwo.css('opacity', '1');
      stepTwo.css('z-index', '5');
      progress(index, 0);
      break;
    case 3 :
      stepFour.css('opacity', '0');
      stepThree.css('opacity', '1');
      stepThree.css('z-index', '4');
      progress(index, 0);
      break;
    case 4 :
      stepFive.css('opacity', '0');
      stepFour.css('opacity', '1');
      stepFour.css('z-index', '3');
      progress(index, 0);
      break;
  }

});

$('.login-submit-btn').on('click', (e) => {
  const index = $(e.target).parent().data('index');
  loginSuccess.css('opacity', '1');
  stepFive.css('opacity', '0');
  stepFive.css('z-index', '0');

  stepCircle.eq(index).addClass('completed');
  stepCircle.eq(index + 1).addClass('selected');
  stepCircleIcon.eq(index).css('visibility', 'visible');

  // stepCircle.eq(stepNum).addClass('completed');
  // stepCircle.eq(stepNum + 1).addClass('selected');
  // stepCircleIcon.eq(stepNum).css('visibility', 'visible');

  // progress(index, 1);
  // let isEmpty = true;
  // if (protectorNumber.eq(0).val().length === 3 &&
  //     protectorNumber.eq(1).val().length === 4 &&
  //     protectorNumber.eq(2).val().length === 4) {

  //         isEmpty = false;
  // }

  // if (isEmpty) {
  //   warningMsg.eq(9).css('visibility', 'visible');
  //   e.preventDefault();
  //   e.stopPropagation();
  // } else {
  //   loginSuccess.css('opacity', '1');
  //   stepFour.css('opacity', '0');
  //   stepFour.css('z-index', '0');

  //   stepCircle.eq(index).addClass('completed');
  //   stepCircle.eq(index + 1).addClass('selected');
  //   stepCircleIcon.eq(index).css('visibility', 'visible');
  // }
});

protectorNumber.on('input', (e) => {
    if (protectorNumber.eq(0).val().length > 3) {
        protectorNumber.eq(0).val(protectorNumber.eq(0).val().slice(0, 3));
    }

    if (protectorNumber.eq(1).val().length > 4 || protectorNumber.eq(2).val().length > 4) {
        protectorNumber.eq(1).val(protectorNumber.eq(1).val().slice(0, 4));
        protectorNumber.eq(2).val(protectorNumber.eq(2).val().slice(0, 4));
    }

    if (protectorNumber.eq(0).val().length === 3 &&
        protectorNumber.eq(1).val().length === 4 &&
        protectorNumber.eq(2).val().length === 4) {

        const realProtectorNumber = $('#protectorPhone');
        realProtectorNumber.val(protectorNumber.eq(0).val() + protectorNumber.eq(1).val() + protectorNumber.eq(2).val());
    }
});

/*** Step One JS Start ***/
infoMsg.css('opacity', '0');
infoMsg.eq(0).css('opacity', '1');

phoneValues.on('input', (e) => {

  if (phoneValues.eq(0).val().length > 3) {
    phoneValues.eq(0).val(phoneValues.eq(0).val().slice(0, 3));
  }

  if (phoneValues.eq(1).val().length > 4 || phoneValues.eq(2).val().length > 4) {
    phoneValues.eq(1).val(phoneValues.eq(1).val().slice(0, 4));
    phoneValues.eq(2).val(phoneValues.eq(2).val().slice(0, 4));
  }

  let isEmpty = true;
  if (phoneValues.eq(0).val().length === 3 &&
        phoneValues.eq(1).val().length === 4 &&
        phoneValues.eq(2).val().length === 4) {

          isEmpty = false;

        const phoneNum = $('#userPhone');
        phoneNum.val(phoneValues.eq(0).val() + phoneValues.eq(1).val() + phoneValues.eq(2).val());
  }
  if (isEmpty === false) {
    infoMsg.eq(0).css('opacity', '0');
    infoMsg.eq(1).css('opacity', '1');
  } else {
    infoMsg.eq(0).css('opacity', '1');
    infoMsg.eq(1).css('opacity', '0');
  }

});

$('.take-auth-btn').on('click', () => {
  infoMsg.eq(1).css('opacity', '0');
  infoMsg.eq(2).css('opacity', '1');
});

$('.confirm-auth-btn').on('click', () => {
  infoMsg.eq(2).css('opacity', '0');
  infoMsg.eq(3).css('opacity', '1');
});

$('.pw-input').on('input', (e) => {
  if ($('.pw-input').val() >= 8) {
    infoMsg.eq(3).css('opacity', '0');
    infoMsg.eq(4).css('opacity', '1');
  }
});

$('.see-pw').on('click', (e) => {
  $('.pw-input').prop('type', 'text');
});
$('.hide-pw').on('click', (e) => {
  $('.pw-input').prop('type', 'password');
});

$('.see-check-pw').on('click', (e) => {
  $('.pw-check-input').prop('type', 'text');
});
$('.hide-check-pw').on('click', (e) => {
  $('.pw-check-input').prop('type', 'password');
});
/*** Step One JS End ***/

/*** Step Two JS Start ***/
infoMsg.eq(5).css('opacity', '1');

const birthYearEl = $('#birth-year');
const birthMonthEl = $('#birth-month');
const birthDayEl = $('#birth-day');
// option 목록 생성 여부 확인
let isYearOptionExisted = false;
let isMonthOptionExisted = false;
let isDayOptionExisted = false;
birthYearEl.on('focus', function () {
  // year 목록 생성되지 않았을 때 (최초 클릭 시)
  if(!isYearOptionExisted) {
    isYearOptionExisted = true
    for(let i = 1940; i <= 2022; i++) {
      // option element 생성
      const YearOption = document.createElement('option')
      YearOption.setAttribute('value', i)
      YearOption.innerText = i
      // birthYearEl의 자식 요소로 추가
      this.appendChild(YearOption);
    }
  }
});
birthMonthEl.on('focus', function () {
  if(!isMonthOptionExisted) {
    isMonthOptionExisted = true
    for(let i = 1; i <= 12; i++) {
      // option element 생성
      const monthOption = document.createElement('option');
      let idx = i < 10 ? '0' + i : i;
      monthOption.setAttribute('value', idx);
      monthOption.innerText = idx;
      // birthYearEl의 자식 요소로 추가
      this.appendChild(monthOption);
    }
  }
});
birthDayEl.on('focus', function () {
  // year 목록 생성되지 않았을 때 (최초 클릭 시)
  if(!isDayOptionExisted) {
    isDayOptionExisted = true
    for(let i = 1; i <= 31; i++) {
      // option element 생성
      const dayOption = document.createElement('option');
      let idx = i < 10 ? '0' + i : i;
      dayOption.setAttribute('value', idx);
      dayOption.innerText = idx;
      // birthYearEl의 자식 요소로 추가
      this.appendChild(dayOption);
    }
  }
});
let yearValue = '';
let monthValue = '';
let dayValue = '';
let userBirth = $('#userBirth');
function insertValue(el, val) {
  if (el === 'year') yearValue = val;
  if (el === 'month') monthValue = val;
  if (el === 'day') dayValue = val;
  const birth = yearValue + '-' + monthValue + '-' + dayValue;
  userBirth.val(birth);
}

birthYearEl.on('change', (e) => {
  insertValue('year', birthYearEl.val());
});
birthMonthEl.on('change', (e) => {
  insertValue('month', birthMonthEl.val());
});
birthDayEl.on('change', (e) => {
  insertValue('day', birthDayEl.val());
});

infoValues.on('input', (e) => {
  if (infoValues.eq(0).val().length >= 2) {
    infoMsg.eq(5).css('opacity', '0');
    infoMsg.eq(6).css('opacity', '1');
  }

  // if (infoValues.eq(1).val().length >= 1) {
  //   infoMsg.eq(4).css('opacity', '0');
  //   infoMsg.eq(5).css('opacity', '1');
  // }
});

selectValues.on('change', (e) => {
  if (selectValues.eq(0).val() != null && selectValues.eq(1).val() != null && selectValues.eq(2).val() != null) {
    infoMsg.eq(6).css('opacity', '0');
    infoMsg.eq(7).css('opacity', '1');
  }

  if (selectValues.eq(3).val() != '선택') {
    infoMsg.eq(7).css('opacity', '0');
    infoMsg.eq(8).css('opacity', '1');
  }

  if (selectValues.eq(4).val() != '선택') {
    infoMsg.eq(8).css('opacity', '0');
    infoMsg.eq(9).css('opacity', '1');
  }
});
/*** Step Two JS End ***/

/*** Step Three JS Start ***/
infoMsg.eq(9).css('opacity', '1');

userAddr.on('input', (e) => {
  if (userAddr.eq(0).val().length > 0) {
    infoMsg.eq(9).css('opacity', '0');
    infoMsg.eq(10).css('opacity', '1');
  }
});
/*** Step Three JS End ***/


/*** File Upload Start ***/
// 소셜 로그인 회원가입으로 넘어오는 이미지가 있다면
if('${oAuth2User.images[0].imgOriginName}' !== '') {
    $('#selected-image').css('opacity', '1');
    $('.image-info-text').addClass('hide');
}

$('#file-selector-btn').on('change', function(e) {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
        const fileReader = new FileReader();
        fileReader.onload = function(event) {
             $('#selected-image').attr('src', event.target.result);
             // $('#selected-image')
        }
        fileReader.readAsDataURL(selectedFile);
    }

    $('#selected-image').css('opacity', '1');
    $('.image-info-text').addClass('hide');
});
/*** File Upload End ***/

/*** Address API Start ***/
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
            $('#userAddr3').focus(); // 우편번호 + 주소 입력이 완료되었음으로 상세주소로 포커스 이동
        }
    }).open();
}

$('.find-addr-btn').on('click', (e) => {
    execDaumPostcode();
});

/*** Address API End ***/

/*** Ajax Function Start ***/
function ajax (urlParam, methodParam, dataParam, dataTypeParam, successF, errorF) {
    $.ajax({
        url : urlParam,
        method : methodParam,
        data : dataParam,
        dataType : dataTypeParam,
        success : (response, status, xhr) => {
            successF();
        },
        error : (xhr, status, err) => {
            errorF();
        }
    });
}
/*** Ajax Function End ***/

/*** Login Submit Ajax Start ***/
$('.login-submit-btn').on('click', (e) => {

    const registerForm = $('#personal-register-form');
    const formData = new FormData(registerForm[0]);

    $.ajax({

        url : registerForm.attr('action'),
        method : registerForm.attr('method'),
        data : formData,
        processData: false,
        contentType: false,
        dataType : "text",
        success : (response, status, xhr) => {
            alert("회원가입 성공");
        },
        error : (xhr, status, err) => {
            alert('ajax fail');
        }

    });

});
/*** Login Submit Ajax End ***/

/*** 아이디 중복 검사 & 인증번호 Ajax Start ***/
$('.take-auth-btn').on('click', (e) => {

    if($('#userPhone').val() >= 10){

//        const success = () => {
//            if (response == 'true') {
//                alert('이미 회원가입된 번호입니다');
//                phoneValues.eq(1).val('');
//                phoneValues.eq(2).val('');
//                phoneValues.eq(1).focus();
//                infoMsg.eq(0).css('opacity', '1');
//                infoMsg.eq(2).css('opacity', '0');
//                e.preventDefault();
//                e.stopPropagation();
//            }
//            if (response == 'false') alert('문자를 전송하였습니다');
//        }
//
//        const error = () => {
//            alert('send sms ajax fail');
//        }
//        ajax('/personal/send-sms', 'POST', `userPhone=${$('#userPhone').val()}`, 'text', success(), error());

        $.ajax({
            url : '/personal/send-sms',
            method : 'POST',
            data : 'userPhone=' + $('#userPhone').val(),
            dataType : 'text',
            success : (response, status, xhr) => {
                if (response == 'true') {
                    alert('이미 회원가입된 번호입니다');
                    phoneValues.eq(1).val('');
                    phoneValues.eq(2).val('');
                    phoneValues.eq(1).focus();
                    infoMsg.eq(0).css('opacity', '1');
                    infoMsg.eq(2).css('opacity', '0');
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
/*** 아이디 중복 검사 & 인증번호 전송 Ajax End ***/

/*** 인증번호 검사 Ajax Start ***/
$('.confirm-auth-btn').on('click', (e) => {

    if (authNumber.val().length > 1) {

        $.ajax({
            url : '/userView/account/check-number',
            method : 'GET',
            data : 'number=' + authNumber.val(),
            dataType : 'text',
            success : (response, status, xhr) => {
                if (response == 'success') alert('확인되었습니다');
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
/*** 인증번호 검사 Ajax End ***/

