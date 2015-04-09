'use strict';

describe('AuthService', function (){
   var $http, $rootScope, $cookies, service, md5;

  beforeEach(module('app', function () {
    angular.module('app').factory('AuthInterceptor', window.mocks.AuthInterceptorMock.getInstance);
  }));

  beforeEach(inject(function ($injector) {
    $http = $injector.get('$httpBackend');
    $rootScope = $injector.get('$rootScope');
    $cookies = $injector.get('cookieStore');
    md5 = $injector.get('md5');
    service = $injector.get('AuthService');
  }));

  it('Should have the service injected and defined', function () {
    expect(service).toBeDefined();
  });

  describe('When user creates a new account', function () {
    var url = '/api/auth/create';
    var createAccountData = {
      email: 'liviu@ignat.email',
      password: 'test123',
      firstName: 'Liviu',
      lastName: 'Ignat'
    };

    describe('When user is created is successful with status code 201', function () {
      var response;
      beforeEach(function () {
        $http.expectPOST(url).respond(201);

        service.createAccount(createAccountData).then(function (result) {
          response = result;
        });

        $http.flush();
      });

      it('Should have the response successful', function () {
        expect(response).toBeDefined();
        expect(response.success).toBe(true);
      });
    });

    describe('When user is NOT successful with status code 401', function () {
      var response;
      beforeEach(function () {
        $http.expectPOST(url).respond(401);

        service.createAccount(createAccountData).then(function (result) {
          response = result;
        });

        $http.flush();
      });

      it('Should NOT have a successful response', function () {
        expect(response).toBeDefined();
        expect(response.success).toBe(false);
      });
    });
  });

  describe('When user logs in', function () {
    var url = '/api/auth/access_token';
    var loginData = {
      email: 'liviu@ignat.email',
      password: 'test123'
    };
    var loginResponse = {
      access_token: 'access_token',
      refresh_token: 'refresh_token'
    };

    describe('When login is successful with status code 200', function () {
      var loginServiceResponse;
      beforeEach(function () {
        spyOn($cookies, 'put');
        spyOn(service, 'setAuthToken');

        $http.expectPOST(url).respond(200, loginResponse);

        service.login(loginData).then(function (result) {
          loginServiceResponse = result;
        });

        $http.flush();
      });

      it('Should have the response successful', function () {
        expect(loginServiceResponse).toBeDefined();
        expect(loginServiceResponse.success).toBe(true);
      });

      it('Should set cookie with the refresh_token', function () {
        expect($cookies.put).toHaveBeenCalled();
      });

      it('Should set access_token', function () {
        expect(service.setAuthToken).toHaveBeenCalledWith(loginResponse.access_token);
      });

      it('To be logged in the system', function () {
        expect($rootScope.isLoggedIn).toBe(true);
      });

      describe('When refreshing token', function () {
        var refreshTokenResponse = {
          access_token: 'new_access_token',
          refresh_token: 'refresh_token'
        };
        var refreshTokenResult;

        beforeEach(function () {
          $http.expectPOST(url).respond(200, refreshTokenResponse);
          service.login(loginData).then(function (result) {
            refreshTokenResult = result;
          });
          $http.flush();
        });

        it('Should set the new access_token', function () {
          expect(service.setAuthToken).toHaveBeenCalledWith(refreshTokenResponse.access_token);
        });
      });
    });

    describe('When login is NOT successful with status code 401', function () {
      var loginServiceResponse;
      beforeEach(function () {
        $http.expectPOST(url).respond(401);

        service.login(loginData).then(function (result) {
          loginServiceResponse = result;
        });

        $http.flush();
      });

      it('Should NOT have a successful response', function () {
        expect(loginServiceResponse).toBeDefined();
        expect(loginServiceResponse.success).toBe(false);
      });
    });
  });

  describe('When user resets password', function () {
      var url = '/api/auth/resetpassword';
      var postData = {
        email: 'liviu@ignat.email'
      };

      describe('When reset password is successful with status code 200', function () {
        var resetPasswordResponse;
        beforeEach(function () {
          $http.expectPOST(url).respond(200);

          service.resetPassword(postData).then(function (result) {
            resetPasswordResponse = result;
          });

          $http.flush();
        });

        it('Should have the response successful', function () {
          expect(resetPasswordResponse).toBeDefined();
          expect(resetPasswordResponse.success).toBe(true);
        });
      });

      describe('When reset password is successful with status code 401', function () {
        var resetPasswordResponse;
        beforeEach(function () {
          $http.expectPOST(url).respond(401);

          service.resetPassword(postData).then(function (result) {
            resetPasswordResponse = result;
          });

          $http.flush();
        });

        it('Should NOT have a successful response', function () {
          expect(resetPasswordResponse).toBeDefined();
          expect(resetPasswordResponse.success).toBe(false);
        });
      });
    });
});
