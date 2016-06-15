(function() {
    'user strict';

    angular
        .module('app.core')
        .provider('msConfigService', msConfigServiceProvider);

    function msConfigServiceProvider() {
        var config = {
        	domain: 'http://localhost:1337'
        };

        service = this;

        service.getConfig = getConfig;

        function getConfig() {
            return config;
        };

        this.$get = function() {
            var service = {
            	getConfig: getConfig
            }

            return service;
        };
    };
})();
