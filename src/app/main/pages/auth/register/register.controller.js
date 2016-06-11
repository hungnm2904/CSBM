(function() {
    'use strict';

    angular
        .module('app.pages.auth.register')
        .controller('RegisterController', RegisterController);

    RegisterController.$inject = ['$rootScope', '$location', '$timeout', '$http', '$cookies', '$window', '$state', 'msUserService'];

    function RegisterController($rootScope, $location, $timeout, $http, $cookies, $window, $state, msUserService) {
        var vm = this;

        vm.register = function() {
            vm.dataLoading = true;
            if (vm.passwordConfirm != vm.password) {
                vm.error = 'These passwords do not match. Try again?';
            } else {
                vm.error = '';
                msUserService.register(vm.username, vm.password, function(response) {
                    if (response) {
                        vm.error = response;
                    } else {
                        msUserService.login(vm.username, vm.password, function(response) {});
                    }
                });
            }
        };
    }
})();
