app.controller('EmployeeController', ['$scope', '$http', '$log', '$routeParams', 'EmployeesResource', 'RolesResource',
	function EmployeeController($scope, $http, $log, $routeParams, EmployeesResource, RolesResource){
		'use strict';
		$scope.roles = RolesResource.query();
		var affectedMissionFilter = $scope.affectedMissionFilter = {
			filter: null,
			onlyCustomers: true
		};
		$scope.employeesResource = EmployeesResource;

		$scope.employee = {};
		if ($routeParams.trigramme) {
			$scope.employee = $scope.employeesResource.get({criteria: $routeParams.trigramme}, filterAffectedMissions);
		}
		$scope.$watch('affectedMissionFilter', filterAffectedMissions, true);

		function filterAffectedMissions(){
			if ($scope.employee.affectedMissions) {
				$scope.totalAffectedMissions = $scope.employee.affectedMissions.length;

				var am = _($scope.employee.affectedMissions)
					.filter(function(affectedMission){
						return (affectedMissionFilter.onlyCustomers) ? affectedMission.mission.isGenesis === false : true;
					});

				if (!affectedMissionFilter.filter) {
					$scope.filteredAffectedMissions = am.value();
				} else {
					$scope.filteredAffectedMissions = am
						.filter(function(affectedMission){

							return (_(affectedMission.mission)
								.pick('customerName', 'code', 'startDate', 'endDate')
								.filter(function(item){
									return _.include(angular.uppercase(item), affectedMissionFilter.filter.toUpperCase());
								})
								.size());
						})
						.value();
				}
			}
		}


		$scope.save = function(){
			// Set Role
			$scope.employee.role = _.find($scope.roles, function(role){
				return this.id === role.id
			}, $scope.employee.role);

			if ($scope.employee.id) {
				$scope.employee.$update();
			} else {
				$scope.employeesResource.create($scope.employee, function(employee){
					$scope.employee = employee;
					filterAffectedMissions();
				});

			}
		};

		$scope.addMission = function(missionId){
			$http({
				method: 'PUT',
				url: '/employees/' + $scope.employee.id + '/affectedMissions',
				data: angular.toJson({
					'id': missionId
				})
			})
				.success(function(employee, status, headers, config){
					$scope.employee = employee;
					filterAffectedMissions();
				})
				.error(function(data, status, headers, config){
					angular.$log.error(data, status);
				});
		}

		$scope.removeMission = function(missionId){
			$http({
				method: 'DELETE',
				url: '/employees/' + $scope.employee.id + '/affectedMissions/' + missionId
			})
				.success(function(removedAffectedMission, status, headers, config){
					$scope.employee.affectedMissions = _.reject($scope.employee.affectedMissions, function(affectedMission, index, list){
						return this.mission.id === affectedMission.mission.id;
					}, removedAffectedMission);
					filterAffectedMissions();
				})
				.error(function(data, status, headers, config){
					$log.error(data, status);
				});
		}

	}]);

app.controller('EmployeeCustomerController', ['$scope', '$http', '$log', '$routeParams', 'CustomersResource', function CustomerController($scope, $http, $log, $routeParams, CustomersResource){
	$scope.customers = CustomersResource.query();
	$scope.selectedCustomer = null;

	$scope.listMissions = function(id){
		$http({
			method: 'GET',
			url: '/customers/' + id + '/missions'
		})
			.success(function(missions, status, headers, config){
				$scope.selectedCustomer = _($scope.customers)
					.find(function(customer){
						return id === customer.id
					});
				$scope.selectedCustomer.missions = missions;
			})
			.error(function(data, status, headers, config){
				$log.error(data, status);
			});
	}

	$scope.selectMission = function(id){
		$scope.$parent.addMission(id);
	}
}]);