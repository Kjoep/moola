moolaApp.directive('typeFilter', ['$parse', function ($parse) {

    var self = this;

    return {
        restrict: 'A',
        scope: {
            onApply: '&',
            query: '=query'
        },
        link: function($scope, $elem, attrs){
            var filters, grouping;
            var shown = false;
            $elem.hide();
            $elem.addClass('report-query-panel');
            $elem.parent().mousedown(function(e){
                if (shown) return;
                prepareData();
                $elem.fadeIn(150, function(){
                    shown = true;
                    $('body').bind('mousedown', hideFunction);
                    console.log("Handler bound");
                });
            });

            var prepareData = function(){
                filters = $scope.query.filters.type;
                grouping = $scope.query.grouping.type;



                if (filters && filters.length>0) $elem.parent().addClass('filtered');
                else $elem.parent().removeClass('filtered');
                if (grouping) $elem.parent().addClass('grouped');
                else $elem.parent().removeClass('grouped');

                $elem.find('.filter-item').each(function(){
                    $(this).prop('checked',$.inArray(this.value,filters)>=0);
                });
                $elem.find('#rq_transtype_group').prop('checked', grouping);
            }

            var apply = function(){
                var filter = $elem.find('.filter-item')
                    .filter(ifChecked)
                    .map(function(idx, e){return e.value;});

                if (filter.length == 0) filter = undefined;

                var group = $elem.find('#rq_transtype_group').is(":checked");
                if (group==false) group = undefined;

                $scope.$apply(function(){
                    $scope.onApply($scope.$parent)('type', filters, group);
                })
                hideFunction();

            };

            var ifChecked = function(idx, e){
                return $(e).prop('checked');
            }

            $elem.find('.query-apply').click(apply);

            var hideFunction = function(e){
                if (e && e.target && $(e.target).parents().filter(function(idx, e){return e == $elem[0]}).length>0) return;
                $('body').unbind('mousedown', hideFunction);
                console.log('Hiding!');
                console.log(e);
                if (!shown) return;
                shown = false;
                console.log("Handler unbound");
                setTimeout(function(){
                    $elem.fadeOut(250);
                },50);
            };

            prepareData();

        },
        templateUrl: 'components/filters/typeFilter.html'
    }


}]);
