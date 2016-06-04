(function ()
{
    'use strict';

    angular
        .module('app.managements.applications', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msApiProvider)
    {
        $stateProvider.state('app.managements_applications', {
            url    : '/managements/applications',
            views  : {
                'content@app': {
                    templateUrl: 'app/main/managements/applications/applications.html',
                    controller : 'ApplicationsController as vm'
                }
            },
            resolve: {
                Applications: function (msApi)
                {
                    return msApi.resolve('applications@get');
                }
            }
        });

        // Api
        msApiProvider.register('applications', ['app/data/applications/applications.json']);
    }

})();