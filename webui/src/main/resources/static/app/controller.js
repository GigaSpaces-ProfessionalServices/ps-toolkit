angular.module('xapConfigApp.controllers', [])
.controller('MainController', ['$scope','$http', 'FileSaver', function($scope, $http, FileSaver) {
	$scope.xapConfigOptions = {};

	$scope.download = function(options) {
		$scope.xapConfigOptions = angular.copy(options);
		var config = {responseType: 'arraybuffer', cache: false};
		$http.post('/generate', options, config).then(function (response) {
			var data = response.data;
			var blob = new Blob([data], {type: response.headers('content-type')});
			var fileName = response.headers('Content-Disposition').split("filename=")[1];
			FileSaver.saveAs(blob, fileName);
		});
	};
	
	$scope.reset = function(form) {
		if (form) {
			form.$setPristine();
			form.$setUntouched();
		}
		$scope.options = {};
		$scope.xapConfigOptions = {};
	};
	$scope.reset();
}]);
