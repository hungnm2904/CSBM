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

            for (var index in $scope.fields) {
                if ($scope.fields[index] === 'ACL') {
                    $scope.fields.splice(index, 1);
                }
            }

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
            // console.log(strVal);

            var keys = [];
            for (var k in $scope.documents[0]) {
                if (k != "objectId" && k != "createdAt" && k != "updatedAt" && k != "$$hashKey") {
                    keys.push(k);
                }
            }
            // console.log(keys);

            for(var i in $scope.documents) {
                for(var index in keys) {
                    var className = $scope.className;
                    var columnName = keys[index];
                    var value = strVal.shift();
                    var objectId = $scope.documents[i].objectId;
                    ClassesService.updateSchemas(className, appId, accessToken, columnName, value, objectId, function(results) {
                        console.log(results);
                    });
                }
            }
        }
    });
})();
