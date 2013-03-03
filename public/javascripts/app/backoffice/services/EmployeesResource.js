'use strict';

app.factory('EmployeesResource', ['$resource', function($resource) {
  return $resource(
    '/employees/:criteria', {criteria: '@id'},
    {
        create: {method:'POST'},
        update: {method:'PUT'}
    }
  );
}]);