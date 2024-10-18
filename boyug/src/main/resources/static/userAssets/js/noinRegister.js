document.addEventListener('DOMContentLoaded', function() {
    $('.modal-container').on('click', '#noinRegister-btn', function() {
        const ok = confirm('활동 등록하시겠습니까 ?')

        if (ok) {
            $.ajax({
                "url": "/userView/activityPages/noinRegister2",
                "method":"get",
                "success": function(response, status, xhr) {
                    if (response === "success") {
                        alert('활동 등록하였습니다.');
                        location.reload();
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

    $('.modal-container').on('click','#noinUnRegister-btn' , function() {
            const ok = confirm('등록된 활동을 취소하시겠습니까 ?')

            if (ok) {
                $.ajax({
                    "url": "/userView/activityPages/noinUnRegister",
                    "method":"get",
                    "success": function(response, status, xhr) {
                        if (response === "success") {
                            alert('등록된 활동을 취소하였습니다.');
                            location.reload();
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

        $('.modal-container').on('click','.edit-btn' , function() {
            $.ajax({
                url : "/personal/request-modal",
                method : "GET",
                data : "index=3",
                dataType : "text",
                success : (response, status, xhr) => {
                        $('.modal-container').load(response, () => {
                            $('.mypage-session-module').load("myPage/boyug-to-personal");
                            $('.main-item').removeClass('item-active');
                            $('.main-item[data-index="3"]').addClass(' item-active');
                    })
                },
                error : (xhr, status, err) => {
                    alert('fail');
                }
            });
        })


})