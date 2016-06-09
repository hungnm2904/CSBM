(function() {
    'use strict';

    angular
        .module('app.core')
        .provider('msSchemasService', msSchemasServiceProvider);

    function msSchemasServiceProvider() {
        var $rootScope = angular.injector(['ng']).get('$rootScope');

        var schemas = [];

        this.$get = function($rootScope, $http, msConfigService, msMasterKeyService, msModeService) {
            var domain = (msConfigService.getConfig()).domain;

            var service = {
                setSchemas: setSchemas,
                getSchemas: getSchemas,
                getSchema: getSchema,
                addSchema: addSchema,
                findByName: findByName,
                updateFields: updateFields,
            }

            return service;

            function setSchemas(_appId, _schemas) {
                schemas = _schemas;
                msModeService.setToApplicationMode();
                $rootScope.$broadcast('schemas-changed', { 'appId': _appId });
            };

            function getSchemas(_appId, callback) {
                if (schemas && schemas.length > 0) {
                    return callback(null, schemas);
                }

                msMasterKeyService.getMasterKey(_appId, function(error, results) {
                    if (error) {
                        return callback(error);
                    }

                    var masterKey = results.data.data.masterKey;
                    $http({
                        method: 'GET',
                        url: domain + '/csbm/schemas',
                        headers: {
                            'X-CSBM-Application-Id': _appId,
                            'X-CSBM-Master-Key': masterKey
                        }
                    }).then(function(response) {
                        service.setSchemas(_appId, response.data.results);
                        callback(null, schemas);
                    }, function(response) {
                        callback(response);
                    });
                });
            };

            function getSchema(_appId, index, callback) {
                if (schemas && schemas.length > 0) {
                    return callback(null,   schemas[index]);
                }

                service.getSchemas(_appId, function(error, results){
                    if (error) {
                        return callback(error);
                    }

                    callback(null, schemas[index]);
                });
            };

            function addSchema(_schema) {
                schemaObj.schemas.push(_schema);
                $rootScope.$broadcast('schemas-changed', { 'schema': _schema });
            };

            function findByName(_name) {
                schemaObj.schemas.forEach(function(schema, index) {
                    if (schema.className === _name) {
                        return schema;
                    }
                });
            };

            function updateFields(_name, _fields) {
                schemas.forEach(function(schema, index) {
                    if (schema.className === _name) {
                        schema.fields = _fields;
                        return;
                    }
                });
                $rootScope.$broadcast('fields-change', { 'fields': _fields });
            }
        };
    };
})();
