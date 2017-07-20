angular.module('moola')
.controller('CategoryController', ['Categories', function(Categories){

    var self = this;

    self.categories = Categories.get();

}]);
