(function() {
    'use strict';

    angular
        .module('app.application.appsettings.notifications')
        .controller('NotificationsController', function($scope, $stateParams, msUserService,
            msModeService) {

            if (!msUserService.getAccessToken()) {
                $state.go('app.pages_auth_login');
            }

            msModeService.setToApplicationMode();

            $scope.appId = $stateParams.appId;

        });
})();
