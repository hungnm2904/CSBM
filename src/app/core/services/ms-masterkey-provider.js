(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msMasterKeyService', msMasterKeyServiceProvider);

    function msMasterKeyServiceProvider() {
        var service = this;
        this.$get = function($cookies, $http, msUserService, msConfigService) {
            var domain = (msConfigService.getConfig()).domain;

            var service = {
                getMasterKey: getMasterKey
            };

            return service;

            function getMasterKey(appId, callback) {
                var accessToken = msUserService.getAccessToken();
                $http({
                    method: 'GET',
                    url: domain + '/masterKey',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken,
                        'X-CSBM-Application-Id': appId
                    }
                }).then(function(response) {
                    callback(null, response.data.data.masterKey);
                }, function(response) {
                    callback(response);
                });
            };
        }
    };
})();
