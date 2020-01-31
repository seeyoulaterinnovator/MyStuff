module.controller('UserRoleMappingCtrl', function ($scope, $http, realm, user, clients, client, Notifications, RealmRoleMapping,
                                                   ClientRoleMapping, AvailableRealmRoleMapping, AvailableClientRoleMapping,
                                                   CompositeRealmRoleMapping, CompositeClientRoleMapping, $location) {
    $scope.realm = realm;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }
    $scope.user = user;
    $scope.selectedRealmRoles = [];
    $scope.selectedRealmMappings = [];
    $scope.realmMappings = [];
    $scope.clients = [];
    $scope.client = client;
    $scope.clientRoles = [];
    $scope.clientComposite = [];
    $scope.selectedClientRoles = [];
    $scope.selectedClientMappings = [];
    $scope.clientMappings = [];
    $scope.dummymodel = [];

    $scope.updateRealmData = function () {
        $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/realm/available').then(function (data) {
            $scope.realmRoles = data.data;
            $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/realm/composite').then(function (data) {
                $scope.realmComposite = data.data;
                $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/realm').then(function (data) {
                    $scope.realmMappings = data.data;
                    $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/clients').then(function (data) {
                        $scope.clients = data.data;
                    });
                });
            });
        });
    }

    $scope.updateClientData = function () {
        $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/clients/' + $scope.targetClient.id + '/available').then(function (data) {
            $scope.clientRoles = data.data;
            $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/clients/' + $scope.targetClient.id + '/composite').then(function (data) {
                $scope.clientComposite = data.data;
                $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/clients/' + $scope.targetClient.id).then(function (data) {
                    $scope.clientMappings = data.data;
                });
            });
        });
    }

    $scope.updateRealmData();

    $scope.addRealmRole = function () {
        $scope.realmRolesToAdd = JSON.parse('[' + $scope.selectedRealmRoles + ']');
        $scope.selectedRealmRoles = [];
        $http.post(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/realm',
            $scope.realmRolesToAdd).then(function () {
            $scope.updateRealmData();
            $scope.selectedRealmMappings = [];
            $scope.selectRealmRoles = [];
            if ($scope.targetClient) {
                console.log('load available');
                $scope.selectedClientRoles = [];
                $scope.selectedClientMappings = [];
            }
            Notifications.success("Role mappings updated.");

        });
    };

    $scope.deleteRealmRole = function () {
        $scope.realmRolesToRemove = JSON.parse('[' + $scope.selectedRealmMappings + ']');
        $http.delete(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/realm',
            {data: $scope.realmRolesToRemove, headers: {"content-type": "application/json"}}).then(function () {
            $scope.updateRealmData();
            $scope.selectedRealmMappings = [];
            $scope.selectRealmRoles = [];
            if ($scope.targetClient) {
                console.log('load available');
                $scope.selectedClientRoles = [];
                $scope.selectedClientMappings = [];
            }
            Notifications.success("Role mappings updated.");
        });
    };

    $scope.addClientRole = function () {
        $scope.clientRolesToAdd = JSON.parse('[' + $scope.selectedClientRoles + ']');
        $http.post(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/clients/' + $scope.targetClient.id,
            $scope.clientRolesToAdd).then(function () {
            $scope.updateClientData();
            $scope.selectedClientRoles = [];
            $scope.selectedClientMappings = [];
            Notifications.success("Role mappings updated.");
        });
    };

    $scope.deleteClientRole = function () {
        $scope.clientRolesToRemove = JSON.parse('[' + $scope.selectedClientMappings + ']');
        $http.delete(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/role-mappings/' + $scope.user.id + '/clients/' + $scope.targetClient.id,
            {data: $scope.clientRolesToRemove, headers: {"content-type": "application/json"}}).then(function () {
            $scope.updateClientData();
            $scope.selectedClientRoles = [];
            $scope.selectedClientMappings = [];
            Notifications.success("Role mappings updated.");
        });
    };


    $scope.changeClient = function () {
        console.log('changeClient');
        if ($scope.targetClient) {
            console.log('load available');
            $scope.updateClientData();
        } else {
            $scope.clientRoles = null;
            $scope.clientMappings = null;
            $scope.clientComposite = null;
        }
        $scope.selectedClientRoles = [];
        $scope.selectedClientMappings = [];
    };


});

module.controller('UserSessionsCtrl', function ($scope, realm, user, sessions, UserSessions, UserLogout,
                                                UserSessionLogout, Notifications, $location) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.sessions = sessions;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    $scope.logoutAll = function () {
        UserLogout.save({realm: realm.realm, user: user.id}, function () {
            Notifications.success('Logged out user in all clients');
            UserSessions.query({realm: realm.realm, user: user.id}, function (updated) {
                $scope.sessions = updated;
            })
        });
    };

    $scope.logoutSession = function (sessionId) {
        console.log('here in logoutSession');
        UserSessionLogout.delete({realm: realm.realm, session: sessionId}, function () {
            UserSessions.query({realm: realm.realm, user: user.id}, function (updated) {
                $scope.sessions = updated;
                Notifications.success('Logged out session');
            })
        });
    }
});

module.controller('UserFederatedIdentityCtrl', function ($scope, $location, realm, user, federatedIdentities, UserFederatedIdentity, Notifications, Dialog) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.federatedIdentities = federatedIdentities;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    $scope.hasAnyProvidersToCreate = function () {
        return realm.identityProviders.length - $scope.federatedIdentities.length > 0;
    };

    $scope.removeProviderLink = function (providerLink) {

        console.log("Removing provider link: " + providerLink.identityProvider);

        Dialog.confirmDelete(providerLink.identityProvider, 'Identity Provider Link', function () {
            UserFederatedIdentity.remove({
                realm: realm.realm,
                user: user.id,
                provider: providerLink.identityProvider
            }, function () {
                Notifications.success("The provider link has been deleted.");
                var indexToRemove = $scope.federatedIdentities.indexOf(providerLink);
                $scope.federatedIdentities.splice(indexToRemove, 1);
            });
        });
    }
});

module.controller('UserFederatedIdentityAddCtrl', function ($scope, $location, realm, user, federatedIdentities, UserFederatedIdentity, Notifications) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.federatedIdentity = {};

    var getAvailableProvidersToCreate = function () {
        var realmProviders = [];
        for (var i = 0; i < realm.identityProviders.length; i++) {
            var providerAlias = realm.identityProviders[i].alias;
            realmProviders.push(providerAlias);
        }
        for (var i = 0; i < federatedIdentities.length; i++) {
            var providerAlias = federatedIdentities[i].identityProvider;
            var index = realmProviders.indexOf(providerAlias);
            realmProviders.splice(index, 1);
        }

        return realmProviders;
    };
    $scope.availableProvidersToCreate = getAvailableProvidersToCreate();

    $scope.save = function () {
        UserFederatedIdentity.save({
            realm: realm.realm,
            user: user.id,
            provider: $scope.federatedIdentity.identityProvider
        }, $scope.federatedIdentity, function (data, headers) {
            $location.url("/realms/" + realm.realm + '/users/' + $scope.user.id + '/federated-identity');
            Notifications.success("Provider link has been created.");
        });
    };

    $scope.cancel = function () {
        $location.url("/realms/" + realm.realm + '/users/' + $scope.user.id + '/federated-identity');
    };

});

module.controller('UserConsentsCtrl', function ($scope, realm, user, userConsents, UserConsents, Notifications,
                                                $location) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.userConsents = userConsents;

    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    $scope.revokeConsent = function (clientId) {
        UserConsents.delete({realm: realm.realm, user: user.id, client: clientId}, function () {
            UserConsents.query({realm: realm.realm, user: user.id}, function (updated) {
                $scope.userConsents = updated;
            });
            Notifications.success('Grant revoked successfully');
        }, function () {
            Notifications.error("Grant couldn't be revoked");
        });
        console.log("Revoke consent " + clientId);
    }
});

module.controller('UserOfflineSessionsCtrl', function ($scope, $location, realm, user, client, offlineSessions) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.client = client;
    $scope.offlineSessions = offlineSessions;

    $scope.cancel = function () {
        $location.url("/realms/" + realm.realm + '/users/' + user.id + '/consents');
    };
});

