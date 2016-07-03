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
            $scope.fields_add = [];
            $scope.documents = [];
            $scope.add = [];
            $scope.addNewSchema = false;

            var skip;
            $scope.numPerPage = 5;

            var renderClass = function() {
                msSchemasService.getSchema(appId, index, function(error, results) {
                    if (error) {
                        if (error.status === 401) {
                            return $state.go('app.pages_auth_login');
                        }

                        return alert(error.statusText);
                    }

                    $scope.className = results.className;
                    $scope.schemas = results.fields;
                    var fields = Object.getOwnPropertyNames(results.fields);
                    $scope.fields = [].concat(fields);
                    $scope.fields_add = [].concat(fields);
                    $scope.fields_add.splice(0, 3);

                    pagination();
                });
            };

            var pagination = function() {
                if ($scope.currentPage === 1) {
                    skip = 0;
                } else {
                    skip = ($scope.currentPage - 1) * $scope.numPerPage;
                }
                msSchemasService.getDocuments(appId, $scope.className, $scope.numPerPage, skip,
                    function(error, results, count) {
                        if (error) {
                            return alert(error.statusText);
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
                        $scope.documents.forEach(function(_document) {
                            objectIdList.push(_document.objectId);
                        });

                        $scope.totalItems = count;
                    });
            }

            var sort = function() {
                $scope.predicate = 'updatedAt';
                $scope.reverse = true;
                $scope.currentPage = 1;
                $scope.order = function(predicate) {
                    $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
                    $scope.predicate = predicate;
                };
            };

            $scope.$watch('currentPage + numPerPage', function() {
                pagination();
                checked = [];
            });

            renderClass();
            sort();

            $rootScope.$on('fields-change', function(event, args) {
                var fields = Object.getOwnPropertyNames(args.fields);
                $scope.fields = [].concat(fields);
                $scope.fields_add = [].concat(fields);
                $scope.fields_add.splice(0, 3);
                
                pagination();
            });

            // $rootScope.$on('field-name-changed', function(event, args) {
            //     var fieldName = args.fieldName;
            //     var newFieldName = args.newFieldName;

            //     $scope.fields.forEach(function(field, index) {
            //         if (field === fieldName) {
            //             return $scope.fields[index] = newFieldName;
            //         }
            //     });
            // });

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

            $scope.updateField = function(ev) {
                msDialogService
                    .showDialog(ev, 'app/core/services/dialogs/updateField.html');
            };

            $scope.updateValues = function() {
                var data = [];
                var checkType = true;
                var errorMessage;
                var titleMessage;

                $scope.documents.forEach(function(_document, index) {
                    var newDocument = {};

                    for (var key in _document) {
                        if (key != "objectId" && key != "createdAt" &&
                            key != "updatedAt") {

                            var value = _document[key];

                            if (value) {
                                if ($scope.schemas[key].type === 'Number') {

                                    if (!Number(value) && value) {
                                        checkType = false;
                                        errorMessage = key + ' column has type is: ' + $scope.schemas[key].type;
                                        titleMessage = 'Update Values Fail';
                                    } else {
                                        value = Number(value);
                                    }
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
                    if (checkType) {
                        data.push(newDocument);
                    }
                });
                if (checkType) {
                    data.forEach(function(d, i) {
                        var objectId = $scope.documents[i].objectId;
                        msSchemasService.updateValues($scope.className, appId,
                            objectId, d,
                            function(results) {});

                        pagination();
                    });
                } else {
                    msDialogService.showAlertDialog(titleMessage, errorMessage);
                }
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
                if (objectIdList != 0) {
                    return checked.length === objectIdList.length;
                } else {
                    return false;
                }
            };

            $scope.toggleAll = function() {
                if (checked.length === objectIdList.length) {
                    checked = [];
                } else if (checked.length === 0 || checked.length > 0) {
                    checked = objectIdList.slice(0);
                }
            };

            $scope.checkList = function() {
                return checked.length;
            }

            $scope.deleteRow = function() {
                var confirm = $mdDialog.confirm()
                    .title('Are you sure to delete this rows ?')
                    .ok('Yes')
                    .cancel('No');

                $mdDialog.show(confirm).then(function() {
                    msSchemasService.deleteDocuments($scope.className, appId, checked,
                        function(error, results) {
                            if (error) {
                                return alert(error.statusText);
                            }

                            pagination();
                        });
                }, function() {});
            };

            $scope.addRow = function() {
                var newSchema = {};
                var checkType = true;
                var errorMessage;
                var titleMessage;
                $scope.fields.forEach(function(field) {
                    if (field != 'objectId' && field != 'createdAt' &&
                        field != 'updatedAt') {

                        var value = $scope.add[field];

                        if ($scope.schemas[field].type === 'Number') {
                            if (!Number(value) && value) {
                                checkType = false;
                                errorMessage = field + ' column has type is: ' + $scope.schemas[field].type;
                                titleMessage = 'Add New Row Fail';
                            } else {
                                value = Number(value);
                            }
                        }

                        if ($scope.schemas[field].type === 'Array' && value.length > 0) {

                            value = value.split(',');
                            value = value.map(function(v) {
                                return v.trim();
                            });
                        }

                        if (checkType) {
                            newSchema[field] = value;
                        }
                    }
                });
                if (checkType) {
                    msSchemasService.createDocument($scope.className, appId, newSchema,
                        function(error, results) {
                            if (error) {
                                return alert(error.statusText);
                            }
                            $scope.add = [];
                            
                            pagination();

                            objectIdList.push(results.objectId);
                        });
                    $scope.addNewSchema = false;
                } else {
                    msDialogService.showAlertDialog(titleMessage, errorMessage);
                }
            };

            $scope.showAddRow = function() {
                $scope.addNewSchema = !$scope.addNewSchema;
            }

            $scope.cancelAddRow = function() {
                $scope.addNewSchema = false;
                $scope.fields.forEach(function(field) {
                    $scope.add[field] = '';
                });
            }

            // $scope.dtOptions = {
            //     dom: '<"top"f>rt<"bottom"<"left"<"length"l>><"right"<"info"i><"pagination"p>>>',
            //     pagingType: 'full_numbers',
            //     autoWidth: false,
            //     responsive: false,
            // };
        });
})();
