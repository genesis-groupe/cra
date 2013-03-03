/* partTime.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** PartTimeSettingController */
	var PartTimeSettingController = window.App.PartTimeSettingController = function(){
		var _model   = new PartTimeSettingModel();
		var _view	= new PartTimeSettingView();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(PartTimeSettingController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderPartTimeSetting", $.proxy(this.onRender, this));	
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("PartTimeSettingController:init", element);
		}
	});
	/** PartTimeSettingModel */
	var PartTimeSettingModel = window.App.PartTimeSettingController.PartTimeSettingModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(PartTimeSettingModel.prototype, {
		bindEvents: function(){
			$(window).on("PartTimeSettingView:submit", $.proxy(this.onSubmit, this));	
			$(window).on("PartTimeSettingView:getCurrentPartTime", $.proxy(this.onGetCurrentCurrentPartTime, this));	
			$(window).on("PartTimeSettingView:disablePartTime", $.proxy(this.onDisable, this));	
		},
		onSubmit: function(event, data){
			jsRoutes.controllers.PartsTimes.addPartTime().ajax({
	            data: JSON.stringify(data),
				context: this
	        })
	        .done(function(data, textStatus, jqXHR){
	        	$(window).trigger("PartTimeSettingModel:partTimeSaved", data);
	        });
		},
		onDisable: function(event, data){
			jsRoutes.controllers.PartsTimes.disablePartTime().ajax({
				context: this
	        })
	        .done(function(data, textStatus, jqXHR){
	        	$(window).trigger("PartTimeSettingModel:partTimeDisabled", data);
	        });	
		}
	});
	/** PartTimeSettingView */
	var PartTimeSettingView = window.App.PartTimeSettingController.PartTimeSettingView = function(){
		this.$el = $("#divPartTimeSetting");
		this.$form = this.$el.find('#formPartTime');

		this.$startPartTime = this.$el.find('#startPartTime');
		this.$chksDayOfWeek = this.$el.find('.day');
		this.$repeatEveryXWeeks = this.$el.find('#repeatEveryXWeeks');
		this.$endPartTime = this.$el.find('#endPartTime');
		this.$morning = this.$el.find('#morning');
		this.$afternoon = this.$el.find('#afternoon');
		this.$dataError = this.$el.find('#dataError');

		this.$currentPartTime = this.$el.find('#currentPartTime');
		this.$btnDisablePartTime = this.$el.find('#btnDisablePartTime');
		this.$tablePartTime = this.$el.find("#tablePartTime").dataTable({
			// Paramètre ajax Source
			"sAjaxDataProp": "partTimes",
			// Paramètres de tri
			"bSort": true,
			"aaSorting": [[0,"asc"]],
			//Paramètre pour le scroll vertical
			"sScrollY": "150px",
			"bScrollCollapse": true,
			// Paramètres généraux
			"oLanguage": App.I18N_DATA_TABLE,
			"bPaginate": false,			
			"bFilter": false,
			"bAutoWidth": false,
			"fnDrawCallback": function(oSettings){
				if(oSettings.fnRecordsTotal()){
					$(window).trigger("PartTimeSettingView:renderCurrentPartTime", this);
				}
			},			
			// Paramètre des colonnes
			"aoColumnDefs": [
				{ 
					"mData": "startPartTime",
					"bSortable": true,
					"aTargets": [0],
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return moment(data, "DD/MM/YYYY").format("YYYYMMDD");
							default:
								return data;
						}
					}
				}, {
					"mData": "endPartTime",	
					"aTargets": [1]
				}, { 
					"mData": "daysOfWeek",
					"aTargets": [2],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								var display = _.toSentence(_.map(data, function(element, index, list){
									return App.DAYS_OF_WEEK.getNameByValue(element);
								}), ', ', ' et ');
								if(full.morning && !full.afternoon){
									display += " matin";
								}
								else if(full.afternoon && !full.morning){
									display += " après-midi";
								}
								return display;
							default:
								return data;
						}
					}
				}, { 
					"mData": "repeatEveryXWeeks",
					"aTargets": [3],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								return data + ((data > 1) ? " semaines" : " semaine");
							default:
								return data;
						}
					}
				}
			]
		});

		this.today = new Date();
		this.startPartTime = null;
		this.endPartTime = null;
		this.msgError = null;

		this.$form.on("submit", {instance: this}, function(event){
			event.preventDefault();
			if(event.data.instance.checkDateIsCorrect() 
				&& event.data.instance.checkBoxsDaysIsCorrect() 
				&& event.data.instance.checkBoxsHalfDaysIsCorrect()){
				jsRoutes.controllers.PartsTimes.isPartTimeEnabled()
				.ajax({
					context: this
				})
				.done(function(data, textStatus, jqXHR){
					if(data){
						var msg = "Vous avez déjà un Temps Partiel actif. "
								+ "Êtes-vous sur de vouloir continuer ?</br>"
								+ "Le Temps Partiel précédent sera désactivé.",
							instance = this;

						bootbox.confirm(msg, function(result) {
							if (result) {
								$(window).trigger("PartTimeSettingView:submit", $(instance).serializeObject());
							}
						});
					}else{
						$(window).trigger("PartTimeSettingView:submit", $(this).serializeObject());
					}
				});
			} else{
				event.data.instance.displayError();
			}
		});
		this.$chksDayOfWeek.on('click', {instance: this}, function(event){
	        $(window).trigger("PartTimeSettingView:dataChange", this);
	    });
	    this.$startPartTime.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.startPartTime = event.date;
			$(window).trigger("PartTimeSettingView:dataChange", this);
		});
		this.$endPartTime.datepicker().on("changeDate", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.endPartTime = event.date;
			$(window).trigger("PartTimeSettingView:dataChange", this);
		});
		this.$morning.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("PartTimeSettingView:dataChange", this);
		});
		this.$afternoon.on("change", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("PartTimeSettingView:dataChange", this);
		});
		this.$btnDisablePartTime.on("click", {instance: this}, function(event){
			var msg = "Êtes-vous sur de vouloir désactivé votre Temps Partiel ?";

			bootbox.confirm(msg, function(result) {
				if (result) {
					$(window).trigger("PartTimeSettingView:disablePartTime", this);
				}
			});
		});

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(PartTimeSettingView.prototype, {
		bindEvents: function(){
			$(window).on("PartTimeSettingController:init", $.proxy(this.onInit, this));	
			$(window).on("PartTimeSettingView:dataChange", $.proxy(this.verifyData, this));	
			$(window).on("PartTimeSettingModel:partTimeSaved", $.proxy(this.onSaved, this));	
			$(window).on("PartTimeSettingModel:partTimeDisabled", $.proxy(this.onDisabled, this));	
			$(window).on("PartTimeSettingView:renderCurrentPartTime", $.proxy(this.onRenderCurrentPartTime, this));	
		},
		/* Event response*/
		onInit: function(){
			this.$el.show();
			this.startPartTime = this.today;
			this.$startPartTime.val(App.formatDate(this.today));
			this.initDataTable();
		},
		onSaved: function(event, data){
			this.$tablePartTime.fnAddData(data);
			//this.displayCurrentPartTime(data);
			App.showModal({type: "success", title:"Temps Partiel", message: "Saisie validée", timeout:3000});
		},
		onDisabled: function(event, data){
			this.$currentPartTime.find('#content').empty();
			this.$currentPartTime.hide();
		},
		initDataTable: function(){
			this.$tablePartTime.fnReloadAjax(
				jsRoutes.controllers.PartsTimes.getPartTime().url
			);	
			return this;
		},
		onRenderCurrentPartTime: function(){
			if(this.$tablePartTime.fnSettings().fnRecordsTotal() >= 1){
				var instance = this;
				_.each(this.$tablePartTime.fnSettings().aoData, function(element, index, list){
					if(element._aData.isActive){
						instance.displayCurrentPartTime(element._aData);
					}	
				});
			}
		},
		displayCurrentPartTime: function(data){
			var lastPartTime = (data.partTimes) ? data.partTimes[data.partTimes.length - 1] : data;
			var text = "Votre Temps Partiel est actuellement : </br>" + "<ul>" + "<li>Le ";
			text  += _.toSentence(_.map(lastPartTime.daysOfWeek, function(element, index, list){
									return App.DAYS_OF_WEEK.getNameByValue(element);
								}), ', ', ' et ');
			text += " ";
			if(lastPartTime.morning != null){
				text += "matin";
			}
			if(lastPartTime.morning != null && lastPartTime.afternoon != null){ 
				text += " et ";
			}
			if(lastPartTime.afternoon != null){
				text += "après-midi";
			}
			text += ", toutes les ";
			if(lastPartTime.repeatEveryXWeeks == 1){
				text += "semaines";
			} else { 
				text += lastPartTime.repeatEveryXWeeks + " semaines";
			}
			text += "</li><li>A partir du " + lastPartTime.startPartTime;
			if(lastPartTime.endPartTime != null){
	            text += " jusqu'au " + lastPartTime.endPartTime;
	        } else {
	            text += ", sans date de fin.";
	        }
	        text += "</li>" +
	        	"</ul>";
			this.$currentPartTime.find('#content').empty().append(text);
			this.$currentPartTime.fadeIn('slow');
			return this;
		},
		verifyData: function(){
			if(this.checkBoxsDaysIsCorrect() && this.checkDateIsCorrect() && this.checkBoxsHalfDaysIsCorrect()) {
				this.$dataError.hide();	
	        } else {
				this.displayError();
	        }	
			return this;
		},
		checkDateIsCorrect: function(){
			if(this.startPartTime){
				if(this.endPartTime){
					if(this.startPartTime.toLocaleDateString() === this.endPartTime.toLocaleDateString()){
	                	this.msgError = "Vous ne pouvez pas saisir sur une journée.";   
	                	return false;
	                }else if (this.startPartTime > this.endPartTime){
	                	this.msgError = "La date de début doit être avant la date de fin.";
	                	return false;
	                }else {
	                	return true;
	                }
	            }else {
	            	return true;
	            }
	        }
		},
		checkBoxsHalfDaysIsCorrect: function(event, target){
			if(this.$morning.is(":checked") || this.$afternoon.is(":checked")){
	        	this.msgError = null;	
				return true;
	        } else {
	        	this.msgError = "Matin et/ou après-midi doit être coché";
	        	return false;
	        }
		},
		checkBoxsDaysIsCorrect: function(){
			var $chks = this.$form.find(".day").filter(':checked');
			if($chks.length === 0){
	        	this.msgError = "Veuillez saisir au moins un jour";	
				return false;
	        } else {
	        	this.msgError = null;
	        	return true;
	        }
		},
		displayError: function(){
			if(this.msgError){
				this.$dataError
					.empty()
					.append(this.msgError)
					.fadeIn("slow");
			}
		},
	});
})(window);
