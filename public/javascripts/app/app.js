/* app.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** Global options for Ajax call */
	$.ajaxSetup({
		cache: false,
		dataType:"json",
		contentType: "application/json"
	});


	$("body")
		.ajaxStart(function() {
			$('.ajax-waitter').show();
		})
		.ajaxStop(function() {
			$('.ajax-waitter').fadeOut('fast');
			$('.waitter').fadeOut('fast');
		})
		.ajaxError(function(event, jqXHR, settings, errorThrown) {
			$('.ajax-waitter').fadeOut('fast');
			$('.waitter').fadeOut('fast');
			App.error({
				jqXHR: jqXHR,
				settings: settings,
				errorThrown: errorThrown
			});
		});
	/** App */
	/* Définition de la classe */
	var App = window.App = function(){
		$('.waitter').show();
		/* private instance of calendar controller */
		var _calendarCtrl = null;
		/* private instance of pay controller */
		var _payCtrl = null;
		/* private instance of holiday controller */
		var _holidayCtrl = null;
		/* private instance of partTimeSetting controller */
		var _partTimeSettingCtrl = null;
		/* private instance of changePassword controller */
		var _changePasswordCtrl = null;
		/* private instance of FeesSetting controller */
		var _feesSettingCtrl = null;
		/* private instance of Fees controller */
		var _feesCtrl = null;
		/* private instance of globalSettings controller */
		var _globalSettingsCtrl = null;
		/* private instance of admin controller */
		var _adminCtrl = null;
		/**
		* Create calendar instance of app.
		* 
		* @returns {APP.CalendarController}
		*/
		this.createCalendar = function(criterias){
			if(!_calendarCtrl){
				_calendarCtrl = new App.CalendarController(criterias.readOnly);
			}
			return this;
		};
		/**
		* Create pay instance of app.
		* 
		* @returns {APP.PayController}
		*/
		this.createPay = function(criterias){
			if(!_payCtrl){
				_payCtrl = new App.PayController();
			}
			return this;
		};
		/**
		* Create holiday instance of app.
		* 
		* @returns {APP.HolidayController}
		*/
		this.createHoliday = function(criterias){
			if(!_holidayCtrl){
				_holidayCtrl = new App.HolidayController();
			}
			return this;
		};
		/**
		* Create PartTimeSetting instance of app.
		* 
		* @returns {APP.PartTimeSettingController}
		*/
		this.createPartTimeSetting = function(criterias){
			if(!_partTimeSettingCtrl){
				_partTimeSettingCtrl = new App.PartTimeSettingController();
			}
			return this;
		};
		/**
		* Create ChangePassword instance of app.
		* 
		* @returns {APP.ChangePasswordController}
		*/
		this.createChangePassword = function(criterias){
			if(!_changePasswordCtrl){
				_changePasswordCtrl = new App.ChangePasswordController();
			}
			return this;
		};
		/**
		* Create FeesSetting instance of app.
		* 
		* @returns {APP.FeesSettingController}
		*/
		this.createFeesSetting = function(criterias){
			if(!_feesSettingCtrl){
				_feesSettingCtrl = new App.FeesSettingController();
			}
			return this;
		};
		/**
		* Create Fees instance of app.
		* 
		* @returns {APP.FeesController}
		*/
		this.createFees = function(criterias){
			if(!_feesCtrl){
				_feesCtrl = new App.FeesController();
			}
			return this;
		};
		/**
		* Create GlobalSettings instance of app.
		* 
		* @returns {APP.GlobalSettingsController}
		*/
		this.createGlobalSettings = function(criterias){
			if(!_globalSettingsCtrl){
				_globalSettingsCtrl = new App.GlobalSettingsController();
			}
			return this;
		};
		/**
		* Create GlobalSettings instance of app.
		* 
		* @returns {APP.GlobalSettingsController}
		*/
		this.createAdmin = function(criterias){
			if(!_adminCtrl){
				_adminCtrl = new App.AdminController(criterias);
			}
			return this;
		};
		this.bindEvents();
	};
	_.extend(App.prototype,{
		bindEvents: function(){
			$(window).on("ShowCreate:openCalendar ShowSearch:openCalendar ShowPay:openCalendar", $.proxy(this.onOpenCalendar, this));
			$(window).on("ShowPay:renderPay", $.proxy(this.onOpenPay, this));
			$(window).on("ShowCreate:renderHoliday", $.proxy(this.onOpenHoliday, this));
			$(window).on("ShowCreate:renderFees", $.proxy(this.onOpenFees, this));
			$(window).on("ShowAccount:divPartTimeSetting", $.proxy(this.onOpenPartTime, this));
			$(window).on("ShowAccount:divChangePassword", $.proxy(this.onOpenChangePassword, this));
			$(window).on("ShowAccount:divFeesSetting", $.proxy(this.onOpenFeesSetting, this));
			$(window).on("ShowAccount:divGlobalSettings", $.proxy(this.onOpenGlobalSettings, this));
			$(window).on("ShowAdmin:renderAdmin", $.proxy(this.onOpenAdmin, this));
		},
		onOpenCalendar: function(event, criterias){
			this.createCalendar(criterias);
			$(window).trigger("App:renderCalendar", criterias);
		},
		onOpenPay: function(event, criterias){
			this.createPay(criterias);
			$(window).trigger("App:renderPay", criterias);
		},
		onOpenHoliday: function(event, criterias){
			this.createHoliday(criterias);
			$(window).trigger("App:renderHoliday", criterias);
		},
		onOpenPartTime: function(event, criterias){
			this.createPartTimeSetting(criterias);
			$(window).trigger("App:renderPartTimeSetting", criterias);	
		},
		onOpenChangePassword: function(event, criterias){
			this.createChangePassword(criterias);
			$(window).trigger("App:renderChangePassword", criterias);	
		},
		onOpenFeesSetting: function(event, criterias){
			this.createFeesSetting(criterias);
			$(window).trigger("App:renderFeesSetting", criterias);	
		},
		onOpenFees: function(event, criterias){
			this.createFees(criterias);
			$(window).trigger("App:renderFees", criterias);	
		},
		onOpenGlobalSettings: function(event, criterias){
			this.createGlobalSettings(criterias);
			$(window).trigger("App:renderGlobalSettings", criterias);	
		},
		onOpenAdmin: function(event, criterias){
			this.createAdmin(criterias);
			$(window).trigger("App:renderAdmin", criterias);	
		}
	});
	App.showModal = function(options){
		var defaultOptions = {
			type: "danger",
			title : "Erreur",
			message: "Oupss ... il s'est passé quelque chose d'anormal on dirait ;) !",
			timeout: null,
			positionClass: "toast-top-right" 
		};
		options = $.extend(defaultOptions, options);

		var msg = "<div class=\"alert alert-" + options.type + "\" >"  + options.message  + "</div>";

		if(options.timeout){
			toastr.options.timeout = options.timeout;
			toastr.options.positionClass = options.positionClass;
			toastr[options.type](options.message, options.title);
		} else {
			bootbox.dialog(
				msg, {
					"label" : "OK",
					"class" : "btn-" + options.type
				}
			);
		}
	};
	App.error = function(error) {
		console.error(error);
		var msg;
		if(error) {
			if(error.jqXHR){
				if(error.jqXHR.status === 400) {
					var jsonError = JSON.parse(error.jqXHR.responseText);
					msg = _.flatten(_.map(jsonError, function(valueArr, key, list){
						return valueArr;
					}));	
				} else {
					msg = ["Erreur système.","Veuillez contacter votre service après vente !"];
				}
			} else if(error.msg){
				msg = _.flatten([error.msg]);
			}
		} else {
			msg = ["Oups... il s'est passé quelque chose d'anormal on dirait ;) !"];
		}
		toastr.error(msg.join("<BR>"));
		return this;
	};
	App.createSelectPlaceholder = function(data){
		return $(window.document.createElement("option"))
				.val("")
				.text(data.label)
				.addClass("placeholder");
	};
	App.formatDate = function(date){
		return (date) ? moment(date).format('DD/MM/YYYY') : null;
	};
	App.commentDialog = function(title, comment, cb, readOnly){
		var form = $("<form></form>");
		form.append("<textarea class=\"span5\" placeholder=\"votre commentaire\" name=\"comment\" rows=\"5\" spellcheck=\"true\">" + (comment || "") + "</textarea>");
		var buttons = [];
		if(readOnly){
			buttons = [{
				"label": "Fermer",
				"callback": function() {
					cb({
						"action":"cancel"
					});
				}
			}];
		} else {
			buttons = [{
				"label": "Annuler",
				"callback": function() {
					cb({
						"action":"cancel"
					});
				}
			}, {
				"label": "Supprimer",
				"class" : "btn-danger",
				"callback": function() {
					cb({
						"action":"delete"
					});
				}
			}, {
				"label": "Sauvegarder",
				"class" : "btn-primary",
				"callback": function() {
					cb({
						"action":"save",
						"comment": form.find("textarea").val()
					});
				}
			}];
		}
		var div = bootbox.dialog(form, buttons, {
			"header": "<h6>" + title + "</h6>"
		});

		div.on("shown", function() {
			form.find("textarea").focus();

			// ensure that submitting the form (e.g. with the enter key)
			// replicates the behaviour of a normal prompt()
			form.on("submit", function(e) {
				e.preventDefault();
				div.find(".btn-primary").click();
			});
		});
	};
	/** I18N_DATA_TABLE */
	var I18N_DATA_TABLE = App.I18N_DATA_TABLE = {
		"sProcessing":		"Traitement en cours...",
		"sLengthMenu":		"Afficher _MENU_ &eacute;l&eacute;ments",
		"sZeroRecords":		"Aucun &eacute;l&eacute;ment &agrave; afficher",
		"sInfo":			"Affichage de l'&eacute;lement _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments",
		"sInfoEmpty":		"Affichage de l'&eacute;lement 0 &agrave; 0 sur 0 &eacute;l&eacute;ments",
		"sInfoFiltered":	"(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)",
		"sInfoPostFix":		"",
		"sSearch":			"Rechercher&nbsp;:",
		"sLoadingRecords":	"Téléchargement...",
		"sUrl":				"",
		"oPaginate": {
			"sFirst":		"Premier",
			"sPrevious":	"Pr&eacute;c&eacute;dent",
			"sNext":		"Suivant",
			"sLast":		"Dernier"
		}
	};
	var DAYS_OF_WEEK = App.DAYS_OF_WEEK = {
		"MONDAY": {"value": 1, "name": "lundi"},
		"TUESDAY": {"value": 2, "name": "mardi"},
		"WEDNESDAY": {"value": 3, "name": "mercredi"},
		"THURSDAY": {"value": 4, "name": "jeudi"},
		"FRIDAY": {"value": 5, "name": "vendredi"},

		getNameByValue: function(value){
			return _.find(this, function(element){
				return element.value === parseInt(this);
			}, value).name;					
		}
	};
	var FEE_TYPE = App.FEE_TYPE = {
		"KILOMETER":{"order": 0, "label": "Frais kilométriques"},
		"PARKING":{"order": 1, "label": "Parking"},
		"TOLL":{"order": 2, "label": "Péages"},
		"TAXI":{"order": 3, "label": "Taxi"},
		"TRAIN":{"order": 4, "label": "Train"},
		"PLANE":{"order": 5, "label": "Avion"},
		"PUBLIC_TRANSPORT":{"order": 6, "label": "Bus/Métro/RER"},
		"TICKET":{"order": 7, "label": "Titre de transport"},
		"HOTEL":{"order": 8, "label": "Hôtel/ Petit déjeuner"},
		"LUNCH":{"order": 9, "label": "Déjeuner"},
		"DINNER":{"order": 10, "label": "Dîner"},
		"MISCELLANEOUS":{"order": 11, "label": "Frais divers"},
		"VEHICLE":{"order": 12, "label": "Frais de véhicule"},
		"MISSION_ALLOWANCE":{"order": 13, "label": "Mission"},

		get: function(code){
			return this[code];
		},
		getLabel: function(code){
			return this.get(code).label;
		},
		getOrder: function(code){
			return this.get(code).order;	
		}

	}
})(window);