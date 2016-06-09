(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msApplicationService', msApplicationServiceProvider);

    function msApplicationServiceProvider() {
        var service = this;
        this.$get = function($http, $cookies, $rootScope, msConfigService) {
            var domain = (msConfigService.getConfig()).domain;
            var accessToken = $cookies.get('accessToken');

            var applications = [];

            var service = {
                getAll: getAll,
                create: create
            };

            return service;

            function setNew(_applications) {
                applications = _applications;
            };

            function add(application) {
                applications.push(application);
            };

            function getAll(callback) {
                if (applications && applications.length > 0) {
                    callback(applications);
                }
                $http({
                    method: 'GET',
                    url: domain + '/application',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    setNew(response.data);
                    callback(null, applications);
                }, function(response) {
                    callback(response);
                });
            };

            function create(name, callback) {
                var data = {
                    "className": name,
                    "fields": {

                    }
                }
                $http({
                    method: 'POST',
                    url: domain + '/application/' + name,
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
        };
    };
})();
