app.controller('MissionController', ['$scope', '$http', '$log', '$routeParams', 'dateFilter', 'MissionsResource',
	function MissionController($scope, $http, $log, $routeParams, dateFilter, MissionsResource){
		'use strict';

		$scope.missionsResource = MissionsResource;
		$scope.sbm = {};
		$scope.mission = $scope.missionsResource.get({id: $routeParams.id}, function(mission){
			$log.log(mission);
		});

		$scope.update = function(){
			if($scope.mission.standByMoments){
				$scope.mission.standByMoments = _($scope.mission.standByMoments)
					.sortBy('index')
					.value();
			}
			$scope.mission.$update();
		};

		$scope.addStandByMoment = function(){
			if (!$scope.mission.standByMoments) {
				$scope.mission.standByMoments = [];
			}
			$scope.mission.standByMoments.push($scope.sbm);
			$scope.sbm = {};
		}
		$scope.removeStandByMoment = function(index){
			if (!$scope.mission.standByMoments) {
				return;
			}
			$scope.mission.standByMoments = _($scope.mission.standByMoments)
				.filter(function(sbm){
					return this !== sbm.index;
				}, index)
				.value();
		}
	}]);