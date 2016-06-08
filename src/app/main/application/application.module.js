(function() {
    'use strict';

    angular
        .module('app.application', [
            'app.application.classes'
        ])
        .config(config)
        .run(run);

    /** @ngInject */
    function config($stateProvider) {

    };

    function run($rootScope, msSchemasService, msNavigationService, msModeService, $state) {
        $rootScope.$on('schemas-changed', function(event, args) {

            var appId = args.appId;

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
                weight: 1
            });

            var schemas = msSchemasService.getSchemas(appId, function(error, results) {
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
                $state.go('app.application_classes', { 'appId': appId, 'index': 0 });
            });

        });
    };
})();
