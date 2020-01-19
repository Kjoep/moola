angular.module('moola')
.controller('CategoryController', ['Categories', 'CategoryService', '$q', function(Categories, CategoryService, $q){

    var self = this;

    self.categories = Categories.get();
    self.updates = {};

    self.categories.$promise.then(function(){
        // TODO this should be done in a repo
        byId = mapByProperty(self.categories, 'id');
        self.roots = [];
        for (var category of self.categories) {
            if (category.parentId) {
                category.$parent = byId[category.parentId];
                category.$parent.children = category.$parent.children || [];
                category.$parent.children.push(category);
            }
            else
                self.roots.push(category);
        }
        recursiveSort(self.roots);
    });

    var mapByProperty = function(list, key){
        var r = {};
        for (var item of list)
            r[item[key]] = item;
        return r;
    }

    var recursiveSort = function(items){
        var byId = function(a, b){ return a.id.toLowerCase() < b.id.toLowerCase() ? -1 : 1};
        items.sort(byId);
        for (var child of items.children || [])
            recursiveSort(items.children);
    }


    self.setParent = function(parent, child){
        console.log('linking', parent, child);
        if (child.$parent) child.$parent.children.splice(child.$parent.children.indexOf(child));
        child.$parent = parent;
        if (parent){
            if (!parent.children) parent.children = [];
            parent.children.push(child);
        }

        self.save(child)
            .then(function(){
                self.roots = self.categories.filter(function(category){ return !category.$parent});
                recursiveSort(self.roots);
            })
    }

    self.save = function(category){
        return (self.updates[category.id] || $q.when()).then(function(){
            var updatePromise = Categories.update(category);
            self.updates[category.id] = updatePromise.then(function(){
                self.updates[category.id] = undefined;
            })
            return updatePromise;
        })
    }

    self.addNew = function(name){
        CategoryService.create(name).then(function(newEntry){
            self.categories.push(newEntry);
            self.roots.push(newEntry);
            recursiveSort(self.roots);
        })

    }

    self.delete = function(category){
        alert('Delete '+category.id);
    }
}]);
