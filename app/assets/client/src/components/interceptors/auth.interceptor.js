(function (angular) {
  var $inject = [
    '$injector',
    '$rootScope',
    '$timeout',
    '$q',
    '$log',
    '$window'
  ];

  let self;

  class AuthInterceptor {
    constructor($injector, $rootScope, $timeout, $q, $log, $window) {
      self = this;
      this.$injector = $injector;
      this.$rootScope = $rootScope;
      this.$timeout = $timeout;
      this.$q = $q;
      this.$log = $log;
      this.$window = $window;
    }

    static getInstance($injector, $rootScope, $timeout, $q, $log, $window) {
      return new AuthInterceptor($injector, $rootScope, $timeout, $q, $log, $window);
    }

    request(config) {
      return self.onRequest(config);
    }

    requestError(rejection) {
      return rejection;
    }

    responseError(rejection) {
      return self.onResponseError(rejection);
    }

    init() {
      return this.$timeout(() => {
        this.$location =  this.$injector.get('$location');
        this.authService = this.$injector.get('AuthService');
      });
    }

    tryRefreshToken() {
      return this.authService.refreshAuthTokenIfNotExpired().then(() => {
        return this.authService.getAuthToken();
      }).then((authToken) => {
        if(authToken) {
          return authToken;
        }
        return this.$q.when();
      });
    }

    onLoggedOut() {
      this.$location.path('login');
      return this.authService.logout();
    }

    onRequest(config) {
      var deferred = this.$q.defer();

      this.init().then(() => {
        if(config.isSecure) {
          var isLoggedIn = this.authService.isLoggedIn();

          if(isLoggedIn) {
            return this.tryRefreshToken().then((accessToken) => {
              config.headers.Authorization = 'Bearer ' + accessToken;
              deferred.resolve(config);
            });
          } else {
            return this.onLoggedOut().then(() => {
              deferred.resolve(config);
            });
          }
        } else {
          deferred.resolve(config);
        }
      }).catch(() => {
        return this.onLoggedOut().then(() => {
          deferred.resolve(config);
        });
      });

      return deferred.promise;
    }

    onResponseError(rejection) {
      if(rejection.status === 401) {
        this.onLoggedOut();
      }
      return this.$q.reject(rejection);
    }
  }

  AuthInterceptor.getInstance.$inject = $inject;
  angular.module('app')
    .factory('AuthInterceptor', AuthInterceptor.getInstance);

})(angular);
