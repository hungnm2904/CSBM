(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesController', function($scope, $http, $cookies, $window, $state, $stateParams,
            $mdDialog, $document, $rootScope, msModeService, msSchemasService, ClassesService) {

            var accessToken = $cookies.get('accessToken');
            if (!accessToken) {
                return $state.go('app.pages_auth_login');
            }

            var index = $stateParams.index;
            var appId = $stateParams.appId;
            $scope.fields = [];
            $scope.documents = [];
            $scope.columnName = '';

            msSchemasService.getSchema(appId, index, function(error, results) {
                if (error) {
                    return alert(error.statusText);
                }

                $scope.className = results.className;
                var fields = Object.getOwnPropertyNames(results.fields);
                $scope.fields = [].concat(fields);
                console.log(fields);

                for (var index in $scope.fields) {
                    if ($scope.fields[index] === 'ACL') {
                        $scope.fields.splice(index, 1);
                    }
                }

                console.log($scope.fields);

                ClassesService.getDocuments($scope.className, appId, function(results) {
                    for (var i in results) {

                        var _document = results[i];
                        var newDocument = {};

                        for (var y in $scope.fields) {
                            var field = $scope.fields[y];
                            newDocument[field] = _document[field];
                        };

                        $scope.documents.push(newDocument);
                    }
                });
            });

            $rootScope.$on('fields-change', function(event, args) {
                $scope.fields = Object.getOwnPropertyNames(args.fields);
            });

            $scope.showDialog = function(ev) {
                $mdDialog.show({
                    controller: 'DialogController',
                    controllerAs: 'vm',
                    templateUrl: 'app/main/application/classes/dialogs/dialog.html',
                    parent: angular.element($document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true
                });
            };

            $scope.showAddColumnDialog = function(ev) {
                $mdDialog.show({
                    controller: 'DialogController',
                    controllerAs: 'vm',
                    templateUrl: 'app/main/application/classes/dialogs/addColumnDialog.html',
                    parent: angular.element($document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true
                });
            }
        });
})();
