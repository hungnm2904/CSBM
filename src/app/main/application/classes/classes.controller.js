(function() {
    'use strict';

    angular
        .module('app.application.classes')
        .controller('ClassesController', function($scope, $http, $cookies, $window, $state) {
            if (!$cookies.get('accessToken')) {
                // $window.location.href = '/login';
                $state.go('app.pages_auth_login');
            }

            $http.get('/app/data/classes/classes.json').success(function(data) {
                $scope.classes = data.classes;
            });

            var myform = $('#myform');

            $scope.addRow = function() {
                var key = $scope.Key;
                var count = 0;

                for (var k in $scope.classes[0]) {
                    if (key === k) {
                        alert("Column already exists!!");
                        count++;
                    }
                }

                if (count == 0 && key != "" && key != null) {
                    $('#table thead tr').append($("<th>"));
                    $('#table thead tr>th:last').html(key);
                    $('#table tbody tr').append($("<td>"));
                    for (var i = 0; i < $scope.classes.length; i++) {
                        $scope.classes[i][key] = null;
                        var index = i + 1;
                        $('#table tbody tr:eq(' + i + ')').children('td:last')
                            .append("<input type='text' id='" + index + "txtVal' name='" + index +
                                "txtVal' ng-model='Val' placeholder='Value'>");
                    }
                }
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
