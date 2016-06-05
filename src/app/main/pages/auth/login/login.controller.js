(function ()
{
    'use strict';

    angular
    .module('app.pages.auth.login')
    .controller('LoginController', LoginController);

    /** @ngInject */
    LoginController.$inject = ['$rootScope', '$location', '$timeout', '$http', '$cookies', '$window', '$state', 'LoginService'];
    function LoginController($rootScope, $location, $timeout, $http, $cookies, $window, $state, LoginService)
    {
        var vm = this;

        vm.login = function () {
            vm.dataLoading = true;
            LoginService.Login(vm.username, vm.password,function (response) {
                if(response.message){
                    vm.error = response.message;
                }
            });
        };

    };
})();