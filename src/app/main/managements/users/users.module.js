(function ()
{
    'use strict';

    angular
        .module('app.managements.users', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msApiProvider)
    {
        $stateProvider.state('app.managements_users', {
            url    : '/managements/users',
            views  : {
                'content@app': {
                    templateUrl: 'app/main/managements/users/users.html',
                    controller : 'UsersController as vm'
                }
            },
            resolve: {
                Users: function (msApi)
                {
                    return msApi.resolve('users@get');
                }
            }
        });

        // Api
        msApiProvider.register('users', ['app/data/users/users.json']);
    }

})();