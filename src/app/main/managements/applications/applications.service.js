(function() {
    'use strict';

    angular
        .module('app.managements.applications')
        .factory('ApplicationService', function($http, $cookies, $state, $mdDialog, $document,
            msModeService, msConfigService) {

            var domain = (msConfigService.getConfig()).domain;
            var accessToken = $cookies.get('accessToken');

            var service = {};
            service.getAll = function(callback) {
                $http({
                    method: 'GET',
                    url: domain + '/application',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                }).then(function(response) {
                    callback(null, response);
                }, function(response) {
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

            return service;
        });
})()
