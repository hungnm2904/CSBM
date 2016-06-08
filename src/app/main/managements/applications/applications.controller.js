(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .controller('ApplicationsController',
            function($scope, $http, $cookies, $window, $state, $rootScope, ApplicationService,
                msModeService, msSchemasService) {

                if (!$cookies.get('accessToken')) {
                    $state.go('app.pages_auth_login');
                }

                $scope.applications = [];

                ApplicationService.getAll(function(error, results) {
                    if (error) {
                        console.log(error);
                        alert(response.statusText);
                    } else {
                        $scope.applications = results.data;
                    }
                });

                $scope.showDialog = function() {
                    ApplicationService.showDialog();
                };

                $scope.goToAppManagement = function(appId) {
                    msSchemasService.getSchemas(appId, function(error, results) {
                        if (error) {
                            return alert(error.statusText);
                        }
                    });
                };
            });
})();