module.controller('UserListCtrl', function ($scope, realm, User, UserSearchState, UserImpersonation,
                                            BruteForce, Notifications, $route, Dialog/*, CustomUser*/,
                                            $http, $window,
                                            RealmClearUserCache, RealmClearRealmCache, RealmClearKeysCache) {

    $scope.userRealms = [];

    $scope.pages = {};
    $scope.pages.number = 1;
    var isInitPagination = false;
    $scope.pageSize = {};

    sortAsc = true;
    $scope.sortMarkEmail = "↓"
    $scope.sortMarkName = ""
    $scope.SORT_FIELD_EMAIL = "email";
    $scope.SORT_FIELD_NAME = "firstName";
    currentSortField = $scope.SORT_FIELD_EMAIL;

    $scope.init = function () {
        $scope.realm = realm;
        $http.get(authUrl + '/realms/' + realm.realm + '/users-info/accessible-realms').then(function (data) {
            $scope.userRealms = angular.fromJson(data).data;

            UserSearchState.query.realm = realm.realm;
            $scope.query = UserSearchState.query;
            $scope.query.briefRepresentation = 'false';

            $scope.query.search = $scope.getSearchParameter($route.current.params.search);
            $scope.query.searchByUserId = $scope.getSearchParameter($route.current.params.searchUser);
            $scope.query.searchByTomsId = $scope.getSearchParameter($route.current.params.searchToms);
            $scope.query.searchRealm = $scope.getSearchParameter($route.current.params.searchRealm);

            if ($scope.query.searchRealm === '' || !$scope.userRealms.some(function (realm) {
                return realm === $scope.query.searchRealm
            })) {
                if (realm.realm === 'manager' && $scope.userRealms.length === 1) {
                    $scope.query.searchRealm = $scope.userRealms[0];
                } else {
                    $scope.query.searchRealm = realm.realm;
                }
            }

            if (!UserSearchState.isFirstSearch) {
                $scope.search();
            } else $scope.firstPage();
        });
    };

    $scope.clearCache = function () {
        RealmClearUserCache.save({realm: $scope.realm.realm}, function () {
            //Notifications.success("User cache cleared");
        });
        RealmClearRealmCache.save({realm: $scope.realm.realm}, function () {
            //Notifications.success("Realm cache cleared");
        });
        RealmClearKeysCache.save({realm: $scope.realm.realm}, function () {
            //Notifications.success("Public keys cache cleared");
        });
    }

    $scope.sort = function (sortField) {
        if (currentSortField === sortField) {
            sortAsc = sortAsc === false;
        } else {
            sortAsc = true;
        }

        if (sortField === $scope.SORT_FIELD_EMAIL) {
            currentSortField = sortField;
            $scope.sortMarkEmail = getSortMark(sortAsc);
            $scope.sortMarkName = "";
        }
        if (sortField === $scope.SORT_FIELD_NAME) {
            currentSortField = sortField;
            $scope.sortMarkName = getSortMark(sortAsc);
            $scope.sortMarkEmail = "";
        }
        $scope.search();
    }

    function getSortMark(sortAsc) {
        if (sortAsc === true) {
            return "↓";
        } else {
            return "↑";
        }
    }

    function initPagination() {
        isInitPagination = true;

        var lastPage = 1;

        $('.pagination')
            .find('li')
            .slice(2, -2)
            .remove();

        for (var i = 1; i <= $scope.pages.totalPages;) {
            $('.pagination #prev')
                .before(
                    '<li data-page="' + i + '">\
                         <span>' + i++ + '<span class="sr-only">(current)</span></span>\
                    </li>').show();
        }

        $('.pagination [data-page="1"]').addClass('active');

        limitPagging();

        $('.pagination li').on('click', function (evt) {
            // on click each page
            evt.stopImmediatePropagation();
            evt.preventDefault();
            var pageNum = $(this).attr('data-page'); // get it's number

            if (pageNum == 'first') {
                if (lastPage == 1) {
                    return;
                }
                pageNum = 1;
            }
            if (pageNum == 'prev') {
                if (lastPage == 1) {
                    return;
                }
                pageNum = --lastPage;
            }
            if (pageNum == 'next') {
                if (lastPage == $('.pagination li').length - 4) {
                    return;
                }
                pageNum = ++lastPage;
            }
            if (pageNum == 'last') {
                if (lastPage == $('.pagination li').length - 4) {
                    return;
                }
                pageNum = $('.pagination li').length - 4;
            }
            lastPage = pageNum;
            $('.pagination li').removeClass('active'); // remove active class from all li
            $('.pagination [data-page="' + lastPage + '"]').addClass('active'); // add active class to the clicked
            limitPagging();
            $scope.pages.number = pageNum;
            $scope.search();
        }); // end of on click pagination list
    }

    function limitPagging() {
        if ($('.pagination li').length > 9) {
            var currentPage = $('.pagination li.active').attr('data-page');
            if (currentPage <= 3) {
                $('.pagination li:gt(6)').hide();
                $('.pagination li:lt(7)').show();
                $('.pagination [data-page="next"]').show();
                $('.pagination [data-page="last"]').show();
            }
            if (currentPage > 3) {
                $('.pagination li:gt(1)').hide();
                $('.pagination [data-page="next"]').show();
                $('.pagination [data-page="last"]').show();
                for (let i = (parseInt($('.pagination li.active').attr('data-page')) - 2); i <= (parseInt($('.pagination li.active').attr('data-page')) + 2); i++) {
                    $('.pagination [data-page="' + i + '"]').show();
                }
            }
        }
    }

    $scope.getSearchParameter = function (param) {
        if (param === undefined) {
            return '';
        }
        return param;
    };

    $scope.changeSearchRealm = function () {
        $scope.query.search = '';
        $scope.query.searchByUserId = '';
        $scope.query.searchByTomsId = '';

        $scope.firstPage();

        //$window.location.href = '?searchRealm=' + $scope.query.searchRealm + '#/realms/' + $scope.realm + '/users';
    };

    $scope.impersonate = function (userId) {
        var hackedRealm;
        if (realm.realm === 'manager')
            hackedRealm = 'user';
        else
            hackedRealm = realm.realm;
        UserImpersonation.save({realm: hackedRealm, user: userId}, function (data) {
            if (data.sameRealm) {
                window.location = data.redirect;
            } else {
                window.open(data.redirect, "_blank");
            }
        });
    };


    $scope.selectAll = function () {
        if ($scope.selectedAll) {
            $scope.users.forEach(user => user.active = true);
        } else {
            $scope.users.forEach(user => user.active = false);
        }
    };

    $scope.firstPage = function () {
        $scope.query.first = 0;
        isInitPagination = false;
        $scope.pages.number = 1;
        $scope.search();
    };

    $scope.unlockUsers = function () {
        let userForUnlock = $scope.users.filter(user => user.active).map(user => user.id);
        $http.post(`${authUrl}/realms/${realm.realm}/manage/unlock`, userForUnlock).then(response => {
            Notifications.success("Selected users has been unlocked");
            $scope.users.filter(user => user.active).forEach(user => user.enabled = true)
        })
    };

    $scope.selectedResetPassword = function () {
        let userForResetPassword = $scope.users.filter(user => user.active).map(user => user.id);
        $http.post(`${authUrl}/realms/${realm.realm}/manage/credential/reset`, userForResetPassword).then(response => {
            Notifications.success("Password Reset");
        })
    };

    $scope.selectedBlockUsers = function () {
        let userForResetPassword = $scope.users.filter(user => user.active).map(user => user.id);
        $http.post(`${authUrl}/realms/${realm.realm}/manage/block`, userForResetPassword).then(response => {
            Notifications.success("Users has been blocking");
            $scope.users.filter(user => user.active).forEach(user => user.enabled = false)
        })
    };

    $scope.importFileCSV = function (files) {
        var formData = new FormData();
        var file = files[0];
        formData.append('file', file);
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/uploadUsers`, formData, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined,
                'Content-Disposition': `form-data; name="file"; filename="import.csv"`
            }
        }).then(response => {
            var resp = angular.fromJson(response).data.results['import-report'];
            this.importMsg(resp)
        }).catch(error => {
            if (error.status === 400) {
                Notifications.error(error.data.message);
            } else {
                Notifications.error(error.statusText);
            }
        })
    };

    $scope.importFileExcel = function (files) {
        var formData = new FormData();
        var file = files[0];
        formData.append('file', file);
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/uploadUsers`, formData, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined,
                'Content-Disposition': `form-data; name="file"; filename="import.xlsx"`
            }
        }).then(response => {
            var resp = angular.fromJson(response).data.results['import-report'];
            this.importMsg(resp)
        }).catch(error => {
            if (error.status === 400) {
                Notifications.error(error.data.message);
            } else {
                Notifications.error(error.statusText);
            }
        })
    };

    function onlyUnique(value, index, self) {
        return self.indexOf(value) === index;
    }

    $scope.importMsg = function (resp) {
        var errors = [];
        if (resp.errors) {
            errors = resp.errors.map(error => error.error).filter(onlyUnique);
        }
        var errorMsg = errors.length === 0 ? "Ошибок нет" : errors.join(",\n\t\t\t\t\t\t\t   ");
        var msg = `
            Количество записей, для которых найдены дубли: ${resp.countClones}
            Количество созданых пользователей: ${resp.createdUsers}
            Информация об ошибках: ${errorMsg}`;

        Dialog.message('Информация', msg, () => location.reload());

        return msg;
    };

    $scope.downloadTemplateXlsx = function () {
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/downloadImportUsersTemplate/xlsx`, null, {
            headers: {
                'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8',
                'Content-Type': 'application/json'
            },
            responseType: 'arraybuffer'
        }).then((response) => {
            $scope.responseHandleXlsx(response);
        })
    };

    $scope.exportXlsx = function () {
        let payload = {
            type: 'xlsx',
            userParameters: [
                "USER_ID",
                "FIRST_NAME",
                "EMAIL",
                "PHONE",
                "TOMS_ID",
                "DMP_ID",
                "ROLE",
                "SYSTEM",
                "ENABLED"
            ],
            userIds: $scope.users.filter(user => user.active).map(user => user.id)
        };
        $scope.exportTemplateXlsx(payload)
    };

    $scope.exportTemplateXlsx = function (payload) {
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/downloadUsers`, payload, {
            headers: {
                'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8',
                'Content-Type': 'application/json'
            },
            responseType: 'arraybuffer'
        }).then((response) => {
            $scope.responseHandleXlsx(response);
        })
    };

    $scope.responseHandleXlsx = function (response) {
        var linkElement = document.createElement('a');
        var headers = response.headers();
        var filename = 'users_info.xlsx';
        var contentType = headers['content-type'];
        var blob = new Blob([response.data], {type: contentType});
        var url = window.URL.createObjectURL(blob, {
            type: 'data:attachment/xlsx'
        });

        linkElement.setAttribute('href', url);
        linkElement.setAttribute("download", filename);

        var clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": false
        });
        linkElement.dispatchEvent(clickEvent);

        if ($scope.tempRealm !== undefined) {
            $scope.query.searchRealm = $scope.tempRealm;
        }
    }

    $scope.downloadTemplateCSV = function () {
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/downloadImportUsersTemplate/csv`, null, {
            headers: {'Accept': 'application/octet-stream;charset=UTF-8', 'Content-Type': 'application/json'}
        }).then((response) => {
            $scope.responseHandleCsv(response);
        })
    };

    $scope.exportCSV = function () {
        let payload = {
            type: 'csv',
            userParameters: [
                "USER_ID",
                "FIRST_NAME",
                "EMAIL",
                "PHONE",
                "TOMS_ID",
                "DMP_ID",
                "ROLE",
                "SYSTEM",
                "ENABLED"
            ],
            userIds: $scope.users.filter(user => user.active).map(user => user.id)
        };
        $scope.exportTemplateCSV(payload)
    };

    $scope.exportTemplateCSV = function (payload) {
        var linkElement = document.createElement('a');
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/downloadUsers`, payload, {
            headers: {'Accept': 'application/octet-stream;charset=UTF-8', 'Content-Type': 'application/json'}
        }).then((response) => {
            $scope.responseHandleCsv(response);
        })
    };

    $scope.responseHandleCsv = function (response) {
        var linkElement = document.createElement('a');
        var headers = response.headers();
        var filename = 'users_info.csv';
        var contentType = headers['content-type'];
        var blob = new Blob(["\ufeff", response.data], {type: contentType});
        var url = window.URL.createObjectURL(blob);

        linkElement.setAttribute('href', url);
        linkElement.setAttribute("download", filename);

        var clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": false
        });
        linkElement.dispatchEvent(clickEvent);

        if ($scope.tempRealm !== undefined) {
            $scope.query.searchRealm = $scope.tempRealm;
        }
    }

    $scope.search = function () {
        console.log("query.search: " + $scope.query.search);
        $http.get(`${authUrl}/realms/user/users-info/search?` +
            `searchRealm=${$scope.query.searchRealm}&search=${$scope.query.search}` +
            `&searchUser=${$scope.query.searchByUserId}&searchToms=${$scope.query.searchByTomsId}` +
            `&pageNum=${$scope.pages.number}&pageSize=${$scope.pageSize}` +
            `&sortAsc=${sortAsc}&sortField=${currentSortField}`).then(function (data) {
            $scope.users = angular.fromJson(data).data.results['users-info'];
            $scope.pages = angular.fromJson(data).data.results['page-info'];
            $scope.searchLoaded = true;
            $scope.lastSearch = $scope.query.search;
            UserSearchState.isFirstSearch = false;

            if (!isInitPagination) {
                initPagination();
            }
        });
    };

    $scope.editUser = function (user) {
        $window.location.href = `#/realms/${realm.realm}/users/${user.id}?searchRealm=${$scope.query.searchRealm}`;
    };

    $scope.removeUser = function (user) {
        Dialog.confirmDelete(user.id, 'user', function () {
            $http.delete(`${authUrl}/admin/realms/${realm.realm}/users/${user.id}`)
                .then(() => {
                    Notifications.success("The user has been deleted.");
                    $route.reload();
                }).catch((error) => {
                Notifications.error("User couldn't be deleted");
            })
        });
    };

    $scope.getTomsIds = function (userPost) {
        var tomsIds = Array.from([userPost.tomsId]);
        if (userPost.systemRoles === undefined || userPost.systemRoles.length === 0) {
            return tomsIds;
        }
        for (var i = 1; i < userPost.systemRoles.length; i++) {
            tomsIds.push('\u00A0');  //пустой символ
        }
        return tomsIds;
    }

    $scope.getOrgs = function (userPost) {
        var orgName = '\u00A0';
        if (userPost.organization !== undefined && userPost.organization.trim() !== '') {
            orgName = userPost.organization;
        }

        var orgs = Array.from([orgName]);
        if (userPost.systemRoles === undefined || userPost.systemRoles.length === 0) {
            return orgs;
        }
        for (var i = 1; i < userPost.systemRoles.length; i++) {
            orgs.push('\u00A0');  //пустой символ
        }
        return orgs;
    }

    $scope.getRoleNames = function (userPost, sysName) {
        var roleNames = Array.from([userPost.userRole.name]);
        if (userPost.systemRoles === undefined || userPost.systemRoles.length === 0) {
            return roleNames;
        }
        for (var i = 1; i < userPost.systemRoles.length; i++) {
            roleNames.push('\u00A0');  //пустой символ
        }
        return roleNames;
    }

    $scope.getSystemNames = function (userPost) {
        if (userPost.systemRoles === undefined || userPost.systemRoles.length === 0) {
            return Array.from('\u00A0'); //пустой символ
        }
        return userPost.systemRoles.map(role => role.externalSystem.name);
    }

});

