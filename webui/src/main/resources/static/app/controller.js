angular.module('xapConfigApp.controllers', [])
    .controller('MainController', ['$scope', '$http', 'FileSaver', '$uibModal', function ($scope, $http, FileSaver, $uibModal) {

        $scope.xapConfigOptions = {};
        $scope.data = {};
        $scope.zones = [{id: 'zone1'}];

        $scope.download = function (options) {
            $scope.xapConfigOptions = angular.copy(options);
            $scope.xapConfigOptions['zoneOptions'] = $scope.zones;

            var config = {responseType: 'arraybuffer', cache: false};
            $http.post('/generate', $scope.xapConfigOptions, config).then(function (response) {
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

        $scope.addNewZone = function () {
            var newItemNo = $scope.zones.length + 1;
            $scope.zones.push({'id': 'zone' + newItemNo});
        };

        $scope.removeZone = function (zone) {
            var index = $scope.zones.indexOf(zone);
            $scope.zones.splice(index, index + 1);
        };

        $scope.reset = function (form) {
            if (form) {
                form.$setPristine();
                form.$setUntouched();
            }
            $scope.options = {};
            $scope.xapConfigOptions = {};
            $scope.zones = [{id: 'zone1'}];
        };
        $scope.reset();


    }]).controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, data) {

        $scope.data = data;

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.close = function () {
            $uibModalInstance.close($scope.data);
        };
    }).directive('equals', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elem, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }
                scope.$watch(attrs.ngModel, function () {
                    validate();
                });
                attrs.$observe('equals', function (val) {
                    validate();
                });
                var validate = function () {
                    var val1 = ngModel.$viewValue;
                    var val2 = attrs.equals;
                    ngModel.$setValidity('equals', val1 === val2);
                };
            }
        };
    }).directive('within', function () {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope, elem, attrs, ngModel) {
            if (!ngModel) {
                return;
            }
            scope.$watch(attrs.ngModel, function () {
                validate();
            });
            attrs.$observe('within', function (val) {
                validate();
            });
            var validate = function () {
                var val1 = ngModel.$viewValue;
                if (val1) {
                    var value = 0;
                    if (val1.indexOf("g") != -1) {
                        value = parseInt(val1) * 1024;
                    } else if (val1.indexOf("m") != -1) {
                        value = parseInt(val1);
                    }

                    var bounds = attrs.within.split(",", 3);
                    var valueToCompare = bounds[0].trim();
                    if (valueToCompare.indexOf("g") != -1) {
                        valueToCompare = parseInt(valueToCompare) * 1024;
                    } else if (valueToCompare.indexOf("m") != -1) {
                        valueToCompare = parseInt(valueToCompare);
                    }
                    var leftBound = bounds[1].trim() * valueToCompare;
                    var rightBound = bounds[2].trim() * valueToCompare;
                    ngModel.$setValidity('within', (leftBound <= value && value <= rightBound));
                }
            };
        }
    };
});