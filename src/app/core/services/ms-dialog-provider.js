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

    function msDialogController($scope, $mdDialog, $cookies, $stateParams, ClassesService, msSchemasService,
        msApplicationService) {

        var vm = this;

        // // Data
        // var schemaObj = msSchemasService.getSchema($stateParams.index);
        // $scope.className = '';
        // // $scope.appId = msSchemasService.getAppId();
        // var accessToken = $cookies.get('accessToken');
        // if (!accessToken) {
        //     $state.go('app.pages_auth_login');
        // }
        // $scope.types = ['String', 'Number'];
        // $scope.type = '';
        // $scope.columnName = '';

        // $scope.fields = Object.getOwnPropertyNames(schemaObj.fields);
        // $scope.fields.splice(0, 4);
        // //////////

        vm.closeDialog = function() {
            $mdDialog.hide();
        };

        // $scope.createClass = function() {
        //     ClassesService.createClass($scope.className, $scope.appId, accessToken, function(result) {
        //         console.log(result);
        //         msSchemasService.addSchema(result);
        //     });
        //     $mdDialog.hide();
        // };

        // $scope.addColumn = function() {
        //     ClassesService.addColumn(schemaObj.className, $scope.appId, accessToken, $scope.columnName,
        //         $scope.type,
        //         function(result) {
        //             msSchemasService.updateFields(schemaObj.className, result.fields);
        //             console.log(result);
        //         });
        //     $mdDialog.hide();
        // };

        // $scope.delColumn = function() {
        //     ClassesService.delColumn(schemaObj.className, $scope.appId, accessToken, $scope.columnName,
        //         function(result) {
        //             msSchemasService.updateFields(schemaObj.className, result.fields);
        //             console.log(result);
        //         });
        //     $mdDialog.hide();
        // };

        $scope.createApplication = function() {
            msApplicationService.create($scope.applicationName,
                function(result) {
                    console.log(result);
            });
            $mdDialog.hide();
        }

        function closeDialog() {
            $mdDialog.hide();
        };
    };
})();
