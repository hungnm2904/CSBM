(function() {
    'use strict';

    angular
        .module('app.managements', [
            'app.managements.applications',
            'app.managements.users'
            // 'app.managements.classes',
        ])
        .config(config);

    /** @ngInject */
    function config(msNavigationServiceProvider, msModeServiceProvider) {
        // Navigation
        msNavigationServiceProvider.saveItem('managements', {
            title: 'MANAGEMENT',
            group: true,
            weight: 1,
            hidden: function() {
                return !msModeServiceProvider.isUserMode();
            }
        });

        msNavigationServiceProvider.saveItem('managements.applications', {
            title: 'Applications',
            icon: 'icon-apps',
            state: 'app.managements_applications'
        });

        msNavigationServiceProvider.saveItem('managements.users', {
            title: 'Users',
            icon: 'icon-account-multiple',
            state: 'app.managements_users'
        });
    }
})();
