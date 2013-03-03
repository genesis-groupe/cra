/* holiday.js */
;
(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** HolidayController */
	var HolidayController = window.App.HolidayController = function(){
		var _model = new HolidayModel();
		var _view = new HolidayView();

		this.getModel = function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(HolidayController.prototype, {
		bindEvents: function(){
			$(window).on("App:renderHoliday", $.proxy(this.onRender, this));
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("HolidayController:initForms", element);
		}
	});
	/** HolidayModel */
	var HolidayModel = window.App.HolidayController.HolidayModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(HolidayModel.prototype, {
		bindEvents: function(){
			$(window).on("FormDayView:saveHoliday", $.proxy(this.onSaveOneDayHoliday, this));
			$(window).on("FormPeriodView:saveHoliday", $.proxy(this.onSaveHolidayPeriod, this));
			$(window).on("FormRecurrentView:saveHoliday", $.proxy(this.onSaveHolidayRecurrent, this));
			$(window).on("HolidayView:deleteHoliday", $.proxy(this.deleteHoliday, this));
		},
		onSaveOneDayHoliday: function(event, parameters){
			window.jsRoutes.controllers.Holidays.createOneDayHoliday()
				.ajax({
					data: JSON.stringify(parameters.data),
					context: this
				})
				.done(function(data, textStatus, jqXHR){
					$(window).trigger("HolidayModel:holidaySaved", {"holidays": data, "reloadDataTable": parameters.reloadDataTable});
				});
		},
		onSaveHolidayPeriod: function(event, parameters){
			window.jsRoutes.controllers.Holidays.createHolidayPeriod()
				.ajax({
					data: JSON.stringify(parameters.data),
					context: this
				})
				.done(function(data, textStatus, jqXHR){
					$(window).trigger("HolidayModel:holidaySaved", {"holidays": data, "reloadDataTable": parameters.reloadDataTable});
				});
		},
		onSaveHolidayRecurrent: function(event, parameters){
			window.jsRoutes.controllers.Holidays.createHolidayRecurrent()
				.ajax({
					data: JSON.stringify(parameters.data),
					context: this
				})
				.done(function(data, textStatus, jqXHR){
					$(window).trigger("HolidayModel:holidaySaved", {"holidays": data, "reloadDataTable": parameters.reloadDataTable});
				});
		},
		deleteHoliday: function(event, holiday){
			window.jsRoutes.controllers.Holidays.deleteHoliday()
				.ajax({
					data: JSON.stringify(holiday)
				})
				.done(function(data, textStatus, jqXHR){
					$(window).trigger("HolidayModel:holidayDeleted", data);
				});
		}
	});
	/** HolidayView */
	var HolidayView = window.App.HolidayController.HolidayView = function(){
		var _formDayView = new FormDayView();
		var _formPeriodView = new FormPeriodView();
		var _formRecurrentView = new FormRecurrentView();

		this.today = new Date();

		this.$el = $("#holidays");
		this.$tableAbsences = this.$el.find("#tableAbsences").dataTable({
			// Paramètre ajax Source
			"sAjaxDataProp": "holidays",
			// Paramètres de tri
			"bSort": true,
			"aaSorting": [
				[1, "asc"]
			],
			//Paramètre pour le scroll vertical
			"sScrollY": "400px",
			"bScrollCollapse": true,
			// Paramètres généraux
			"oLanguage": App.I18N_DATA_TABLE,
			"bPaginate": false,
			"bFilter": false,
			"bAutoWidth": false,
			"fnDrawCallback": function(oSettings){
				if (oSettings.fnRecordsTotal()) {
					this.tooltip({"selector": "span[rel=tooltip]"});
				}
			},
			// Paramètre des colonnes
			"aoColumnDefs": [
				{
					"mData": "missionCode",
					"aTargets": [0]
				},
				{
					"mData": "start",
					"aTargets": [1],
					"bSortable": true,
					"mRender": function(data, type, full){
						switch(type) {
							case "sort" :
								return moment(data, "DD/MM/YYYY").format("YYYYMMDD");
							default:
								return data;
						}
					}
				},
				{
					"mData": "end",
					"aTargets": [2],
					"mRender": function(data, type, full){
						switch(type) {
							case "sort" :
								return moment(data, "DD/MM/YYYY").format("YYYYMMDD");
							default:
								return data;
						}
					}
				},
				{
					"mData": "nbDay",
					"aTargets": [3],
					"mRender": function(data, type, full){
						switch(type) {
							case "display" :
								switch(full.halfDayType) {
									case "MORNING":
										return data + " Matin";
										break;
									case "AFTERNOON":
										return data + " Après-midi";
										break;
									default:
										return data;
										break;
								}
							default:
								return data;
						}
					}
				},
				{
					"mData": "isValidated",
					"aTargets": [4],
					"mRender": function(data, type, full){
						switch(type) {
							case "display" :
								return (data) ? "Validé" : "Refusé";
							default:
								return data;
						}
					},
				},
				{
					"mData": "comment",
					"aTargets": [5],
					"mRender": function(data, type, full){
						switch(type) {
							case "display" :
								if (data) {
									var $img = $(document.createElement("i"))
										.addClass("icon-comment");
									var $badge = $(document.createElement("span"))
										.addClass("badge badge-info")
										.attr("data-original-title", data)
										.attr("rel", "tooltip")
										.append($img);
									return $badge[0].outerHTML;
								} else {
									return "";
								}
							default:
								return data;
						}
					}
				},
				{
					"mData": "start",
					"aTargets": [6],
					"bSortable": false,
					"mRender": function(data, type, full){
						var firstDayOfMonth = moment().date(1).hours(0).minutes(0).seconds(0).milliseconds(0),
							holidayDate = moment(data, "DD/MM/YYYY");
						switch(type) {
							case "display" :
								if (holidayDate >= firstDayOfMonth) {
									var $img = $(document.createElement("i"))
										.addClass("icon-trash");
									var $btn = $(document.createElement("button"))
										.addClass("btn btn-danger btn-mini removeLine")
										.attr("id", "btnRemove")
										.append($img);
									return $btn[0].outerHTML;
								} else {
									return "";
								}
							default:
								return data;
						}
					}
				}
			]
		});

		this.getFormDayView = function(){
			return _formDayView;
		};
		this.getFormPeriodView = function(){
			return _formPeriodView;
		};
		this.getFormRecurrentView = function(){
			return _formRecurrentView;
		};

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(HolidayView.prototype, {
		bindEvents: function(){
			$(window).on("HolidayController:initForms", $.proxy(this.init, this));
			$(window).on("DisplayNbDays", $.proxy(this.displayNbDays, this));
			$(window).on("DisplayError", $.proxy(this.displayError, this));
			$(window).on("HolidayModel:holidaySaved", $.proxy(this.onSave, this));
			$(window).on("HolidayModel:holidayDeleted", $.proxy(this.onDelete, this));
			$(window).on("HolidayView:reloadDataTable", $.proxy(this.initDataTable, this));
		},
		/* Event response*/
		init: function(event, data){

			this
				.initFormDay(this.today)
				.initFormRecurrent(this.today)
				.attachEvents()
				.initDataTable(null, this.$el.find('#trigramme').val());
			return this;
		},
		/* Events */
		attachEvents: function(cra){
			this
				.attachRemoveTableAbsenceRowEvent();
			return this;
		},
		attachRemoveTableAbsenceRowEvent: function(){
			this.$tableAbsences
				.off("click", ".removeLine")
				.on("click", ".removeLine", {instance: this}, function(event){
					event.preventDefault();
					var msg = "Êtes-vous sur de vouloir supprimer cette absence ?",
						tr = $(this).parents("tr")[0];

					bootbox.confirm(msg, function(result){
						if (result) {
							var line = event.data.instance.$tableAbsences.fnGetData(tr);
							var holidays = {
								"trigramme": event.data.instance.$el.find('#trigramme').val(),
								"startHoliday": line.start,
								"endHoliday": line.end,
								"isRecurrent": false
							};
							$(window).trigger("HolidayView:deleteHoliday", holidays);
						}
					});
				});
			return this;
		},
		initDataTable: function(event, trigramme){
			if (trigramme) {
				this.$tableAbsences.fnReloadAjax(
					jsRoutes.controllers.Holidays.fetchHolidays(trigramme, this.today.getFullYear()).url
				);
			}
			return this;
		},
		initFormDay: function(data){
			$(window).trigger("HolidayView:initFormDay", data);
			return this;
		},
		initFormRecurrent: function(data){
			$(window).trigger("HolidayView:initFormRecurrent", data);
			return this;
		},
		displayNbDays: function(event, parameters){
			parameters.selector.removeClass("label-important")
				.addClass("label-info")
				.empty()
				.append("Nombre de jour(s) ouvré(s) : " + parameters.nbDays)
				.fadeIn("slow");
		},
		displayError: function(event, parameters){
			parameters.selector.removeClass("label-info")
				.addClass("label-important")
				.empty()
				.append(parameters.msg)
				.fadeIn("slow");
		},
		onSave: function(event, parameters){
			App.showModal({type: "success", title: "Absence", message: "Saisie validée", timeout: 3000});
			if (parameters.reloadDataTable) {
				this.$tableAbsences.fnReloadAjax();
			} else {
				this.$tableAbsences.fnAddData(parameters.holidays);
			}

			$(window).trigger("HolidayView:resetAllForm", this);
		},
		onDelete: function(event, data){
			App.showModal({type: "success", title: "Absence", message: "Congé(s) supprimé(s)", timeout: 3000});
			this.$tableAbsences.fnReloadAjax();
		}
	});
	/** FormDay */
	var FormDayView = window.App.HolidayController.HolidayView.FormDayView = function(){
		this.$el = $("#formDay");

		this.$trigramme = this.$el.find("#trigramme");
		this.$date = this.$el.find("#date");
		this.$morning = this.$el.find("#morning");
		this.$afternoon = this.$el.find("#afternoon");

		this.$infoDay = this.$el.find("#infoNbDayOnDayForm");
		this.$btnReset = this.$el.find("#btnReset");

		this.dateHoliday = "";

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();

		this.$date.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.dateHoliday = event.date;
			$(window).trigger("FormDayView:changeData", this.dateHoliday);
		});
		this.$morning.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormDayView:changeData", event.data.instance);
		});
		this.$afternoon.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormDayView:changeData", event.data.instance);
		});
		this.$btnReset.on("click", {instance: this}, function(event){
			event.data.instance.$infoDay.empty().hide();
			event.data.instance.dateHoliday = null;
		});
		this.$el.on("submit", {instance: this}, function(event){
			event.preventDefault();
			if (event.data.instance.dateHoliday && event.data.instance.checkBoxsIsCorrect()) {
				jsRoutes.controllers.Holidays.holidaysExisteOnDates()
					.ajax({
						data: JSON.stringify($(this).serializeObject()),
						context: this
					})
					.done(function(data, textStatus, jqXHR){
						var holidays = $(this).serializeObject();
						if (data) {
							var msg = "Vous avez déjà saisie sur ce jour. Êtes-vous sur de vouloir continuer ?</br>"
								+ "Les données précédentes seront effacées et le CRA sera invalidé.";

							bootbox.confirm(msg, function(result){
								if (result) {
									$(window).trigger("FormDayView:saveHoliday", {"data": holidays, "reloadDataTable": true});
								}
							});

						} else {
							$(window).trigger("FormDayView:saveHoliday", {"data": holidays, "reloadDataTable": false});
						}
					});
			}
		});
		this.$trigramme.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("HolidayView:reloadDataTable", event.data.instance.$trigramme.val());
		});
	};
	_.extend(FormDayView.prototype, {
		bindEvents: function(){
			$(window).on("HolidayView:initFormDay", $.proxy(this.init, this));
			$(window).on("FormDayView:changeData", $.proxy(this.checkData, this));
			$(window).on("HolidayView:resetAllForm", $.proxy(this.resetForm, this));
		},
		/* Event response*/
		init: function(event, data){
			$(this.$date).val(App.formatDate(data));
			this.dateHoliday = data;
			return this;
		},
		checkData: function(event, data){
			if (this.checkBoxsIsCorrect()) {
				this.computeWorkingDays();
			} else {
				var dataError = {
					"selector": this.$infoDay,
					"msg": "Veuillez sélectionner au moins une case"
				}
				$(window).trigger("DisplayError", dataError);
			}
			return this;
		},
		checkBoxsIsCorrect: function(){
			return (this.$morning.is(":checked") || this.$afternoon.is(":checked"))
		},
		computeWorkingDays: function(){
			if (this.dateHoliday) {
				jsRoutes.controllers.Times.isWorkingDay().ajax({
					data: JSON.stringify({"startHoliday": App.formatDate(this.dateHoliday)}),
					context: this
				})
					.done(function(data, textStatus, jqXHR){
						if (data) {
							var dataToDisplay = {
								"selector": this.$infoDay
							}
							//Si matin ET après-midi
							if (this.$morning.is(":checked") && this.$afternoon.is(":checked")) {
								dataToDisplay.nbDays = 1;
							} else {
								dataToDisplay.nbDays = 0.5;
							}
							$(window).trigger("DisplayNbDays", dataToDisplay);
						} else {
							var dataError = {
								"selector": this.$infoDay,
								"msg": "Veuillez saisir un jour ouvré"
							}
							$(window).trigger("DisplayError", dataError);
						}
					});
			}
		},
		resetForm: function(){
			this.$infoDay.empty().hide();
			this.dateHoliday = null;
			_.each(this.$el, function(element, index, list){
				element.reset();
			});
		}
	});
	/** FormPeriodView */
	var FormPeriodView = window.App.HolidayController.HolidayView.FormPeriodView = function(){
		this.$el = $("#formPeriod");

		this.$trigramme = this.$el.find("#trigramme");
		this.$startHoliday = this.$el.find("#startHoliday");
		this.$startMorning = this.$el.find("#startMorning");
		this.$startAfternoon = this.$el.find("#startAfternoon");
		this.$endHoliday = this.$el.find("#endHoliday");
		this.$endMorning = this.$el.find("#endMorning");
		this.$endAfternoon = this.$el.find("#endAfternoon");

		this.$infoDay = this.$el.find("#infoNbDayPeriod");
		this.$btnReset = this.$el.find("#btnReset");

		this.startDate = null;
		this.endDate = null;

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();

		this.$el.on("submit", {instance: this}, function(event){
			event.preventDefault();
			if (event.data.instance.endDate > event.data.instance.startDate) {
				jsRoutes.controllers.Holidays.holidaysExisteOnDates()
					.ajax({
						data: JSON.stringify($(this).serializeObject()),
						context: this
					})
					.done(function(data, textStatus, jqXHR){
						var holidays = $(this).serializeObject();
						if (data) {
							var msg = "Vous avez déjà saisie sur ces jours. Êtes-vous sur de vouloir continuer ?</br>"
								+ "Les données précédentes seront effacées et le CRA sera invalidé .";

							bootbox.confirm(msg, function(result){
								if (result) {
									$(window).trigger("FormPeriodView:saveHoliday", {"data": holidays, "reloadDataTable": true});
								}
							});

						} else {
							$(window).trigger("FormPeriodView:saveHoliday", {"data": holidays, "reloadDataTable": false});
						}
					});
			}
		});
		this.$startHoliday.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.startDate = event.date;
			$(window).trigger("FormPeriodView:changeData", this.startDate);
		});
		this.$btnReset.on("click", {instance: this}, function(event){
			event.data.instance.$infoDay.empty().hide();
			event.data.instance.startDate = null;
			event.data.instance.endDate = null;
		});
		this.$endHoliday.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.endDate = event.date;
			$(window).trigger("FormPeriodView:changeData", this.endDate);
		});
		this.$startMorning.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormPeriodView:changeData", event.data.instance);
		});
		this.$endAfternoon.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormPeriodView:changeData", event.data.instance);
		});
		this.$trigramme.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("HolidayView:reloadDataTable", event.data.instance.$trigramme.val());
		});
	};
	_.extend(FormPeriodView.prototype, {
		bindEvents: function(){
			$(window).on("FormPeriodView:changeData", $.proxy(this.checkData, this));
			$(window).on("HolidayView:resetAllForm", $.proxy(this.resetForm, this));
		},
		/* Event response*/
		checkData: function(event, data){
			if (this.startDate && this.endDate) {
				if (this.startDate.toLocaleDateString() === this.endDate.toLocaleDateString()) {
					var dataError = {
						"selector": this.$infoDay,
						"msg": "Pour saisir une seule journée</br>Veuillez utiliser l'onglet Absence jour"
					}
					$(window).trigger("DisplayError", dataError);
				} else {
					this.computeWorkingDays();
				}
			}
			return this;
		},
		computeWorkingDays: function(){
			var period = {
				"startHoliday": App.formatDate(this.startDate),
				"startMorning": this.$startMorning.is(':checked'),
				"startAfternoon": this.$startAfternoon.is(':checked'),
				"endHoliday": App.formatDate(this.endDate),
				"endMorning": this.$endMorning.is(':checked'),
				"endAfternoon": this.$endAfternoon.is(':checked')
			};
			jsRoutes.controllers.Times.computeWorkingDays().ajax({
				data: JSON.stringify(period),
				context: this
			})
				.done(function(data, textStatus, jqXHR){
					var dataToDisplay = {
						"selector": this.$infoDay,
						"nbDays": data.nbDay
					}
					$(window).trigger("DisplayNbDays", dataToDisplay);
					if (data.findDayOff) {
						App.showModal({
							type: "warning",
							title: "Attention",
							message: "Jour férié détecté dans votre saisie</br>Il n'est pas compté",
							timeout: 3000,
							positionClass: 'toast-top-left'
						});
					}
				})
				.fail(function(jqXHR, settings, errorThrown){
					var dataError = {
						"selector": this.$infoDay,
						"msg": $.parseJSON(jqXHR.responseText)['']
					}
					$(window).trigger("DisplayError", dataError);
				});
		},
		resetForm: function(){
			this.$infoDay.empty().hide();
			this.startDate = null;
			this.endDate = null;
			_.each(this.$el, function(element, index, list){
				element.reset();
			});
		}
	});
	/** FormRecurrentView */
	var FormRecurrentView = window.App.HolidayController.HolidayView.FormRecurrentView = function(){
		this.$el = $("#formRecurrent");

		this.$trigramme = this.$el.find("#trigramme");
		this.$recurrence = this.$el.find("#recurrence");
		this.$chksDayOfWeek = this.$el.find('.day');
		this.$startRecurrence = this.$el.find("#startRecurrence");
		this.$radios = this.$el.find('input[type=radio]');
		this.$nbOfOccurences = this.$el.find("#nbOfOccurences");
		this.$inputAt = this.$el.find("#inputAt");
		this.$morning = this.$el.find("#morning");
		this.$afternoon = this.$el.find("#afternoon");

		this.$infoDay = this.$el.find("#infoNbDay");
		this.$infoDayOff = this.$el.find('#infoDayOff');

		this.$btnReset = this.$el.find("#btnReset");

		this.startDate = null;
		this.endDate = null;
		this.nbOfOccurences = 1;
		this.nbDay = 0;
		this.msgError = null;

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();

		this.$el.on("submit", {instance: this}, function(event){
			event.preventDefault();
			if (event.data.instance.nbDay > 1 && event.data.instance.checkBoxsDaysIsCorrect()) {
				var msg = "Êtes-vous sur de vouloir continuer ?</br>"
						+ "Les données saisies sur ces jours seront effacées et le(s CRA(s) associé(s) invalidé(s).",
					holidays = $(this).serializeObject();

				bootbox.confirm(msg, function(result){
					if (result) {
						$(window).trigger("FormRecurrentView:saveHoliday", {"data": holidays, "reloadDataTable": true});
					}
				});
			}
		});
		this.$btnReset.on("click", {instance: this}, function(event){
			$(window).trigger("FormRecurrentView:resetForm", this);
		});
		this.$startRecurrence.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.startDate = event.date;
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$recurrence.on('change', {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$chksDayOfWeek.on('click', {instance: this}, function(event){
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$radios.on('change', {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormRecurrentView:changeRadio", this);
		});
		this.$nbOfOccurences.on('change', {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.nbOfOccurences = $(this).val();
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$inputAt.datepicker().on('changeDate', {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.endDate = event.date;
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$trigramme.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("HolidayView:reloadDataTable", event.data.instance.$trigramme.val());
		});
		this.$morning.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
		this.$afternoon.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FormRecurrentView:reloadInfoDay", this);
		});
	};
	_.extend(FormRecurrentView.prototype, {
		bindEvents: function(){
			$(window).on("HolidayView:initFormRecurrent", $.proxy(this.init, this));
			$(window).on("FormRecurrentView:reloadInfoDay", $.proxy(this.renderInfoDay, this));
			$(window).on("FormRecurrentView:changeRadio", $.proxy(this.onChangeRadio, this));
			$(window).on("FormRecurrentView:resetForm HolidayView:resetAllForm", $.proxy(this.resetForm, this));
		},
		/* Event response*/
		init: function(event, data){
			this
				.initStartRecurrence(data)
				.initChksDaysOfWeek(data);

			return this;
		},
		initStartRecurrence: function(data){
			$(this.$startRecurrence).val(App.formatDate(data));
			this.startDate = data;
			return this;
		},
		initChksDaysOfWeek: function(data){
			$(this.$el.find('#' + data.getDay())).attr('checked', true);
			return this;
		},
		checkBoxsDaysIsCorrect: function(){
			var $chks = this.$el.find(".day").filter(':checked');
			if ($chks.length === 0) {
				this.msgError = "Veuillez saisir au moins un jour";
				return false;
			} else {
				this.msgError = null;
				return true;
			}
		},
		checkBoxsHalfDaysIsCorrect: function(event, target){
			if (this.$morning.is(":checked") || this.$afternoon.is(":checked")) {
				this.msgError = null;
				return true;
			} else {
				this.msgError = "Matin et/ou après-midi doit être coché";
				return false;
			}
		},
		renderInfoDay: function(){
			// Si une erreur a déjà été générée
			if (this.checkBoxsDaysIsCorrect() && this.checkBoxsHalfDaysIsCorrect()) {
				if (this.startDate && this.endDate) {
					if (this.startDate.toLocaleDateString() === this.endDate.toLocaleDateString()) {
						var dataError = {
							"selector": this.$infoDay,
							"msg": "Pour saisir une seule journée</br>Veuillez utiliser l'onglet Absence jour"
						}
						$(window).trigger("DisplayError", dataError);
					} else {
						this.computeWorkingDays();
					}
				} else if (this.startDate && this.nbOfOccurences) {
					this.computeWorkingDays();
				}
			} else {
				var dataError = {
					"selector": this.$infoDay,
					"msg": this.msgError
				}
				$(window).trigger("DisplayError", dataError);
			}
			return this;
		},
		onChangeRadio: function(){
			//TODO Refactorer
			switch(this.$el.find('input[type=radio]').filter(':checked').val()) {
				case 'after':
					$(this.$nbOfOccurences)
						.attr('disabled', false)
						.attr('required', true);
					$(this.$inputAt)
						.attr('disabled', true)
						.attr('required', false)
						.val('');
					this.endDate = null;
					break;
				case 'at':
					$(this.$nbOfOccurences)
						.attr('disabled', true)
						.attr('required', false)
						.val('');
					$(this.$inputAt)
						.attr('disabled', false)
						.attr('required', true);
					this.nbOfOccurences = null;
					break;
			}
			return this;
		},
		computeWorkingDays: function(){
			var $chks = this.$el.find(".day").filter(':checked'),
				period = {
					"startRecurrence": App.formatDate(this.startDate),
					"repeatEveryXWeeks": this.$recurrence.val(),
					"morning": this.$morning.is(':checked'),
					"afternoon": this.$afternoon.is(':checked'),
					"daysOfWeek": [],
					"nbOfOccurences": this.nbOfOccurences,
					"endRecurrence": App.formatDate(this.endDate)
				};
			_.each($chks, function(element, index, list){
				period.daysOfWeek[index] = element.value;
			});
			jsRoutes.controllers.Times.computeWorkingRecurrentDays().ajax({
				data: JSON.stringify(period),
				context: this
			})
				.done(function(data, textStatus, jqXHR){
					this.displayNbDays(data);
				})
				.fail(function(jqXHR, settings, errorThrown){
					var dataError = {
						"selector": this.$infoDay,
						"msg": $.parseJSON(jqXHR.responseText)['']
					}
					$(window).trigger("DisplayError", dataError);
				});
		},
		resetForm: function(){
			this.$infoDay.empty().hide();
			this.startDate = null;
			this.endDate = null;
			_.each(this.$el, function(element, index, list){
				element.reset();
			});
			this.init(null, new Date());
		},
		displayNbDays: function(parameters){
			this.nbDay = parameters.nbDay;
			if (parameters.isOnlyDay) {
				var dataError = {
					"selector": this.$infoDay,
					"msg": "Pour saisir une seule journée</br>Veuillez utiliser l'onglet Absence jour"
				}
				$(window).trigger("DisplayError", dataError);
			} else {
				this.$infoDay.removeClass("label-important")
					.addClass("label-info")
					.empty()
					.append("Premier jour posé : " + parameters.start
						+ "</br>Dernier jour posé : " + parameters.end
						+ "</br>Total : " + this.nbDay + " jour(s)")
					.fadeIn("slow");

				if (parameters.findDayOff) {
					App.showModal({
						type: "warning",
						title: "Attention",
						message: "Jour férié détecté dans votre saisie</br>Il n'est pas compté",
						timeout: 3000,
						positionClass: 'toast-top-left'
					});
				}
			}
		}
	});
})(window);