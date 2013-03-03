app.controller('EmployeesController', ['$scope', '$location', 'EmployeesResource',
	function EmployeesController($scope, $location, EmployeesResource) {
    'use strict';
    var employeesFilter = $scope.employeesFilter = {
        filter: "",
        sortBy: null,
        sortAsc: true
    };

    $scope.employees = EmployeesResource.query();

    $scope.sortBy = function(key) {
        if (employeesFilter.sortBy === key) {
            employeesFilter.sortAsc = !employeesFilter.sortAsc;
        } else {
            employeesFilter.sortBy = key;
            employeesFilter.sortAsc = true;
        }

        $scope.employees = _.sortBy($scope.employees, employeesFilter.sortBy);
        if(!employeesFilter.sortAsc){
            $scope.employees.reverse();
        }
    };

    $scope.sortIconFor = function(key) {
        if (employeesFilter.sortBy !== key) {
            return '';
        }
        return employeesFilter.sortAsc ? '\u25B2' : '\u25BC';
    };
}]);



