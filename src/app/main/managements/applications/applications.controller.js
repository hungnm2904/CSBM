(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .controller('ApplicationsController',
            function($scope, $http, $cookies, $window, $state, ApplicationService, msModeService) {

                if (!$cookies.get('accessToken')) {
                    // $window.location.href = '/login';
                    $state.go('app.pages_auth_login');
                }
                // $http.get('/app/data/applications/applications.json').success(function(data) {
                //     $scope.applications = data.data;
                // });

                var obj = {};
                ApplicationService.getAll(function(response) {
                    if (response.message) {
                        alert(response.message);
                    } else {
                        $scope.applications = response.data;
                        console.log($scope.applications);
                    }
                });

                $scope.showDialog = function() {
                    ApplicationService.showDialog();
                };

                $scope.goToAppManagement = function(appId) {
                    msModeService.setMode('application');
                    ApplicationService.getSchema(appId);
                };
            });
})();
