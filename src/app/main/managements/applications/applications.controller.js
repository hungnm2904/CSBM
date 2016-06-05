(function ()
{
    'use strict';

    angular
    .module('app.managements.applications')
    .controller('ApplicationsController', function($scope, $http, $cookies, $window, $state, ApplicationService){
        if(!$cookies.get('accessToken')){
            // $window.location.href = '/login';
            $state.go('app.pages_auth_login');
        }
        // $http.get('/app/data/applications/applications.json').success(function(data) {
        //     $scope.applications = data.data;
        // });

        var obj = {};
        ApplicationService.getAll(function (response) {
            if(response.message) {
                alert(response.message);
            } else {
                $scope.applications = response;
                console.log($scope.applications);
            }
        });

        $scope.showDialog = function () {
            ApplicationService.showDialog();
        }
    });
})();