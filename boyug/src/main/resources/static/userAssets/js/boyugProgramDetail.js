document.addEventListener('DOMContentLoaded', function() {
        var container = document.getElementById('map');

        const mapData = document.getElementById('map-data');

        const latitude = parseFloat(mapData.getAttribute('data-latitude'));
        const longitude = parseFloat(mapData.getAttribute('data-longitude'));

		var options = {
			center: new kakao.maps.LatLng(latitude,longitude),
			level: 3
		};

		var map = new kakao.maps.Map(container, options);


		var markerPosition  = new kakao.maps.LatLng(latitude,longitude);  // 마커가 표시될 위치
        		var marker = new kakao.maps.Marker({  // 마커를 생성한다
        		    position: markerPosition
        		});

        		marker.setMap(map);

        // navbar load
        $('.container-fluid #nav-container-box').load('/navbar');

        $('.apply-btn').on('click', function() {
            const detailNo = $(this).data("detailno");

            const ok = confirm("해당 활동을 신청하시겠습니까?")

            if (ok) {
                $.ajax({
                    "url": "apply-userToBoyug",
                    "method":"get",
                    "data": { "detailNo": detailNo },
                    "success": function(response, status, xhr) {
                        if (response === "success") {
                            alert('신청하였습니다.');
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


        $('#goToList-btn').on('click', function() {

            location.href = "boyugProgramList";

        })

        $('#modify-btn').on('click', function() {

            const boyugProgramId = $(this).data('programid');

            location.href = "boyugProgramEdit?boyugProgramId=" + boyugProgramId;

        })

        $('#delete-btn').on('click', function() {

            const boyugProgramId = $(this).data('programid');

            const yes = confirm("해당 모집글을 삭제하시겠습니까?")

            if (yes) {
                location.href = 'delete-boyugProgram?boyugProgramId=' + boyugProgramId;
            }
        })

        $('#bookmark-btn').on('click', function(e) {
            const userId = $(this).data("userid");
            const isValue = $(this).data("bookmark");
            if(!isValue) {
                $.ajax({
                    url: "/bookmark-add/" + userId,
                    success: function(resp) {
                        alert('관심등록이 완료되었습니다');
                        location.reload();
                    },
                    error: function(err) {
                        alert("실행 중 오류가 발생하였습니다");
                    }
                });
            } else {
                $.ajax({
                    url: "/bookmark-delete/" + userId,
                    success: function(resp) {
                        alert('관심등록이 삭제되었습니다');
                        location.reload();
                    },
                    error: function(err) {
                        alert("실행 중 오류가 발생하였습니다");
                    }
                });
            }
        });

        // <채팅>
        function goRoom(number){
            const options = 'width=978, height=718, top=50, left=100, scrollbars=no, location=no, resizable=no';
            window.open("/moveChatting?roomNumber=" + encodeURIComponent(number), '_black', options);
        }

        function createRoom(){
            $("#chat").click(function(){
                const userId = $(this).data("userid");
                $.ajax({
                    url: "/createRoom",
                    data: { userId : userId },
                    success: function (res) {
                        const lastIndex = res[res.length - 1];
                        const roomNumber = lastIndex.roomNumber;
                        goRoom(roomNumber); // alert(lastIndex.toUserId);
                    },
                    error : function(err){
                        alert("채팅 접속 중 오류가 발생하였습니다");
                    }
                });
            });
        }
        createRoom();
        // </채팅>



})