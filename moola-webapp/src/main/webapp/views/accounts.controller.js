angular.module('moola')
.controller('AccountController', ['Accounts', function(Accounts){

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

}]);
