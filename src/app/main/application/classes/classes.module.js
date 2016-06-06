(function() {
    'use strict';

    angular
        .module('app.application.classes', [])
        .config(config)
        .run(run);

    /** @ngInject */
    function config($stateProvider, msApiProvider, msModeServiceProvider,
        msNavigationServiceProvider, msSchemasServiceProvider) {

        // //Navigation
        // msNavigationServiceProvider.saveItem('application', {
        //     title: 'Application',
        //     group: true,
        //     weight: 1,
        //     hidden: function() {
        //         return !msModeServiceProvider.checkMode('application');
        //     }
        // });

        // msNavigationServiceProvider.saveItem('application.classes', {
        //     title: 'Classes',
        //     icon: 'icon-library-plus'
        // });


        // var schemas = msSchemasServiceProvider.getSchemas();
        // console.log(schemas);
        // for (var i = 0; i < schemas.length; i++) {
        //     var schema = schemas[i];

        //     // Create navigation for schema
        //     msNavigationServiceProvider.saveItem('application.classes.' + schema.className, {
        //         title: schema.className,
        //         icon: 'icon-apps',
        //         state: 'app.application.classes_' + schema.className,
        //     });

        //     $stateProvider.state('app.application.classes_' + schema.className, {
        //         url: '/application/classes/' + schema.className,
        //         views: {
        //             'content@app': {
        //                 templateUrl: 'app/main/application/classes/classes.html',
        //                 controller: 'ClassesController as vm'
        //             }
        //         }
        //     });
        // }

        $stateProvider.state('app.application_classes', {
            url: '/application/classes/:index',
            views: {
                'content@app': {
                    templateUrl: 'app/main/application/classes/classes.html',
                    controller: 'ClassesController as vm'
                }
            }
        });

        // Api
        msApiProvider.register('classes', ['app/data/classes/classes.json']);
    }

    function run($rootScope, msSchemasService, msNavigationService, msModeService) {
        $rootScope.$on('schemas-change', function() {

            //Navigation
            msNavigationService.saveItem('application', {
                title: 'Application',
                group: true,
                weight: 1,
                hidden: function() {
                    return !msModeService.checkMode('application');
                }
            });

            msNavigationService.saveItem('application.classes', {
                title: 'Classes',
                icon: 'icon-library-plus'
            });


            var schemas = msSchemasService.getSchemas();
            for (var i = 0; i < schemas.length; i++) {
                var schema = schemas[i];

                // Create navigation for schema
                msNavigationService.saveItem('application.classes.' + schema.className, {
                    title: schema.className,
                    icon: 'icon-apps',
                    state: 'app.application_classes',
                    stateParams: {'index': i}
                });
            }
        });
    }

})();
