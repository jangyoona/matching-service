document.addEventListener('DOMContentLoaded', function() {
    // navbar load
    $('.container-fluid #nav-container-box').load('/navbar');

    const userBirthString = $('#userBirth').val();
    const birthYear = new Date(userBirthString).getFullYear();

    const currentYear = new Date().getFullYear();

    const age = currentYear - birthYear;

    $('#userBirth').val(age + "세");

    $('#noinRegisterList-btn').on('click', function() {
        location.href="/userView/activityPages/noinRegisterList";
    })
    $('#noinRequest-btn').on('click', function() {
        $("#myModal").modal("show");
    })

    $("#modalSubmit").on('click', function() {
        $("#programDetails").submit();
    })
})