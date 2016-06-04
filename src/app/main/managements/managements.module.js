(function ()
{
    'use strict';

    angular
        .module('app.managements', [
            'app.managements.applications',
            'app.managements.users',
            'app.managements.classes',
        ])
        .config(config);

    /** @ngInject */
    function config(msNavigationServiceProvider)
    {
        // Navigation
        msNavigationServiceProvider.saveItem('managements', {
            title : 'MANAGEMENT',
            group : true,
            weight: 1
        });

        msNavigationServiceProvider.saveItem('managements.applications', {
            title : 'Applications',
            icon  : 'icon-apps',
            state : 'app.managements_applications'
        });

        msNavigationServiceProvider.saveItem('managements.users', {
            title : 'Users',
            icon  : 'icon-account-multiple',
            state : 'app.managements_users'
        });

        msNavigationServiceProvider.saveItem('managements.classes', {
            title : 'Classes',
            icon  : 'icon-library-plus',
            state : 'app.managements_classes'
        });
    }
})();