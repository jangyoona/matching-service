
$(() => {
//    $('.container-fluid #nav-container-box').load('/navbar');
    $('#nav-container-box').load('/navbar');
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
                $('#userAddr1-view').text(data.zonecode);
                $('#userAddr1').val(data.zonecode);
                $('#userAddr2-view').text(addr);
                $('#userAddr2').val(addr);
                $("#userAdd2").val(function(index, value) {
                        return value + extraAddr;
                });
                // $('#userAddr3').focus(); // 우편번호 + 주소 입력이 완료되었음으로 상세주소로 포커스 이동
                // 여기서 Addr 수정하는 Ajax 날려야됨!!!!!!!!
                modifyMyInfo(-1);
            }
        }).open();
    }
    /*** Address API End ***/

    const container = $('.main-container');
    const mainItem = $(".main-item");
    const mainContainer = $('.main-item-container');
    const mainContainerDiv = $('.main-item-container div');
    const modalContainer = $('.modal-container');


    /*** Main-Item-Container Click Event Start ***/
    mainItem.on('click', (e) => {

        $(".nav-container").css('padding', '0.5rem 1rem 0.5rem 2rem');
        mainItem.removeClass('item-active');
        mainItem.css('padding', '1rem');
        mainContainerDiv.css('padding', '1rem');
        $(e.target).addClass('item-active');
        mainContainer.css('bottom', '-5%');
        mainContainer.css('padding', '1rem');

//        previousBtn.css('opacity', '1');
//        nextBtn.css('opacity', '1');

        const idx = $(e.target).data('index');

        $.ajax({
            url : "request-modal",
            method : "GET",
            data : "index=" + idx,
            dataType : "text",
            success : (response, status, xhr) => {
                if (idx == 3) {
                    modalContainer.load(response, () => {
                        $('.mypage-session-module').load("myPage/boyug-to-personal");
                    });
                } else {
                    modalContainer.load(response);
                }
            },
            error : (xhr, status, err) => {
                alert('fail');
            }
        });

    });
    /*** Main-Item-Container Click Event End ***/



    /*** MyPage JS Start ***/
    const infoText = $('.info-text');
    const infoModifyInput = $('.info-modify-input');
    const infoModifyBtn = $('.info-modify-btn');
    const infoChangeBtnContainer = $('.info-change-btn-container');
    const infoCancelBtn = $('.info-cancel-btn');
    const changeBtn = $('.info-change-btn');

    /*** 정보 수정 Ajax Start ***/
    function modifyMyInfo(idx) {
        switch (idx) {
            case -1 :
                break;
            case 0 :
                // 유효성 검사
                // 이름 0
                // <input class="info-modify-input user-name" type="text" name="userName" th:value="${ user.userName }">
                if ($('.user-name').val().length < 2 || /[0-9]/.test($('.user-name').val())) {
                    alert('이름을 다시 확인해주세요');
                    return;
                }
                break;
            case 1 :
                // 본인번호 1
                // <input type="hidden" name="userPhone" class="modify-phone" th:value="${user.userPhone}">
                if ($('.modify-phone').eq(3).val() < 10) {
                    alert('개인 연락처를 다시 확인해주세요');
                    return;
                }
                // 인증번호 1
                // <input type="number" class="modify-auth-number" data-auth="false">
                if ($('.modify-auth-number').data('auth') == false) {
                    alert('연락처 인증을 해주세요');
                    return;
                }
                break;
            case 2 :
                // 생년월일 2
                // <input type="hidden" name="birthDay" id="userBirth" th:value="${#dates.format(userDetail.userBirth, 'yyyy-MM-dd')}">
                if ($('#userBirth').val().length < 8) {
                    alert('생년월일을 다시 확인해주세요');
                    return;
                }
                break;
            case 5 :
                // 건강상태 5
                // <input type="hidden" class="health-value" name="userHealth" th:value="${userDetail.userHealth}">
                if ($('.health-value').val() == '선택') {
                    alert('건강상태를 다시 확인해주세요');
                    return;
                }
                break;
            case 8 :
                // Addr3 8
                // <input class="info-modify-input addr3" type="text" name="userAddr3" th:value="${ user.userAddr3 }">
                if ($('.addr3').val().length < 1) {
                    alert('상세주소를 다시 확인해주세요');
                    return;
                }
                break;
        }

        const modifyMyInfoForm = $('#modifyMyInfoForm');
        const formData = new FormData(modifyMyInfoForm[0]);

        $.ajax({
            url : modifyMyInfoForm.attr('action'),
            method : modifyMyInfoForm.attr('method'),
            data : formData,
            processData: false,
            contentType: false,
            dataType : "text",
            success : (response, status, xhr) => {
                if (response == "success") modalContainer.load("myPage");
            },
            error : (xhr, status, err) => {
                alert('ajax fail');
            }
        });
    }

    modalContainer.on('click', '.mypage-modify-btn', (e) => {
        const idx = $(e.target).parents('.info-item').data('index');
        modifyMyInfo(idx);
    });
    /*** 정보 수정 Ajax End ***/
    /*** 선호활동 Function Start ***/
    function existingSession(sessionName) {
        const session = `<div class="session-item">
                            <span>${sessionName}</span>
                            <button type="button" class="remove-session-btn" data-sessionname="${sessionName}">
                              <i class="fas fa-times" data-sessionname="${sessionName}"></i>
                            </button>
                        </div>`;
        $('.session-list').append(session);
    }
    /*** 선호활동 Function End ***/

    /*** File Upload Start ***/
    modalContainer.on('change', '#file-selector-btn', function(e) {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            const fileReader = new FileReader();
            fileReader.onload = function(event) {
                 $('#selected-image').attr('src', event.target.result);
                 // $('#selected-image')
            }
            fileReader.readAsDataURL(selectedFile);
        }

        // 정보수정 Ajax
        modifyMyInfo(-1);

//        $('#selected-image').css('opacity', '1');
//        $('.image-info-text').addClass('hide');
    });
    /*** File Upload End ***/

    /*** 수정하기 Btn Start ***/
    modalContainer.on('click', '.info-modify-btn', (e) => {
        const infoText = $('.info-text');
        const infoModifyInput = $('.info-modify-input');
        const infoModifyBtn = $('.info-modify-btn');
        const infoChangeBtnContainer = $('.info-change-btn-container');
        const modifyPhone = $('.modify-phone');

        const idx = $(e.target).parent().data('index');

        if ($(e.target).attr('class') != "info-modify-btn") {
            e.preventDefault();
            e.stopPropagation();
            return;
        }

//        infoText.eq(idx).css('display', 'none');
//        infoModifyInput.eq(idx).css('display', 'block');
//
//        infoModifyBtn.eq(idx).css('display', 'none');
//        infoChangeBtnContainer.eq(idx).css('display', 'grid');

        /* 연락처 수정 */
        if (idx === 1) {
            $('.nav-container').css('z-index', '0');
            $('.modal-backdrop').remove();
            const phoneNum = infoText.eq(idx).text();
            modifyPhone.eq(0).val(phoneNum.slice(0, 3));
            modifyPhone.eq(1).val(phoneNum.slice(3, 7));
            modifyPhone.eq(2).val(phoneNum.slice(7));
        } else if (idx === 3) {
            $('.nav-container').css('z-index', '0');
            $('.modal-backdrop').remove();
        } else if (idx === 6) {
            execDaumPostcode();
        } else {
            infoText.eq(idx).css('display', 'none');
            if (idx == 0) infoModifyInput.eq(idx).css('display', 'block');
            if (idx == 2) {
                infoModifyInput.eq(idx-1).css('display', 'block');
            } else if (idx > 0) {
                infoModifyInput.eq(idx-2).css('display', 'block');
            }

            if (idx == 8) {
                infoModifyBtn.eq(idx-1).css('display', 'none');
                infoChangeBtnContainer.eq(idx-1).css('display', 'grid');
            }
            if (idx < 8) {
                infoModifyBtn.eq(idx).css('display', 'none');
                infoChangeBtnContainer.eq(idx).css('display', 'grid');
            }
        }

        /* 보호자 연락처 수정 */
        const phoneNum = infoText.eq(idx).text();
        $('.modify-protector-phone').eq(0).val(phoneNum.slice(0, 3));
        $('.modify-protector-phone').eq(1).val(phoneNum.slice(3, 7));
        $('.modify-protector-phone').eq(2).val(phoneNum.slice(7));

        /* 선호활동 */
        const hiddenValue = $('.hidden-value');
        hiddenValue.each((i, v) => {
            const isHave = sessionArr.findIndex(v => v == hiddenValue.eq(i).val());
            if (isHave === -1) {
                sessionArr.push(hiddenValue.eq(i).val());
            }
            existingSession(hiddenValue.eq(i).val());
        });
    });
    /*** 수정하기 Btn End ***/

    /*** 변경취소 Btn Start ***/
    modalContainer.on('click', '.info-cancel-btn', (e) => {
        const infoText = $('.info-text');
        const infoModifyInput = $('.info-modify-input');
        const infoModifyBtn = $('.info-modify-btn');
        const infoChangeBtnContainer = $('.info-change-btn-container');

        const idx = $(e.target).parent().parent().data('index');

        if ($(e.target).attr('class') != "info-cancel-btn") {
            e.preventDefault();
            e.stopPropagation();
            return;
        }

        if (idx == 0) infoModifyInput.eq(idx).css('display', 'none');
        if (idx == 2) {
            infoModifyInput.eq(idx-1).css('display', 'none');
        } else if (idx > 0) {
            infoModifyInput.eq(idx-2).css('display', 'none');
        }
        infoText.eq(idx).css('display', 'block');

        if (idx == 8) {
            infoChangeBtnContainer.eq(idx-1).css('display', 'none');
            infoModifyBtn.eq(idx-1).css('display', '');
        }
        if (idx < 8) {
            infoChangeBtnContainer.eq(idx).css('display', 'none');
            infoModifyBtn.eq(idx).css('display', '');
        }
    });
    /*** 변경취소 Btn End ***/

    /*** 연락처 수정 Start ***/
    modalContainer.on('input', '.modify-phone', (e) => {
        const modifyPhone = $('.modify-phone');
        if (modifyPhone.eq(0).val().length > 3) {
            modifyPhone.eq(0).val(modifyPhone.eq(0).val().slice(0, 3));
        }

        if (modifyPhone.eq(1).val().length > 4 || modifyPhone.eq(2).val().length > 4) {
            modifyPhone.eq(1).val(modifyPhone.eq(1).val().slice(0, 4));
            modifyPhone.eq(2).val(modifyPhone.eq(2).val().slice(0, 4));
        }

        if (modifyPhone.eq(0).val().length >= 2 && modifyPhone.eq(1).val().length >= 3 && modifyPhone.eq(2).val().length >= 3) {
            modifyPhone.eq(3).val(modifyPhone.eq(0).val() + modifyPhone.eq(1).val() + modifyPhone.eq(2).val());
        }
    });
    /*** 인증번호 전송 Ajax Start ***/
    modalContainer.on('click', '.modify-take-auth-btn', (e) => {

        if($('.modify-phone').eq(3).val() >= 10){

            $.ajax({
                url : '/personal/send-sms',
                method : 'POST',
                data : 'userPhone=' + $('.modify-phone').eq(3).val(),
                dataType : 'text',
                success : (response, status, xhr) => {
                    if (response == 'true') {
                        alert('이미 회원가입된 번호입니다');
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
    /*** 인증번호 전송 Ajax End ***/
    /*** 인증번호 검사 Ajax Start ***/
    modalContainer.on('click', '.check-auth-btn', (e) => {

        const authNumber = $('.modify-auth-number').val();

        if (authNumber.length > 1) {

            $.ajax({
                url : '/userView/account/check-number',
                method : 'GET',
                data : 'number=' + authNumber,
                dataType : 'text',
                success : (response, status, xhr) => {
                    if (response == 'success') {
                        alert('확인되었습니다');
                        $('.modify-auth-number').data('auth', 'true');
                    }
                    if (response == 'error') {
                        $('.modify-auth-number').val('');
                        $('.modify-auth-number').focus();
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
    /*** 연락처 수정 End ***/

    /* 보호자 연락처 Start */
    modalContainer.on('input', '.modify-protector-phone', (e) => {
        const modifyPhone = $('.modify-protector-phone');
        if (modifyPhone.eq(0).val().length > 3) {
            modifyPhone.eq(0).val(modifyPhone.eq(0).val().slice(0, 3));
        }

        if (modifyPhone.eq(1).val().length > 4 || modifyPhone.eq(2).val().length > 4) {
            modifyPhone.eq(1).val(modifyPhone.eq(1).val().slice(0, 4));
            modifyPhone.eq(2).val(modifyPhone.eq(2).val().slice(0, 4));
        }

        if (modifyPhone.eq(0).val().length >= 2 && modifyPhone.eq(1).val().length >= 3 && modifyPhone.eq(2).val().length >= 3) {
            modifyPhone.eq(3).val(modifyPhone.eq(0).val() + modifyPhone.eq(1).val() + modifyPhone.eq(2).val());
        }
    });
    /* 보호자 연락처 End */

    /*** 모달 닫았을 때 navbar Control Start ***/
    modalContainer.on('click', '.cancel-modify-btn', (e) => {
        $('.nav-container').css('z-index', '100');
        $('body').css('padding', '0');
        $('.session-item').remove();
    });
    modalContainer.on('click', '.btn-close', (e) => {
        $('.nav-container').css('z-index', '100');
        $('body').css('padding', '0');
        $('.session-item').remove();
    });
    /*** 모달 닫았을 때 navbar Control End ***/

    /*** 선호활동 Start ***/
    function appendSession(sessionName) {
      const session = `<div class="session-item">
                            <span>${sessionName}</span>
                            <button type="button" class="remove-session-btn" data-sessionname="${sessionName}">
                              <i class="fas fa-times" data-sessionname="${sessionName}"></i>
                            </button>
                        </div>
                        <input type="hidden" name="sessionItemName" class="hidden-value" value="${sessionName}">`;
      $('.session-list').append(session);
    }
    let sessionArr = [];
    $('.modal-container').on('change', '.session-select', (e) => {
      const selectValue = $('.session-select').val();

      if (selectValue == '선택') {
        return;
      } else {

        let findIdx = sessionArr.findIndex(v => v == selectValue);
        if (findIdx == -1) sessionArr.push(selectValue);
        if (findIdx != -1) {
            $(".session-select").val("선택").prop("selected", true);
            return;
        }

        $('.session-item').remove();
        $('.hidden-value').remove();
        sessionArr.forEach((v, i) => {
          appendSession(v);
        });

      }
      $(".session-select").val("선택").prop("selected", true);

    });

    $('.modal-container').on('click', '.remove-session-btn', (e) => {
      const removeValue = $(e.target).data('sessionname');
      const findIdx = sessionArr.findIndex(v => v == removeValue);
      $('.session-item').remove();
      $('.hidden-value').remove();
      sessionArr.splice(findIdx, 1);
      sessionArr.forEach((v, i) => {
        appendSession(v);
      });
    });
    /*** 선호활동 End ***/

    /*** 생년월일 Start ***/
    // option 목록 생성 여부 확인
    let isYearOptionExisted = false;
    let isMonthOptionExisted = false;
    let isDayOptionExisted = false;
    modalContainer.on('focus', '#birth-year', function () {
        const birthYearEl = $('#birth-year');
        // year 목록 생성되지 않았을 때 (최초 클릭 시)
        if(!isYearOptionExisted) {
            isYearOptionExisted = true
            for(let i = 1940; i <= 2022; i++) {
                // option element 생성
                const YearOption = document.createElement('option');
                YearOption.setAttribute('value', i);
                YearOption.innerText = i;
                // birthYearEl의 자식 요소로 추가
                this.appendChild(YearOption);
            }
        }
    });
    modalContainer.on('focus', '#birth-month', function () {
        const birthMonthEl = $('#birth-month');
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
    modalContainer.on('focus', '#birth-day', function () {
        const birthDayEl = $('#birth-day');
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
    function insertValue(el, val) {
        let userBirth = $('#userBirth');
        if (el === 'year') yearValue = val;
        if (el === 'month') monthValue = val;
        if (el === 'day') dayValue = val;
        const birth = yearValue + '-' + monthValue + '-' + dayValue;
        userBirth.val(birth);
    }

    modalContainer.on('change', '#birth-year', (e) => {
        insertValue('year', $('#birth-year').val());
    });
    modalContainer.on('change', '#birth-month', (e) => {
        insertValue('month', $('#birth-month').val());
    });
    modalContainer.on('change', '#birth-day', (e) => {
        insertValue('day', $('#birth-day').val());
    });
    /*** 생년월일 End ***/

    /*** 건강상태 Start ***/
    modalContainer.on('change', '.health-select', (e) => {
        const healthValue = $('.health-select').val();
        $('.health-value').val(healthValue);
    });
    /*** 건강상태 End ***/
    /*** MyPage JS End ***/

    /*** MyPage Session Start ***/
    modalContainer.on('click', '.btp-btn', (e) => {
        $('.mypage-session-module').load('myPage/boyug-to-personal');
        $('.mypage-session-btn').removeClass('select-active');
        $('.btp-btn').addClass('select-active');
        $('.mypage-session-module').removeClass('speech-bubble-middle');
        $('.mypage-session-module').removeClass('speech-bubble-bottom');
        $('.mypage-session-module').addClass('speech-bubble-top');
    })

    modalContainer.on('click', '.ptb-btn', (e) => {
        $('.mypage-session-module').load('myPage/personal-to-boyug');
        $('.mypage-session-btn').removeClass('select-active');
        $('.ptb-btn').addClass('select-active');
        $('.mypage-session-module').removeClass('speech-bubble-top');
        $('.mypage-session-module').removeClass('speech-bubble-bottom');
        $('.mypage-session-module').addClass('speech-bubble-middle');
    });

    modalContainer.on('click', '.matching-btn', (e) => {
        $('.mypage-session-module').load('myPage/matching');
        $('.mypage-session-btn').removeClass('select-active');
        $('.matching-btn').addClass('select-active');
        $('.mypage-session-module').removeClass('speech-bubble-middle');
        $('.mypage-session-module').removeClass('speech-bubble-top');
        $('.mypage-session-module').addClass('speech-bubble-bottom');
    });

    // Personal-To-Boyug Pagination
    modalContainer.on('click', '.ptb-pagination .page-item', function(e) {
        let pageNo = $('.paging').data('pageno');
        const lastPageNo = Math.floor(($('.paging').data('datacount') / 5) + (($('.paging').data('datacount') % 5) > 0 ? 1 : 0));
        if (/«/.test($(this).text())) pageNo = 1;
        if ($(this).text() == 'Previous') pageNo = pageNo - 1 < 1 ? 1 : pageNo - 1;
        if ($(this).text() == 'Next') pageNo = pageNo + 1 > lastPageNo ? lastPageNo : pageNo + 1;
        if (/»/.test($(this).text())) pageNo = lastPageNo;
        if (!isNaN($(this).text())) pageNo = $(this).text();
        $('.mypage-session-module').load('myPage/personal-to-boyug?&pageNo=' + pageNo);
    });

    // Boyug-To-Personal Pagination
    modalContainer.on('click', '.btp-pagination .page-item', function(e) {
        let pageNo = $('.paging').data('pageno');
        const lastPageNo = Math.floor(($('.paging').data('datacount') / 5) + (($('.paging').data('datacount') % 5) > 0 ? 1 : 0));
        if (/«/.test($(this).text())) pageNo = 1;
        if ($(this).text() == 'Previous') pageNo = pageNo - 1 < 1 ? 1 : pageNo - 1;
        if ($(this).text() == 'Next') pageNo = pageNo + 1 > lastPageNo ? lastPageNo : pageNo + 1;
        if (/»/.test($(this).text())) pageNo = lastPageNo;
        if (!isNaN($(this).text())) pageNo = $(this).text();
        $('.mypage-session-module').load('myPage/boyug-to-personal?&pageNo=' + pageNo);
    });

    // Matching Pagination
    modalContainer.on('click', '.matching-pagination .page-item', function(e) {
        let pageNo = $('.paging').data('pageno');
        const lastPageNo = Math.floor(($('.paging').data('datacount') / 5) + (($('.paging').data('datacount') % 5) > 0 ? 1 : 0));
        if (/«/.test($(this).text())) pageNo = 1;
        if ($(this).text() == 'Previous') pageNo = pageNo - 1 < 1 ? 1 : pageNo - 1;
        if ($(this).text() == 'Next') pageNo = pageNo + 1 > lastPageNo ? lastPageNo : pageNo + 1;
        if (/»/.test($(this).text())) pageNo = lastPageNo;
        if (!isNaN($(this).text())) pageNo = $(this).text();
        $('.mypage-session-module').load('myPage/matching?&pageNo=' + pageNo);
    });

    // 보육원이 나한테 참가 요청 했을 때 수락 버튼
    modalContainer.on('click', '.my-session-confirm-btn', (e) => {
        const pageNo = $('.btp-pagination .paging').data('pageno');
        const programDetailId = $(e.target).parent().data('detailid');
        $.ajax({
            url : "myPage/request-confirm",
            method : "POST",
            data : "programDetailId=" + programDetailId + "&isConfirm=" + 'true',
            dataType : "text",
            success : (response, status, xhr) => {
                $('.mypage-session-module').load('myPage/boyug-to-personal?&pageNo=' + pageNo);
            },
            error : (xhr, status, err) => {
                alert('다시 시도해주세요');
            }
        });

    });

    // 보육원이 나한테 참가 요청 했을 때 거절 버튼
        modalContainer.on('click', '.my-session-refuse-btn', (e) => {
            const pageNo = $('.btp-pagination .paging').data('pageno');
            const programDetailId = $(e.target).parent().data('detailid');
            $.ajax({
                url : "myPage/request-confirm",
                method : "POST",
                data : "programDetailId=" + programDetailId + "&isConfirm=" + 'false',
                dataType : "text",
                success : (response, status, xhr) => {
                    $('.mypage-session-module').load('myPage/boyug-to-personal?&pageNo=' + pageNo);
                },
                error : (xhr, status, err) => {
                    alert('다시 시도해주세요');
                }
            });

        });
    /*** MyPage Session End ***/

    /*** Session JS Start ***/
//    modalContainer.on('click', '.radio-item', function(e) {
//
//        const index = $(e.target).data('index');
//        const radioItems = $('.radio-item');
//        const $carousel = $('#carousel');
//
//        if ($(e.target).attr('class') == "session-list-container") {
//            e.preventDefault();
//            e.stopPropagation();
//            return;
//        }
//        radioItems.removeClass('radio-checked');
//        radioItems.eq(index).addClass('radio-checked');
//        $carousel.css('--position', index + 1);
//    });
    /*** Session JS End ***/

    /*** Next, Previous Btn Start ***/
    const xArray = [0, -100, -200];
    const textArray = ['내 정보', '신청 내역', '일정'];
//    const previousBtn = $('.modal-previous-btn');
//    const nextBtn = $('.modal-next-btn');
    let xIndex = 0;

    function textChange(index) {
        const previousText = $('.modal-previous-btn span');
        const nextText = $('.modal-next-btn span');

        // 0 -> 2, 1
        // 1 -> 0, 2
        // 2 -> 1, 0
        let prevIdx = 0;
        let nextIdx = 0;
        if (index == 0) {
            prevIdx = 2;
            nextIdx = 1;
        }
        if (index == 1) {
            prevIdx = 0; nextIdx = 2;
        }
        if (index == 2) {
            prevIdx = 1, nextIdx = 0;
        }

        previousText.html(textArray[prevIdx]);
        nextText.html(textArray[nextIdx]);
    }



    modalContainer.on('click', '.modal-previous-btn', (e) => {
        const containerItem = $('.container-item');
        if (xIndex <= 0) xIndex = 2;
        else             xIndex -= 1;
        containerItem.css('transform', `translateX(${xArray[xIndex]}vw)`);
        console.log(xArray[xIndex]);
        textChange(xIndex);
    });

    modalContainer.on('click', '.modal-next-btn', (e) => {
        const containerItem = $('.container-item');
        if (xIndex >= 2) xIndex = 0;
        else             xIndex += 1;
        containerItem.css('transform', `translateX(${xArray[xIndex]}vw)`);
        textChange(xIndex);
    });
    /*** Next, Previous Btn End ***/

    /*** Advice Start ***/

    // 이용안내 모달 show
    modalContainer.on('click', '#information-btn', function() {
        $('#myModal').modal('show');
    });

    // 상담요청 버튼 클릭이벤트
    modalContainer.on('click', '#advice-btn', function() {
        const loginUserId = $('#currentUserId').data("loginid");
        if(loginUserId != null || loginUserId != undefined) {
            $('#advice').modal('show');
        } else {
            alert('신속하고 정확한 상담을 위해 로그인이 필요한 서비스입니다.\n로그인 페이지로 이동합니다.');
            location.href = '/userView/account/login';
        }
    });

    // 문의 게시판으로 이동
    modalContainer.on('click', '#board-move', function(){
        location.href = '/board/list';
    });

    // 모달 - 전화상담 요청
    modalContainer.on('click', '#call-request', function(){

        $('.call-form').css({
            'opacity': '1',
            'height': 'auto',
            'display': ''
        }).animate({
            'opacity': '0',
            'height': '0'
        }, 500, function() {
            $(this).css('display', 'none'); // 애니메이션이 끝나면 display를 none으로 설정
            $('.card-call').css('height', '22rem');
        });

        // 요소 보이기
        $('.select-call').css({
            'opacity': '0',
            'height': '0',
            'display': ''
        }).animate({
            'opacity': '1',
            'height': 'auto'
        }, 500);
        });

    modalContainer.on('click', '.select-option', function(e) {
        const content = $(this).text();
//        alert(keyword);

        if(confirm(`'${content}'로 전화 상담을 요청할까요?`)) {
            const userId = $('#currentUserId').data("loginid");
            $.ajax({
                url:"/userView/advice/call",
                data: { userId, content },
                success: function(resp) {
                    alert('전화 상담 신청이 완료되었습니다.\n담당자 확인 후 가입하신 휴대폰 번호로 전화드릴 예정입니다.\n이용해 주셔서 감사드립니다.');
                    location.reload();
                },
                error: function(err) {
                    console.log(err);
                    alert('실행 중 오류가 발생하였습니다');
                }
            });
        }
    });
    /*** Advice End ***/

});
