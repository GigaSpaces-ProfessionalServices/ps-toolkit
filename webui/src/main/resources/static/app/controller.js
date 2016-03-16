angular.module('xapConfigApp.controllers', [])
    .controller('MainController', ['$scope', '$http', 'FileSaver', '$uibModal', function ($scope, $http, FileSaver, $uibModal) {

        $scope.xapConfigOptions = {};
        $scope.data = {};

        $scope.download = function (options) {
            $scope.xapConfigOptions = angular.copy(options);
            var config = {responseType: 'arraybuffer', cache: false};
            $http.post('/generate', options, config).then(function (response) {
                var data = response.data;
                var blob = new Blob([data], {type: response.headers('content-type')});
                var fileName = response.headers('Content-Disposition').split("filename=")[1];
                FileSaver.saveAs(blob, fileName);
            }).catch(function (response) {
                var data = String.fromCharCode.apply(null, new Uint8Array(response.data));
                $scope.data = JSON.parse(data);
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'myModalContent.html',
                    controller: 'ModalInstanceCtrl',
                    keyboard: true,
                    size: 'lg',
                    resolve: {
                        data: function () {
                            return $scope.data;
                        }
                    }
                });

                modalInstance.closed.then(function () {
                    $scope.data = '';
                });
            });
        };

        $scope.reset = function (form) {
            if (form) {
                form.$setPristine();
                form.$setUntouched();
            }
            $scope.options = {};
            $scope.xapConfigOptions = {};
        };
        $scope.reset();
    }]).controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, data) {

    $scope.data = data;

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };

    $scope.close = function(){
        $uibModalInstance.close($scope.data);
    };
});
