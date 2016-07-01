(function() {
    'use strict';

    angular
        .module('app.application', [
            'app.application.classes',
            'app.application.appsettings.keys',
            'app.application.appsettings.notifications'
        ])
        .config(config)
        .run(run);

    /** @ngInject */
    function config($stateProvider) {

    };

    function run($rootScope, msSchemasService, msNavigationService, msModeService, $state) {

        $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
            var appId = toParams.appId;
            if (appId) {
                msModeService.setToApplicationMode();
                msSchemasService.getSchemas(appId, null, function(error, results) {
                    if (error) {
                        if (error.status === 401) {
                            return $state.go('app.pages_auth_login', { error: error });
                        }

                        return alert(error.statusText);
                    }
                });
            }
        });


        $rootScope.$on('schemas-changed', function(event, args) {

            var appId = args.appId;
            var index = args.index;

            msNavigationService.saveItem('application', {
                title: 'Application',
                group: true,
                weight: 1,
                hidden: function() {
                    return !msModeService.isApplicationMode;
                }
            });

            msNavigationService.saveItem('application.classes', {
                title: 'Classes',
                icon: 'icon-library-plus',
                group: true
            });

            msNavigationService.saveItem('application.appsettings', {
                title: 'Application Settings',
                icon: 'icon-key'
            });

            msNavigationService.saveItem('application.appsettings.keys', {
                title: 'Keys',
                state: 'app.application_appsettings_keys',
                stateParams: { 'appId': appId }
            });

            msNavigationService.saveItem('application.appsettings.notifications', {
                title: 'Push Notifications',
                state: 'app.application_appsettings_notifications',
                stateParams: { 'appId': appId }
            });

            var schemas = msSchemasService.getSchemas(appId, index, function(error, results) {
                if (error) {
                    if (error.status === 401) {
                        return $state.go('app.pages_auth_login', { error: error });
                    }

                    return alert(error.statusText);
                }

                var schemas = results;
                for (var i = 0; i < schemas.length; i++) {
                    var schema = schemas[i];

                    // Create navigation for schema
                    msNavigationService.saveItem('application.classes.' + schema.className, {
                        title: schema.className,
                        state: 'app.application_classes',
                        stateParams: { 'appId': appId, 'index': i }
                    });
                }
            });

        });
    };
})();
