(function() {
    'use strict';

    angular
    .module('app.application.classes')
    .factory('ClassesService', function($http, $cookies, $state) {
        var service = {};
        var domain = 'http://192.168.1.29:1337';

        service.getClassData = function(appId, className, callback) {
            $http({
                method: 'GET',
                url: domain + '/csbm/classes/' + className,
                headers: {
                    'X-CSBM-Application-Id': appId
                }
            }).then(function(response) {
                console.log(response);

                response.message = '';
                callback(response);
                    // $window.location.href = '/managements/applications';

                    // this callback will be called asynchronously
                    // when the response is available
                }, function(response) {
                    response.message = 'error';
                    callback(response);
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
        };

        service.addColumn = function(appId, className, newField) {
            service.getMasterKey(appId, className, function(masterKey) {
                $http({
                    method: 'PUT',
                    url: domain + '/csbm/classes/' + className,
                    headers: {
                        'X-CSBM-Application-Id': appId,
                        'X-CSBM-MasterKey': masterKey
                    },
                    data: {
                        'classname': className,
                        'fields': {
                            "'"+newField+"'": {
                                'type': 'String'
                            }
                        }
                    }
                }).then(function(response) {
                    console.log(response);
                       // $window.location.href = '/managements/applications';

                    // this callback will be called asynchronously
                    // when the response is available
                }, function(response) {
                    response.message = 'error';
                    alert('error');
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
            });
        };

        service.getMasterKey = function(appId, callback) {
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

        return service;
    });
})()
