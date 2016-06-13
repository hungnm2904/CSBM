(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesController', function($scope, $http, $cookies, $window,
            $state, $stateParams, $mdDialog, $document, $rootScope, msModeService,
            msSchemasService, msDialogService, msToastService, msUserService) {

            if (!msUserService.getAccessToken()) {
                $state.go('app.pages_auth_login');
            }

            var index = $stateParams.index;
            var appId = $stateParams.appId;
            $scope.columnName = '';
            $scope.fields = [];
            $scope.documents = [];

            var renderClass = function() {
                msSchemasService.getSchema(appId, index, function(error, results) {
                    if (error) {
                        if (error.status === 401) {
                            return $state.go('app.pages_auth_login');
                        }

                        return alert(error.statusText);
                    }

                    $scope.className = results.className;
                    $scope.schemas = results.fields
                    var fields = Object.getOwnPropertyNames(results.fields);
                    $scope.fields = [].concat(fields);

                    msSchemasService.getDocuments($scope.className, appId,
                        function(error, results) {

                            if (error) {
                                return alert(error.statusText);
                            }

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
                msSchemasService.getDocuments($scope.className, appId,
                    function(error, results) {
                        if (error) {
                            return results.statusText;
                        }

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

            $scope.showAddClassDialog = function(ev) {
                msDialogService
                    .showDialog(ev, 'app/core/services/dialogs/addClassDialog.html');
            };

            $scope.showAddColumnDialog = function(ev) {
                msDialogService
                    .showDialog(ev, 'app/core/services/dialogs/addColumnDialog.html');
            };

            $scope.showDeleteColumnDialog = function(ev) {
                msDialogService
                    .showDialog(ev, 'app/core/services/dialogs/deleteColumnDialog.html');
            };

            $scope.updateValues = function() {
                var data = [];
                $scope.documents.forEach(function(_document) {
                    var newDocument = {};
                    for (var key in _document) {
                        if (key != "objectId" && key != "createdAt" &&
                            key != "updatedAt") {

                            var value = _document[key];

                            if ($scope.schemas[key].type === "Number") {
                                value = Number(value);
                            }
                            // msToastService.show(key + ' must be ' + $scope.schemas[key].type, 'error');

                            newDocument[key] = value;
                        }
                    }
                    data.push(newDocument);
                });

                console.log(data);

                data.forEach(function(d, i) {
                    var objectId = $scope.documents[i].objectId;
                    msSchemasService.updateValues($scope.className, appId,
                        objectId, d,
                        function(results) {});
                });
                msToastService.show('Update values successful.', 'success');
            };

            var checked = [];
            $scope.toggle = function(objectId) {
                var index = checked.indexOf(objectId);
                if (index === -1) {
                    checked.push(objectId);
                } else {
                    checked.splice(index, 1);
                }
            };

            $scope.exists = function() {
                return checked.length === 0;
            };

            $scope.deleteRow = function() {
                console.log(checked);
                checked.forEach(function(objectId) {
                    msSchemasService.deleteRow($scope.className, appId, objectId,
                        function(results) {});
                });
                msToastService.show('Delete row(s) successful.', 'success');
            };

            $scope.add = [];
            $scope.addRow = function() {
                var newSchema = {};
                $scope.fields.forEach(function(field) {
                    if (field != "objectId" && field != "createdAt" &&
                        field != "updatedAt") {
                        newSchema[field] = $scope.add[field];
                    }
                });
                msSchemasService.addRow($scope.className, appId, newSchema,
                    function(results) {});
            };
        });
})();
