angular.module('moola').factory('Accounts', ['$resource',
  function($resource){
    return $resource('http://localhost:8080/moola/rest/accounts', {}, {
      types: {method:'GET', params:{}, url:'http://localhost:8080/moola/rest/accountTypes', isArray:true},
      uploadFormats: {method:'GET', params:{}, url:'http://localhost:8080/moola/rest/uploadFormats', isArray:true},
      query: {method:'GET', params:{}, isArray:true},
      save: {method:'PUT', url:'http://localhost:8080/moola/rest/accounts/:id'},
      delete: {method:'DELETE', url:'http://localhost:8080/moola/rest/accounts/:id'}
    });
  }]);

angular.module('moola').factory('Categories', ['$resource', function($resource){

  var getId = function(template){
    return template.replace(" ", "_");
  };

  var categoriesResource = $resource('http://localhost:8080/moola/rest/categories', {}, {
    get: {method:'GET', url:'http://localhost:8080/moola/rest/categories/:id', params:{}},
    update: {method:'PUT', url:'http://localhost:8080/moola/rest/categories/:id', params:{}},
    find: {method:'GET', params:{q: ':query'}, isArray:true}
  });

  var categories;
  var DEFAULT_COLOR = {fg: '#FFF', bg: '#00D'};

  return {

    get: function(){
      if (!categories)
        categories = categoriesResource.find({q:''});
      return categories;
    },

    update: function(category){
      return categoriesResource.update({id:category.id}, category).$promise.then(function() {
        console.log("Updated category: " + JSON.stringify(category))
      });
    },

    addNew : function(name, id){
      if (!id) id = getId(name);
      var newCat = {id: id, name:name, color: DEFAULT_COLOR};
      return categoriesResource.update({id: id}, newCat).$promise.then(function(){
        console.log("Created category: "+JSON.stringify(newCat))
        categories.push(newCat);
        return newCat;
      });
    }
  };


}]);

angular.module('moola').factory('Peers', ['$resource', function($resource){

  var getId = function(template){
    return template.replace(" ", "_");
  }

  var peersResource = $resource('http://localhost:8080/moola/rest/peers', {}, {
    get: {method:'GET', url:'http://localhost:8080/moola/rest/peers/:id', params:{}},
    update: {method:'PUT', url:'http://localhost:8080/moola/rest/peers/:id', params:{}},
    find: {method:'GET', params:{q: ':query'}, isArray:true}
  });

  var peers;

  return {

    get: function(){
      if (!peers)
        peers = peersResource.find({q:''});
      return peers;
    },

    update: function(peer){
      return peersResource.update({id:peer.id}, peer).$promise.then(function() {
        console.log("Updated peer: " + JSON.stringify(peer))
      });
    },

    addNew : function(name, id){
      if (!id) id = getId(name);
      var newPeer = {id: id, name:name};
      return peersResource.update({id: id}, newPeer).$promise.then(function(){
        console.log("Created peer: "+JSON.stringify(newPeer))
        peers.push(newPeer);
        return newPeer;
      });
    }
  };


}]);
