'use strict';

app.factory('CustomersResource', ['$resource',function($resource) {
  return $resource(
    '/customers/:id', {id: '@id'},
    {
        create: {method:'POST'},
        update: {method:'PUT'}
    }
  );
}]);