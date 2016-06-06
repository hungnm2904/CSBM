(function() {
    'use strict';

    angular
    .module('app.application.classes')
    .controller('ClassesController', function($scope, $http, $cookies, $window, $state, $stateParams,
        msSchemasService, ClassesService) {

        if (!$cookies.get('accessToken')) {
                // $window.location.href = '/login';
                $state.go('app.pages_auth_login');
            }

            var obj = msSchemasService.getSchema($stateParams.index);
            console.log(obj);
            console.log(msSchemasService.getAppId());
            delete obj.fields.objectId;
            delete obj.fields.ACL;
            $scope.classes = obj.fields;

            ClassesService.getClassData(msSchemasService.getAppId(), obj.className, function (response) {
                if (response.message) {
                    alert(response.message);
                } else {
                    $scope.datas = response.data.results;
                }
            });

            console.log($scope.datas);

            

            // $http.get('/app/data/classes/classes.json').success(function(data) {
            //     $scope.classes = data.classes;
            // });

            var myform = $('#myform');

            $scope.addRow = function() {
                var key = $scope.Key;
                ClassesService.addColumn(msSchemasService.getAppId(), obj.className, key);
            };

            $scope.updateRow = function() {
                $scope.Key = '';
                var keys = [];
                for (var k in $scope.classes[0]) {
                    if (k != "id" && k != "CreateAt" && k != "UpdateAt") {
                        keys.push(k);
                    }
                }

                var strVal = [];
                for (var i = 0; i < $scope.classes.length; i++) {
                    var index = i + 1;
                    $('input[name="' + index + 'txtVal"]').each(function() {
                        strVal.push($(this).val());
                    });
                }

                for (var j = 0; j < $scope.classes.length; j++) {
                    for (var i = 0; i < keys.length; i++) {
                        $scope.classes[j][keys[i]] = strVal.shift();
                    }
                }
                console.log($scope.classes);
            };
        });
})();
