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

    function msDialogController($scope, $mdDialog, $cookies, $stateParams, ClassesService,
        msSchemasService, msApplicationService) {

        var vm = this;
        var index = $stateParams.index;
        var appId = $stateParams.appId;

        $scope.className = '';
        $scope.fields = [];
        $scope.types = ['String', 'Number'];
        $scope.type = '';

        msSchemasService.getSchema(appId, index, function(error, results) {
            if (error) {
                return alert(error.statusText);
            }

            $scope.className = results.className;
            var fields = Object.getOwnPropertyNames(results.fields);
            $scope.fields = [].concat(fields);
            $scope.fields.splice(0, 3);
        });

        $scope.createApplication = function() {
            msApplicationService.create($scope.applicationName,
                function(result) {
                    // console.log(result);
                });
            closeDialog();
        };

        $scope.createClass = function() {
            msSchemasService.createSchema($scope.className, appId, function(result) {
                // console.log(result);
                // msSchemasService.addSchema(result);
            });
            closeDialog();
        };

        $scope.addColumn = function() {
            msSchemasService.addField($scope.className, appId, $scope.columnName, $scope.type,
                function(error, results) {
                    if (error) {
                        return alert(error.statusText)
                    }
                });

            $mdDialog.hide();
        };

        $scope.deleteColumn = function() {
            var confirm = $mdDialog.confirm()
                .title('Are you sure to delete ' + $scope.columnName + ' ?')
                .ok('Yes')
                .cancel('No');
            $mdDialog.show(confirm).then(function() {
                msSchemasService.deleteField($scope.className, appId, $scope.columnName,
                    function(error, results) {
                        if (error) {
                            return alert(error.statusText);
                        }
                    });
                closeDialog();
            }, function() {
                closeDialog();
            });
        };

        function closeDialog() {
            $mdDialog.hide();
        };

        vm.closeDialog = function() {
            $mdDialog.hide();
        };
    };
})();
