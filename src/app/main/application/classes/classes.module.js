(function() {
    'use strict';

    angular
        .module('app.application.classes', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msApiProvider) {
        $stateProvider.state('app.application_classes', {
            url: '/application/classes',
            views: {
                'content@app': {
                    templateUrl: 'app/main/application/classes/classes.html',
                    controller: 'ClassesController as vm'
                }
            },
            resolve: {
                Classes: function(msApi) {
                    return msApi.resolve('classes@get');
                }
            }
        });

        // Api
        msApiProvider.register('classes', ['app/data/classes/classes.json']);
    }

})();
