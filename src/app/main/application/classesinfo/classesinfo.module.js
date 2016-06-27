(function() {
    'use strict';

    angular
        .module('app.application.classes.info', [])
        .config(config);
    // .run(run);

    /** @ngInject */
    function config($stateProvider, msApiProvider) {
        $stateProvider.state('app.application_classesinfo', {
            url: '/application/classes/:appId',
            views: {
                'content@app': {
                    templateUrl: 'app/main/application/classesinfo/classesinfo.html',
                    controller: 'ClassesInfoController as vm'
                }
            }
        });
    }
})();
