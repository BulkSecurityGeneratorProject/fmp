(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('PriorityDialogController', PriorityDialogController);

    PriorityDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Priority'];

    function PriorityDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Priority) {
        var vm = this;

        vm.priority = entity;
        vm.clear = clear;
        vm.save = save;
        vm.priority.type = "DAYS";
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.priority.id !== null) {
                Priority.update(vm.priority, onSaveSuccess, onSaveError);
            } else {
                Priority.save(vm.priority, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('fmpApp:priorityUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
