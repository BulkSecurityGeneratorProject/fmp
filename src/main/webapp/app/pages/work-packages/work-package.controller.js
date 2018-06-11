(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('WorkPackageController', WorkPackageController);

    WorkPackageController.$inject = ['Principal', '$uibModal', '$state', '$stateParams', 'WorkPackage', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', 'FileSaver'];

    function WorkPackageController(Principal, $uibModal, $state, $stateParams, WorkPackage, ParseLinks, AlertService, paginationConstants, pagingParams, FileSaver) {
        var vm = this;
        vm.reviewLevel = true;
        vm.woStatus = true;
        vm.distributionType = true;
        vm.loadPage = loadPage;
        vm.workPackageActionButton = [];
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.users = {
        		businessAreas:[]
        };
        Principal.identity().then(function(account) {
//            vm.loginInfo = copyAccount(account);
        	vm.users = account;
        	console.log(vm.users);
        });
        
        if($stateParams.size != null || $stateParams.size != undefined){
        	vm.itemsPerPage = $stateParams.size;
        }
        else{
        	vm.itemsPerPage = "10";
        }
        vm.loadAll = loadAll;
        
        if($stateParams.workPackageFilter != null){
        	vm.workPackageFilter = $stateParams.workPackageFilter;
        }
        else{
	        vm.workPackageFilter = {
	    		reviewLevel:{
	    			ho:true,
	    			lso:true,
	    			distribution:true,
	    			routeManagement:true
	    		},
	    		distributionType:{
	    			atpco:true,
	    			market:true
	    		},
	    		status:{
	    			newStatus:true,
	    			pending:true,
	    			reviewing:true,
	    			readyToRelease:true,
	    			distributed:true,
	    			withdrawn:true,
	    			discontinue:true,
	    			referred:true,
	    			replace:false,
	    			reuse:false
	    		},
	    		type:{
	    			regular:true,
	    			discount:true,
	    			waiver:true
	    		},
	        };
        }
        vm.loadAll();

        function loadAll () {
        	console.log("LOAD ALL");
            WorkPackage.query({
            	"reviewLevel.ho": vm.workPackageFilter.reviewLevel.ho,
            	"reviewLevel.lso": vm.workPackageFilter.reviewLevel.lso,
            	"reviewLevel.distribution": vm.workPackageFilter.reviewLevel.distribution,
            	"reviewLevel.routeManagement": vm.workPackageFilter.reviewLevel.routeManagement,
            	
            	"status.newStatus": vm.workPackageFilter.status.newStatus,
            	"status.pending": vm.workPackageFilter.status.pending,
            	"status.reviewing": vm.workPackageFilter.status.reviewing,
            	"status.readyToRelease": vm.workPackageFilter.status.readyToRelease,
            	"status.distributed": vm.workPackageFilter.status.distributed,
            	"status.withdrawn": vm.workPackageFilter.status.withdrawn,
            	"status.replace": vm.workPackageFilter.status.replace,
            	"status.reuse": vm.workPackageFilter.status.reuse,
            	"status.referred": vm.workPackageFilter.status.referred,
            	"distributionType.atpco":vm.workPackageFilter.distributionType.atpco,
            	"distributionType.market":vm.workPackageFilter.distributionType.market,
            	
            	"type.regular":vm.workPackageFilter.type.regular,
            	"type.discount":vm.workPackageFilter.type.discount,
            	"type.waiver":vm.workPackageFilter.type.waiver,
            	
            	"approvalReference": vm.workPackageFilter.approvalReference,
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
            	    vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.workPackages = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {        	
            $state.transitionTo($state.$current, {
            	page: vm.page,
                size: vm.itemsPerPage,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                workPackageFilter : vm.workPackageFilter
            });
        }
        
        vm.rowSelected = function(idx, workPackage){
    		vm.selectedRow = workPackage;
    		vm.workPackageActionButton[idx] = !vm.workPackageActionButton[idx];
        }
        
        vm.reuse = function(index){
        	vm.workPackages[index].reuseReplaceConfig = {};
        	if(vm.workPackages[index].status == 'NEW'){
        		$uibModal.open({
                    templateUrl: 'app/pages/work-packages/work-package-reuse-replace-confirm-dialog.html',
                    controller: 'WorkPackageReuseReplaceConfirmDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'full-page-modal',
                    resolve: {
                    	workPackage: function(){
                    		return vm.workPackages[index];
                    	},
                    	 businessAreas: ['User', function(User) {
                             return User.getBusinessArea().$promise;
                         }],
                    }
    			}).result.then(function(workPackage) {
    				WorkPackage.reuse(workPackage, onReuseSuccess, onReuseFailed);
    	        	
    				function onReuseSuccess(result){
    	        		alert('Reuse Success');
    	        		$state.go('work-package-detail', {id:result.id});
    	        	}
    	        	
    	        	function onReuseFailed(error){
    	        		alert("An error occured, please try again");
    	        	}				
    			});
        	}
        	else{
        		WorkPackage.reuse(vm.workPackages[index], onReuseSuccess, onReuseFailed);
	        	
				function onReuseSuccess(result){
	        		alert('Reuse Success');
	        		$state.go('work-package-detail', {id:result.id});
	        	}
	        	
	        	function onReuseFailed(error){
	        		alert("An error occured, please try again");
	        	}
        	}
        }
        
        vm.replace = function(index){
        	vm.workPackages[index].reuseReplaceConfig = {};
        	if(vm.workPackages[index].status == 'NEW'){
        		$uibModal.open({
                    templateUrl: 'app/pages/work-packages/work-package-reuse-replace-confirm-dialog.html',
                    controller: 'WorkPackageReuseReplaceConfirmDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'full-page-modal',
                    resolve: {
                    	workPackage: function(){
                    		return vm.workPackages[index];
                    	}
                    }
    			}).result.then(function(option) {
    				vm.workPackages[index].reuseReplaceConfig.attachment = option.attachment;
    				
    				WorkPackage.replace(vm.workPackages[index], onReplaceSuccess, onReplceFailed);
    	        	
    	        	function onReplaceSuccess(result){
    	        		alert('Replace Success');
    	        		$state.go('work-package-detail', {id:result.id});
    	
    	        	}
    	        	
    	        	function onReplceFailed(error){
    	        		
    	        	}
    			});
        	}
        	else{
	        	WorkPackage.replace(vm.workPackages[index], onReplaceSuccess, onReplceFailed);
	        	
	        	function onReplaceSuccess(result){
	        		alert('Replace Success');
	        		$state.go('work-package-detail', {id:result.id});
	
	        	}
	        	
	        	function onReplceFailed(error){
	        		
	        	}
        	}
        }
        vm.withdraw = function(index){
        	WorkPackage.withdraw(vm.workPackages[index], onWithdrawSuccess, onWithdrawFailed);
        	
        	function onWithdrawSuccess(result){
        		alert('Withdraw Success '+result.id);
//        		$state.go('work-package-detail', {id:result.id});

        	}
        	
        	function onWithdrawFailed(error){
        		
        	}
        }
        
        vm.showHistory = function(index){
    		WorkPackage.history({id:vm.workPackages[index].id}, onSuccess, onError);
    			
			function onSuccess(history){
				$uibModal.open({
                    templateUrl: 'app/pages/work-packages/work-package-history-dialog.html',
                    controller: 'WorkPackageHistoryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: history.$promise
                    }
                }).result.then(function(workPackage) {
                	  
                }, function() {
            			
                });
			}
			
			function onError(){
				
			}
        	
        }

        vm.refresh = function(){
        	loadAll();
        	console.log(vm.workPackageFilter);
        }
       
        vm.unlock = function(wp){
        	 vm.workPackages[wp].locked = false;
	      	  WorkPackage.unlock(vm.workPackages[wp], onUnlockedSuccess, onUnlockedFailure);
	      	  function onUnlockedSuccess (result) {
	      		  alert('Work Package Successful Unlocked');
	      	  }
	      	  function onUnlockedFailure (error) {
	      		  
	      	  }
        };
        
        vm.changeItemsPerPage = function(){
        	vm.loadAll();
//        	loadPage(1);
        }
        
        function b64toBlob(b64Data, contentType, sliceSize) {
  		  contentType = contentType || '';
  		  sliceSize = sliceSize || 512;

  		  var byteCharacters = atob(b64Data);
  		  var byteArrays = [];

  		  for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
  		    var slice = byteCharacters.slice(offset, offset + sliceSize);

  		    var byteNumbers = new Array(slice.length);
  		    for (var i = 0; i < slice.length; i++) {
  		      byteNumbers[i] = slice.charCodeAt(i);
  		    }

  		    var byteArray = new Uint8Array(byteNumbers);

  		    byteArrays.push(byteArray);
  		  }

  		  var blob = new Blob(byteArrays, {type: contentType});

  		  return blob;
  	}
        
        vm.printExport = function(){
        	var exportConfig = {
        		workPackageFilter:vm.workPackageFilter,
        		outputTo:'excel',
        		gridLines:true,
        		columnHeaders:true,
        		onlySelectedRows:true
        	};
        	
        	console.log(exportConfig);
        	WorkPackage.exportQueue(exportConfig, function onExportSuccess(result){
//        		alert('Export Success');
        		 var fileType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
       			var templateFilename = "Workorder_Queue.xlsx";
       			var blob = b64toBlob(result.file, fileType);
       			FileSaver.saveAs(blob, templateFilename);
        	}, function onExportFailed(){
        		
        	});
        	
        	
//        	$uibModal.open({
//                templateUrl: 'app/pages/work-packages/work-package-reuse-replace-confirm-dialog.html',
//                controller: 'WorkPackageReuseReplaceConfirmDialogController',
//                controllerAs: 'vm',
//                backdrop: 'static',
//                size: 'lg',
//                windowClass: 'full-page-modal',
//                resolve: {
//                	workPackage: function(){
//                		return vm.workPackages[index];
//                	},
//                	 businessAreas: ['User', function(User) {
//                         return User.getBusinessArea().$promise;
//                     }],
//                }
//			}).result.then(function(workPackage) {
//				WorkPackage.reuse(workPackage, onReuseSuccess, onReuseFailed);
//	        	
//				function onReuseSuccess(result){
//	        		alert('Reuse Success');
//	        		$state.go('work-package-detail', {id:result.id});
//	        	}
//	        	
//	        	function onReuseFailed(error){
//	        		alert("An error occured, please try again");
//	        	}				
//			});
        }
    }
})();
