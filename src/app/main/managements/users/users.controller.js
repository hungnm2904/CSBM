(function ()
{
    'use strict';

    angular
    .module('app.managements.users')
    .controller('UsersController', function($scope,$http,$cookies,$window,$state, msUserService){
        if (!msUserService.getAccessToken()) {
                $state.go('app.pages_auth_login');
            }
        
        $http.get('/app/data/users/users.json').success(function(data) {
            $scope.users = data.data;
        });
        
        $scope.dtOptions = {
            dom       : '<"top"f>rt<"bottom"<"left"<"length"l>><"right"<"info"i><"pagination"p>>>',
            pagingType: 'simple',
            autoWidth : false,
            responsive: true,
        };
    });
})();