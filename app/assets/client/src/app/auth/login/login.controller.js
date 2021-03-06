(function (angular) {
  var $inject = [
    '$location',
    'AuthService',
    'AlertService'
  ];

  class LoginController {
    constructor ($location, authService, alertService) {
      this.$location = $location;
      this.authService = authService;
      this.alertService = alertService;

      this.user = {
        email: '',
        password: ''
      };
    }

    login() {
      return this.authService.login(this.user).then(result => {
        if(!result.success) {
          this.alertService.showAlert(result.message);
          return;
        }

        this.$location.path('search');
      }).catch(() => {
        this.alertService.showAlert('Unknown error occurred');
      });
    }
  }

  LoginController.$inject = $inject;
  angular.module('app')
    .controller('LoginController', LoginController);

})(angular);

