Dropzone.autoDiscover = false;

angular.module('moola').directive('dropzone',
    function(){
        return {
            restrict: 'A',
            scope: {
                onFileAdd: '&onFileAdd',
                url: '&url',
                onError: '&onError',
                onSuccess: '&onSuccess'
            },
            link: function(scope, $element, attrs){

                var cfg = {
                    url: function(){
                        return scope.url();
                    },
                    autoProcessQueue: false,
                    previewTemplate: '<span></span>',
                    error: function(file, response, xhr){
                        scope.onError(scope)(response, xhr);
                        scope.$apply();
                    },
                    success: function(file, response){
                        scope.onSuccess(scope)(response);
                        scope.$apply();
                    }
                };

                var dz = new Dropzone($element[0], cfg);

                dz.on('dragstart', function(){
                    console.log("drag start");
                    $element.addClass('dragHere');
                });
                dz.on('dragstop', function(){
                    console.log("drag stop");
                    $element.removeClass('dragHere dragHereOver');
                });
                dz.on('dragenter', function(){
                    console.log("drag enter");
                    $element.addClass('dragHereOver');
                });
                dz.on('dragleave', function(){
                    console.log("drag leave");
                    $element.removeClass('dragHereOver');
                });
                dz.on('addedfile', function(file){
                    var callback = scope.onFileAdd();
                    while (typeof callback == 'function')
                        callback = callback(file, dz);
                    scope.$apply();
                });
            }
        };
    });
