(function (angular) {
  var $inject = [
    'MyAccountService',
    'AlertService'
  ];

  class BasicProfileController {
    constructor (myAccountService, alertService) {
      this.myAccountService = myAccountService;
      this.alertService = alertService;

      this.myAccountService.getBasicProfile().then((user) => {
        this.me = user;
      }).catch(() => {
        this.alertService.showAlert('Failed to load current user from the server');
      });
    }
  }

  BasicProfileController.$inject = $inject;
  angular.module('app')
    .controller('BasicProfileController', BasicProfileController);
})(angular);
