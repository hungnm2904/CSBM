(function() {
    'use strict';

    angular
        .module('app.application', [
            'app.application.classes',
            'app.application.info'
        ])
        .config(config)
        .run(run);

    /** @ngInject */
    function config($stateProvider) {

    };

    function run($rootScope, msSchemasService, msNavigationService, msModeService, $state) {
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

            msNavigationService.saveItem('application.info', {
                title: 'Information',
                icon: 'icon-key',
                state: 'app.application_info',
                stateParams: { 'appId': appId }
            });

            msNavigationService.saveItem('application.classes', {
                title: 'Classes',
                icon: 'icon-library-plus',
                group: true
            });

            var schemas = msSchemasService.getSchemas(appId, index, function(error, results) {
                if (error) {
                    alert(error.statusText);
                }

                var schemas = results;
                for (var i = 0; i < schemas.length; i++) {
                    var schema = schemas[i];

                    // Create navigation for schema
                    msNavigationService.saveItem('application.classes.' + schema.className, {
                        title: schema.className,
                        icon: 'icon-apps',
                        state: 'app.application_classes',
                        stateParams: { 'appId': appId, 'index': i }
                    });
                }
                $state.go('app.application_classes', { 'appId': appId, 'index': index });
            });

        });
    };
})();
