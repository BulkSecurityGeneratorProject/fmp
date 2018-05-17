(function() {
    'use strict';

    angular
        .module('fmpApp', [
            'ngStorage',
            'ngResource',
            'ngCookies',
            'ngAria',
            'ngCacheBuster',
            'ngFileUpload',
            'ui.bootstrap',
            'ui.bootstrap.datetimepicker',
            'ui.router',
            'infinite-scroll',
            // jhipster-needle-angularjs-add-module JHipster will add new module here
            'angular-loading-bar',
            'ngFileSaver',
            'ui.select',
            'textAngular'
        ])
        .run(run);

    run.$inject = ['stateHandler'];

    function run(stateHandler) {
        stateHandler.initialize();
    }
})();
