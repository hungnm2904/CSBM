(function() {
    'use strict';

    angular
        .module('app.pages', [
            'app.pages.auth.login',
            'app.pages.auth.register'
        ])
        .config(config);
        // .run(run);

    /** @ngInject */
    function config(msNavigationServiceProvider) {
        // Navigation
    };

})();
