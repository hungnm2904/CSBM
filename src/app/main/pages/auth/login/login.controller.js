(function() {
    'use strict';

    angular
        .module('app.pages.auth.login')
        .controller('LoginController', LoginController);

    function LoginController($state, msUserService) {
        var vm = this;
        // if ($state.params.error) {
        //     vm.error = error;
        // }
        vm.login = function() {
            vm.dataLoading = true;
            msUserService.login(vm.username, vm.password, function(error, results) {
                if (error) {
                    return vm.error = error.data.message;
                }
                $state.go('app.managements_applications');

                var user = msUserService.getCurrentUsername();
                console.log(user);
            });
        };
    };
})();
