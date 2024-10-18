$(function() {
    getRoom();
    createRoom();
    console.log($('#roomNumber').val());
});

function getRoom(){
    commonAjax('/getRoom', "", 'post', function(result){
        createChattingRoom(result);
    });
}

function createRoom(){
    $("#createRoom").click(function(){
        var msg = {	roomName : $('#roomName').val()	};

        commonAjax('/createRoom', msg, 'post', function(result){
            createChattingRoom(result);
        });

        $("#roomName").val("");
    });
}

function goRoom(number, name){
    location.href = "/moveChatting?roomName=" + name + "&" + "roomNumber=" + number;
}

function createChattingRoom(res){
    if(res != null) {
        var tag = "<tr><th class='num'>순서</th><th class='room'>방 이름</th><th class='go'></th></tr>";
        res.forEach(function(d, idx){
            var rn = d.roomName.trim();
            var roomNumber = d.roomNumber;
            tag += "<tr>"+
                        "<td class='num'>" + (idx + 1) + "</td>"+
                        "<td class='room'>" + rn + "</td>" +
                        "<td class='go'><button type='button' onclick='goRoom(\"" + roomNumber + "\", \"" + rn + "\")'>참여</button></td>" +
                    "</tr>";
        });
        $("#roomList").empty().append(tag);
    }
}

function commonAjax(url, parameter, type, calbak, contentType){
    $.ajax({
        url: url,
        data: parameter,
        type: type,
        contentType : contentType != null ? contentType:'application/x-www-form-urlencoded; charset=UTF-8',
        success: function (res) {
            calbak(res);
        },
        error : function(err){
            console.log('error');
            calbak(err);
        }
    });
}