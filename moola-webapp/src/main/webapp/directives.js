var moolaDirectives = angular.module('moolaDirectives',[]);

Dropzone.autoDiscover = false;

moolaDirectives.directive('dropzone',
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
                        scope.onError(response, xhr);
                        scope.$apply();
                    },
                    success: function(file, response){
                        scope.onSuccess(response);
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

moolaDirectives.directive('inlineEdit', ['$parse', function($parse) {
        return {
            link: function(scope, $elem, attrs) {
                var $edit = $("<input type='text' />");
                var expression = attrs['ngModel'];
                var expressionFn = $parse(expression);
                var onChange = attrs['ngChange'];

                $edit.insertAfter($elem);
                $edit.hide();
                $elem.addClass('inline-edit');
                $edit.addClass('inline-edit');
                $elem.click(function(){
                    startEdit();
                });
                $edit.blur(function(){
                    apply($edit.val());
                    endEdit();
                });

                $edit.keyup(function(k){
                    if (k.keyCode==27)
                        cancelEdit();
                });

                var startEdit = function() {
                    $elem.hide();
                    $edit.show();
                    $edit.val(expressionFn(scope));
                    $edit.focus();
                    $edit.select();
                }

                var endEdit = function(){
                    $elem.show();
                    $edit.hide();
                }

                var cancelEdit = function(){
                    $edit.val(expressionFn(scope));
                    endEdit();
                }

                var apply = function(value){
                    var prev = expressionFn(scope);
                    if (prev===value) return;
                    scope.$apply(function(){
                        expressionFn.assign(scope, value);
                        if (onChange) {
                            scope.$eval(onChange);
                        }
                    });
                }
            }
        }
    }]
);

// checklist directive courtisy of Michelle Tilley
// see http://stackoverflow.com/a/14519881/396618
moolaDirectives.directive('checkList', function() {
    return {
        scope: {
            list: '=checkList',
            value: '@'
        },
        link: function(scope, elem, attrs) {
            var handler = function(setup) {
                var checked = elem.prop('checked');
                var index = scope.list ? scope.list.indexOf(scope.value) : -1;

                if (checked && index == -1) {
                    if (setup) elem.prop('checked', false);
                    else scope.list.push(scope.value);
                } else if (!checked && index != -1) {
                    if (setup) elem.prop('checked', true);
                    else scope.list.splice(index, 1);
                }
            };

            var setupHandler = handler.bind(null, true);
            var changeHandler = handler.bind(null, false);

            elem.bind('change', function() {
                scope.$apply(changeHandler);
            });
            scope.$watch('list', setupHandler, true);
        }
    };
});