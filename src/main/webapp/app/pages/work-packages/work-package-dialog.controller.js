(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('WorkPackageDialogController', WorkPackageDialogController);

    WorkPackageDialogController.$inject = ['type', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'WorkPackage', 'reviewLevels', 'businessAreas'];

    function WorkPackageDialogController (type, $timeout, $scope, $stateParams, $uibModalInstance, entity, WorkPackage, reviewLevels, businessAreas) {
        var vm = this;

        vm.workPackage = entity;
        vm.workPackage.fareSheet = [{fareCarrier:'GA'}];
        vm.workPackage.addonFareSheet = [{fareCarrier:'GA'}];
        vm.workPackage.discountFareSheet = [{fareCarrier:'GA'}];
        vm.reviewLevels = reviewLevels;
        vm.businessAreas = businessAreas;
        
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;

        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        
        vm.datePickerOpenStatus.saleDate = false;
        
        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
        
        
        vm.fareType = ["Yearly", "Promotion", "Ad-hoc", "Corporate", "SPA & Code-share", "Miles"];
        
        
        vm.distributionTypes = ['ATPCO'];
        
        if($stateParams.type == null){
        		$stateParams.type = type;
        }
        
        if($stateParams.type == 'reguler'){
        		vm.workPackage.type = 'REGULAR';
        		vm.distributionTypes = ['ATPCO', 'Market'];
        }
        else if($stateParams.type == 'discount'){
        		vm.workPackage.type = 'DISCOUNT';
        		vm.distributionTypes = ['ATPCO'];
        }
        else if($stateParams.type == 'waiver'){
    		vm.workPackage.type = 'WAIVER';
    		//vm.distributionTypes = ['ATPCO'];
        }
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.workPackage.id !== null) {
                WorkPackage.update(vm.workPackage, onSaveSuccess, onSaveError);
            } else {
                WorkPackage.save(vm.workPackage, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('fmpApp:workPackageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.fillingDate = false;
        vm.datePickerOpenStatus.distributionDate = false;
        vm.datePickerOpenStatus.discExpiryDate = false;
        vm.datePickerOpenStatus.queuedDate = false;
        vm.datePickerOpenStatus.lockedSince = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
