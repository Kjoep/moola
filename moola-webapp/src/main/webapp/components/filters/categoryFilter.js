angular.module('moola').directive('categoryFilter', [function(){

    var hideFunction;

    return {
        restrict: 'A',
        scope: {
            onApply: '&',
            query: '=query',
            categories: '='
        },
        bindToController: true,
        controllerAs: 'vm',
        controller: ['$scope', function($scope){
            var self = this;

            self.categoryControllers = [];
            self.grouping = 'none';

            var selectedCategories = [];

            $scope.$watch(function(){return self.categories;}, function(){
                var createController = function(category){
                    var r = {
                        id: category.id,
                        name: category.name
                    }
                    Object.defineProperty(r, 'selected', {
                        get: function(){ return selectedCategories.indexOf(category.id)>=0; },
                        set: function(value){
                            var currentIdx = selectedCategories.indexOf(category.id);
                            if (value && currentIdx < 0)
                                selectedCategories.push(category.id);
                            else if (!value && currentIdx >= 0)
                                selectedCategories.splice(currentIdx, 1);
                        }
                    });
                    return r;
                };
                self.categoryControllers = self.categories.map(createController);
                self.categoryControllers.unshift(createController({id: '__income', name: 'Income'}));
                self.categoryControllers.unshift(createController({id: '__expenses', name: 'Expenses'}));
            });

            self.reset = function(){
                selectedCategories = self.query.filters['category'] || [];
                self.grouping = self.query.grouping['category'] || 'none';
            };

            self.apply = function(){
                self.onApply()('category',
                    selectedCategories.length === 0 ? undefined : selectedCategories,
                    self.grouping === 'none' ? undefined : self.grouping);
                hideFunction();
            }
        }],
        link: function($scope, $elem, $attrs){
            var shown=false;
            var key = 'date';
            $elem.hide();
            $elem.parent().mousedown(function(e){
                if (shown) return;
                $scope.$apply(function(){ $scope.vm.reset(); });
                $elem.fadeIn(150, function(){
                    shown = true;
                    $('body').bind('mousedown', hideFunction);
                });
            });

            var filters, grouping;

            hideFunction = function(e){
                if (e && e.target && $(e.target).parents().filter(function(idx, e){return e == $elem[0]}).length>0) return;
                $('body').unbind('mousedown', hideFunction);
                if (!shown) return;
                shown = false;
                setTimeout(function(){
                    $elem.fadeOut(250);
                },50);
            };

        },
        templateUrl: 'components/filters/categoryFilter.html'
    }
}])