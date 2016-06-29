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
                msUserService.register(vm.username, vm.password, vm.email, function(response) {
                    if (response.status != 200) {
                        vm.error = response.data.message;
                    } else {
                        msUserService.login(vm.username, vm.password, function(response) {
                            $state.go('app.managements_applications');
                        });
                    }
                });
            }
        };
    }
})();
