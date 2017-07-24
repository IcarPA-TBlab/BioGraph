/**
 * jQuery Loading Block
 *
 * demos / documentation: https://github.com/chan15/jquery-loading-block
 *
 * @author Chan
 */
;(function ($) {
    $.loadingBlockShow = function(opts) {
        var defaults = {
            imgPath: '/images/icon.gif',
            imgStyle: {
                width: 'auto',
                textAlign: 'center',
                marginTop: '20%'
            },
            text: 'loading...',
            style: {
                position: 'fixed',
                width: '100%',
                height: '100%',
                background: 'rgba(255, 255, 255, .8)',
                left: 0,
                top: 0,
                zIndex: 10000
            }
        };
        $.extend(defaults, opts);
        $.loadingBlockHide();

        var img = $('<div><img src="' + defaults.imgPath + '"><div>' + defaults.text + '</div></div>');
        var block = $('<div id="loading_block"></div>');

        block.css(defaults.style).appendTo('body');
        img.css(defaults.imgStyle).appendTo(block);
    };

    $.loadingBlockHide = function() {
        $('div#loading_block').remove();
    };
}(jQuery));
