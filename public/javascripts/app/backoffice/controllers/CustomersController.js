app.controller('CustomersController', ['$scope', '$location', '$log', 'CustomersResource',
	function CustomersController($scope, $location, $log, CustomersResource) {
    'use strict';
    var customersFilter = $scope.customersFilter = {
        filter: "",
        sortBy: null,
        sortAsc: true
    };
    $scope.customers = CustomersResource.query(filterCustomer);

    $scope.$watch('customersFilter', filterCustomer, true);

    function filterCustomer() {
        if ($scope.customers.length) {
            $scope.totalCustomers = $scope.customers.length;
            if (!customersFilter.filter) {
                $scope.filteredCustomers = $scope.customers;
            } else {
                $scope.filteredCustomers = _($scope.customers)
                    .filter(function (customer) {
                        return (_(customer)
                            .pick("code", "name", "finalCustomer")
                            .filter(function (value) {
                                return _.include(angular.uppercase(value), angular.uppercase(customersFilter.filter));
                            }).size());
                    })
                    .value();
            }
        }
    }

    $scope.sortBy = function (key) {
        if (customersFilter.sortBy === key) {
            customersFilter.sortAsc = !customersFilter.sortAsc;
        } else {
            customersFilter.sortBy = key;
            customersFilter.sortAsc = true;
        }

        $scope.customers = _.sortBy($scope.filteredCustomers, customersFilter.sortBy);
        if (!customersFilter.sortAsc) {
            $scope.filteredCustomers.reverse();
        }
    };

    $scope.sortIconFor = function (key) {
        if (customersFilter.sortBy !== key) {
            return '';
        }
        return customersFilter.sortAsc ? '\u25B2' : '\u25BC';
    };
}]);