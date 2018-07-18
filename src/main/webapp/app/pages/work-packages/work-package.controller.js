(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('WorkPackageController', WorkPackageController);

    WorkPackageController.$inject = ['Principal', '$uibModal', '$state', '$stateParams', 'WorkPackage', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', 'FileSaver', 'DateUtils', 'user'];

    function WorkPackageController(Principal, $uibModal, $state, $stateParams, WorkPackage, ParseLinks, AlertService, paginationConstants, pagingParams, FileSaver, DateUtils, user) {
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
        vm.reviewLevelHO = null;
        vm.reviewLevelLSO = null;
        vm.reviewLevelDISTRIBUTION = null;
        vm.reviewLevelROUTEMANAGEMENT = null;
        vm.workPackageFilter =null;
        vm.login =user;

        if($stateParams.size != null || $stateParams.size != undefined){
        	vm.itemsPerPage = $stateParams.size;
        }
        else{
        	vm.itemsPerPage = "10";
        }
        vm.loadAll = loadAll;
   
        Principal.identity().then(function(account) {
        	   vm.users = account;
               if($stateParams.workPackageFilter != null){
            	   for(var i=0; i<=vm.users.reviewLevels.length; i++){
  		             //console.log(vm.users.reviewLevels[i]);
  		             if(vm.users.reviewLevels[i] == "HO"){
  		                 vm.reviewLevelHO = vm.users.reviewLevels[i];
  		             } else if(vm.users.reviewLevels[i] == "LSO"){
  		                 vm.reviewLevelLSO = vm.users.reviewLevels[i];
  		             } else if(vm.users.reviewLevels[i] == "DISTRIBUTION"){
  		                 vm.reviewLevelDISTRIBUTION = vm.users.reviewLevels[i];
  		             } else if(vm.users.reviewLevels[i] == "ROUTE_MANAGEMENT") {
  		                 vm.reviewLevelROUTEMANAGEMENT = vm.users.reviewLevels[i];
  		             }
  		           }
            	   vm.workPackageFilter = $stateParams.workPackageFilter;
            	   vm.loadAll();
               }
               else {
            	   WorkPackage.workPackagefilter({}, function onSuccess (response) {
            		   vm.workPackageFilter = response;
            		   for(var i=0; i<=vm.users.reviewLevels.length; i++){
      		             //console.log(vm.users.reviewLevels[i]);
      		             if(vm.users.reviewLevels[i] == "HO"){
      		                 //vm.workPackageFilter.reviewLevel.ho = true;
      		                 vm.reviewLevelHO = vm.users.reviewLevels[i];
      		             } else if(vm.users.reviewLevels[i] == "LSO"){
      		                 //vm.workPackageFilter.reviewLevel.lso = true;
      		                 vm.reviewLevelLSO = vm.users.reviewLevels[i];
      		             } else if(vm.users.reviewLevels[i] == "DISTRIBUTION"){
      		                 //vm.workPackageFilter.reviewLevel.distribution = true;
      		                 vm.reviewLevelDISTRIBUTION = vm.users.reviewLevels[i];
      		             } else if(vm.users.reviewLevels[i] == "ROUTE_MANAGEMENT") {
      		                 //vm.workPackageFilter.reviewLevel.routeManagement = true;
      		                 vm.reviewLevelROUTEMANAGEMENT = vm.users.reviewLevels[i];
      		             }
      		          }
            		   vm.loadAll();
            	   }, function onError(error){});	   	   	        
               }
               
        });
        
       
        function loadAll () {
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
            	"status.discontinued": vm.workPackageFilter.status.discontinued,
            	"status.replace": vm.workPackageFilter.status.replace,
            	"status.reuse": vm.workPackageFilter.status.reuse,
            	"status.referred": vm.workPackageFilter.status.referred,
            	"distributionType.atpco":vm.workPackageFilter.distributionType.atpco,
            	"distributionType.market":vm.workPackageFilter.distributionType.market,

            	"type.regular":vm.workPackageFilter.type.regular,
            	"type.discount":vm.workPackageFilter.type.discount,
            	"type.waiver":vm.workPackageFilter.type.waiver,

            	"approvalReference": vm.workPackageFilter.approvalReference,
            	"createdTime": vm.workPackageFilter.createdTime,
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
                vm.timezone = headers('timezone');
                
                $(".custom-range-slider").trigger("change");

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
            
            console.log("CREATED TIME : "+vm.workPackageFilter.createdTime);
        }

        vm.rowSelected = function(idx, workPackage){
    		vm.selectedRow = workPackage;
    		vm.workPackageActionButton[idx] = !vm.workPackageActionButton[idx];
        }

        vm.reuse = function(){
        	vm.selectedRow.reuseReplaceConfig = {};
        		$uibModal.open({
                    templateUrl: 'app/pages/work-packages/work-package-reuse-replace-confirm-dialog.html',
                    controller: 'WorkPackageReuseReplaceConfirmDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'full-page-modal',
                    resolve: {
                    	workPackage: function(){
                    		return vm.selectedRow;
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

        vm.replace = function(){
        	vm.selectedRow.reuseReplaceConfig = {};
        		$uibModal.open({
                    templateUrl: 'app/pages/work-packages/work-package-reuse-replace-confirm-dialog.html',
                    controller: 'WorkPackageReuseReplaceConfirmDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'full-page-modal',
                    resolve: {
                    	workPackage: function(){
                    		return vm.selectedRow;
                    	},
	                   	 businessAreas: ['User', function(User) {
	                         return User.getBusinessArea().$promise;
	                     }],
                    }
    			}).result.then(function(option) {
    				vm.selectedRow.reuseReplaceConfig.attachment = option.attachment;

    				WorkPackage.replace(vm.selectedRow, onReplaceSuccess, onReplceFailed);

    	        	function onReplaceSuccess(result){
    	        		alert('Replace Success');
    	        		$state.go('work-package-detail', {id:result.id});

    	        	}

    	        	function onReplceFailed(error){

    	        	}
    			});        	
        }
        vm.withdraw = function(){
        	WorkPackage.withdraw(vm.selectedRow, onWithdrawSuccess, onWithdrawFailed);

        	function onWithdrawSuccess(result){
        		alert('Withdraw Success '+result.id);
//        		$state.go('work-package-detail', {id:result.id});

        	}

        	function onWithdrawFailed(error){

        	}
        }

        vm.showHistory = function(){
    		WorkPackage.history({id:vm.selectedRow.id}, onSuccess, onError);

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
        	vm.loadAll();
        }

        vm.unlock = function(){
        	WorkPackage.unlock(vm.selectedRow, onUnlockedSuccess, onUnlockedFailure);
	      	  function onUnlockedSuccess (result) {
	      		vm.selectedRow.locked = false;
	      		alert('Work Package Successful Unlocked');
	      	  }
	      	  function onUnlockedFailure (error) {
	      		  alert(error.data.detail);
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

        vm.discontinue = function(){
        	if(vm.selectedRow.targetDistribution == 'MARKET' && vm.selectedRow.status == 'DISTRIBUTED'){
        		WorkPackage.discontinue(vm.selectedRow, function onSuccess(){
            		alert("Work Package successfully discontinued");
            	}, function onError(){});
        	}        	
        }
        
        vm.checkDisabled = function(bakso){
        	if(bakso.targetDistribution == 'MARKET' && bakso.status == 'DISTRIBUTED'){
        		return false;
        	}else {
        		return true;
        	}
        }
        
        function max_date(all_dates) {
        	var max_dt = all_dates[0],
        	  max_dtObj = new Date(all_dates[0]);
        	all_dates.forEach(function(dt, index)
        	  {
        	  if ( new Date( dt ) > max_dtObj)
        	  {
        	  max_dt = dt;
        	  max_dtObj = new Date(dt);
        	  }
        	  });
        	 return max_dt;
        	  }
        
        function min_date(all_dates) {
        	var max_dt = all_dates[0],
        	  max_dtObj = new Date(all_dates[0]);
        	all_dates.forEach(function(dt, index)
        	  {
        	  if ( new Date( dt ) < max_dtObj)
        	  {
        	  max_dt = dt;
        	  max_dtObj = new Date(dt);
        	  }
        	  });
        	 return max_dt;
        	  }
        
        vm.rightClick = function(field){
        	var value=[];
        	if(field.match(/date/gi) != null ){
            	for(var l=0; l< vm.workPackages.length; l++){
            		if(!vm.workPackages[l].hide){
    	        		if(value.indexOf(vm.workPackages[l][field]) < 0){
    	        			value.push(vm.workPackages[l][field]);
    	        		}
            		}
            	}
            	var maxDate = max_date(value);
            	var minDate = min_date(value);
            	var isDate = true;
            	vm.filterDialog(value, field, DateUtils.convertLocalDateToServer(maxDate), DateUtils.convertLocalDateToServer(minDate),isDate);
        	}else{
            	for(var l=0; l< vm.workPackages.length; l++){
            		if(!vm.workPackages[l].hide){
    	        		if(value.indexOf(vm.workPackages[l][field]) < 0){
    	        			value.push(vm.workPackages[l][field]);
    	        		}
            		}
            	}
            	var maxDate = null;
            	var minDate = null;
            	var isDate = false;
            	vm.filterDialog(value,field,maxDate,minDate,isDate);
        	}
        }
        
        vm.filterList = [];
        vm.filterDialog = function(value,field,maxDate,minDate,isDate){
      	  $uibModal.open({
                templateUrl: 'app/pages/work-packages/work-package-filter-dialog.html',
                controller: 'WorkPackageFilterDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                windowClass: 'full-page-modal',
                resolve: {
                    value : function(){
  	              		return value;
  	              	},
                    field : function(){
  	              		return field;
  	              	},
  	              	maxDate : function(){
  	              		return maxDate;
  	              	},
  	              	minDate : function(){
	              		return minDate;
	              	},
	              	isDate : function(){
	              		return isDate;
	              	},
                }
  			}).result.then(function(result) {
  				if(result.key == 'distinctValue'){
	  				vm.filterList.push({key:field, value:result.value});
	  				for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(vm.workPackages[l][vm.filterList[m].key] == vm.filterList[m].value){
	  							countTrue.push(true);
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}
  				else if(result.key == 'removeAllColumnFilter'){
  					vm.filterList = [];
  					vm.loadAll();
  				}
  				else if(result.key == 'removeThisColumnFilter'){
  					for(var x=0;x<vm.filterList.length;x++){
  						if(vm.filterList[x].key == field){
  							var index = vm.filterList.indexOf(vm.filterList[x]);
  							vm.filterList.splice(index, 1);
  						}
  					}
  					for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(vm.workPackages[l][vm.filterList[m].key] == vm.filterList[m].value){
	  							countTrue.push(true);
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}
  				else if(result.key == 'removeLastColumnFilter'){
					vm.filterList.splice(vm.filterList.length-1, 1);
  					for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(vm.workPackages[l][vm.filterList[m].key] == vm.filterList[m].value){
	  							countTrue.push(true);
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}else if(result.key == '*'){
  					vm.filterList.push({key:field, value:result.value});
	  				for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(vm.workPackages[l][vm.filterList[m].key].indexOf(vm.filterList[m].value) > -1){
	  							countTrue.push(true);
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}else if(result.key == '?'){
  					vm.filterList.push({key:field, value:result.value});
	  				for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(vm.workPackages[l][vm.filterList[m].key] == vm.filterList[m].value){
	  							countTrue.push(true);
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}else if(result.key == 'range'){
  					console.log(result.value);
  					vm.filterList.push({key:field, value:result.value});
	  				for(var l=0; l< vm.workPackages.length; l++){
	  					var countTrue = [];
	  					for(var m=0;m<vm.filterList.length;m++){
	  						if(result.value.from != null && result.value.to != null ){
	  							var to = new Date(result.value.to);
	  							to.setHours(23,59,59)
	  							if(new Date( vm.workPackages[l][vm.filterList[m].key] ) >= result.value.from && new Date( vm.workPackages[l][vm.filterList[m].key] ) <= to){
	  								countTrue.push(true);
	  							}
	  						}else if (result.value.from != null){
	  							if(new Date( vm.workPackages[l][vm.filterList[m].key] ) >= result.value.from){
	  								countTrue.push(true);
	  							}
	  						}else if(result.value.to != null){
	  							var to = new Date(result.value.to);
	  							to.setHours(23,59,59)
	  							if(new Date( vm.workPackages[l][vm.filterList[m].key] ) <= to){
	  								countTrue.push(true);
	  							}
	  						}
	  					}  					
	  					
	  					if(countTrue.length == vm.filterList.length){
	  						vm.workPackages[l].hide = false;
	  					}
	  					else{
	  						vm.workPackages[l].hide = true;
	  					}
	  	        	}
  				}else if(result.key == 'advance'){
  					vm.filterDialogAdvance(result.value, field);
  				}
            }, function() {
        			
            });
        }
        
        vm.filterDialogAdvance = function(result,field){
        	$uibModal.open({
                templateUrl: 'app/pages/work-packages/work-package-filter-advance-dialog.html',
                controller: 'WorkPackageFilterAdvanceDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                windowClass: 'full-page-modal',
                resolve: {
                	value : function(){
   	              		return result;
   	              	},
   	              	field : function(){
	              		return field;
	              	},
                }
  			}).result.then(function(result) {
  				vm.rightClick(result.value);
            }, function() {
        			
            });
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