module.controller('UserTabCtrl', function ($scope, $location, Dialog, Notifications, Current) {

    $scope.query = {};
    $scope.query.searchRealm = $location.search().searchRealm;

    $scope.removeUser = function () {
        Dialog.confirmDelete($scope.user.id, 'user', function () {
            $scope.user.$remove({
                realm: Current.realm.realm,
                userId: $scope.user.id
            }, function () {
                $location.url("/realms/" + Current.realm.realm + "/users");
                Notifications.success("The user has been deleted.");
            }, function () {
                Notifications.error("User couldn't be deleted");
            });
        });
    };
});

module.controller('UserDetailCtrl', function ($scope, realm, user, BruteForceUser, User,
                                              Components,
                                              UserImpersonation, RequiredActions,
                                              UserStorageOperations,
                                              $location, $http, Dialog, Notifications) {
    $scope.realm = realm;
    $scope.create = !user.id;
    $scope.editUsername = $scope.create || $scope.realm.editUsernameAllowed;

    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    if ($scope.create) {
        $scope.user = {enabled: true, attributes: {}}
    } else {
        if (!user.attributes) {
            user.attributes = {}
        }
        convertAttributeValuesToString(user);

        $scope.user = angular.copy(user);
        $scope.impersonate = function () {
            $http.post(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/impersonation/' + $scope.user.id).then(function (data) {
                if (data.data.sameRealm) {
                    window.location = data.data.redirect;
                } else {
                    window.open(data.data.redirect, "_blank");
                }
            });
        };
        if (user.federationLink) {
            console.log("federationLink is not null. It is " + user.federationLink);

            if ($scope.access.viewRealm) {
                Components.get({realm: realm.realm, componentId: user.federationLink}, function (link) {
                    $scope.federationLinkName = link.name;
                    $scope.federationLink = "#/realms/" + realm.realm + "/user-storage/providers/" + link.providerId + "/" + link.id;
                });
            } else {
                // KEYCLOAK-4328
                UserStorageOperations.simpleName.get({
                    realm: realm.realm,
                    componentId: user.federationLink
                }, function (link) {
                    $scope.federationLinkName = link.name;
                    $scope.federationLink = $location.absUrl();
                })
            }

        } else {
            console.log("federationLink is null");
        }
        if (user.origin) {
            if ($scope.access.viewRealm) {
                Components.get({realm: realm.realm, componentId: user.origin}, function (link) {
                    $scope.originName = link.name;
                    $scope.originLink = "#/realms/" + realm.realm + "/user-storage/providers/" + link.providerId + "/" + link.id;
                })
            } else {
                // KEYCLOAK-4328
                UserStorageOperations.simpleName.get({realm: realm.realm, componentId: user.origin}, function (link) {
                    $scope.originName = link.name;
                    $scope.originLink = $location.absUrl();
                })
            }
        } else {
            console.log("origin is null");
        }
        console.log('realm brute force? ' + realm.bruteForceProtected);
        $scope.temporarilyDisabled = false;
        var isDisabled = function () {
            BruteForceUser.get({realm: realm.realm, userId: user.id}, function (data) {
                console.log('here in isDisabled ' + data.disabled);
                $scope.temporarilyDisabled = data.disabled;
            });
        };

        console.log("check if disabled");
        isDisabled();

        $scope.unlockUser = function () {
            BruteForceUser.delete({realm: realm.realm, userId: user.id}, function (data) {
                isDisabled();
            });
        }
    }

    $scope.changed = false; // $scope.create;
    if (user.requiredActions) {
        for (var i = 0; i < user.requiredActions.length; i++) {
            console.log("user require action: " + user.requiredActions[i]);
        }
    }
    // ID - Name map for required actions. IDs are enum names.
    RequiredActions.query({realm: realm.realm}, function (data) {
        $scope.userReqActionList = [];
        for (var i = 0; i < data.length; i++) {
            console.log("listed required action: " + data[i].name);
            if (data[i].enabled) {
                var item = data[i];
                $scope.userReqActionList.push(item);
            }
        }
        console.log("---------------------");
        console.log("ng-model: user.requiredActions=" + JSON.stringify($scope.user.requiredActions));
        console.log("---------------------");
        console.log("ng-repeat: userReqActionList=" + JSON.stringify($scope.userReqActionList));
        console.log("---------------------");
    });
    $scope.$watch('user', function () {
        if (!angular.equals($scope.user, user)) {
            $scope.changed = true;
        }
    }, true);

    /**
     * @return {string}
     */
    $scope.GetPhoneAttr = function () {
        var phone = '';
        var attrs = $scope.user.attributes;
        for (var attribute in attrs) {
            if (attribute === 'phone') {
                phone = attrs[attribute];
            }
        }
        return phone;
    };

    $scope.GetPhoneCheckerResult = function () {
        return $http.get(authUrl + '/realms/' + realm.realm + '/users-info/attribute?phone=' + $scope.GetPhoneAttr() + '&excludedUserId=' + $scope.user.id)
            .then(function (response) {
                return angular.fromJson(response).data.results['foundUserId'];
            });
    };

    $scope.save = function () {
        convertAttributeValuesToLists();

        if ($scope.create) {
            User.save({
                realm: $scope.query.searchRealm
            }, $scope.user, function (data, headers) {
                $scope.changed = false;
                convertAttributeValuesToString($scope.user);
                user = angular.copy($scope.user);
                var l = headers().location;

                console.debug("Location == " + l);

                var id = l.substring(l.lastIndexOf("/") + 1);

                $location.url("/realms/" + realm.realm + "/users/" + id + "?searchRealm=" + $scope.query.searchRealm);
                Notifications.success("The user has been created.");
            });
        } else {
            if ($scope.GetPhoneAttr() === '') {
                User.update({
                    realm: realm.realm,
                    userId: $scope.user.id
                }, $scope.user, function () {
                    $scope.changed = false;
                    convertAttributeValuesToString($scope.user);
                    user = angular.copy($scope.user);
                    Notifications.success("Your changes have been saved to the user.");
                });
            } else {
                $scope.GetPhoneCheckerResult().then(function (result) {
                    if (result != null) {
                        Notifications.error("The user phone number not unique");
                        return;
                    }
                    User.update({
                        realm: realm.realm,
                        userId: $scope.user.id
                    }, $scope.user, function () {
                        $scope.changed = false;
                        convertAttributeValuesToString($scope.user);
                        user = angular.copy($scope.user);
                        Notifications.success("Your changes have been saved to the user.");
                    });
                });
            }
        }
    };

    function convertAttributeValuesToLists() {
        var attrs = $scope.user.attributes;
        for (var attribute in attrs) {
            if (typeof attrs[attribute] === "string") {
                var attrVals = attrs[attribute].split("##");
                attrs[attribute] = attrVals;
            }
        }
    }

    function convertAttributeValuesToString(user) {
        var attrs = user.attributes;
        for (var attribute in attrs) {
            if (typeof attrs[attribute] === "object") {
                var attrVals = attrs[attribute].join("##");
                attrs[attribute] = attrVals;
            }
        }
    }

    $scope.reset = function () {
        $scope.user = angular.copy(user);
        $scope.changed = false;
    };

    $scope.cancel = function () {
        $location.url("/realms/" + realm.realm + "/users");
    };

    $scope.addAttribute = function () {
        $scope.user.attributes[$scope.newAttribute.key] = $scope.newAttribute.value;
        delete $scope.newAttribute;
    };

    $scope.removeAttribute = function (key) {
        delete $scope.user.attributes[key];
    }
});

