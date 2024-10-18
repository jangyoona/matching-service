$(function(){
    $('.container-fluid #nav-container-box').load('/navbar');

    // 이용안내 모달 show
    $('#information-btn').on('click', function() {
        $('#myModal').modal('show');
    });

    // 상담요청 버튼 클릭이벤트
    $('#advice-btn').on('click', function() {
        const loginUserId = $('#currentUserId').data("loginid");
        if(loginUserId != null || loginUserId != undefined) {
            $('#advice').modal('show');
        } else {
            alert('신속하고 정확한 상담을 위해 로그인이 필요한 서비스입니다.\n로그인 페이지로 이동합니다.');
            location.href = '/userView/account/login';
        }
    });

    // 문의 게시판으로 이동
    $('#board-move').on('click', function(){
        location.href = '/board/list';
    });

    // 모달 - 전화상담 요청
    $('#call-request').on('click', function(){

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

    $('.select-option').on('click', function(e) {
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




});