
module.controller('CustomTabCtrl', function($scope, realm, $location, $http, Notifications) {
    $scope.realm = realm;

    $scope.init = () => {
        $http.get(`${authUrl}/realms/${realm.realm}/settings`).then(function(data) {
            $scope.settings = angular.fromJson(data).data.results['settings'];
        });
    };

    $scope.removeSetting = (setting) => {
        $http.delete(`${authUrl}/realms/${realm.realm}/settings/${setting.id}`).then(function(data) {
            $scope.init();
            Notifications.success('Settings was deleted');
        });
    };

    $scope.updateSetting = (setting) => {
        $http.put(`${authUrl}/realms/${realm.realm}/settings/${setting.id}`, setting).then(function(data) {
            let index = $scope.settings.indexOf(setting);
            $scope.settings[index] = setting;
            Notifications.success('Settings was updated');
        });
    };

    $scope.init();
});