module.controller('UserCredentialsCtrl', function ($scope, realm, user, $route, RequiredActions, User,
                                                   UserExecuteActionsEmail, UserCredentials, Notifications, Dialog,
                                                   TimeUnit2, $location) {
    console.log('UserCredentialsCtrl');

    $scope.realm = realm;
    $scope.user = angular.copy(user);
    $scope.temporaryPassword = true;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    $scope.isTotp = false;
    if (!!user.totp) {
        $scope.isTotp = user.totp;
    }
    // ID - Name map for required actions. IDs are enum names.
    RequiredActions.query({realm: realm.realm}, function (data) {
        $scope.userReqActionList = [];
        for (var i = 0; i < data.length; i++) {
            console.log("listed required action: " + data[i].name);
            if (data[i].enabled) {
                var item = data[i];
                $scope.userReqActionList.push(item);
            }
        }

    });

    $scope.resetPassword = function () {
        // hit enter without entering both fields - ignore
        if (!$scope.passwordAndConfirmPasswordEntered()) return;

        if ($scope.pwdChange) {
            if ($scope.password != $scope.confirmPassword) {
                Notifications.error("Password and confirmation does not match.");
                return;
            }
        }

        var msgTitle = 'Change password';
        var msg = 'Are you sure you want to change the users password?';

        Dialog.confirm(msgTitle, msg, function () {
            UserCredentials.resetPassword({realm: realm.realm, userId: user.id}, {
                type: "password",
                value: $scope.password,
                temporary: $scope.temporaryPassword
            }, function () {
                Notifications.success("The password has been reset");
                $scope.password = null;
                $scope.confirmPassword = null;
                $route.reload();
            });
        }, function () {
            $scope.password = null;
            $scope.confirmPassword = null;
        });
    };

    $scope.passwordAndConfirmPasswordEntered = function () {
        return $scope.password && $scope.confirmPassword;
    };

    $scope.disableCredentialTypes = function () {
        Dialog.confirm('Disable credentials', 'Are you sure you want to disable these users credentials?', function () {
            UserCredentials.disableCredentialTypes({
                realm: realm.realm,
                userId: user.id
            }, $scope.disableableCredentialTypes, function () {
                $route.reload();
                Notifications.success("Credentials disabled");
            }, function () {
                Notifications.error("Failed to disable credentials");
            });
        });
    };

    $scope.emailActions = [];
    $scope.emailActionsTimeout = TimeUnit2.asUnit(realm.actionTokenGeneratedByAdminLifespan);
    $scope.disableableCredentialTypes = [];

    $scope.sendExecuteActionsEmail = function () {
        if ($scope.changed) {
            Dialog.message("Cannot send email", "You must save your current changes before you can send an email");
            return;
        }
        Dialog.confirm('Send Email', 'Are you sure you want to send email to user?', function () {
            UserExecuteActionsEmail.update({
                realm: realm.realm,
                userId: user.id,
                lifespan: $scope.emailActionsTimeout.toSeconds()
            }, $scope.emailActions, function () {
                Notifications.success("Email sent to user");
                $scope.emailActions = [];
            }, function () {
                Notifications.error("Failed to send email to user");
            });
        });
    };


    $scope.$watch('user', function () {
        if (!angular.equals($scope.user, user)) {
            $scope.userChange = true;
        } else {
            $scope.userChange = false;
        }
    }, true);

    $scope.$watch('password', function () {
        if (!!$scope.password) {
            $scope.pwdChange = true;
        } else {
            $scope.pwdChange = false;
        }
    }, true);

    $scope.reset = function () {
        $scope.password = "";
        $scope.confirmPassword = "";

        $scope.user = angular.copy(user);

        $scope.isTotp = false;
        if (!!user.totp) {
            $scope.isTotp = user.totp;
        }

        $scope.pwdChange = false;
        $scope.userChange = false;
    };
});

module.controller('UserFederationCtrl', function ($scope, $location, $route, realm, serverInfo, Components, Notifications, Dialog) {
    console.log('UserFederationCtrl ++++****');
    $scope.realm = realm;
    $scope.providers = serverInfo.componentTypes['org.keycloak.storage.UserStorageProvider'];
    $scope.instancesLoaded = false;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    if (!$scope.providers) $scope.providers = [];

    $scope.addProvider = function (provider) {
        console.log('Add provider: ' + provider.id);
        $location.url("/create/user-storage/" + realm.realm + "/providers/" + provider.id);
    };

    $scope.getInstanceLink = function (instance) {
        return "/realms/" + realm.realm + "/user-storage/providers/" + instance.providerId + "/" + instance.id;
    };

    $scope.getInstanceName = function (instance) {
        return instance.name;
    };
    $scope.getInstanceProvider = function (instance) {
        return instance.providerId;
    };

    $scope.isProviderEnabled = function (instance) {
        return !instance.config['enabled'] || instance.config['enabled'][0] == 'true';
    };

    $scope.getInstancePriority = function (instance) {
        if (!instance.config['priority']) {
            console.log('getInstancePriority is undefined');
        }
        return instance.config['priority'][0];
    };

    Components.query({
        realm: realm.realm,
        parent: realm.id,
        type: 'org.keycloak.storage.UserStorageProvider'
    }, function (data) {
        $scope.instances = data;
        $scope.instancesLoaded = true;
    });

    $scope.removeInstance = function (instance) {
        Dialog.confirmDelete(instance.name, 'user storage provider', function () {
            Components.remove({
                realm: realm.realm,
                componentId: instance.id
            }, function () {
                $route.reload();
                Notifications.success("The provider has been deleted.");
            });
        });
    };
});

