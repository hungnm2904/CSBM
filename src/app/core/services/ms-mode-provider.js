(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msModeService', msModeServiceProvider);

    function msModeServiceProvider() {
        var mode = 'user';
        var service = this;

        service.setToUserMode = setToUserMode;
        service.setToApplicationMode = setToApplicationMode;
        service.isApplicationMode = isApplicationMode;
        service.isUserMode = isUserMode;

        function setToUserMode() {
            mode = 'user';
        };

        function setToApplicationMode() {
            mode = 'application'
        };

        function isApplicationMode(_mode) {
            return mode === 'application';
        };

        function isUserMode() {
            return mode === 'user';
        };

        this.$get = function() {
            var service = {
                setToUserMode: setToUserMode,
                setToApplicationMode: setToApplicationMode,
                isApplicationMode: isApplicationMode,
                isUserMode: isUserMode
            }

            return service;
        };
    };
})();
