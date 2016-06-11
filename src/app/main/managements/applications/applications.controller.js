(function() {
    'use strict';

    angular
    .module('app.managements.applications')
    .controller('ApplicationsController',
        function($scope, $http, $cookies, $window, $state, $rootScope, $mdDialog, $document,
            msApplicationService, msModeService, msSchemasService, msDialogService) {

            if (!$cookies.get('accessToken')) {
                $state.go('app.pages_auth_login');
            }

            $scope.applications = [];

            msApplicationService.getAll(function(error, results) {
                if (error) {
                    return alert(error.statusText);
                }

                $scope.applications = results;
            });

            $scope.showAddDialog = function(ev) {
                msDialogService.showDialog(ev, 'app/core/services/dialogs/newApplicationDialog.html');
            };

            $scope.goToAppManagement = function(appId) {
                msSchemasService.getSchemas(appId, null, function(error, results) {
                    if (error) {
                        return alert(error.statusText);
                    }
                });
            };
        });
})();