module.controller('GenericUserStorageCtrl', function ($scope, $location, Notifications, $route, Dialog, realm,
                                                      serverInfo, instance, providerId, Components, UserStorageOperations) {
    console.log('GenericUserStorageCtrl');
    console.log('providerId: ' + providerId);
    $scope.create = !instance.providerId;
    console.log('create: ' + $scope.create);
    var providers = serverInfo.componentTypes['org.keycloak.storage.UserStorageProvider'];
    console.log('providers length ' + providers.length);
    var providerFactory = null;
    for (var i = 0; i < providers.length; i++) {
        var p = providers[i];
        console.log('provider: ' + p.id);
        if (p.id == providerId) {
            $scope.providerFactory = p;
            providerFactory = p;
            break;
        }

    }
    $scope.showSync = false;
    $scope.changed = false;

    console.log("providerFactory: " + providerFactory.id);

    function initUserStorageSettings() {
        if ($scope.create) {
            $scope.changed = true;
            instance.name = providerFactory.id;
            instance.providerId = providerFactory.id;
            instance.providerType = 'org.keycloak.storage.UserStorageProvider';
            instance.parentId = realm.id;
            instance.config = {};
            instance.config['priority'] = ["0"];
            instance.config['enabled'] = ["true"];

            $scope.fullSyncEnabled = false;
            $scope.changedSyncEnabled = false;
            if (providerFactory.metadata.synchronizable) {
                instance.config['fullSyncPeriod'] = ['-1'];
                instance.config['changedSyncPeriod'] = ['-1'];

            }
            instance.config['cachePolicy'] = ['DEFAULT'];
            instance.config['evictionDay'] = [''];
            instance.config['evictionHour'] = [''];
            instance.config['evictionMinute'] = [''];
            instance.config['maxLifespan'] = [''];
            if (providerFactory.properties) {

                for (var i = 0; i < providerFactory.properties.length; i++) {
                    var configProperty = providerFactory.properties[i];
                    if (configProperty.defaultValue) {
                        instance.config[configProperty.name] = [configProperty.defaultValue];
                    } else {
                        instance.config[configProperty.name] = [''];
                    }

                }
            }

        } else {
            $scope.changed = false;
            $scope.fullSyncEnabled = (instance.config['fullSyncPeriod'] && instance.config['fullSyncPeriod'][0] > 0);
            $scope.changedSyncEnabled = (instance.config['changedSyncPeriod'] && instance.config['changedSyncPeriod'][0] > 0);
            if (providerFactory.metadata.synchronizable) {
                if (!instance.config['fullSyncPeriod']) {
                    console.log('setting to -1');
                    instance.config['fullSyncPeriod'] = ['-1'];

                }
                if (!instance.config['changedSyncPeriod']) {
                    console.log('setting to -1');
                    instance.config['changedSyncPeriod'] = ['-1'];

                }
            }
            if (!instance.config['enabled']) {
                instance.config['enabled'] = ['true'];
            }
            if (!instance.config['cachePolicy']) {
                instance.config['cachePolicy'] = ['DEFAULT'];

            }
            if (!instance.config['evictionDay']) {
                instance.config['evictionDay'] = [''];

            }
            if (!instance.config['evictionHour']) {
                instance.config['evictionHour'] = [''];

            }
            if (!instance.config['evictionMinute']) {
                instance.config['evictionMinute'] = [''];

            }
            if (!instance.config['maxLifespan']) {
                instance.config['maxLifespan'] = [''];

            }
            if (!instance.config['priority']) {
                instance.config['priority'] = ['0'];
            }

            if (providerFactory.properties) {
                for (var i = 0; i < providerFactory.properties.length; i++) {
                    var configProperty = providerFactory.properties[i];
                    if (!instance.config[configProperty.name]) {
                        instance.config[configProperty.name] = [''];
                    }
                }
            }

        }
        if (providerFactory.metadata.synchronizable) {
            if (instance.config && instance.config['importEnabled']) {
                $scope.showSync = instance.config['importEnabled'][0] == 'true';
            } else {
                $scope.showSync = true;
            }
        }

    }

    initUserStorageSettings();
    $scope.instance = angular.copy(instance);
    $scope.realm = realm;

    $scope.$watch('instance', function () {
        if (!angular.equals($scope.instance, instance)) {
            $scope.changed = true;
        }

    }, true);

    $scope.$watch('fullSyncEnabled', function (newVal, oldVal) {
        if (oldVal == newVal) {
            return;
        }

        $scope.instance.config['fullSyncPeriod'][0] = $scope.fullSyncEnabled ? "604800" : "-1";
        $scope.changed = true;
    });

    $scope.$watch('changedSyncEnabled', function (newVal, oldVal) {
        if (oldVal == newVal) {
            return;
        }

        $scope.instance.config['changedSyncPeriod'][0] = $scope.changedSyncEnabled ? "86400" : "-1";
        $scope.changed = true;
    });


    $scope.save = function () {
        console.log('save provider');
        $scope.changed = false;
        if ($scope.create) {
            console.log('saving new provider');
            Components.save({realm: realm.realm}, $scope.instance, function (data, headers) {
                var l = headers().location;
                var id = l.substring(l.lastIndexOf("/") + 1);

                $location.url("/realms/" + realm.realm + "/user-storage/providers/" + $scope.instance.providerId + "/" + id);
                Notifications.success("The provider has been created.");
            });
        } else {
            console.log('update existing provider');
            Components.update({
                    realm: realm.realm,
                    componentId: instance.id
                },
                $scope.instance, function () {
                    $route.reload();
                    Notifications.success("The provider has been updated.");
                });
        }
    };

    $scope.reset = function () {
        //initUserStorageSettings();
        //$scope.instance = angular.copy(instance);
        $route.reload();
    };

    $scope.cancel = function () {
        console.log('cancel');
        if ($scope.create) {
            $location.url("/realms/" + realm.realm + "/user-federation");
        } else {
            $route.reload();
        }
    };

    $scope.triggerFullSync = function () {
        console.log('GenericCtrl: triggerFullSync');
        triggerSync('triggerFullSync');
    };

    $scope.triggerChangedUsersSync = function () {
        console.log('GenericCtrl: triggerChangedUsersSync');
        triggerSync('triggerChangedUsersSync');
    };

    function triggerSync(action) {
        UserStorageOperations.sync.save({
            action: action,
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Sync of users finished successfully. " + syncResult.status);
        }, function () {
            $route.reload();
            Notifications.error("Error during sync of users");
        });
    }

    $scope.removeImportedUsers = function () {
        UserStorageOperations.removeImportedUsers.save({
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Remove imported users finished successfully. ");
        }, function () {
            $route.reload();
            Notifications.error("Error during remove");
        });
    };
    $scope.unlinkUsers = function () {
        UserStorageOperations.unlinkUsers.save({
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Unlink of users finished successfully. ");
        }, function () {
            $route.reload();
            Notifications.error("Error during unlink");
        });
    };

});

function removeGroupMember(groups, member) {
    for (var j = 0; j < groups.length; j++) {
        //console.log('checking: ' + groups[j].path);
        if (member.path == groups[j].path) {
            groups.splice(j, 1);
            break;
        }
        if (groups[j].subGroups && groups[j].subGroups.length > 0) {
            //console.log('going into subgroups');
            removeGroupMember(groups[j].subGroups, member);
        }
    }
}

