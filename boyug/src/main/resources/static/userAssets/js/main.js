(function ($) {
    "use strict";

    // Spinner
    var spinner = function () {
        setTimeout(function () {
            if ($('#spinner').length > 0) {
                $('#spinner').removeClass('show');
            }
        }, 1);
    };
    spinner(0);


    // Sticky Navbar
    $(window).scroll(function () {
        if ($(this).scrollTop() > 45) {
            $('.navbar').addClass('sticky-top shadow-sm');
        } else {
            $('.navbar').removeClass('sticky-top shadow-sm');
        }
    });


    // International Tour carousel
    $(".InternationalTour-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1000,
        center: false,
        dots: true,
        loop: true,
        margin: 25,
        nav : false,
        navText : [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0:{
                items:1
            },
            768:{
                items:2
            },
            992:{
                items:2
            },
            1200:{
                items:3
            }
        }
    });


    // packages carousel
    $(".packages-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1000,
        center: false,
        dots: false,
        loop: true,
        margin: 25,
        nav : true,
        navText : [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0:{
                items:1
            },
            768:{
                items:2
            },
            992:{
                items:2
            },
            1200:{
                items:3
            }
        }
    });


    // testimonial carousel
    $(".testimonial-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1000,
        center: true,
        dots: true,
        loop: true,
        margin: 25,
        nav : true,
        navText : [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0:{
                items:1
            },
            768:{
                items:2
            },
            992:{
                items:2
            },
            1200:{
                items:3
            }
        }
    });

    
   // Back to top button
   $(window).scroll(function () {
    if ($(this).scrollTop() > 300) {
        $('.back-to-top').fadeIn('slow');
    } else {
        $('.back-to-top').fadeOut('slow');
    }
    });
    $('.back-to-top').click(function () {
        $('html, body').animate({scrollTop: 0}, 1500, 'easeInOutExpo');
        return false;
    });

    // navbar JS

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

        if (scrollTop >= 5) {
            $('.nav-container').css('background-color', '#fff');
            $('.nav-item-container a').css('color', 'black');
            $('.nav-main-name h1').css('color', 'black');
        } else {
            $('.nav-container').css('background-color', '');
            $('.nav-item-container a').css('color', '');
            $('.nav-main-name h1').css('color', '');
        }

    });

    navItemA.on('mouseover', (e) => {
        console.log($(e.target).attr('class'));

        let scrollTop = document.querySelector('html').scrollTop;

        if ($(e.target).attr('class') == "dropdown-toggle text-light") {
            $('.dropdown-toggle text-light').css('border', '1px solid black');
            $('.nav-item-container a small').css('color', '#fff');
        } else if ($(e.target).attr('class') == "fas fa-bell me-2") {
            return;
        } else {
            $(e.target).css('border', '1px solid black');
            $(e.target).css('color', '#fff');
        }


//        if (scrollTop >= 10) {
//            $(e.target).css('border', '1px solid black');
//            $(e.target).css('color', '#fff');
//        } else {
//            $(e.target).css('border', '1px solid #fff');
//        }
    });

    navItemA.on('mouseout', (e) => {
        let scrollTop = document.querySelector('html').scrollTop;

        if ($(e.target).attr('class') == "dropdown-toggle text-light") {
            $('.dropdown-toggle text-light').css('border', '1px solid transparent');
            $('.nav-item-container a small').css('color', 'black');
        }

        $(e.target).css('border', '1px solid transparent');
        $(e.target).css('color', 'black');
    });

})(jQuery);










