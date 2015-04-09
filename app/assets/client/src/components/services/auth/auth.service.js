/*jshint esnext: true */
(function (angular) {
  var $inject = [
    '$rootScope',
    '$http',
    '$q',
    '$window',
    'cookieStore',
    'md5'
  ];

  const REFRESH_TOKEN_COOKIE_KEY = 'session_id';
  const AUTH_TOKEN_CACHE_KEY = 'session_auth_token';
  const CLIENT_ID = 'DefaultClient';
  const CLIENT_SECRET = 'DefatultClientSecret';
  const TOKEN_LIFETIME = 50 * 60 * 1000;

  class AuthService {
    constructor($rootScope, $http, $q, $window, $cookies, md5) {
      this.$rootScope = $rootScope;
      this.$http = $http;
      this.$q = $q;
      this.$window = $window;
      this.$cookies = $cookies;
      this.md5 = md5;
    }

    isLoggedIn() {
      var refreshToken = this.$cookies.get(REFRESH_TOKEN_COOKIE_KEY);
      return (refreshToken || '').length > 0;
    }

    getAuthToken() {
      var value = this.$window.sessionStorage.getItem(AUTH_TOKEN_CACHE_KEY);

      if(!value) {
        return this.$q.when(null);
      }

      var json = JSON.parse(value);
      var isExpired = json.expires < Date.now();

      return this.$q.when(isExpired ? null : json.accessToken);
    }

    setAuthToken(token) {
      var json = {
        accessToken: token,
        expires: Date.now() + TOKEN_LIFETIME
      };
      this.$window.sessionStorage.setItem(AUTH_TOKEN_CACHE_KEY, JSON.stringify(json));
      this.$q.when();
    }

    refreshAuthToken() {
      if(!this.isLoggedIn()) {
        this.$q.reject(new Error('Not logged in'));
      }

      var deferred = this.$q.defer();
      var failResponse = new AuthResponse(false, 'Cannot refresh token');
      var refreshToken = this.$cookies.get(REFRESH_TOKEN_COOKIE_KEY);
      var payload = angular.copy({
        grant_type: 'refresh_token',
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        refresh_token: refreshToken
      });

      this.$http({
        method: 'POST',
        url: '/api/auth/access_token',
        data: payload
      }).success((tokenData, status) => {
        if (status === 200) {
          this.setAuthToken(tokenData.access_token);
          return deferred.resolve(new AuthResponse(true));
        }

        return deferred.resolve(failResponse);
      }).error(() => {
        return deferred.resolve(failResponse);
      }).catch(() => {
        return deferred.resolve(failResponse);
      });

      return deferred.promise;
    }

    refreshAuthTokenIfNotExpired() {
      return this.getAuthToken().then((authToken) => {
        if(!authToken) {
          return this.refreshAuthToken();
        }
        return this.$q.when();
      });
    }

    login(model) {
      var deferred = this.$q.defer();
      var failResponse = new AuthResponse(false, 'User or password does not exist');
      var payload = angular.copy({
        grant_type: 'password',
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        scope: 'offline_access',
        username: model.email,
        password: model.password
      });

      payload.password = this.md5.createHash(model.password || '');

      this.$http({
        method: 'POST',
        url: '/api/auth/access_token',
        data: payload
      }).success((tokenData, status) => {
        if (status === 200) {
          this.$cookies.put(REFRESH_TOKEN_COOKIE_KEY, tokenData.refresh_token, {
            path: '/',
            secure: false,
            expires: Date.now() + (30 * 24 * 3600 * 1000)
          });
          this.setAuthToken(tokenData.access_token);
          this.$rootScope.isLoggedIn = true;
          this.$rootScope.$broadcast('auth:login');
          return deferred.resolve(new AuthResponse(true));
        }

        return deferred.resolve(failResponse);
      }).error(() => {
        return deferred.resolve(failResponse);
      }).catch(() => {
        return deferred.resolve(failResponse);
      });

      return deferred.promise;
    }

    logout() {
      this.$window.sessionStorage.removeItem(AUTH_TOKEN_CACHE_KEY);
      this.$cookies.remove(REFRESH_TOKEN_COOKIE_KEY, {
        path: '/',
        secure: false
      });
      this.$rootScope.$broadcast('auth:logout');
      this.$rootScope.isLoggedIn = false;
      return this.$q.when();
    }

    createAccount(model) {
      var deferred = this.$q.defer();

      this.$http({
        method: 'POST',
        url: '/api/auth/create',
        data: model
      }).success((data, status) => {
        if (status === 201) {
          deferred.resolve(new AuthResponse(true));
        }
        deferred.resolve(new AuthResponse(false, 'User already exists'));
      }).error(() => {
        deferred.resolve(new AuthResponse(false, 'Unexpected error from the server'));
      }).catch(() => {
        deferred.resolve(new AuthResponse(false, 'Unexpected error from the server'));
      });

      return deferred.promise;
    }

    resetPassword(model) {
      var deferred = this.$q.defer();

      this.$http({
        method: 'POST',
        url: '/api/auth/resetpassword',
        data: model
      }).success((data, status) => {
        if (status === 200) {
          deferred.resolve(new AuthResponse(true));
        }
        deferred.resolve(new AuthResponse(false, 'User already exists'));
      }).error(() => {
        deferred.resolve(new AuthResponse(false, 'Unexpected error from the server'));
      }).catch(() => {
        deferred.resolve(new AuthResponse(false, 'Unexpected error from the server'));
      });

      return deferred.promise;
    }
  }

  class AuthResponse {
    constructor(success, errorMessage) {
      this.success = success;
      this.message = errorMessage;
    }
  }

  AuthService.$inject = $inject;
  angular.module('app')
    .service('AuthService', AuthService);

})(angular);
