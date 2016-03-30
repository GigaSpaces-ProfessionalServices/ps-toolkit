angular.module('xapConfigApp.controllers', [])
    .controller('MainController', ['$scope', '$http', 'FileSaver', '$uibModal', function ($scope, $http, FileSaver, $uibModal) {

        $scope.xapConfigOptions = {};
        $scope.data = {};
        $scope.zones = [{id: 'zone1'}];
        $scope.profiles = [];
        $scope.selectedProfile = null;

        $http.get('/profiles').then(function (result) {
                $scope.profiles = result.data;
            });

        $scope.download = function (options, force) {
            $scope.xapConfigOptions = angular.copy(options);
            $scope.xapConfigOptions['zoneOptions'] = $scope.zones;

            if ( !force && !validateOptions($scope.xapConfigOptions)) {
                $uibModal.open({
                    animation: true,
                    templateUrl: 'warningModalContent.html',
                    controller: 'WarningModalInstanceCtrl',
                    keyboard: true,
                    size: 'md',
                    resolve: {mainScope : $scope}
                });
                return;
            }

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
            $scope.selectedProfile = null;
        };
        $scope.reset();

        var validateOptions = function (options) {
            var result =
                options['javaHome'] != undefined &&
                options['xapHome'] != undefined &&
                options['maxProcessesNumber'] != undefined &&
                options['maxOpenFileDescriptorsNumber'] != undefined &&
                options['lookupGroups'] != undefined;

            if (options['isUnicast']) {
                result = result && options['discoveryPort'] != undefined && options['lookupLocators'] != undefined;
            }
            if (options['zoneOptions'] != undefined) {
                options.zoneOptions.forEach(function (item, i, arr) {
                    result = result && item['xmx'] != undefined && item['xms'] != undefined && item['xmn'] != undefined &&
                        item['gscNum'] != undefined && item['gsmNum'] != undefined && item['lusNum'] != undefined;
                });
            } else {
                result = false;
            }
            return result;
        };

        $scope.applyProfile = function () {
            var profile = $scope.selectedProfile.options;

            $scope.options.javaHome = profile.javaHome;
            $scope.options.xapHome = profile.xapHome;
            $scope.options.maxProcessesNumber = profile.maxProcessesNumber;
            $scope.options.maxOpenFileDescriptorsNumber = profile.maxOpenFileDescriptorsNumber;
            $scope.options.isUnicast = profile.isUnicast;
            $scope.options.discoveryPort = profile.discoveryPort;
            $scope.options.lookupLocators = profile.lookupLocators;
            $scope.options.lookupGroups = profile.lookupGroups;

            $scope.zones.splice(0, 1);
            profile.zoneOptions.forEach(function (item, i, arr) {
                var zone = {'id': 'zone' + (i + 1)};
                for(var k in item) zone[k] = item[k];
                $scope.zones.push(zone);
            });
        };

    }]).controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, data) {

    $scope.data = data;

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };

    $scope.close = function () {
        $uibModalInstance.close($scope.data);
    };
}).controller('WarningModalInstanceCtrl', function ($scope, $uibModalInstance, mainScope) {

    $scope.mainScope = mainScope;

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };

    $scope.forceDownload = function () {
        $uibModalInstance.close();
        mainScope.download(mainScope.xapConfigOptions, true);
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
}).directive('third', function () {
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
            attrs.$observe('third', function (val) {
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

                    var valueToCompare = attrs.third.trim();
                    if (valueToCompare.indexOf("g") != -1) {
                        valueToCompare = parseInt(valueToCompare) * 1024;
                    } else if (valueToCompare.indexOf("m") != -1) {
                        valueToCompare = parseInt(valueToCompare);
                    }
                    valueToCompare = Math.ceil(valueToCompare / 3);
                    ngModel.$setValidity('third', (value == valueToCompare));
                }
            };
        }
    };
}).directive('unique', function () {
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
            attrs.$observe('unique', function (val) {
                validate();
            });
            var validate = function () {
                var zoneName = ngModel.$viewValue;
                var index = ngModel.$name.replace(/[^0-9\.]/g, '');
                var isUnique = true;
                scope.zones.forEach(function (item, i, arr) {
                    if (i != index && item.zoneName == zoneName) {
                        isUnique = false;
                    }
                });
                ngModel.$setValidity('unique', isUnique);
            };
        }
    };
});