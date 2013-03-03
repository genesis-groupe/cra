'use strict';

var app = angular.module('app', ['ngResource', 'capitalizeFilters', 'bDatepicker']);

app.config(['$routeProvider', function($routeProvider){
	$routeProvider
		.when('/employees', {
			controller: 'EmployeesController',
			templateUrl: '/assets/javascripts/app/backoffice/views/employees.html'
		})
		.when('/employee/:trigramme', {
			controller: 'EmployeeController',
			templateUrl: '/assets/javascripts/app/backoffice/views/employee.html'
		})
		.when('/employees/new', {
			controller: 'EmployeeController',
			templateUrl: '/assets/javascripts/app/backoffice/views/employee.html'
		})
		.when('/customers', {
			controller: 'CustomersController',
			templateUrl: '/assets/javascripts/app/backoffice/views/customers.html'
		})
		.when('/customer/:id', {
			controller: 'CustomerController',
			templateUrl: '/assets/javascripts/app/backoffice/views/customer.html'
		})
		.when('/customers/new', {
			controller: 'CustomerController',
			templateUrl: '/assets/javascripts/app/backoffice/views/customer.html'
		})
		.when('/mission/:id', {
			controller: 'MissionController',
			templateUrl: '/assets/javascripts/app/backoffice/views/mission.html'
		});
}]);
