angular.module('moola').directive('contextMenu', ['$parse', function ($parse) {

    var self = this;

    return {
        restrict: 'A',
        scope: {},
        link: function($scope, $elem, attrs){
            var button = attrs.contextButton === undefined ? 2 : attrs.contextButton;
            $elem.hide();
            $elem.addClass('context-menu');
            $elem.parent().mousedown(function(e){
                if (e.button!=button) return;
                console.log(e);
                $elem.fadeIn(150);
                setTimeout(function(){
                    $('body').bind('mousedown', hideFunction);
                },10);
            });

            var hideFunction = function(){
                $('body').unbind('mousedown', hideFunction);
                setTimeout(function(){
                    $elem.fadeOut(250);
                },50);
            }
        }
    }
}]);

