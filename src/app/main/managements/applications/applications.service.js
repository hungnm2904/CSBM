(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .factory('ApplicationService', function($http, $cookies, $state, $mdDialog, $document,
            msModeService, msSchemasService) {

            var service = {};
            var domain = 'http://192.168.1.32:1337';
            var accessToken = $cookies.get('accessToken');

            service.getAll = function(callback) {
                $http({
                    method: 'GET',
                    url: domain + '/application',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    response.message = '';
                    callback(response);
                }, function(response) {
                    response.message = 'error';
                    callback(response);
                });
            };

            service.showDialog = function(ev) {
                $mdDialog.show({
                    controller: 'DialogController',
                    controllerAs: 'vm',
                    templateUrl: 'app/main/managements/applications/dialogs/dialog.html',
                    parent: angular.element($document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true
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

            service.getSchema = function(appId) {
                service.getMasterKey(appId, function(masterKey) {
                    $http({
                        method: 'GET',
                        url: domain + '/csbm/schemas',
                        headers: {
                            'X-CSBM-Application-Id': appId,
                            'X-CSBM-Master-Key': masterKey
                        }
                    }).then(function(response) {
                        // console.log(response);
                        var schemas = response.data.results;
                        msModeService.setMode('application');
                        msSchemasService.setAppId(appId);
                        msSchemasService.setSchemas(schemas);
                    }, function(response) {
                        alert(response.data.data.message);
                        console.log(response);
                    });

                });
            }

            return service;
        });
})()
