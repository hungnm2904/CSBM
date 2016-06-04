(function ()
{
    'use strict';

    angular
        .module('app.managements.users')
        .controller('UsersController', UsersController);

    /** @ngInject */
    function UsersController(Users)
    {
        var vm = this;

        // Data
        vm.users = Users.data;

        // Methods
        vm.dtOptions = {
            dom       : '<"top"f>rt<"bottom"<"left"<"length"l>><"right"<"info"i><"pagination"p>>>',
            pagingType: 'simple',
            autoWidth : false,
            responsive: true,
        };
        //////////
    }

})();