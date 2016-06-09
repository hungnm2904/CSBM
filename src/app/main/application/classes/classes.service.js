(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .factory('ClassesService', function($http, $cookies, $state, msConfigService) {
            var service = {};
            var domain = (msConfigService.getConfig()).domain;

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

            service.delColumn = function(className, appId, accessToken, columnName, callback) {
                var data = {
                    'className': className,
                    'fields': {}
                }
                data.fields[columnName] = {
                    '__op': 'Delete'
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

            service.updateSchemas = function(className, appId, accessToken, columnName, value, objectId, callback) {
                var data = {};
                data[columnName] = value;

                service.getMasterKey(appId, accessToken, function(result) {
                    var masterKey = result;
                    $http({
                        method: 'PUT',
                        url: domain + '/csbm/classes/' + className + '/' + objectId,
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

            return service;
        });
})()
