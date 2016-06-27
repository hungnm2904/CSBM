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
            var checked = [];
            var objectIdList = [];
            $scope.columnName = '';
            $scope.fields = [];
            $scope.documents = [];
            $scope.add = [];

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

                            $scope.documents.forEach(function(_document) {
                                objectIdList.push(_document.objectId);
                            });
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
                console.log($scope.schemas);
                $scope.documents.forEach(function(_document, index) {
                    var newDocument = {};
                    for (var key in _document) {
                        if (key != "objectId" && key != "createdAt" &&
                            key != "updatedAt") {

                            var value = _document[key];

                            if (value) {
                                if ($scope.schemas[key].type === 'Number') {
                                    value = Number(value);
                                }
                                if ($scope.schemas[key].type === 'Array' && value.constructor !== Array &&
                                    value.length > 0) {

                                    value = value.split(',');
                                    value = value.map(function(v) {
                                        return v.trim();
                                    });
                                }
                            }
                            newDocument[key] = value;
                        }
                    }
                    data.push(newDocument);
                });

                data.forEach(function(d, i) {
                    var objectId = $scope.documents[i].objectId;
                    msSchemasService.updateValues($scope.className, appId,
                        objectId, d,
                        function(results) {});
                });
            };

            $scope.toggle = function(objectId) {
                var index = checked.indexOf(objectId);
                if (index === -1) {
                    checked.push(objectId);
                } else {
                    checked.splice(index, 1);
                }
            };

            $scope.exists = function(objectId) {
                return checked.indexOf(objectId) > -1;
            };

            $scope.isIndeterminate = function() {
                return (checked.length !== 0 &&
                    checked.length !== objectIdList.length);
            };

            $scope.isChecked = function() {
                return checked.length === objectIdList.length;
            };

            $scope.toggleAll = function() {
                if (checked.length === objectIdList.length) {
                    checked = [];
                } else if (checked.length === 0 || checked.length > 0) {
                    checked = objectIdList.slice(0);
                }
            }

            $scope.deleteRow = function() {
                msSchemasService.deleteDocuments($scope.className, appId, checked,
                    function(error, results) {
                        if (error) {
                            return alert(error.statusText);
                        }

                        results.forEach(function(objectId, index) {
                            $scope.documents.forEach(function(_document, index) {
                                if (_document.objectId === objectId) {
                                    return $scope.documents.splice(index, 1);
                                }
                            });
                        });
                    });


                // checked.forEach(function(objectId) {
                //     msSchemasService.deleteDocuments($scope.className, appId, objectId,
                //         function(results) {});
                // });
                // msToastService.show('Delete row(s) successful.', 'success');
            };

            $scope.addRow = function() {
                var newSchema = {};
                $scope.fields.forEach(function(field) {
                    if (field != 'objectId' && field != 'createdAt' &&
                        field != 'updatedAt') {

                        var value = $scope.add[field];
                        if ($scope.schemas[field].type === 'Number') {
                            value = Number(value);
                        }

                        if ($scope.schemas[field].type === 'Array' && value.length > 0) {

                            value = value.split(',');
                            value = value.map(function(v) {
                                return v.trim();
                            });
                        }

                        newSchema[field] = value;
                    }
                });
                msSchemasService.createDocument($scope.className, appId, newSchema,
                    function(error, results) {
                        if (error) {
                            return alert(error.statusText);
                        }

                        $scope.add = [];
                        $scope.documents.push(results);
                    });
            };

            $scope.updateField = function(ev) {
                msDialogService
                    .showDialog(ev, 'app/core/services/dialogs/updateField.html');
            };

            $scope.dtOptions = {
                dom: '<"top"f>rt<"bottom"<"left"<"length"l>><"right"<"info"i><"pagination"p>>>',
                pagingType: 'full_numbers',
                autoWidth: false,
                responsive: false,
            };
        });
})();
