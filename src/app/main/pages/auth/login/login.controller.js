(function ()
{
    'use strict';

    angular
    .module('app.pages.auth.login')
    .controller('LoginController', LoginController);

    /** @ngInject */
    LoginController.$inject = ['$rootScope', '$location', '$timeout','$http'];
    function LoginController($rootScope, $location, $timeout,$http)
    {
        var vm = this;
        vm.login = function () {
            vm.dataLoading = true;
            Login(vm.form.email, vm.form.password);
        };

        vm.createApplication = function()
        {
            createApplication();
        }

        vm.logout = function () {
            console.log('sign out');
            $location.path('/');
        };

        

        function Login(email, password) {
            $http({
                method: 'POST',
                url: 'http://192.168.1.29:1337/login',
                data: {
                    username: email,
                    password: password
                }
            }).then(function (response) {
                console.log(response);
                
            // this callback will be called asynchronously
            // when the response is available
        }, function (response) {
            alert("error");
            // called asynchronously if an error occurs
            // or server returns response with an error status.
        });
        };
        function createApplication(){
            $http({
                method: 'GET',
                url: 'http://192.168.1.29:1337/application/create/toanpb'
            }).then(function (response) {
                console.log(response);
            // this callback will be called asynchronously
            // when the response is available
        }, function (response) {
            alert("error");
            // called asynchronously if an error occurs
            // or server returns response with an error status.
        });
        };
    };


})();