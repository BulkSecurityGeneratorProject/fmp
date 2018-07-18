(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('WorkPackageFilterDialogController', WorkPackageFilterDialogController);

    WorkPackageFilterDialogController.$inject = ['$uibModal', '$scope', 'FileSaver', 'DataUtils', '$uibModalInstance', 'WorkPackage', '$state', 'value', 'field','maxDate','minDate','isDate'];

    function WorkPackageFilterDialogController($uibModal, $scope, FileSaver, DataUtils, $uibModalInstance, WorkPackage, $state, value, field, maxDate, minDate,isDate) {

        var vm = this;
        vm.clear = clear;        
        vm.sortType     = 'cityCode'; // set the default sort type
        vm.sortReverse  = false;  // set the default sort order
        vm.searchFish   = '';     // set the default search/filter term
        vm.currentTab = 'value';
        vm.selectedRow = null;
        vm.selectedCityGroupRow = null;
        vm.value = value;
        vm.field = field;
        vm.datePickerOpenStatus = {};
        vm.dateFormat = "dd/MM/yyyy";
        vm.openCalendar = openCalendar;
        vm.minDate = minDate;
        vm.maxDate = maxDate;
        vm.isDate = isDate;
                 
        vm.rowValueHighlighted = function (idSelected) {
            vm.selectedValueRow = idSelected;
        };
                        
        vm.select = function(){
        	if(vm.currentTab == 'range'){
        		$uibModalInstance.close({key:'range', value:{from : vm.From, to : vm.To}});
        	}
        	else if(vm.currentTab == 'value'){
        		$uibModalInstance.close({key:'distinctValue', value:vm.selectedValueRow});
        	}
        	else if(vm.currentTab == 'card'){
        		$uibModalInstance.close({key:vm.option, value:vm.search});
        	}
        }
        
        vm.selectTab = function(tab){
        	vm.currentTab = tab;
        }
        
        vm.removeAllColumnFilter = function(){
        	$uibModalInstance.close({key:'removeAllColumnFilter'});
        }
        
        vm.removeThisColumnFilter = function(){
        	$uibModalInstance.close({key:'removeThisColumnFilter'});
        }
        
        vm.removeLastColumnFilter = function(){
        	$uibModalInstance.close({key:'removeLastColumnFilter'});
        }
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        
        function openCalendar (e, date) {
        	e.preventDefault();
            e.stopPropagation();
            
            vm.datePickerOpenStatus = {};
            vm.datePickerOpenStatus[date] = true;
        }
        
        vm.advance = function(){
        	clear();
        	$uibModal.open({
                templateUrl: 'app/pages/work-packages/work-package-filter-advance-dialog.html',
                controller: 'WorkPackageFilterAdvanceDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                windowClass: 'full-page-modal',
                resolve: {
  	              	/*fare: function(){
  	              		return fare;
  	              	},
                    cities: ['City', function(City) {
                        return City.getAll().$promise;
                    }],
                    cityGroup: ['CityGroup', function(CityGroup) {
                        return CityGroup.getAll().$promise;
                    }],*/
                }
  			}).result.then(function(option) {
  				if(option != null){
  					if(option.type == 'city'){
  						fare[field] = option.cityCode;					
  					}
  					else if(option.type == 'cityGroup'){
  						fare[field] = option.code;	
  					}
  				}
            }, function() {
        			
            });
        }
        
    }
})();
