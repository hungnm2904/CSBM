(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msToastService', msToastServiceProvider);

    function msToastServiceProvider() {
        var service = this;
        this.$get = function($http, $cookies, $rootScope, msConfigService, $mdToast) {

            var service = {
                show: show
            };

            return service;

            function show(message, theme) {
                var toast = $mdToast.simple()
                    .textContent(message)
                    .action('OK')
                    .theme(theme + '-toast')
                    .position("bottom right");
                $mdToast.show(toast);
            }
        };
    };
})();
