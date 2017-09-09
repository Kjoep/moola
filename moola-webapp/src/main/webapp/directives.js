var moolaDirectives = angular.module('moolaDirectives',[]);



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
