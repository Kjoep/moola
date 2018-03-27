'use strict';

/**
 * Same as ngClick, but if the handler returns a promise, disable the button until the promise is resolved
 * or rejected.  The disabling is done by adding the class 'single-click-disabled'.
 *
 * The click handler is also disabled if the button has the 'disabled' class.
 *
 * This code is based on the source of ngClick
 */
angular.module('moola').directive('ngClickSingle', ['$parse', '$q', function($parse, $q){
    var MARKER_CLASS = 'single-click-disabled';
    return {
        restrict: 'A',
        compile: function($element, attr) {
            var handler = $parse(attr.ngClickSingle);
            return function ngEventHandler(scope, element) {
                element.on('click', function (event) {
                    if (element.is('.'+MARKER_CLASS) || element.is('.disabled')) {
                        return;
                    }
                    scope.$apply(function () {
                        var handlerPromise = $q.when(handler(scope, {$event: event}));
                        element.addClass(MARKER_CLASS);
                        handlerPromise.then(function(result){
                            element.removeClass(MARKER_CLASS);
                            return result;
                        }, function(error){
                            element.removeClass(MARKER_CLASS);
                            throw error;
                        });
                    });
                });
            };
        }
    };
}]);
