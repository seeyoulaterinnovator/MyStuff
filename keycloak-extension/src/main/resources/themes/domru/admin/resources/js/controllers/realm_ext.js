module.controller('RealmDropdownCtrl_ext', function($scope, Realm, Current, Auth, $location) {
    $scope.current = Current;

    $scope.changeRealm = function(selectedRealm) {
        $location.url("/realms/" + selectedRealm);
    };

    $scope.showSearchRealm = function() {

        if ($scope.query === undefined || $scope.query.searchRealm === undefined)
            return '';
        else
            return `(Realm ${$scope.query.searchRealm})`;
    };
});