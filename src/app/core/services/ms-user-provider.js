(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msUserService', msUserServiceProvider);

    function msUserServiceProvider() {
        // var $rootScope = angular.injector(['ng']).get('$rootScope');

        var _currentUser;

        this.$get = function($http, $cookies, msConfigService) {

            var _domain = (msConfigService.getConfig()).domain;

            var service = {
                login: login,
                logout: logout,
                register: register,
                getCurrentUser: getCurrentUser,
                getAccessToken: getAccessToken,
                getCurrentUsername: getCurrentUsername
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
                    var user = {
                        'userId': response.data.data.userId,
                        'username': response.data.data.name,
                        'accessToken': response.data.data.token
                    }

                    var expiresDate = new Date();
                    expiresDate.setDate(expiresDate.getDate() + 1);
                    $cookies.putObject('USER', user, { expires: expiresDate });
                    setCurrentUser(user);
                    callback(null, _currentUser);
                }, function(response) {
                    callback(response);
                });
            };

            function logout(callback) {
                var accessToken = getAccessToken();
                $http({
                    method: 'GET',
                    url: _domain + '/signout',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    deleteCurrentUser();
                    callback(null, response);
                }, function(response) {
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

            function setCurrentUser(user) {
                _currentUser = user;
            };

            function getCurrentUser() {
                if (_currentUser) {
                    return _currentUser;
                }

                var user = $cookies.getObject('USER');
                if (user) {
                    setCurrentUser(user);
                    return _currentUser;
                }

                return null;
            };

            function getAccessToken() {
                if (_currentUser) {
                    return _currentUser.accessToken;
                }

                var user = $cookies.getObject('USER');
                if (user) {
                    setCurrentUser(user);
                    return _currentUser.accessToken;
                }

                return null;
            }

            function getCurrentUsername() {
                if (_currentUser) {
                    return _currentUser.username;
                }

                var user = $cookies.getObject('USER');
                if (user) {
                    setCurrentUser(user);
                    return _currentUser.username;
                }

                return null;
            }

            function deleteCurrentUser() {
                _currentUser = null;
                $cookies.remove('USER');
            };
        };
    };
})();
