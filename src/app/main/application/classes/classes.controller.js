(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesController', function($scope, $http, $cookies, $window, $state, $stateParams,
            $mdDialog, $document, $rootScope, msModeService, msSchemasService, ClassesService, msDialogService) {

            var accessToken = $cookies.get('accessToken');
            if (!accessToken) {
                return $state.go('app.pages_auth_login');
            }

            var index = $stateParams.index;
            var appId = $stateParams.appId;
            $scope.columnName = '';
            $scope.fields = [];
            $scope.documents = [];

            var renderClass = function() {
                msSchemasService.getSchema(appId, index, function(error, results) {
                    if (error) {
                        return alert(error.statusText);
                    }

                    $scope.className = results.className;
                    $scope.schemas = results.fields
                    console.log($scope.schemas);
                    var fields = Object.getOwnPropertyNames(results.fields);
                    $scope.fields = [].concat(fields);

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
            };
            renderClass();

            $rootScope.$on('fields-change', function(event, args) {
                $scope.fields = Object.getOwnPropertyNames(args.fields);
                ClassesService.getDocuments($scope.className, appId, function(results) {
                    $scope.documents = [];

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

            var uneditableFileds = ['objectId', 'createdAt', 'updatedAt'];
            $scope.editable = function(field) {
                return uneditableFileds.indexOf(field) === -1;
            };

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

            $scope.showDelColumnDialog = function(ev) {
                $mdDialog.show({
                    controller: 'DialogController',
                    controllerAs: 'vm',
                    templateUrl: 'app/main/application/classes/dialogs/delColumnDialog.html',
                    parent: angular.element($document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true
                });
            }

            $scope.update = function() {
                var strVal = [];
                $('input[name="schemavalue"]').each(function() {
                    strVal.push($(this).val());
                });

                var keys = [];
                for (var k in $scope.documents[0]) {
                    if (k != "objectId" && k != "createdAt" && k != "updatedAt" && k != "$$hashKey") {
                        keys.push(k);
                    }
                }

                for (var i in $scope.documents) {
                    var data = {};
                    var objectId = $scope.documents[i].objectId;
                    for (var index in keys) {
                        var columnName = keys[index];
                        var value = strVal.shift();
                        if ($scope.schemas[columnName].type === "Number") {
                            value = Number(value);
                        }
                        data[columnName] = value
                    }

                    ClassesService.updateSchemas($scope.className, appId, objectId, data,
                        function(results) {});
                }
            }
        });
})();
