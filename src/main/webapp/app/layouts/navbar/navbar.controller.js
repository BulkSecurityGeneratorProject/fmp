(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$state', 'Auth', 'Principal', 'ProfileService', 'LoginService', '$window', 'GlobalService'];

    function NavbarController ($state, Auth, Principal, ProfileService, LoginService, $window, GlobalService) {
        var vm = this;

        vm.isNavbarCollapsed = true;
        vm.isAuthenticated = Principal.isAuthenticated;

        ProfileService.getProfileInfo().then(function(response) {
            vm.inProduction = response.inProduction;
            vm.swaggerEnabled = response.swaggerEnabled;
        });

        vm.login = login;
        vm.logout = logout;
        vm.toggleNavbar = toggleNavbar;
        vm.collapseNavbar = collapseNavbar;
        vm.$state = $state;
        vm.loginInfo = null;

     
        var copyAccount = function (account) {
            return {
                activated: account.activated,
                email: account.email,
                firstName: account.firstName,
                langKey: account.langKey,
                lastName: account.lastName,
                login: account.login
            };
        };
        
        Principal.identity().then(function(account) {
            vm.loginInfo = copyAccount(account);
        });
        
        
        vm.test = function(){
        	 
        }
        
        function login() {
            collapseNavbar();
            LoginService.open();
        }

        function logout() {
            collapseNavbar();
            Auth.logout();
            $state.go('home'); $window.location.reload();
        }

        function toggleNavbar() {
            vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
        }

        function collapseNavbar() {
            vm.isNavbarCollapsed = true;
        }
        
        
        GlobalService.navbar();
    }
})();