module.controller('UserGroupMembershipCtrl', function ($scope, $q, realm, user, UserGroupMembership,
                                                       UserGroupMembershipCount, UserGroupMapping, Notifications,
                                                       Groups, GroupsCount, $location) {
    $scope.realm = realm;
    $scope.user = user;
    $scope.groupList = [];
    $scope.allGroupMemberships = [];
    $scope.groupMemberships = [];
    $scope.tree = [];
    $scope.membershipTree = [];

    $scope.searchCriteria = '';
    $scope.searchCriteriaMembership = '';
    $scope.currentPage = 1;
    $scope.currentMembershipPage = 1;
    $scope.currentPageInput = $scope.currentPage;
    $scope.currentMembershipPageInput = $scope.currentMembershipPage;
    $scope.pageSize = 20;
    $scope.numberOfPages = 1;
    $scope.numberOfMembershipPages = 1;

    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    var refreshCompleteUserGroupMembership = function () {
        var queryParams = {
            realm: realm.realm,
            userId: user.id
        };

        var promiseGetCompleteUserGroupMembership = $q.defer();
        UserGroupMembership.query(queryParams, function (entry) {
            promiseGetCompleteUserGroupMembership.resolve(entry);
        }, function () {
            promiseGetCompleteUserGroupMembership.reject('Unable to fetch all group memberships' + queryParams);
        });
        promiseGetCompleteUserGroupMembership.promise.then(function (groups) {
            for (var i = 0; i < groups.length; i++) {
                $scope.allGroupMemberships.push(groups[i]);
                $scope.getGroupClass(groups[i]);
            }
        }, function (failed) {
            Notifications.error(failed);
        });
    };

    var refreshUserGroupMembership = function (search) {
        var first = ($scope.currentMembershipPage * $scope.pageSize) - $scope.pageSize;
        var queryParams = {
            realm: realm.realm,
            userId: user.id,
            first: first,
            max: $scope.pageSize
        };

        var countParams = {
            realm: realm.realm,
            userId: user.id
        };

        var isSearch = function () {
            return angular.isDefined(search) && search !== '';
        };

        if (isSearch()) {
            queryParams.search = search;
            countParams.search = search;
        }

        var promiseGetUserGroupMembership = $q.defer();
        UserGroupMembership.query(queryParams, function (entry) {
            promiseGetUserGroupMembership.resolve(entry);
        }, function () {
            promiseGetUserGroupMembership.reject('Unable to fetch ' + queryParams);
        });
        promiseGetUserGroupMembership.promise.then(function (groups) {
            $scope.groupMemberships = groups;
        }, function (failed) {
            Notifications.error(failed);
        });

        var promiseMembershipCount = $q.defer();
        UserGroupMembershipCount.query(countParams, function (entry) {
            promiseMembershipCount.resolve(entry);
        }, function () {
            promiseMembershipCount.reject('Unable to fetch ' + countParams);
        });
        promiseMembershipCount.promise.then(function (membershipEntry) {
            if (angular.isDefined(membershipEntry.count) && membershipEntry.count > $scope.pageSize) {
                $scope.numberOfMembershipPages = Math.ceil(membershipEntry.count / $scope.pageSize);
            } else {
                $scope.numberOfMembershipPages = 1;
            }
        }, function (failed) {
            Notifications.error(failed);
        });
    };

    var refreshAvailableGroups = function (search) {
        var first = ($scope.currentPage * $scope.pageSize) - $scope.pageSize;
        var queryParams = {
            realm: realm.realm,
            first: first,
            max: $scope.pageSize
        };

        var countParams = {
            realm: realm.realm,
            top: 'true'
        };

        if (angular.isDefined(search) && search !== '') {
            queryParams.search = search;
            countParams.search = search;
        }

        var promiseGetGroups = $q.defer();
        Groups.query(queryParams, function (entry) {
            promiseGetGroups.resolve(entry);
        }, function () {
            promiseGetGroups.reject('Unable to fetch ' + queryParams);
        });

        promiseGetGroups.promise.then(function (groups) {
            $scope.groupList = groups;
        }, function (failed) {
            Notifications.error(failed);
        });

        var promiseCount = $q.defer();
        GroupsCount.query(countParams, function (entry) {
            promiseCount.resolve(entry);
        }, function () {
            promiseCount.reject('Unable to fetch ' + countParams);
        });
        promiseCount.promise.then(function (entry) {
            if (angular.isDefined(entry.count) && entry.count > $scope.pageSize) {
                $scope.numberOfPages = Math.ceil(entry.count / $scope.pageSize);
            } else {
                $scope.numberOfPages = 1;
            }
        }, function (failed) {
            Notifications.error(failed);
        });
        return promiseGetGroups.promise;
    };

    $scope.clearSearchMembership = function () {
        $scope.searchCriteriaMembership = '';
        $scope.currentMembershipPage = 1;
        $scope.currentMembershipPageInput = 1;
        refreshUserGroupMembership();
    };

    $scope.searchGroupMembership = function () {
        $scope.currentMembershipPage = 1;
        refreshUserGroupMembership($scope.searchCriteriaMembership);
    };

    refreshAvailableGroups();
    refreshUserGroupMembership();
    refreshCompleteUserGroupMembership();

    $scope.$watch('currentPage', function (newValue, oldValue) {
        if (newValue !== oldValue) {
            refreshAvailableGroups($scope.searchCriteria)
                .then(function () {
                    refreshUserGroupMembership($scope.searchCriteriaMembership);
                });
        }
    });

    $scope.$watch('currentMembershipPage', function (newValue, oldValue) {
        if (newValue !== oldValue) {
            refreshUserGroupMembership($scope.searchCriteriaMembership);
        }
    });

    $scope.clearSearch = function () {
        $scope.searchCriteria = '';
        $scope.currentPage = 1;
        $scope.currentPageInput = 1;
        refreshAvailableGroups();
    };

    $scope.searchGroup = function () {
        $scope.currentPage = 1;
        refreshAvailableGroups($scope.searchCriteria);
    };

    $scope.joinGroup = function () {
        if (!$scope.tree.currentNode) {
            Notifications.error('Please select a group to add');
            return;
        }
        if (isMember($scope.tree.currentNode)) {
            Notifications.error('Group already added');
            return;
        }
        UserGroupMapping.update({
            realm: realm.realm,
            userId: user.id,
            groupId: $scope.tree.currentNode.id
        }, function () {
            $scope.allGroupMemberships.push($scope.tree.currentNode);
            refreshUserGroupMembership();
            Notifications.success('Added group membership');
        });

    };

    $scope.leaveGroup = function () {
        if (!$scope.membershipTree.currentNode) {
            Notifications.error('Please select a group to remove');
            return;
        }
        UserGroupMapping.remove({
            realm: realm.realm,
            userId: user.id,
            groupId: $scope.membershipTree.currentNode.id
        }, function () {
            removeGroupMember($scope.allGroupMemberships, $scope.membershipTree.currentNode);
            refreshAvailableGroups();
            refreshUserGroupMembership();
            Notifications.success('Removed group membership');
        });

    };

    var isLeaf = function (node) {
        return node.id !== 'realm' && (!node.subGroups || node.subGroups.length === 0);
    };

    var isMember = function (node) {
        for (var i = 0; i < $scope.allGroupMemberships.length; i++) {
            var member = $scope.allGroupMemberships[i];
            if (node.id === member.id) {
                return true;
            }
        }
        return false;
    };

    $scope.getGroupClass = function (node) {
        if (node.id == "realm") {
            return 'pficon pficon-users';
        }
        if (isMember(node)) {
            return 'normal deactivate';
        }
        if (isLeaf(node)) {
            return 'normal';
        }
        if (node.subGroups.length && node.collapsed) return 'collapsed';
        if (node.subGroups.length && !node.collapsed) return 'expanded';
        return 'collapsed';

    };

    $scope.getSelectedClass = function (node) {
        if (node.selected) {
            if (isMember(node)) {
                return "deactivate_selected";
            } else {
                return 'selected';
            }
        } else if ($scope.cutNode && $scope.cutNode.id === node.id) {
            return 'cut';
        }
        return undefined;
    }

});

