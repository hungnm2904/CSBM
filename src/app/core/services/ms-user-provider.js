(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msUserService', msUserServiceProvider);

    function msUserServiceProvider() {
        var $rootScope = angular.injector(['ng']).get('$rootScope');

        var _user = [];

        this.$get = function($rootScope, $http, $cookies, msConfigService, msMasterKeyService,
            msModeService, $state) {

            var _domain = (msConfigService.getConfig()).domain;

            var service = {
                login: login,
                logout: logout,
                register: register
            }

            return service;

            function login(username, password, callback) {
                $http({
                    method: 'POST',
                    url: _domain + '/login',
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
                    $cookies.put('accessToken', response.data.data.token);
                    $cookies.put('username', username);
                    $state.go('app.managements_applications');
                    response.message = '';
                    callback(response);
                }, function(response) {
                    response.message = 'Username or password is incorrect';
                    callback(response);
                });
            };

            function logout(callback) {
                var accessToken = $cookies.get('accessToken');
                $http({
                    method: 'GET',
                    url: _domain + '/signout',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    $cookies.remove('accessToken');
                    $cookies.remove('username');
                    $state.go('app.pages_auth_login');
                    response.message = '';
                    callback(response);
                }, function(response) {
                    response.message = 'error';
                    callback(response);
                });
            };

            function register(username, password, callback) {
                $http({
                    method: 'POST',
                    url: _domain + '/signup',
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
                    callback(response);
                }, function(response) {
                    callback(response);
                });
            };
        };
    };
})();
