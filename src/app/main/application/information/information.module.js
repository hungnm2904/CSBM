(function() {
    'use strict';

    angular
        .module('app.application.info', [])
        .config(config);
    // .run(run);

    /** @ngInject */
    function config($stateProvider, msApiProvider) {
        $stateProvider.state('app.application_info', {
            url: '/application/:appId',
            views: {
                'content@app': {
                    templateUrl: 'app/main/application/information/information.html',
                    controller: 'InformationController as vm'
                }
            }
        });
    }
})();
