(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('DialogController', DialogController);

    /** @ngInject */
    function DialogController($scope, $mdDialog, $cookies, $stateParams, ClassesService, msSchemasService, $rootScope) {
        var vm = this;

        // Data
        $scope.className = '';
        var index = $stateParams.index;
        var appId = $stateParams.appId;

        msSchemasService.getSchema(appId, index, function(error, results) {
            if (error) {
                return alert(error.statusText);
            }

            $scope.className = results.className;
            var fields = Object.getOwnPropertyNames(results.fields);
            $scope.fields = [].concat(fields);
            $scope.fields.splice(0, 4);
        });

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
            ClassesService.createClass($scope.className, appId, accessToken, function(result) {
                console.log(result);
                // msSchemasService.addSchema(result);
            });
            $mdDialog.hide();
        };

        $scope.addColumn = function() {
            ClassesService.addColumn($scope.className, appId, accessToken, $scope.columnName,
                $scope.type,
                function(result) {
                    msSchemasService.updateFields($scope.className, result.fields);
                    console.log(result);
                });

            $mdDialog.hide();
        };

        $scope.delColumn = function() {
            ClassesService.delColumn($scope.className, appId, accessToken, $scope.columnName,
                function(result) {
                    msSchemasService.updateFields($scope.className, result.fields);
                    console.log(result);
                });
            $mdDialog.hide();
        };

        function closeDialog() {
            $mdDialog.hide();
        };
    }
})();
