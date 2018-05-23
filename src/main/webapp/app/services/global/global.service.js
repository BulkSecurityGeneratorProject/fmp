(function() {
    'use strict';

    angular
        .module('fmpApp')
        .factory('GlobalService', GlobalService);

    GlobalService.$inject = ['$http'];

    function GlobalService($http) {
    	 return {
    		navbar: function(){
    			console.log("TRIGGER SIDEBAR");
	    		function sidebarOpen(){    			
	    			$('.showLeftPush').toggleClass('active');
	    			$('#sideBar-s1').toggleClass('sideBar-open');
	    			$('.search-wrap').find('i').toggleClass('shrink');
	    			$('.sideBar').find('.menu-list').toggleClass('open');
	    			$('.menu-list').removeClass('sub');
	    			$('.sub-menu').slideUp(200);
	    			$('.page-content').toggleClass('pushed');
	    			$('.sidebar-toggle').toggleClass('pushed');
	    			$('.fa-home').toggleClass('hidden')
	    		}
	    		function sidebarClose(){ 
	    			$('.showLeftPush').removeClass('active');
				 	$('.sidebar-toggle').removeClass('pushed');
				 	$('#sideBar-s1').removeClass('sideBar-open');
				 	$('.search-wrap').find('i').removeClass('shrink');
				 	$('.sideBar').find('.menu-list').removeClass('open');
				 	$('.page-content').removeClass('pushed');
				 	$('.menu-list').removeClass('sub');
	    			$('.sub-menu').slideUp(200);
	    			$('.fa-home').removeClass('hidden');
	    		}
    			$(document).ready(function(){
    				$('.showLeftPush').on('click', function(){	     				
        				sidebarOpen();
        			});
    				$('.menu-list').on('click',function(e){	
    					e.preventDefault();
    					if ($(this).hasClass('open')){
    						$('.menu-list').not(this).removeClass('sub');
    						$('.menu-list').not(this).find('.sub-menu').slideUp(200);
    						$(this).toggleClass('sub');
    						$(this).find('.sub-menu').slideToggle(200);
    					}
    					else{
    						sidebarOpen();
    						if ($(this).hasClass('has-sub')){
    							$(this).toggleClass('sub');
    							$(this).find('.sub-menu').slideToggle(200);
    						}
    					}		
    				});
    				$('.search-wrap i').on('click',function(){	
    					if (!$('.menu-list').hasClass('open')){
    						sidebarOpen()
    						$('.search-wrap input').focus();
    					}		
    				});
					 $('.linkClick').on('click',function(e){
						e.stopPropagation();
						sidebarClose()
					 });	
    			});
	    		
    		},
		    sayHello: function () {
//		    	filter
		    	$('.filter-label').on('click',function(){
		    		$(this).toggleClass('show');
		    		$(this).siblings('.filter-area').slideToggle(200);
		    		if($(this).hasClass('show')){
		    			$(this).html('Hide Filter')
		    		}
		    		else{
		    			$(this).html('Show Filter')
		    		}
		    	});	
		    	
//		    	box-header minimize
		    	$(".box-header").on('click',function(){
		    		$(this).toggleClass('open');
		    		$(this).siblings('.box-body').slideToggle(200);
		    	});
		    	$(".box-header.box-tab").on('click',function(){
		    		$(this).find('.tab-content').slideToggle(200);
		    	});
		    	
//		    	dropdown
		    	$(document).mouseup(function (e){	
    			    if($('.dropdown-menu').has(e.target).length === 0)
    			    {
    			        //hide your element
    			        $('.dropdown').removeClass('open');
    			        $('.dropdown-menu').hide();
    			    }
    			});

	    		$('.dropdown').on('click',function(){
	    			$(this).toggleClass('open');
	    			if($(this).hasClass('open')){
	    				$(this).find('.dropdown-menu').slideDown(100);
	    			}
	    			else{
	    				$(this).find('.dropdown-menu').slideUp(100);
	    			}		
	    		});
	    		
	    		
//	    		tabs
		    	$(".nav-tabs li").on('click',function(e){
		    		e.preventDefault();
		    		var _href = $(this).find('a').attr('href');
		    		var baseurl = window.location.origin+window.location.pathname;
		    
		    		$(".nav-tabs li").removeClass('active');
		    		$(this).addClass('active');
		    		$('.tab-content .tab-pane').removeClass('active');
		    		$(_href).addClass('active');
		    	});
		    	$(".tab-header").on('click',function(){
		    		$(this).toggleClass('open');
		    		$(this).siblings('.tab-body').slideToggle(200);
		    	});
		    
		    	// input
		    	$('input.must-fill').on('input',function(){
		    	  	$(this).removeClass('must-fill');
		    	  	if($(this).val() == ''){
		    	  		$(this).addClass('must-fill');
		    	  	}
		    	});
		    
		    	// select
		    	$('select.must-fill').on('change',function(){
		    		$(this).removeClass('must-fill');
		    	  	if($(this).find('option')[0].selected === true){
		    	  		$(this).addClass('must-fill');
		    	  	}
		    	});
		    },
		    fungsi2: function(a){
		    	alert('CALL GLOBAL FUNCTION 2 '+a);
		    }
		 };
    }
})();
