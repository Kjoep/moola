var moolaApp = angular.module('moola',[]);

moolaApp.controller('AccountController', ['$scope', function ($scope) {
  $scope.accounts = [
      {
          code: "CUMUL1",
          name: "Kapitaal",
          type: "Cumulative",
          code_formatted: "",
          sources: ["BE27506376832160113","BE27506376832160113","BE27506376832160113","HUIS"]
      },
      {
          code: "BE27506376832160113",
          name: "Gemeenschappelijk",
          type: "Checking",
          code_formatted: "BE 2750 6376 8321 6011 3",
          bank: "AXA"
      },
      {
          code: "BE27506376832160113",
          name: "Joeri",
          type: "Checking",
          code_formatted: "BE 2750 6376 8321 6011 3",
          bank: "Fortis"
      },
      {
          code: "BE27506376832160113",
          name: "Joeri",
          type: "Saving",
          code_formatted: "BE 2750 6376 8321 6011 3",
          bank: "AXA"
      },
      {
          code: "HUIS",
          name: "Huis",
          type: "Investments"
      }
  ]
}]);