(function() {
    'use strict';

    angular
        .module('app.managements.applications', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msApiProvider) {
        $stateProvider.state('app.managements_applications', {
            url: '/managements/applications',
            views: {
                'content@app': {
                    templateUrl: 'app/main/managements/applications/applications.html',
                    controller: 'ApplicationsController as vm'
                }
            }
        });
    }

})();
