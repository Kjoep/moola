

angular.module('moola').controller('test', ['$scope', function($scope){

  var self = this;

  var padding=10;

  var KEY_UP=38;
  var KEY_DOWN=40;

  self.filter = 'dude';
  var selectedOption = -1;
  self.options = [
    {id: "One", name: "First", color:{fg:'white', bg:'red'}},
    {id: "Two", name: "Second"},
    {id: "Three", name: "Third"}
  ];

  $scope.selection = {id: "Two", name: "Second"};
  $scope.selected = function(arg){
    alert('hopla');
  }

  self.getOptionClass = function(optionId) {
    if (selectedOption == -1 && optionId == '__new') return "active";
    if (selectedOption>=0 && optionId == self.options[selectedOption].id) return "active";
    else return "";
  };

  var selectOption = function(idx){
    if (idx<-1) idx=-1;
    if (idx>=self.options.length) idx = self.options.length-1;
    selectedOption = idx;
  }

  $scope.showPeerSelection = function($event){
    var $peerSelection = $('.peerSelection');
    var $owner = $(event.currentTarget);

    var pos = $owner.offset();

    $peerSelection.css('width', ($owner.width()+padding+padding)+'px');
    $peerSelection.css('top', (pos.top)+'px');
    $peerSelection.css('left', (pos.left)+'px');
    $peerSelection.show();

    var $filter = $peerSelection.find('input.ps-filter');

    $filter.keydown(function(e){
      if (e.keyCode==27) {
        $peerSelection.hide()
      }
      else if (e.keyCode==KEY_UP) {
        $scope.$apply(function(){
          selectOption(selectedOption-1);
        });
        e.preventDefault(); return false;
      }
      else if (e.keyCode==KEY_DOWN) {
        $scope.$apply(function(){
          selectOption(selectedOption+1);
        });
        e.preventDefault(); return false;
      }
      else
        console.log(e.keyCode);
    })

    $filter.val($owner.find('input').val());
    $filter.focus();
    $filter.select();
  }


}]);

angular.module('moola').controller('MoolaController', ['$scope', 'Accounts', function ($scope, Accounts) {
  var self = this;

  self.currentAccount = null;
  self.categories = {};

  self.accountController = function(){
    var self = this;

    self.accounts = Accounts.query();
    self.types = Accounts.types();

    var edited = null;
    self.editValue = null;

    self.newAccount = function(){
      if (self.types.length==0) {
        alert("No account types found...")
        return;
      }

      self.editValue = {
        type: self.types[0].value,
        isNew: true,
        groupMembers: []
      }

      setTimeout(function(){$('#new__accountName').focus()}, 200);
    };

    self.edit = function(account){
      edited = account;
      self.editValue = jQuery.extend(true, {}, account);
      if (!self.editValue.groupMembers)
        self.editValue.groupMembers = [];
    };

    self.cancelEdit = function(){
      edited = null;
      self.editValue = null;
    };

    self.delete = function(){
      var account = self.editValue;

      if (!confirm('Really delete '+account.name+'? This cannot be undone')) return;

      var result = Accounts.delete({id: account.id});
      result.$promise.then(function(){
        self.editValue = null;
        self.accounts = Accounts.query();
        self.accounts.$promise.then(function(){
          if (self.accounts.length>0)
            $scope.controller.activeAccount = self.accounts[0];
        });
      });
    }

    self.save = function(){
      var account = self.editValue;
      if (!account.id)
        account.id = account.name;
      var result = Accounts.save({id: account.id}, account);
      result.$promise.then(function(){
        if (account.isNew) {
          delete account.isNew;
          self.accounts.push(account);
        }
        else {
          for(var k in self.editValue) edited[k]=self.editValue[k];
          edited = null;
        }
        self.editValue = null;
      });
    };

    self.nonGroups = function(){
      var groups = function(acc){return acc.type!="GROUP"};
      return filter(self.accounts, groups)
    };

    self.accountPanelVisible=false;
    self.toggleAccountPanel = function(){
      if (self.accountPanelVisible) {
        $('.content').animate({'left':'0'}, 500);
        self.accountPanelVisible = false;
      } else {
        $('.content').animate({'left':'400px'}, 500);
        self.accountPanelVisible = true;
      }
    }

    self.accounts.$promise.then(function(){
      if (self.accounts.length>0)
        $scope.controller.activeAccount = self.accounts[0];
    });

    self.upload = {
      forAccount: function(account){
        console.log("for account");

        this.targetId = account.id;
        return function(file, dz){
          console.log("called on file");

          self.upload.selectedFile = file;
          self.upload.perform = function(){dz.processQueue()};
          self.upload.format = self.upload.formats.length == 0 ? null : self.upload.formats[0];
        }
      },
      getUrl: function(){
        var url = 'rest/accounts/'+self.upload.targetId+'/transactions/upload';
        if (self.upload.format)
          url += '?format='+self.upload.format;
        return url;
      },
      selectedFile: null,
      targetId: null,
      perform: function(){},
      formats: Accounts.uploadFormats(),
      format: null,
      cancel: function(){
        this.targetId = null;
      },
      error: function(message) {
        this.targetId = null;
        alert("Upload error: "+message);
      },
      done: function(response){
        this.targetId = null;
        //transactions.showByBatchId(response)
      }
    };

    var filter = function(array, filter){
      var r = [];
      for (var i=0; i<array.length; i++){
        if (filter(array[i])) r.push(array[i]);
      }
      return r;
    }

    return this;
  };

  self.internCategory = function(category) {
    var interned = self.categories[category.id];
    if (interned) return interned;
    self.categories[category.id] = category;
    return category;
  }
}]);
