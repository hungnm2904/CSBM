(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .factory('ClassesService', function($http, $cookies, $state) {
            var service = {};
            var domain = 'http://localhost:1337';

            service.getDocuments = function(className, appId, callback) {
                $http({
                    method: 'GET',
                    url: domain + '/csbm/classes/' + className,
                    headers: {
                        'X-CSBM-Application-Id': appId
                    }
                }).then(function(response) {
                    callback(response.data.results);
                }, function(response) {
                    alert('error');
                });
            };

            service.getMasterKey = function(appId, accessToken, callback) {
                $http({
                    method: 'GET',
                    url: domain + '/masterKey',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken,
                        'X-CSBM-Application-Id': appId
                    }
                }).then(function(response) {
                    callback(response.data.data.masterKey);
                }, function(response) {
                    callback(response);
                });
            }

            service.addColumn = function(className, appId, accessToken, columnName, type, callback) {
                var data = {
                    'className': className,
                    'fields': {}
                }
                data.fields[columnName] = {
                    'type': type
                }

                service.getMasterKey(appId, accessToken, function(result) {
                    var masterKey = result;
                    $http({
                        method: 'PUT',
                        url: domain + '/csbm/schemas/' + className,
                        headers: {
                            'X-CSBM-Application-Id': appId,
                            'X-CSBM-Master-Key': masterKey,
                            'Content-Type': 'application/json'
                        },
                        data: data
                    }).then(function(response) {
                        callback(response.data);
                    }, function(response) {
                        alert('error');
                    });
                });
            }

            service.createClass = function(className, appId, accessToken, callback) {
                service.getMasterKey(appId, accessToken, function(result) {
                    var masterKey = result;
                    $http({
                        method: 'POST',
                        url: domain + '/csbm/schemas/' + className,
                        headers: {
                            'X-CSBM-Application-Id': appId,
                            'X-CSBM-Master-Key': masterKey,
                            'Content-Type': 'application/json'
                        },
                        data: {
                            'className': className,
                            'fields': {

                            }
                        }
                    }).then(function(response) {
                        callback(response.data);
                    }, function() {
                        alert('error');
                    });
                });
            }

            return service;
        });
})()
