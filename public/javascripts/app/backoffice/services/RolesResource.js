app.factory('RolesResource', ['$resource', function ($resource) {
    'use strict';
    return $resource('/roles/:id', {id: '@id'});
}]);
