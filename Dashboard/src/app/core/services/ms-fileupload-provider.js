(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msFileUploadService', msFileUploadServiceProvider);

    function msFileUploadServiceProvider() {
        this.$get = function($http, msConfigService) {
            var _domain = (msConfigService.getConfig()).domain;

            var service = {
                uploadFile: uploadFile
            };

            return service;

            function uploadFile(appId, file, callback) {
                $http({
                    method: 'POST',
                    url: _domain + '/csbm/files/' + file.name,
                    headers: {
                        'X-CSBM-Application-Id': appId,
                        'Content-Type': file.type
                    },
                    data: file
                }).then(function(response) {
                    callback(null, response);
                }, function(response) {
                    callback(response);
                });
            };
        }
    };

})();
