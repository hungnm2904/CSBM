(function() {
    'use strict';

    angular
        .module('app.pages.auth.login')
        .factory('LoginService', function($http, $cookies, $state) {
            var service = {};
            var domain = 'http://192.168.1.32:1337';

            service.Login = function(username, password, callback) {
                $http({
                    method: 'POST',
                    url: domain + '/login',
                    data: {
                        username: username,
                        password: password
                    }
                }).then(function(response) {
                    var obj = {
                        currentUser: {
                            userId: response.data.data.userId,
                            token: response.data.data.token
                        }
                    };
                    // $cookies.putObject('accessToken', obj);
                    $cookies.put('accessToken', response.data.data.token);
                    $state.go('app.managements_applications');
                    response.message = '';
                    callback(response);
                    // $window.location.href = '/managements/applications';

                    // this callback will be called asynchronously
                    // when the response is available
                }, function(response) {
                    response.message = 'Username or password is incorrect';
                    callback(response);
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
            };

            return service;
        });
})()
