(function() {
    'use strict';

    angular
        .module('fmpApp')
        .controller('FootnoteQueryController', FootnoteQueryController);

    FootnoteQueryController.$inject = ['$state', 'FootnoteQuery', 'ParseLinks', 'AlertService', 'paginationConstants', 'queryParams', '$uibModal'];

    function FootnoteQueryController($state, FootnoteQuery, ParseLinks, AlertService, paginationConstants, queryParams, $uibModal) {
    	
    	console.log("queryParams :: ",queryParams);

        var vm = this;
        vm.loadPage = loadPage;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.queryParams = queryParams;
        vm.loadAll = loadAll;
        vm.getRules = getRules;
        vm.getRules2 = getRules2;
        vm.showDetail = showDetail;
        vm.hideDetail = hideDetail;
        vm.reset = reset;
        vm.page = 1;
        vm.clearFilter = clearFilter;
        vm.showCategoryDetail = showCategoryDetail;
        
        vm.datePickerOpenStatus = {};
        vm.dateFormat = "yyyy-MM-dd";
        vm.openCalendar = openCalendar;
        
        vm.loadAll();

        function loadAll () {
        	vm.queryParams.page = vm.page - 1;
        	vm.queryParams.size = vm.itemsPerPage;
        	FootnoteQuery.query(vm.queryParams, onSuccess, onError);
        	
        	$("th").css({
    			"text-align" : "center"
    		});
        	
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.footnoteQueries = data;
            }
            
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
        }

        function getRules(footnoteQuery) {
        	FootnoteQuery.getRules(footnoteQuery, function(data) {
        		vm.footnoteQueryCategories = data;
        	}, function(error) {
        		console.log(error);
        	});
        }
        
        function getRules2(footnoteQuery) {
        	FootnoteQuery.getRules2(footnoteQuery, function(data) {
        		vm.footnoteQueryCategories2 = data;
        		console.log(data);
        	}, function(error) {
        		console.log(error);
        	});
        }
        
        function showDetail() {
        	$("#tblDetail").show();
        	$("#tblDetail").focus();
        	$("#tblDetail").css("display","block");
        	$("#tblDetail").css("height","440px");
        	$("#tblDetail").css("overflow-y","scroll");
        	$("#ruleCategories").show();
        	
        }
        
        function hideDetail() {
        	$("#tblDetail").css("display","none");
        	$("#tblDetail").hide();
        	$("#ruleCategories").hide();
        }
        
        function clearFilter() {
        	$("#cxr").val("");
        	$("#ruleNo").val("");
        	$("#type").val("");
        	$("#src").val("");
        	$("#ruleTarNo").val("");
        	$("#catNo").val("");
        }
        
        function openCalendar (e, date) {
        	e.preventDefault();
            e.stopPropagation();
            
            vm.datePickerOpenStatus[date] = true;
        }
        
        function reset() {
        	vm.queryParams = {
        			cxr: null,
            		ruleTarNo: null,
            		ruleNo: null,
            		type: null,
            		src: null,
            		cat: null,
            		catNo: null
        	}
        	
        	vm.loadAll();
        }
        
        function showCategoryDetail(category) {
        	$uibModal.open({
                templateUrl: 'app/pages/category-modals/category-modal.html',
                controller: 'CategoryModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                windowClass: 'full',
                resolve: {
                    entity: category
                }
            }).result.then(function() {
                $state.go('rule-query', {}, { reload: false });
            }, function() {
                $state.go('rule-query');
            });
        }
    }
})();
