(function ()
{
    'use strict';

    angular
    .module('app.pages.auth.login')
    .controller('LoginController', LoginController);

    LoginController.$inject = ['$rootScope', '$location', '$timeout', '$http', '$cookies', '$window', '$state', 'msUserService'];
    function LoginController($rootScope, $location, $timeout, $http, $cookies, $window, $state, msUserService)
    {
        var vm = this;

        vm.login = function () {
            vm.dataLoading = true;
            msUserService.login(vm.username, vm.password,function (response) {
                if(response.message){
                    vm.error = response.message;
                }
            });
        };

    };
})();