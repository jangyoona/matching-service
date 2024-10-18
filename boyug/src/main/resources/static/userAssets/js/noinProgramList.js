$(function() {



    $('.modal-container').on('click', '#chltlstns', function() {
        $.ajax({
            url : "/personal/request-modal",
            method : "GET",
            data : "index=0",
            dataType : "text",
            success : (response, status, xhr) => {
                $('.modal-container').load(response + "?sort=chltlstns");
            },
            error : (xhr, status, err) => {
              alert('fail');
          }
      });
    })

    $('.modal-container').on('click', '#distance', function() {
            $.ajax({
                url : "/personal/request-modal",
                method : "GET",
                data : "index=0",
                dataType : "text",
                success : (response, status, xhr) => {
                    $('.modal-container').load(response);
                },
                error : (xhr, status, err) => {
                  alert('fail');
              }
          });
    })

    $('.modal-container').on('click', '#reroll', function() {
            $.ajax({
                url : "/personal/request-modal",
                method : "GET",
                data : "index=0",
                dataType : "text",
                success : (response, status, xhr) => {
                    $('.modal-container').load(response + "?sort=reroll");
                },
                error : (xhr, status, err) => {
                  alert('fail');
              }
          });
        })


    $('.modal-container').on('click', '.radio-item', function(e) {

        const index = $(e.target).data('index');
        const radioItems = $('.radio-item');
        const $carousel = $('#carousel');

        if ($(e.target).attr('class') == "session-list-container") {
            e.preventDefault();
            e.stopPropagation();
            return;
        }
        radioItems.removeClass('radio-checked');
        radioItems.eq(index).addClass('radio-checked');
        $carousel.css('--position', index + 1);


        $('[id^="detailForm"]').css('display', 'none');  // detailForm으로 시작하는 모든 요소 숨기기
        $('#detailForm' + index).css('display', 'block')

    });

    $('.modal-container').on('click', '.apply-btn', function(e) {
        const detailNo = $(e.target).data('detailno');

        const ok = confirm("해당 활동을 신청하시겠습니까?")
            if (ok) {
                $.ajax({
                    "url": "/userView/activityPages/apply-userToBoyug",
                    "method":"get",
                    "data": { "detailNo": detailNo },
                    "success": function(response, status, xhr) {
                        if (response === "success") {
                            alert('신청하였습니다.');

                            const button = $(e.target);
                            button.replaceWith('<p>신청 완료</p>'); // 버튼 없애고 해당 코드로 변경
                        } else {
                            alert('오류 발생 1');
                            location.reload();
                        }
                    },
                    "error": function(xhr, status, err) {
                        alert('오류발생 2');
                        location.reload();
                    }
                })
        }
    })

})