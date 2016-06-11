(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesController', function($scope, $http, $cookies, $window, $state,
            $stateParams, $mdDialog, $document, $rootScope, msModeService, msSchemasService,
            ClassesService, msDialogService) {

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
                msDialogService.showDialog(ev, 'app/core/services/dialogs/addClassDialog.html');
            };

            $scope.showAddColumnDialog = function(ev) {
                msDialogService.showDialog(ev, 'app/core/services/dialogs/addColumnDialog.html');
            }

            $scope.showDeleteColumnDialog = function(ev) {
                msDialogService.showDialog(ev, 'app/core/services/dialogs/deleteColumnDialog.html');
            }

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

                // for (var i in $scope.documents) {
                //     var data = {};
                //     var objectId = $scope.documents[i].objectId;
                //     for (var index in keys) {
                //         var columnName = keys[index];
                //         var value = strVal.shift();
                //         if ($scope.schemas[columnName].type === "Number") {
                //             value = Number(value);
                //         }
                //         data[columnName] = value
                //     }

                //     ClassesService.updateSchemas($scope.className, appId, objectId, data,
                //         function(results) {});
                // }
            }
        });
})();
