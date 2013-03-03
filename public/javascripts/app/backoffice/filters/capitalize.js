angular.module('capitalizeFilters', []).filter('capitalize', function(){
	'use strict';
	return function(input){
		return _.str.capitalize(input);
	}
});

