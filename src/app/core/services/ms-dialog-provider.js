(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msDialogService', msDialogServiceProvider)
        .controller('msDialogController', msDialogController);

    function msDialogServiceProvider() {
        var service = this;
        this.$get = function($mdDialog, $document) {
            var service = {
                showDialog: showDialog
            };

            return service;

            function showDialog(ev, path) {
                $mdDialog.show({
                    controller: 'msDialogController',
                    controllerAs: 'vm',
                    templateUrl: path,
                    parent: angular.element($document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true
                });
            };
        };
    };

    function msDialogController($scope, $mdDialog, $cookies, $state, $stateParams,
        msSchemasService, msApplicationService) {

        var vm = this;
        var index = $stateParams.index;
        var appId = $stateParams.appId;
        var className;
        var appName;

        $scope.applications = [];
        $scope.applicationId;
        $scope.className = '';
        $scope.fields = [];
        $scope.types = ['String', 'Number', 'Array'];
        $scope.type = '';

        if (appId) {
            msSchemasService.getSchema(appId, index, function(error, results) {
                if (error) {
                    return alert(error.statusText);
                }

                className = results.className;
                var fields = Object.getOwnPropertyNames(results.fields);
                $scope.fields = [].concat(fields);
                $scope.fields.splice(0, 3);
            });
        } else {
            msApplicationService.getAll(function(error, results) {
                if (error) {
                    if (error.status === 401) {
                        return $state.go('app.pages_auth_login');
                    }

                    return alert(error.statusText);
                }
                $scope.applications = results;
            });
        }

        $scope.createApplication = function() {
            msApplicationService.create($scope.applicationName,
                function(result) {
                    if (result) {
                        $mdDialog.show(
                            $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title('Error')
                            .textContent('Application exists')
                            .ok('OK')
                        );
                    }
                });
            closeDialog();
        };

        $scope.deleteApplication = function(applicationName) {
            var confirm = $mdDialog.confirm()
                .title('Are you sure to delete this application ?')
                .ok('Yes')
                .cancel('No');
            $mdDialog.show(confirm).then(function() {
                msApplicationService.remove($scope.applicationId,
                    function(error, results) {
                        if (error) {
                            if (error.status === 401) {
                                return $state.go('app.pages_auth_login');
                            }
                            return alert(error.statusText);
                        }
                        console.log(results);
                    });
                closeDialog();
            }, function() {
                closeDialog();
            });
        }

        $scope.createClass = function() {
            msSchemasService.createSchema($scope.className, appId, function(result) {
                // console.log(result);
                // msSchemasService.addSchema(result);
            });
            closeDialog();
        };

        $scope.addColumn = function() {
            if ($scope.columnName === null || $scope.type === '') {
                $scope.error = 'Name and Type not allow null.';
            } else {
                $scope.error = '';
                msSchemasService.addField(className, appId, $scope.columnName, $scope.type,
                    function(error, results) {
                        if (error) {
                            if (error.status === 401) {
                                return $state.go('app.pages_auth_login');
                            }
                            $mdDialog.show(
                                $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title('Error')
                                .textContent('Column exists')
                                .ok('OK')
                            );
                        }
                    });
                $mdDialog.hide();
            }
        };

        $scope.deleteColumn = function() {
            if (!$scope.columnName) {
                $scope.error = 'Please select column.';
            } else {
                $scope.error = '';
                var confirm = $mdDialog.confirm()
                    .title('Are you sure to delete ' + $scope.columnName + ' ?')
                    .ok('Yes')
                    .cancel('No');
                $mdDialog.show(confirm).then(function() {
                    msSchemasService.deleteField(className, appId, $scope.columnName,
                        function(error, results) {
                            if (error) {
                                if (error.status === 401) {
                                    return $state.go('app.pages_auth_login');
                                }

                                return alert(error.statusText);
                            }
                        });
                    closeDialog();
                }, function() {
                    closeDialog();
                });
            }
        };

        $scope.updateField = function() {
            var confirm = $mdDialog.confirm()
                .title('Are you sure to change ?')
                .ok('Yes')
                .cancel('No');
            $mdDialog.show(confirm).then(function() {
                msApplicationService.getAppName(appId, function(error, result) {
                    appName = result.data.data.appName;

                    msSchemasService.changeField(appName, className, $scope.field, $scope.columnName, function(error, results) {
                        console.log(error);
                        console.log(results);
                    });
                });
                closeDialog();
            }, function() {
                closeDialog();
            });
        }

        function closeDialog() {
            $mdDialog.hide();
        };

        vm.closeDialog = function() {
            $mdDialog.hide();
        };
    };
})();
