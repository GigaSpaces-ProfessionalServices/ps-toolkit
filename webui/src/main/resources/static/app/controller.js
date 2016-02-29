angular.module('xapConfigApp.controllers', [])
.controller('MainController', ['$scope', 'Config', function($scope, Config) {
	$scope.xapConfigOptions = {};
	
	$scope.update = function(options) {
		$scope.xapConfigOptions = angular.copy(options);
		Config.save($scope.xapConfigOptions);
	};
	
	$scope.reset = function() {
		$scope.options = angular.copy($scope.xapConfigOptions);
	};
	$scope.reset();
}]);
