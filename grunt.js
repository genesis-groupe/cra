/*
 * grunt
 * http://gruntjs.com/
 *
 * Copyright (c) 2012 "Cowboy" Ben Alman
 * Licensed under the MIT license.
 * https://github.com/gruntjs/grunt/blob/master/LICENSE-MIT
 */

module.exports = function(grunt) {

	// Project configuration.
	grunt.initConfig({
		clean: {
			all: [
				'buildjs/',
				'public/javascripts/genesis.min.js',
				'public/javascripts/backoffice.min.js'
			]
		},
		lint: {
			main: [
				'public/javascripts/genesis.min.js'
			],
			backoffice: [
				'public/javascripts/backoffice.min.js'
			],
			dev:[
				'public/javascripts/app/config.js',
				'public/javascripts/app/app.js',
				'public/javascripts/app/pay.js',
				'public/javascripts/app/day.js',
				'public/javascripts/app/standby.js',
				'public/javascripts/app/calendar.js',
				'public/javascripts/app/holiday.js',
				'public/javascripts/app/fees.js',
				'public/javascripts/app/feesCra.js',
				'public/javascripts/app/account/globalSettings.js',
				'public/javascripts/app/account/partTimeSetting.js',
				'public/javascripts/app/account/changePasswordSetting.js',
				'public/javascripts/app/account/feesSetting.js',
				'public/javascripts/app/launch.js'
			]
		},
		jshint: {
			options: {
				curly: true,
				eqeqeq: true,
				immed: true,
				latedef: true,
				newcap: true,
				noarg: true,
				sub: true,
				undef: true,
				boss: true,
				eqnull: true,
				node: true,
				es5: true,
				strict: false
			},
			globals: {}
		},
		concat: {
			genesis: {
				src: [
					'public/javascripts/app/config.js',
					'public/javascripts/app/app.js',
					'public/javascripts/app/pay.js',
					'public/javascripts/app/day.js',
					'public/javascripts/app/standby.js',
					'public/javascripts/app/calendar.js',
					'public/javascripts/app/holiday.js',
					'public/javascripts/app/fees.js',
					'public/javascripts/app/feesCra.js',
					'public/javascripts/app/account/globalSettings.js',
					'public/javascripts/app/account/partTimeSetting.js',
					'public/javascripts/app/account/changePasswordSetting.js',
					'public/javascripts/app/account/feesSetting.js',
					'public/javascripts/app/launch.js'
				],
				dest: 'buildjs/genesis.js'
			},
			backoffice: {
				src: [
					'public/javascripts/app/backoffice/app.js',
					'public/javascripts/app/backoffice/controllers/EmployeesController.js',
					'public/javascripts/app/backoffice/controllers/EmployeeController.js',
					'public/javascripts/app/backoffice/controllers/CustomersController.js',
					'public/javascripts/app/backoffice/controllers/CustomerController.js',
					'public/javascripts/app/backoffice/controllers/MissionController.js',
					'public/javascripts/app/backoffice/filters/capitalize.js',
					'public/javascripts/app/backoffice/directives/bDatepicker.js',
					'public/javascripts/app/backoffice/services/EmployeesResource.js',
					'public/javascripts/app/backoffice/services/CustomersResource.js',
					'public/javascripts/app/backoffice/services/MissionsResource.js',
					'public/javascripts/app/backoffice/services/RolesResource.js'
				],
				dest: 'buildjs/backoffice.js'
			}
		},
		min: {
			genesis: {
				src: ['buildjs/genesis.js'],
				dest: 'public/javascripts/genesis.min.js',
				separator: ';'
			},
			backoffice: {
                src: ['buildjs/backoffice.js'],
                dest: 'public/javascripts/backoffice.min.js',
                separator: ';'
            }
		},
		uglify: {
			mangle: {toplevel: true},
			squeeze: {dead_code: true},
			codegen: {
				beautify: false,
				quote_keys: true
			}
		}
	});

	// Default task.
	grunt.registerTask('default', 'lint:dev');
	grunt.registerTask('dist', 'concat:genesis min:genesis concat:backoffice min:backoffice');

};