module.controller('LDAPUserStorageCtrl', function ($scope, $location, Notifications, $route, Dialog, realm,
                                                   serverInfo, instance, Components, UserStorageOperations, RealmLDAPConnectionTester) {
    console.log('LDAPUserStorageCtrl');
    var providerId = 'ldap';
    console.log('providerId: ' + providerId);
    $scope.create = !instance.providerId;
    console.log('create: ' + $scope.create);
    var providers = serverInfo.componentTypes['org.keycloak.storage.UserStorageProvider'];
    console.log('providers length ' + providers.length);
    var providerFactory = null;
    for (var i = 0; i < providers.length; i++) {
        var p = providers[i];
        console.log('provider: ' + p.id);
        if (p.id == providerId) {
            $scope.providerFactory = p;
            providerFactory = p;
            break;
        }

    }

    $scope.provider = instance;
    $scope.showSync = false;

    if (serverInfo.profileInfo.name == 'community') {
        $scope.ldapVendors = [
            {"id": "ad", "name": "Active Directory"},
            {"id": "rhds", "name": "Red Hat Directory Server"},
            {"id": "tivoli", "name": "Tivoli"},
            {"id": "edirectory", "name": "Novell eDirectory"},
            {"id": "other", "name": "Other"}
        ];
    } else {
        $scope.ldapVendors = [
            {"id": "ad", "name": "Active Directory"},
            {"id": "rhds", "name": "Red Hat Directory Server"}
        ];
    }

    $scope.authTypes = [
        {"id": "none", "name": "none"},
        {"id": "simple", "name": "simple"}
    ];

    $scope.searchScopes = [
        {"id": "1", "name": "One Level"},
        {"id": "2", "name": "Subtree"}
    ];

    $scope.useTruststoreOptions = [
        {"id": "always", "name": "Always"},
        {"id": "ldapsOnly", "name": "Only for ldaps"},
        {"id": "never", "name": "Never"}
    ];

    var DEFAULT_BATCH_SIZE = "1000";


    console.log("providerFactory: " + providerFactory.id);

    $scope.changed = false;

    function initUserStorageSettings() {
        if ($scope.create) {
            $scope.changed = true;
            instance.name = 'ldap';
            instance.providerId = 'ldap';
            instance.providerType = 'org.keycloak.storage.UserStorageProvider';
            instance.parentId = realm.id;
            instance.config = {};
            instance.config['enabled'] = ["true"];
            instance.config['priority'] = ["0"];

            $scope.fullSyncEnabled = false;
            $scope.changedSyncEnabled = false;
            instance.config['fullSyncPeriod'] = ['-1'];
            instance.config['changedSyncPeriod'] = ['-1'];
            instance.config['cachePolicy'] = ['DEFAULT'];
            instance.config['evictionDay'] = [''];
            instance.config['evictionHour'] = [''];
            instance.config['evictionMinute'] = [''];
            instance.config['maxLifespan'] = [''];
            instance.config['batchSizeForSync'] = [DEFAULT_BATCH_SIZE];
            //instance.config['importEnabled'] = ['true'];

            if (providerFactory.properties) {

                for (var i = 0; i < providerFactory.properties.length; i++) {
                    var configProperty = providerFactory.properties[i];
                    if (configProperty.defaultValue) {
                        instance.config[configProperty.name] = [configProperty.defaultValue];
                    } else {
                        instance.config[configProperty.name] = [''];
                    }

                }
            }


        } else {
            $scope.changed = false;
            $scope.fullSyncEnabled = (instance.config['fullSyncPeriod'] && instance.config['fullSyncPeriod'][0] > 0);
            $scope.changedSyncEnabled = (instance.config['changedSyncPeriod'] && instance.config['changedSyncPeriod'][0] > 0);
            if (!instance.config['fullSyncPeriod']) {
                console.log('setting to -1');
                instance.config['fullSyncPeriod'] = ['-1'];

            }
            if (!instance.config['enabled']) {
                instance.config['enabled'] = ['true'];
            }
            if (!instance.config['changedSyncPeriod']) {
                console.log('setting to -1');
                instance.config['changedSyncPeriod'] = ['-1'];

            }
            if (!instance.config['cachePolicy']) {
                instance.config['cachePolicy'] = ['DEFAULT'];

            }
            if (!instance.config['evictionDay']) {
                instance.config['evictionDay'] = [''];

            }
            if (!instance.config['evictionHour']) {
                instance.config['evictionHour'] = [''];

            }
            if (!instance.config['evictionMinute']) {
                instance.config['evictionMinute'] = [''];

            }
            if (!instance.config['maxLifespan']) {
                instance.config['maxLifespan'] = [''];

            }
            if (!instance.config['priority']) {
                instance.config['priority'] = ['0'];
            }
            if (!instance.config['importEnabled']) {
                instance.config['importEnabled'] = ['true'];
            }

            if (providerFactory.properties) {

                for (var i = 0; i < providerFactory.properties.length; i++) {
                    var configProperty = providerFactory.properties[i];
                    if (!instance.config[configProperty.name]) {
                        if (configProperty.defaultValue) {
                            instance.config[configProperty.name] = [configProperty.defaultValue];
                        } else {
                            instance.config[configProperty.name] = [''];
                        }
                    }

                }
            }

            for (var i = 0; i < $scope.ldapVendors.length; i++) {
                if ($scope.ldapVendors[i].id === instance.config['vendor'][0]) {
                    $scope.vendorName = $scope.ldapVendors[i].name;
                }
            }
        }
        if (instance.config && instance.config['importEnabled']) {
            $scope.showSync = instance.config['importEnabled'][0] == 'true';
        } else {
            $scope.showSync = true;
        }

        $scope.lastVendor = instance.config['vendor'][0];
    }

    initUserStorageSettings();
    $scope.instance = angular.copy(instance);
    $scope.realm = realm;

    $scope.$watch('instance', function () {
        if (!angular.equals($scope.instance, instance)) {
            $scope.changed = true;
        }

        if (!angular.equals($scope.instance.config['vendor'][0], $scope.lastVendor)) {
            console.log("LDAP vendor changed. Previous=" + $scope.lastVendor + " New=" + $scope.instance.config['vendor'][0]);
            $scope.lastVendor = $scope.instance.config['vendor'][0];

            if ($scope.lastVendor === "ad") {
                $scope.instance.config['usernameLDAPAttribute'][0] = "cn";
                $scope.instance.config['userObjectClasses'][0] = "person, organizationalPerson, user";
            } else {
                $scope.instance.config['usernameLDAPAttribute'][0] = "uid";
                $scope.instance.config['userObjectClasses'][0] = "inetOrgPerson, organizationalPerson";
            }

            $scope.instance.config['rdnLDAPAttribute'][0] = $scope.instance.config['usernameLDAPAttribute'][0];

            var vendorToUUID = {
                rhds: "nsuniqueid",
                tivoli: "uniqueidentifier",
                edirectory: "guid",
                ad: "objectGUID",
                other: "entryUUID"
            };
            $scope.instance.config['uuidLDAPAttribute'][0] = vendorToUUID[$scope.lastVendor];
        }


    }, true);

    $scope.$watch('fullSyncEnabled', function (newVal, oldVal) {
        if (oldVal == newVal) {
            return;
        }

        $scope.instance.config['fullSyncPeriod'][0] = $scope.fullSyncEnabled ? "604800" : "-1";
        $scope.changed = true;
    });

    $scope.$watch('changedSyncEnabled', function (newVal, oldVal) {
        if (oldVal == newVal) {
            return;
        }

        $scope.instance.config['changedSyncPeriod'][0] = $scope.changedSyncEnabled ? "86400" : "-1";
        $scope.changed = true;
    });


    $scope.save = function () {
        $scope.changed = false;
        if (!$scope.instance.config['batchSizeForSync'] || !parseInt($scope.instance.config['batchSizeForSync'][0])) {
            $scope.instance.config['batchSizeForSync'] = [DEFAULT_BATCH_SIZE];
        } else {
            $scope.instance.config['batchSizeForSync'][0] = parseInt($scope.instance.config.batchSizeForSync).toString();
        }

        if ($scope.create) {
            Components.save({realm: realm.realm}, $scope.instance, function (data, headers) {
                var l = headers().location;
                var id = l.substring(l.lastIndexOf("/") + 1);

                $location.url("/realms/" + realm.realm + "/user-storage/providers/" + $scope.instance.providerId + "/" + id);
                Notifications.success("The provider has been created.");
            });
        } else {
            Components.update({
                    realm: realm.realm,
                    componentId: instance.id
                },
                $scope.instance, function () {
                    $route.reload();
                    Notifications.success("The provider has been updated.");
                });
        }
    };

    $scope.reset = function () {
        $route.reload();
    };

    $scope.cancel = function () {
        if ($scope.create) {
            $location.url("/realms/" + realm.realm + "/user-federation");
        } else {
            $route.reload();
        }
    };

    $scope.triggerFullSync = function () {
        console.log('GenericCtrl: triggerFullSync');
        triggerSync('triggerFullSync');
    };

    $scope.triggerChangedUsersSync = function () {
        console.log('GenericCtrl: triggerChangedUsersSync');
        triggerSync('triggerChangedUsersSync');
    };


    function triggerSync(action) {
        UserStorageOperations.sync.save({
            action: action,
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Sync of users finished successfully. " + syncResult.status);
        }, function () {
            $route.reload();
            Notifications.error("Error during sync of users");
        });
    }

    $scope.removeImportedUsers = function () {
        UserStorageOperations.removeImportedUsers.save({
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Remove imported users finished successfully. ");
        }, function () {
            $route.reload();
            Notifications.error("Error during remove");
        });
    };
    $scope.unlinkUsers = function () {
        UserStorageOperations.unlinkUsers.save({
            realm: $scope.realm.realm,
            componentId: $scope.instance.id
        }, {}, function (syncResult) {
            $route.reload();
            Notifications.success("Unlink of users finished successfully. ");
        }, function () {
            $route.reload();
            Notifications.error("Error during unlink");
        });
    };
    var initConnectionTest = function (testAction, ldapConfig) {
        return {
            action: testAction,
            realm: $scope.realm.realm,
            connectionUrl: ldapConfig.connectionUrl,
            bindDn: ldapConfig.bindDn,
            bindCredential: ldapConfig.bindCredential,
            useTruststoreSpi: ldapConfig.useTruststoreSpi,
            connectionTimeout: ldapConfig.connectionTimeout,
            componentId: instance.id
        };
    };

    $scope.testConnection = function () {
        console.log('LDAPCtrl: testConnection');
        RealmLDAPConnectionTester.save(initConnectionTest("testConnection", $scope.instance.config), function () {
            Notifications.success("LDAP connection successful.");
        }, function () {
            Notifications.error("Error when trying to connect to LDAP. See server.log for details.");
        });
    };

    $scope.testAuthentication = function () {
        console.log('LDAPCtrl: testAuthentication');
        RealmLDAPConnectionTester.save(initConnectionTest("testAuthentication", $scope.instance.config), function () {
            Notifications.success("LDAP authentication successful.");
        }, function () {
            Notifications.error("LDAP authentication failed. See server.log for details");
        });
    }


});

module.controller('LDAPTabCtrl', function (Dialog, $scope, Current, Notifications, $location) {
    $scope.removeUserFederation = function () {
        Dialog.confirmDelete($scope.instance.name, 'ldap provider', function () {
            $scope.instance.$remove({
                realm: Current.realm.realm,
                componentId: $scope.instance.id
            }, function () {
                $location.url("/realms/" + Current.realm.realm + "/user-federation");
                Notifications.success("The provider has been deleted.");
            });
        });
    };
});

module.controller('LDAPMapperListCtrl', function ($scope, $location, Notifications, $route, Dialog, realm, provider, mappers) {
    console.log('LDAPMapperListCtrl');

    $scope.realm = realm;
    $scope.provider = provider;
    $scope.instance = provider;

    $scope.mappers = mappers;

});

