(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msApplicationService', msApplicationServiceProvider);

    function msApplicationServiceProvider() {
        var _applications = [];

        var service = this;
        this.$get = function($http, $state, $cookies, $rootScope, msConfigService,
            msUserService) {

            var _domain = (msConfigService.getConfig()).domain;


            var service = {
                getAll: getAll,
                create: create,
                remove: remove
            };

            return service;

            function setApplications(applications) {
                _applications = applications;
            };

            function removeApplication(id) {
                _applications.forEach(function(application, index) {
                    if (application._id === id) {
                        return _applications.splice(index, 1);
                    }
                });
            }

            function add(application) {
                _applications.push(application);
                $rootScope.$broadcast('app-added', { 'app': application })
            };

            function getAll(callback) {
                if (_applications && _applications.length > 0) {
                    return callback(null, _applications);
                }

                var accessToken = msUserService.getAccessToken();
                $http({
                    method: 'GET',
                    url: _domain + '/applications',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    setApplications(response.data);
                    callback(null, _applications);
                }, function(response) {
                    callback(response);
                });
            };

            function create(name, callback) {
                var data = {
                    "applicationName": name
                }

                var accessToken = msUserService.getAccessToken();
                $http({
                    method: 'POST',
                    url: _domain + '/applications/',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    },
                    data: data
                }).then(function(response) {
                    add(response.data);
                    callback(null, response.data);
                }, function(response) {
                    callback(response);
                });
            };

            function remove(id, callback) {
                var accessToken = msUserService.getAccessToken();
                $http({
                    method: 'DELETE',
                    url: _domain + '/applications/',
                    headers: {
                        'X-CSBM-Application-Id': id,
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    removeApplication(id);
                    callback(null, _applications);
                }, function(response) {
                    callback(response);
                });
            }
        };
    };
})();
