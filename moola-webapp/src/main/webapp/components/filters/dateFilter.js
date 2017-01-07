moolaApp.directive('dateFilter', [function(){
    return {
        restrict: 'A',
        scope: {
            onApply: '&',
            query: '=query'
        },
        link: function($scope, $elem, $attrs){
            var shown=false;
            var key = 'date';
            $elem.hide();
            $elem.parent().mousedown(function(e){
                if (shown) return;
                prepareData();
                $elem.fadeIn(150, function(){
                    shown = true;
                    $('body').bind('mousedown', hideFunction);
                });
            });

            var filters, grouping;

            var prepareData = function(){
                filters = $scope.query.filters.date;
                grouping = $scope.query.grouping.date;

                if (filters && filters.length>0) $elem.parent().addClass('filtered');
                else $elem.parent().removeClass('filtered');
                if (grouping) $elem.parent().addClass('grouped');
                else $elem.parent().removeClass('grouped');

                if (!filters || filters.length==0) $elem.find('#qs-date-filter-none').prop('checked', true);
                else if (any(filters[0], ['7days', '30days', '1month', '12months'])) $elem.find('#qs-date-filter-'+filters[0]).prop('checked', true);
                else {
                    $elem.find('#qs-date-filter-custom').prop('checked', true);
                    var match = /^(.*)-(\d+)(days|weeks|months|years)$/.exec(filters[0])
                    if (match){
                        $elem.find('#qs-date-filter-custom-nr').val(match[2]);
                        $elem.find('#qs-date-filter-custom-type').val(match[3]);
                        $elem.find('#qs-date-filter-custom-from').val(match[1]);
                    }
                }
                if (!grouping) $elem.find('#qs-date-grouping-none').prop('checked', true);
                else if (any(grouping, ['day', 'week', 'month', 'year'])) $elem.find('#qs-date-grouping-'+grouping).prop('checked', true);
            }

            var hideFunction = function(e){
                if (e && e.target && $(e.target).parents().filter(function(idx, e){return e == $elem[0]}).length>0) return;
                $('body').unbind('mousedown', hideFunction);
                if (!shown) return;
                shown = false;
                setTimeout(function(){
                    $elem.fadeOut(250);
                },50);
            };

            var any = function(needle, haystack){
                return haystack.indexOf(needle)>=0;
            };

            var apply = function(){
                var filter = $elem.find('input[name=qs-date-filter]:checked').val();
                if (filter == 'none') filter = undefined;
                else if (filter=='custom') {
                    filter = $elem.find('#qs-date-filter-custom-from').val() + "-" +
                        $elem.find('#qs-date-filter-custom-nr').val() +
                        $elem.find('#qs-date-filter-custom-type').val()
                }

                var filters = filter? [filter] :undefined;

                var group = $elem.find('input[name=qs-date-grouping]:checked').val();
                if (group=='none') group = undefined;

                $scope.$apply(function(){
                    $scope.onApply($scope.$parent)('date', filters, group);
                })
                hideFunction();
            }

            $elem.find('.query-apply').click(apply);
            prepareData();

        },
        templateUrl: 'components/filters/dateFilter.html'
    }
}])