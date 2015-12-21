(function (angular) {
  var $inject = [
    'MyAccountService',
    'AlertService',
    'OptionsFactory'
  ];

  class BasicProfileController {
    constructor (myAccountService, alertService, optionsFactory) {
      this.myAccountService = myAccountService;
      this.alertService = alertService;

      this.options = {
        genders: optionsFactory.getGenders(),
        phoneTypes: optionsFactory.getPhoneTypes()
      };

      this.myAccountService.getBasicProfile().then((user) => {
        this.me = user;
        console.log(this.me);
      }).catch(() => {
        this.alertService.showAlert('Failed to load current user from the server');
      });
    }

    updateBasicProfile() {
      console.log(this.me);
      this.myAccountService.updateBasicProfile(this.me).then(() => {
        this.alertService.showAlert('Success');
      }).catch(() => {
        this.alertService.showAlert('Failed to update user to server');
      });
    }
  }

  BasicProfileController.$inject = $inject;
  angular.module('app')
    .controller('BasicProfileController', BasicProfileController);
})(angular);
