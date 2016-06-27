(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesInfoController', function($scope, $http, $cookies, $window,
            $state, $stateParams, $mdDialog, $document, $rootScope, msModeService,
            msSchemasService, msDialogService, msApplicationService, msUserService) {

            if (!msUserService.getAccessToken()) {
                $state.go('app.pages_auth_login');
            }

            var appId = $stateParams.appId;
            $scope.appId = appId;

            $scope.showMasterKey = function() {
                msApplicationService.getMasterkey(appId, function(error, result) {
                    $scope.masterKey = result.data.data.masterKey;
                });
            }

        });
})();
