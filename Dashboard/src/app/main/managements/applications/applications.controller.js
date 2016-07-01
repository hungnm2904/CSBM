(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .controller('ApplicationsController', ApplicationsController);

    function ApplicationsController($scope, $rootScope, $state, msUserService, msApplicationService,
        msSchemasService, msDialogService) {

        if (!msUserService.getAccessToken()) {
            $state.go('app.pages_auth_login');
        }

        $scope.applications = msApplicationService.applications();

        msApplicationService.getAll(function(error) {
            if (error) {
                if (error.status === 401) {
                    return $state.go('app.pages_auth_login', { error: error.statusText });
                }

                return alert(error.statusText);
            }
        });

        $scope.showAddDialog = function(ev) {
            msDialogService
                .showDialog(ev, 'app/core/services/dialogs/newApplicationDialog.html');
        };

        $scope.showDeleteDialog = function(ev) {
            msDialogService
                .showDialog(ev, 'app/core/services/dialogs/deleteApplicationDialog.html');
        }

        $scope.goToAppManagement = function(appId) {
            $state.go('app.application_classes', { 'appId': appId, 'index': 0 });
            // msSchemasService.getSchemas(appId, null, function(error, results) {
            //     if (error) {
            //         if (error.status === 401) {
            //             return $state.go('app.pages_auth_login', { error: error });
            //         }

            //         return alert(error.statusText);
            //     }
            // });
        };

        // $rootScope.$on('app-added', function(event, agrs){
        //     $scope.applications.push(agrs.app);
        // });
    }
})();
