(function ()
{
    'use strict';

    angular
        .module('app.managements.applications')
        .controller('DialogController', DialogController);

    /** @ngInject */
    function DialogController($mdDialog)
    {
        var vm = this;

        // Data

        //////////

        vm.closeDialog = function () {
            $mdDialog.hide();
        };
        function closeDialog()
        {
            $mdDialog.hide();
        }
    }
})();
