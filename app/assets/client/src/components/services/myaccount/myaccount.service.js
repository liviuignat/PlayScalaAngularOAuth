(function (angular) {
  var $inject = [
    '$http',
    '$q'
  ];

  class MyAccountService {
    constructor($http, $q) {
      this.$http = $http;
      this.$q = $q;
    }

    getBasicProfile() {
      var deferred = this.$q.defer();
      var url = '/api/user/me';

      this.$http({
        method: 'GET',
        url: url,
        isSecure: true
      }).success((data, status) => {
        if (status === 200) {
          deferred.resolve(data);
        }
        deferred.reject();
      }).error(() => {
        deferred.reject();
      }).catch(() => {
        deferred.reject();
      });

      return deferred.promise;
    }

    updateBasicProfile(profileData) {
      var deferred = this.$q.defer();
      var url = '/api/user/me';

      this.$http({
        method: 'PUT',
        url: url,
        data: profileData,
        isSecure: true
      }).success((data, status) => {
        if (status === 200) {
          deferred.resolve(data);
        }
        deferred.reject();
      }).error(() => {
        deferred.reject();
      }).catch(() => {
        deferred.reject();
      });

      return deferred.promise;
    }
  }

  MyAccountService.$inject = $inject;
  angular.module('app')
    .service('MyAccountService', MyAccountService);

})(angular);
