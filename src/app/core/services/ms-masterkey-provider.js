(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msMasterKeyService', msMasterKeyServiceProvider);

    function msMasterKeyServiceProvider() {
        var service = this;
        this.$get = function(msConfigService, $cookies, $http) {
        	var domain = (msConfigService.getConfig()).domain;
        	var accessToken = $cookies.get('accessToken');

            var service = {
                getMasterKey: getMasterKey
            };

            return service;

            function getMasterKey(appId, callback) {
                $http({
                    method: 'GET',
                    url: domain + '/masterKey',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken,
                        'X-CSBM-Application-Id': appId
                    }
                }).then(function(response) {
                    callback(null, response);
                }, function(response) {
                    callback(response);
                });
            };
        }
    };
})();
