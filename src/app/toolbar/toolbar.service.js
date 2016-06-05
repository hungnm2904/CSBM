(function ()
{
	'use strict';

	angular
	.module('app.toolbar')
	.factory('ToolbarService', function ($http, $cookies, $state)
	{
		var service = {};

		service.logout = function (callback) {
			var accessToken = $cookies.get('accessToken');
			$http({
				method: 'GET',
				url: 'http://192.168.1.29:1337/signout',
				headers: {
					'Authorization': 'Bearer ' + accessToken
				}
			}).then(function (response) {
				$cookies.remove('accessToken');
				$state.go('app.pages_auth_login');
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

		return service;
	});
})()