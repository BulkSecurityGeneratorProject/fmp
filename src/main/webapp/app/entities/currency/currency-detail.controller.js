(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('CurrencyDetailController', CurrencyDetailController);

    CurrencyDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Currency'];

    function CurrencyDetailController($scope, $rootScope, $stateParams, previousState, entity, Currency) {
        var vm = this;

        vm.currency = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('fmpApp:currencyUpdate', function(event, result) {
            vm.currency = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
