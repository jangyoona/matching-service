
const navContainer = $('.nav-container');
const navItemA = $('.nav-item-container a');
const navMainName = $('.nav-main-name h1');

$(window).on('scroll', (e) => {

    let scrollTop = document.querySelector('html').scrollTop; // 현재 웹페이지 스크롤 양
    let scrollHeight = document.querySelector('html').scrollHeight; // 현재 웹페이지 실제 높이
    let clientHeight = document.querySelector('html').clientHeight; // 현재 웹페이지 보이는 높이

    // console.log("top: " + scrollTop);
    // console.log("height: " + scrollHeight);
    // console.log("cHeight: " + clientHeight);

    if (scrollTop >= 10) {
        navContainer.css('background-color', '#fff');
        navItemA.css('color', 'black');
        navMainName.css('color', 'black');
    } else {
        navContainer.css('background-color', '');
        navItemA.css('color', '');
        navMainName.css('color', '');
    }

});

navItemA.on('mouseover', (e) => {
    let scrollTop = document.querySelector('html').scrollTop;

    if (scrollTop >= 10) {
        $(e.target).css('border', '1px solid black');
        $(e.target).css('color', '#fff');
    } else {
        $(e.target).css('border', '1px solid #fff');
    }
});

navItemA.on('mouseout', (e) => {
    let scrollTop = document.querySelector('html').scrollTop;

    $(e.target).css('border', '1px solid transparent');

    if (scrollTop >= 10) {
        $(e.target).css('color', 'black');
    } else {
        $(e.target).css('color', '#fff');
    }
});





