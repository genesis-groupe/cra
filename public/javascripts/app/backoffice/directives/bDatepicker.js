angular.module('bDatepicker', []).
	directive('bDatepicker', function(){
		return {
			require: '?ngModel',
			restrict: 'A',
			link: function($scope, element, attrs, ngModelCtrl){
				var originalRender, updateModel;
				updateModel = function(ev){
					return $scope.$apply(function(){
						// TODO use angular.value instead
						return ngModelCtrl.$setViewValue(moment(ev.date).format("DD/MM/YYYY"));
					});
				};
				if (ngModelCtrl != null) {
					originalRender = ngModelCtrl.$render;
					ngModelCtrl.$render = function(){
						originalRender();
						return element.datepicker.date = ngModelCtrl.$viewValue;
					};
				}
				return attrs.$observe('bDatepicker', function(value){
					var options;
					// TODO use angular.value instead
					options = {
						format: 'dd/mm/yyyy',
						weekStart: 1,
						autoclose: true,
						todayBtn: true,
						todayHighlight: true,
						language: "fr-FR"
					};
					if (angular.isObject(value)) {
						options = value;
					}
					if (typeof(value) === "string" && value.length > 0) {
						options = angular.fromJson(value);
					}
					return element.datepicker(options).on('changeDate', updateModel);
				});
			}
		};
	});
