(function() {
    'use strict';
    angular
        .module('fmpApp')
        .factory('WorkPackage', WorkPackage);

    WorkPackage.$inject = ['$resource', 'DateUtils'];

    function WorkPackage ($resource, DateUtils) {
        var resourceUrl =  'api/work-packages/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'findDiscountDerivedFares': { method: 'POST', isArray: true, url:'api/work-packages/derived/findFares'},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.filingDate = DateUtils.convertDateTimeFromServer(data.filingDate);
                        data.createdDate = DateUtils.convertDateTimeFromServer(data.createdDate);
                        data.distributionDate = DateUtils.convertDateTimeFromServer(data.distributionDate);
                        data.discExpiryDate = DateUtils.convertDateTimeFromServer(data.discExpiryDate);
                        data.queuedDate = DateUtils.convertDateTimeFromServer(data.queuedDate);
                        data.lockedSince = DateUtils.convertDateTimeFromServer(data.lockedSince);
                        data.saleDate = DateUtils.convertDateTimeFromServer(data.saleDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'createFromRecommendation': {
            	    method:'POST', url:'api/work-packages/create/fromRecommendation'
            },
            'passup': {
            		method:'POST', url:'api/work-packages/passup'
            },
            'passdown': {
        			method:'POST', url:'api/work-packages/passdown'
            },
            'passsideway': {
	    			method:'POST', url:'api/work-packages/passsideway'
	        },
	        'approve': {
	    			method:'POST', url:'api/work-packages/approve'
	        },
	        'referback': {
	    			method:'POST', url:'api/work-packages/referback'
	        },
	        'createbatch': {
	    			method:'POST', url:'api/work-packages/createbatch'
	        },
	        'revisebatch': {
    				method:'POST', url:'api/work-packages/revisebatch'
	        },
	        'completebatch': {
				method:'POST', url:'api/work-packages/completebatch'
	        },
	        'reuse': {
				method:'POST', url:'api/work-packages/reuse'
	        },
	        'replace': {
				method:'POST', url:'api/work-packages/replace'
	        },
	        'history': { method: 'GET', isArray: true, url:'api/work-packages/history/:id'},	        
	        'changeVersion': { method: 'POST', url:'api/work-packages/changeVersion'},	        
	        'importFares': { method: 'POST',  url:'api/work-packages/import-fares'},
	        'importFaresMarket': { method: 'POST',  url:'api/work-packages/import-fares-market'},
	        'importFaresDiscount': { method: 'POST',  url:'api/work-packages/import-fares-discount'},
	        'exportFares': { method: 'POST',  url:'api/work-packages/export-fares'},
	        'exportFaresMarket': { method: 'POST',  url:'api/work-packages/export-fares-market'},
	        'exportFaresDiscount': { method: 'POST',  url:'api/work-packages/export-fares-discount'},
	        'exportDerivedFares': { method: 'POST',  url:'api/work-packages/derived/exportDerivedFares'},
	        'publish' : { method: 'POST',  url:'api/work-packages/market/publish'},
	        'exportRateSheet': { method: 'POST',  url:'api/work-packages/export-ratesheet'}
        });
    }
})();