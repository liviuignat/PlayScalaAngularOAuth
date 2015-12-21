'use strict';

describe('BasicProfileController', function () {
  var $scope, $q, $location, myAccountService;

  beforeEach(function () {
    module('app');
  });

  beforeEach(inject(function ($injector, $controller, $rootScope, _$location_, _$q_) {
    myAccountService = $injector.get('MyAccountService');
    $scope = $rootScope.$new();
    $q = _$q_;
    $location = _$location_;

    $controller('BasicProfileController as model', {
      $scope: $scope,
      $location: $location,
      myAccountService: myAccountService
    });
  }));

  it('Scope model should be defined', function () {
    expect($scope.model).toBeDefined();
  });
});

