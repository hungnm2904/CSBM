(function ()
{
    'use strict';

    angular
        .module('app.managements.classes', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msApiProvider)
    {
        $stateProvider.state('app.managements_classes', {
            url    : '/managements/classes',
            views  : {
                'content@app': {
                    templateUrl: 'app/main/managements/classes/classes.html',
                    controller : 'ClassesController as vm'
                }
            },
            resolve: {
                Classes: function (msApi)
                {
                    return msApi.resolve('classes@get');
                }
            }
        });

        // Api
        msApiProvider.register('classes', ['app/data/classes/classes.json']);
    }

})();