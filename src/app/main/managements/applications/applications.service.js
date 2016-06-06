(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .factory('ApplicationService', function($http, $cookies, $state, $mdDialog, $document,
            msNavigationService) {

            var service = {};
            var domain = 'http://localhost:1337';
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
                    url: domain + '/schemas',
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
                        console.log(response);
                        var schemas = response.data.results;

                        //Navigation
                        msNavigationService.saveItem('application', {
                            title: 'Application',
                            group: true,
                            weight: 1,
                        });

                        msNavigationService.saveItem('application.classes', {
                            title: 'Classes',
                            icon: 'icon-library-plus'
                                // state: 'app.application_classes'
                        });

                        for (var i = 0; i < schemas.length; i++) {
                            var schema = schemas[i];

                            // Create navigation for schema
                            msNavigationService.saveItem('application.classes.' + schema.className, {
                                title: schema.className,
                                icon: 'icon-apps',
                                state: 'application.classes_' + schema.className
                            });
                        }
                    }, function(response) {
                        alert(response.data.data.message);
                        console.log(response);
                    });

                });
            }

            return service;
        });
})()
