(function ()
{
	'use strict';

	angular
	.module('app.managements.applications')
	.factory('ApplicationService', function ($http, $cookies, $state, $mdDialog, $document)
	{
		var service = {};

		service.getAll = function (callback) {
			var accessToken = $cookies.get('accessToken');
			$http({
				method: 'GET',
				url: 'http://192.168.1.29:1337/application',
				headers: {
					'Authorization': 'Bearer ' + accessToken
				}
			}).then(function (response) {
				response.message = '';
				callback(response);
            // this callback will be called asynchronously
            // when the response is available
        }, function (response) {
        	response.message = 'error';
        	callback(response);
            // called asynchronously if an error occurs
            // or server returns response with an error status.
        });
		};

		service.showDialog = function(ev) {
			$mdDialog.show({
                controller         : 'DialogController',
                controllerAs       : 'vm',
                templateUrl        : 'app/main/managements/applications/dialogs/dialog.html',
                parent             : angular.element($document.body),
                targetEvent        : ev,
                clickOutsideToClose: true
            });
		};

		return service;
	});
})()