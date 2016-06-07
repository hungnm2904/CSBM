(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('DialogController', DialogController);

    /** @ngInject */
    function DialogController($scope, $mdDialog, $cookies, $stateParams, ClassesService, msSchemasService) {
        var vm = this;

        // Data
        var schemaObj = msSchemasService.getSchema($stateParams.index);
        $scope.className = '';
        $scope.appId = msSchemasService.getAppId();
        var accessToken = $cookies.get('accessToken');
        if (!accessToken) {
            $state.go('app.pages_auth_login');
        }
        $scope.types = ['String', 'Number'];
        $scope.type = '';
        $scope.columnName = '';
        //////////

        vm.closeDialog = function() {
            $mdDialog.hide();
        };

        $scope.createClass = function() {
            ClassesService.createClass($scope.className, $scope.appId, accessToken, function(result) {
                console.log(result);
                msSchemasService.addSchema(result);
            });
            $mdDialog.hide();
        };

        $scope.addColumn = function() {
            ClassesService.addColumn(schemaObj.className, $scope.appId, accessToken, $scope.columnName,
                $scope.type,
                function(result) {
                    msSchemasService.updateFields(schemaObj.className, result.fields);
                    console.log(result);
                });
            $mdDialog.hide();
        };

        function closeDialog() {
            $mdDialog.hide();
        };
    }
})();
