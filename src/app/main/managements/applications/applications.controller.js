(function ()
{
    'use strict';

    angular
        .module('app.managements.applications')
        .controller('ApplicationsController', ApplicationsController);

    /** @ngInject */
    function ApplicationsController(Applications)
    {
        var vm = this;

        // Data
        vm.applications = Applications.data;

        // Methods

        //////////
    }

})();