module.controller('LDAPMapperCtrl', function ($scope, $route, realm, provider, mapperTypes, mapper, clients, Components, LDAPMapperSync, Notifications, Dialog, $location) {
    console.log('LDAPMapperCtrl');
    $scope.realm = realm;
    $scope.provider = provider;
    $scope.clients = clients;
    $scope.create = false;
    $scope.changed = false;

    for (var i = 0; i < mapperTypes.length; i++) {
        console.log('mapper.providerId: ' + mapper.providerId);
        console.log('mapperTypes[i].id ' + mapperTypes[i].id);
        if (mapperTypes[i].id == mapper.providerId) {
            $scope.mapperType = mapperTypes[i];
            break;
        }
    }

    if ($scope.mapperType.properties) {

        for (var i = 0; i < $scope.mapperType.properties.length; i++) {
            var configProperty = $scope.mapperType.properties[i];
            if (!mapper.config[configProperty.name]) {
                if (configProperty.defaultValue) {
                    mapper.config[configProperty.name] = [configProperty.defaultValue];
                } else {
                    mapper.config[configProperty.name] = [''];
                }
            }

        }
    }
    $scope.mapper = angular.copy(mapper);


    $scope.$watch('mapper', function () {
        if (!angular.equals($scope.mapper, mapper)) {
            $scope.changed = true;
        }
    }, true);

    $scope.save = function () {
        Components.update({
                realm: realm.realm,
                componentId: mapper.id
            },
            $scope.mapper, function () {
                $route.reload();
                Notifications.success("The mapper has been updated.");
            });
    };

    $scope.reset = function () {
        $scope.mapper = angular.copy(mapper);
        $scope.changed = false;
    };

    $scope.remove = function () {
        Dialog.confirmDelete($scope.mapper.name, 'ldap mapper', function () {
            Components.remove({
                realm: realm.realm,
                componentId: mapper.id
            }, function () {
                $location.url("/realms/" + realm.realm + '/ldap-mappers/' + provider.id);
                Notifications.success("The provider has been deleted.");
            });
        });
    };

    $scope.triggerFedToKeycloakSync = function () {
        triggerMapperSync("fedToKeycloak")
    };

    $scope.triggerKeycloakToFedSync = function () {
        triggerMapperSync("keycloakToFed");
    };

    function triggerMapperSync(direction) {
        LDAPMapperSync.save({
            direction: direction,
            realm: realm.realm,
            parentId: provider.id,
            mapperId: $scope.mapper.id
        }, {}, function (syncResult) {
            Notifications.success("Data synced successfully. " + syncResult.status);
        }, function (error) {
            Notifications.error(error.data.errorMessage);
        });
    }

});

module.controller('LDAPMapperCreateCtrl', function ($scope, realm, provider, mapperTypes, clients, Components, Notifications, Dialog, $location) {
    console.log('LDAPMapperCreateCtrl');
    $scope.realm = realm;
    $scope.provider = provider;
    $scope.clients = clients;
    $scope.create = true;
    $scope.mapper = {config: {}};
    $scope.mapperTypes = mapperTypes;
    $scope.mapperType = null;
    $scope.changed = true;

    $scope.$watch('mapperType', function () {
        if ($scope.mapperType != null) {
            $scope.mapper.config = {};
            if ($scope.mapperType.properties) {

                for (var i = 0; i < $scope.mapperType.properties.length; i++) {
                    var configProperty = $scope.mapperType.properties[i];
                    if (!$scope.mapper.config[configProperty.name]) {
                        if (configProperty.defaultValue) {
                            $scope.mapper.config[configProperty.name] = [configProperty.defaultValue];
                        } else {
                            $scope.mapper.config[configProperty.name] = [''];
                        }
                    }

                }
            }
        }
    }, true);

    $scope.save = function () {
        if ($scope.mapperType == null) {
            Notifications.error("You need to select mapper type!");
            return;
        }

        $scope.mapper.providerId = $scope.mapperType.id;
        $scope.mapper.providerType = 'org.keycloak.storage.ldap.mappers.LDAPStorageMapper';
        $scope.mapper.parentId = provider.id;

        if ($scope.mapper.config && $scope.mapper.config["role"] && !Array.isArray($scope.mapper.config["role"])) {
            $scope.mapper.config["role"] = [$scope.mapper.config["role"]];
        }

        Components.save({realm: realm.realm}, $scope.mapper, function (data, headers) {
            var l = headers().location;
            var id = l.substring(l.lastIndexOf("/") + 1);

            $location.url("/realms/" + realm.realm + "/ldap-mappers/" + $scope.mapper.parentId + "/mappers/" + id);
            Notifications.success("The mapper has been created.");
        });
    };

    $scope.reset = function () {
        $location.url("/realms/" + realm.realm + '/ldap-mappers/' + provider.id);
    };


});

module.controller('UserCustomerCtrl', function ($scope, realm, user, $location, $http) {

    $scope.realm = realm;
    $scope.user = user;
    $scope.userPosts = [];
    $scope.customerRoles = [];
    $scope.systemRoles = [];
    $scope.duplicatedPhone = false;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }

    $scope.init = function () {
        $http.get(authUrl + '/realms/' + realm.realm + '/user-post/users/' + user.id).then(function (data) {
            $scope.userPosts = angular.fromJson(data).data.results.user_post;
        });

        $http.get(authUrl + '/realms/' + realm.realm + '/user-post/roles').then(function (data) {
            $scope.customerRoles = angular.fromJson(data).data.results.roles;
        });

        $http.get(authUrl + '/realms/' + realm.realm + '/user-post/system-roles').then(function (data) {
            let roles = angular.fromJson(data).data.results['system-roles'];
            roles = roles.filter(role => role.name === 'access_granted').filter((role, index, self) => self.indexOf(role) === index);
            $scope.systemRoles = roles;
        });
    };

    //удаление строки
    $scope.removeUserPost = function (userPostId) {
        $http.post(authUrl + '/realms/' + $scope.realm.realm + '/user-post/delete/' + userPostId).then(function () {
            console.info('removeUserPost');
            window.location.reload();
        });
    };

    //удаление одной системы
    $scope.removeSystemRole = function (userPostId, systemRoleId) {
        var mapDelete = {userPostId: userPostId, systemRoleId: systemRoleId};
        $http.post(authUrl + '/realms/' + $scope.realm.realm + '/user-post/remove-system-role', mapDelete).then(function () {
            console.info('removeSystemRole sdf');
            $scope.init();
        });
    };

    //добавление одной роли
    $scope.addSystemRole = function (userPostId, systemRoleId) {
        var addMap = {userPostId: userPostId, systemRoleId: systemRoleId};
        $http.post(authUrl + '/realms/' + $scope.realm.realm + '/user-post/add-system-role', addMap).then(function () {
            console.info('addSystemRole');
            $scope.init();
        });
    };

    //добавление нового доступа
    $scope.addUserPost = function () {
        var addMap = {
            userId: user.id,
            tomsId: $scope.newAccess.tomsId,
            roleId: $scope.newAccess.customerRole.id,
            dmpId: $scope.newAccess.dmpId
        };
        $http.post(authUrl + '/realms/' + $scope.realm.realm + '/user-post/create', addMap).then(function (response) {
            console.info('addUserPost');
            if ($scope.newAccess.systemRole) {
                $scope.addSystemRole(angular.fromJson(response).data.results['user_post'].id, $scope.newAccess.systemRole.id);
            }
            window.location.reload();
        });
    };

    //редактирование роли
    $scope.editUserPost = function (userPostId, systemRoleId) {
        var addMap = {id: userPostId, roleId: systemRoleId};
        $http.post(authUrl + '/realms/' + $scope.realm.realm + '/user-post/edit', addMap).then(function () {
            console.info('editUserPost')
        });
    };

    $scope.init();
});

module.controller('ImportUsersCtrl', function ($scope, realm, $location, $http, Notifications) {

    $scope.realm = realm;
    $scope.query = {};
    $scope.query.searchRealm = realm.realm;
    if ($location.search().searchRealm) {
        $scope.query.searchRealm = $location.search().searchRealm;
    }
    $scope.userPosts = [];
    $scope.customerRoles = [];
    $scope.systemRoles = [];
    $scope.duplicatedPhone = false;

    $scope.ImportReports = [];

    $scope.init = function () {
        $http.get(authUrl + '/realms/' + $scope.query.searchRealm + '/users-toms/importUsersReports').then(function (data) {
            $scope.ImportReports = angular.fromJson(data).data.results['importUsersReports'];
        });
    };

    $scope.importFile = function (files) {
        var formData = new FormData();
        var file = files[0];
        formData.append('file', file);
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/uploadImportUsersFile`, formData, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined,
                'Content-Disposition': `form-data; name="file"; filename=${file.name}`
            }
        }).then(response => {
            if (response.status === 200) {
                Notifications.success("Upload import users file success! Please, refresh 'import users' page");
            }
        }).catch(error => {
            if (error.status === 400) {
                Notifications.error(error.data.message);
            } else {
                Notifications.error(error.statusText);
            }
        })
    };

    $scope.downloadImportUsersReport = function (importReport) {
        if (importReport.status !== "DONE") {
            Notifications.info("Import users report must have status 'DONE'");
            return;
        }
        var linkElement = document.createElement('a');
        $http.post(`${authUrl}/realms/${$scope.query.searchRealm}/users-toms/downloadImportUsersReport/${importReport.id}`, null,
            {
                headers: {
                    'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8',
                    'Content-Type': 'application/json'
                },
                responseType: 'arraybuffer'
            })
            .then((response) => {
                var headers = response.headers();
                var filename = headers['filename'];
                var contentType = headers['content-type'];
                var blob = new Blob([response.data], {type: contentType});
                var url = window.URL.createObjectURL(blob)

                linkElement.setAttribute('href', url);
                linkElement.setAttribute("download", filename);

                var clickEvent = new MouseEvent("click", {
                    "view": window,
                    "bubbles": true,
                    "cancelable": false
                });
                linkElement.dispatchEvent(clickEvent);
            });
    };

    $scope.activateImportUsersReport = function (importReport) {
        if (importReport.status !== "DONE") {
            Notifications.info("Import users report must have status 'DONE'");
            return;
        }
        var linkElement = document.createElement('a');
        $http.post(`${authUrl}/realms/${$scope.realm.realm}/users-toms/activateImportUsersReport/${importReport.id}`)
            .then(response => {
                if (response.status === 200) {
                    Notifications.success("Activate imported users from report success!");
                }
            });
    };

    $scope.init();
});
