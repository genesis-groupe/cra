app.controller('CustomerController', ['$scope', '$http', '$log', '$routeParams', 'CustomersResource',
	function CustomerController($scope, $http, $log, $routeParams, CustomersResource){
		'use strict';

		var missionFilter = $scope.missionFilter = {
			filter: null
		};
		$scope.customersResource = CustomersResource;
		$scope.mission = {};
		$scope.customer = {};
		if ($routeParams.id) {
			$scope.customer = $scope.customersResource.get({id: $routeParams.id}, function(customer){
				$log.log(customer);
				$http({
					method: 'GET',
					url: '/customers/' + customer.id + '/missions'
				})
					.success(function(missions, status, headers, config){
						$scope.customer.missions = missions;
						filterMissions();
					})
					.error(function(data, status, headers, config){
						$log.error(data, status);
					});

			});
		}
		$scope.$watch('missionFilter', filterMissions, true);

		function filterMissions(){
			if ($scope.customer.missions) {
				$scope.totalMissions = $scope.customer.missions.length;
				if (!missionFilter.filter) {
					$scope.filteredMissions = $scope.customer.missions;
				} else {
					$scope.filteredMissions = _($scope.customer.missions)
						.filter(function(mission){
							return (_(mission.mission)
								.pick('code', 'label', 'startDate', 'endDate')
								.filter(function(item){
									return _.include(angular.uppercase(item), missionFilter.filter.toUpperCase());
								})
								.size());
						})
						.value();
				}
			}
		}

		$scope.save = function(){
			if ($scope.customer.id) {
				$scope.customer.$update();
			} else {
				$scope.customersResource.create($scope.customer, function(customer){
					$scope.customer = customer;
					filterMissions();
				});

			}
		};

		$scope.removeMission = function(missionId){
			$http({
				method: 'DELETE',
				url: '/customers/' + $scope.customer.id + '/missions/' + missionId
			})
				.success(function(removedMission, status, headers, config){
					$scope.customer.missions = _.reject($scope.customer.missions, function(mission, index, list){
						return this.id === mission.id;
					}, removedMission);
					filterMissions();
				})
				.error(function(data, status, headers, config){
					$log.error(data, status);
				});
		}

		$scope.addMission = function(){
			$http({
				method: 'PUT',
				url: '/customers/' + $scope.customer.id + '/missions',
				data: angular.toJson($scope.mission)
			})
				.success(function(addedMission, status, headers, config){
					$scope.customer.missions.push(addedMission);
					filterMissions();
				})
				.error(function(data, status, headers, config){
					$log.error(data, status);
				});
		}

	}]);