angular.module('xapConfigApp.services', [])
.factory('Config', ['$resource', function($resource) {
	return $resource('/generate');
}])