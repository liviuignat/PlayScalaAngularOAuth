(function (angular) {
  var $inject = [ ];

  class OptionsFactory {
    constructor() {
    }

    getGenders() {
      return [
        { text: 'Male', value: 1 },
        { text: 'Female', value: 2 }
      ];
    }

    getPhoneTypes() {
      return [
        { text: 'Mobile', value: 1 },
        { text: 'Home', value: 2 },
        { text: 'Work', value: 3 }
      ];
    }
  }

  OptionsFactory.$inject = $inject;
  angular.register('app').factory('OptionsFactory', OptionsFactory);
})(angular);
