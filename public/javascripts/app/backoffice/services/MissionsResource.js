'use strict';

app.factory('MissionsResource', ['$resource',function($resource) {
  return $resource(
    '/missions/:id', {id: '@id'},
    {
        update: {method:'PUT'}
    }
  );
}]);