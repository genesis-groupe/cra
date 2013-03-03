/* pay.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** PayController */
	var PayController = window.App.PayController = function(){
		var _dataModel = new PayModel();
		var _view = new PayView();

		this.getModel = function(){
			return _dataModel;
		}
		this.getView = function(){
			return _view;
		}

		this.bindEvents();
	};
	_.extend(PayController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderPay", $.proxy(this.onRenderPay, this));
			$(window).on("PayView:exportPay", $.proxy(this.onExportPay, this));
		},
		onRenderPay: function(event, criterias){
			this.getModel().loadData(criterias);
		},
		onExportPay: function(event, data){
			var pay = this.getModel().getData();
			if (data.type === "EMPLOYEE"){
				window.open(jsRoutes.controllers.Exports.payEmployee("pdf", pay.employee.trigramme, pay.year, pay.month.value, pay.hourlyRate).url);
			} else {
				window.open(jsRoutes.controllers.Exports.payCustomer("pdf", pay.employee.trigramme, pay.year, pay.month.value, pay.hourlyRate, data.customerId).url);
			}
		}
	});
	/** PayModel */
	var PayModel = window.App.PayController.PayModel = function() {
		var _pay = {};
		this.getData= function(){
			return _pay;
		};
		this.setData = function(data){
			_pay = data;
		};
		this.bindEvents();
	}
	_.extend(PayModel.prototype,{
		bindEvents: function(){
			$(window).on("PayView:validate", $.proxy(this.onValidate, this));
			$(window).on("PayView:invalidate", $.proxy(this.onInvalidate, this));
		},
		loadData: function(criterias) {
			jsRoutes.controllers.Pays.fetch(criterias.trigramme, criterias.year, criterias.month, criterias.hourlyRate)
			.ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				console.log("PayModel.loadData", data);
				this.setData(data);
				$(window).trigger("PayModel:dataLoaded", data);
			});
		},
		onValidate: function(event, data){
			jsRoutes.controllers.Pays.validate(this.getData().id).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				this.setData(data);
				$(window).trigger("PayModel:validated", this.getData());
			});
			return this;
		},
		onInvalidate: function(event,data){
			jsRoutes.controllers.Pays.invalidate(this.getData().id).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				this.setData(data);
				$(window).trigger("PayModel:invalidated", this.getData());
			});
			return this;	
		},
	});
	/** PayView */
	var PayView = window.App.PayController.PayView = function() {
		this.$el = $("#payResult");
		this.$toolbar = $("#payToolbar");
		this.$alertMessage = $("#payAlertMessage");
		this.$craComment = $("#craComment");
		/* accesseurs aux champs de la vue. */
		this.$year						= this.$el.find("#payYear");
		this.$month						= this.$el.find("#payMonth");
		this.$lastName					= this.$el.find("#lastName");
		this.$firstName					= this.$el.find("#firstName");
		this.$trigramme					= this.$el.find("#trigramme");
		this.$nbTheoreticalWorkingDays	= this.$el.find("#nbTheoreticalWorkingDays");
		this.$nbCustomerDays			= this.$el.find("#nbCustomerDays");
		this.$nbPresalesDays			= this.$el.find("#nbPresalesDays");
		this.$nbInternalWorkDays		= this.$el.find("#nbInternalWorkDays");
		this.$nbHolidays				= this.$el.find("#nbHolidays");
		this.$nbWorkingDays				= this.$el.find("#nbWorkingDays");
		this.$normalTimes				= this.$el.find("#normalTimes");
		this.$normalTimesAmount			= this.$el.find("#normalTimesAmount");
		this.$extraTimes125				= this.$el.find("#extraTimes125");
		this.$extraTimes125Amount		= this.$el.find("#extraTimes125Amount");
		this.$extraTimes150				= this.$el.find("#extraTimes150");
		this.$extraTimes150Amount		= this.$el.find("#extraTimes150Amount");
		this.$nightlyTimes				= this.$el.find("#nightlyTimes");
		this.$nightlyTimesAmount		= this.$el.find("#nightlyTimesAmount");
		this.$sundayTimes				= this.$el.find("#sundayTimes");
		this.$sundayTimesAmount			= this.$el.find("#sundayTimesAmount");
		this.$dayOffTimes				= this.$el.find("#dayOffTimes");
		this.$dayOffTimesAmount			= this.$el.find("#dayOffTimesAmount");
		/* StandBy */
		this.$standBys					= this.$el.find("#standBys");
		/* Table des semaines */
		this.$tableWeeksCounters = this.$el.find("#weeksCounters").dataTable({
			"sDom": "rt",
			"aaSorting": [[0,"asc"]],
			"bPaginate": false,
			"bSort": true,
			"bInfo": false,
			"bFilter": false,
			"bProcessing": false,
			"oLanguage": App.I18N_DATA_TABLE,
			"aoColumnDefs": [
				{
					"aTargets": [0],
					"sType": "numeric",
					"mData"   : "weekNumber",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return full.startDay + "-" + full.endDay + " (" + full.weekNumber + ")";
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [1],
					"mData"   : "timeCounter.totalTimes.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.totalTimes) + ((full.timeCounter.totalTimesAmount) ? " / " + PayView.formatAmount(full.timeCounter.totalTimesAmount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [2],
					"mData"   : "timeCounter.normalTimes.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.normalTimes) +  ((full.timeCounter.normalTimesAmount) ? " / " + PayView.formatAmount(full.timeCounter.normalTimesAmount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [3],
					"mData"   : "timeCounter.extraTimes125.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.extraTimes125) +  ((full.timeCounter.extraTimes125Amount) ? " / " + PayView.formatAmount(full.timeCounter.extraTimes125Amount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [4],
					"mData"   : "timeCounter.extraTimes150.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.extraTimes150) +  ((full.timeCounter.extraTimes150Amount) ? " / " + PayView.formatAmount(full.timeCounter.extraTimes150Amount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [5],
					"mData"   : "timeCounter.nightlyTimes.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.nightlyTimes) +  ((full.timeCounter.nightlyTimesAmount) ? " / " + PayView.formatAmount(full.timeCounter.nightlyTimesAmount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [6],
					"mData"   : "timeCounter.sundayTimes.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.sundayTimes) +  ((full.timeCounter.sundayTimesAmount) ? " / " + PayView.formatAmount(full.timeCounter.sundayTimesAmount) : "") ;
							default:
								return data;
						}
					}
				},
				{
					"aTargets": [7],
					"mData"   : "timeCounter.dayOffTimes.asHour",
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return data;
							case "display":
								return PayView.formatTime(full.timeCounter.dayOffTimes) +  ((full.timeCounter.dayOffTimesAmount) ? " / " + PayView.formatAmount(full.timeCounter.dayOffTimesAmount) : "") ;
							default:
								return data;
						}
					}
				}
			]
		});
		/* Table des missions */
		this.$tableMissions = this.$el.find("#missions").dataTable({
			"sDom": "rt",
			"aaSorting": [[0,"asc"]],
			"bPaginate": false,
			"bSort": true,
			"bInfo": false,
			"bFilter": false,
			"bProcessing": false,
			"oLanguage": App.I18N_DATA_TABLE,
			"aoColumnDefs": [
				{
					"aTargets": [0],
					"mData"   : "mission.customerName"
				},
				{
					"aTargets": [1],
					"mData"   : "mission.code"
				},
				{
					"aTargets": [2],
					"mData"   : "mission.label"
				},
				{
					"aTargets": [3],
					"sType": "numeric",
					"mData"   : "nbDays"
				}
			]
		});

		
		this.bindEvents();
	}
	_.extend(PayView.prototype,{
		bindEvents: function(){
			$(window).on("PayModel:dataLoaded", $.proxy(this.onLoadedData, this));
			$(window).on("PayModel:validated", $.proxy(this.onValidated, this));
			$(window).on("PayModel:invalidated", $.proxy(this.onInvalidated, this));
		},
		onLoadedData: function(event, pay){
			this
				.renderMessage(pay)
				.renderComment(pay)
				.renderToolbar(pay)
				.renderMissions(pay)
				.renderMonth(pay)
				.renderWeeks(pay)
				.renderStandBys(pay)
				.attachEvents()
				.show(pay);
			return this;
		},
		onValidated: function(event, pay){
			this.renderToolbar(pay);
			return this;
		},
		onInvalidated: function(event, pay){
			this.renderToolbar(pay);
			return this;
		},
		renderMessage: function(pay){
			if(!pay.isCraValidated){
				this.$alertMessage.find("#message").empty().append("le CRA n'est pas validé.");
				this.$alertMessage.show();
			} else {
				this.$alertMessage.hide();
			}
			return this;
		},
		renderComment: function(pay){
			if(pay.craComment){
				this.$craComment.find("#comment").empty().append(pay.craComment);
			} else {
				this.$craComment.hide();
			}
			return this;
		},
		renderToolbar: function(pay){
			if(pay.isCraValidated){
				if(pay.isValidated) {
					this.$toolbar.find("#validate").hide();
					this.$toolbar.find("#invalidate").show();
				} else {
					this.$toolbar.find("#validate").show();
					this.$toolbar.find("#invalidate").hide();
				}
			} else {
				this.$toolbar.find("#validate").hide();
				this.$toolbar.find("#invalidate").hide();
			}
			jsRoutes.controllers.Cras.fetchCustomers(pay.employee.trigramme, pay.year, pay.month.value)
				.ajax({
					context: this
				})
				.done(function(customers, textStatus, jqXHR){
					var m = _.map(customers, function(customer, index, list){
						return $(document.createElement("li"))
								.append($(document.createElement("a"))
									.addClass("exportCraCustomer")
									.data("customer-id", customer.id)
									.data("customer-name", customer.name)
									.attr("href", "#")
									.text(_.capitalize(customer.name))
								);
					}, this);
					this.$toolbar.find('#printCustomerList').empty().append(m);
				});
			this.$toolbar.show();
			return this;
		},
		renderMonth: function(pay){
			this.$year.empty().append(pay.year);
			this.$month.empty().append(pay.month.label);
			this.$firstName.empty().append(_.capitalize(pay.employee.firstName));
			this.$lastName.empty().append(_.capitalize(pay.employee.lastName));
			this.$trigramme.empty().append(pay.employee.trigramme.toUpperCase());
			this.$nbTheoreticalWorkingDays.empty().append(pay.monthDayCounter.nbTheoreticalWorkingDays);
			this.$nbCustomerDays.empty().append(pay.monthDayCounter.nbCustomerDays);
			this.$nbPresalesDays.empty().append(pay.monthDayCounter.nbPresalesDays);
			this.$nbInternalWorkDays.empty().append(pay.monthDayCounter.nbInternalWorkDays);
			this.$nbHolidays.empty().append(pay.monthDayCounter.nbHolidays);
			this.$nbWorkingDays.empty().append(pay.nbWorkingDays);
			this.$extraTimes125.empty().append(PayView.formatTime(pay.monthTimeCounter.extraTimes125));
			this.$extraTimes125Amount.empty().append(PayView.formatAmount(pay.monthTimeCounter.extraTimes125Amount));
			this.$extraTimes150.empty().append(PayView.formatTime(pay.monthTimeCounter.extraTimes150));
			this.$extraTimes150Amount.empty().append(PayView.formatAmount(pay.monthTimeCounter.extraTimes150Amount));
			this.$nightlyTimes.empty().append(PayView.formatTime(pay.monthTimeCounter.nightlyTimes));
			this.$nightlyTimesAmount.empty().append(PayView.formatAmount(pay.monthTimeCounter.nightlyTimesAmount));
			this.$sundayTimes.empty().append(PayView.formatTime(pay.monthTimeCounter.sundayTimes));
			this.$sundayTimesAmount.empty().append(PayView.formatAmount(pay.monthTimeCounter.sundayTimesAmount));
			this.$dayOffTimes.empty().append(PayView.formatTime(pay.monthTimeCounter.dayOffTimes));
			this.$dayOffTimesAmount.empty().append(PayView.formatAmount(pay.monthTimeCounter.dayOffTimesAmount));
			return this;
		},
		renderWeeks: function(pay){
			this.$tableWeeksCounters.fnClearTable();
			_.each(pay.weeksCounters, function(weekCounters, index, list){
				this.$tableWeeksCounters.fnAddData(weekCounters);
			}, this);
			return this;
		},
		renderMissions: function(pay){
			this.$tableMissions.fnClearTable();
			_.each(pay.missionCounters, function(counter, index, list){
				this.$tableMissions.fnAddData(counter);
			}, this);
			return this;
		},
		renderStandBys: function(pay){
			/* Retrieve all missions according to pay standby */
			this.$standBys.empty();
			if((_.keys(pay.standBys).length)){
				var missionsIds = {
					"ids" :	_.flatten(_.map(pay.standBys, function(week, weekNumber, list){
						return _.map(week, function(standBy, index, list){
							return standBy.missionId;
						});
					}))
				}
				jsRoutes.controllers.Missions.fetchByIds()
					.ajax({
						data: JSON.stringify(missionsIds),
						context: this
					})
					.done(function(missions, textStatus, jqXHR){
						console.log("Missions.fetchByIds",missions)
						var tables = _.map(pay.standBys, function(week, weekNumber, list){
							return $(document.createElement("table"))
								.addClass("table table-striped table-bordered")
								.append($(document.createElement("caption")).text("Semaine " + weekNumber))
								.append(
									_.map(week, function(standBy, index, list){
										var mission = _.find(missions, function(mission){
											return mission.id === standBy.missionId;
										});
										var standByLabel = _.find(mission.standByMoments, function(standByMoment){
											return standByMoment.index === standBy.standByMomentIndex;
										})
										return $(document.createElement("tr"))
											.append($(document.createElement("th")).append(mission.customerMissionName))
											.append($(document.createElement("td")).append(standByLabel.label))
										
									})
								)
						})
						this.$standBys.empty().append(tables);
					});
			}
			return this;
		},
		attachEvents: function(){
			this.$toolbar.find("#validateBtn")
				.off("click")
				.on("click", function(event){
					event.preventDefault();
					var msg = "Êtes-vous sur de vouloir valider la rémunération ?";
					bootbox.confirm(msg, function(result) {
						if (result) {
							$(window).trigger("PayView:validate", {});
						}
					});
				});
			this.$toolbar.find("#invalidateBtn")
				.off("click")
				.on("click", function(event){
					event.preventDefault();
					var msg = "Êtes-vous sur de vouloir invalider la rémunération ?";
					bootbox.confirm(msg, function(result) {
						if (result) {
							$(window).trigger("PayView:invalidate", {});
						}
					});
				});
			this.$toolbar.find("#btn-print-complete")
				.off("click")
				.on("click", function(event) {
					event.preventDefault();
					$(window).trigger("PayView:exportPay", {"type":"EMPLOYEE"});
				});
			this.$toolbar
				.off("click",".exportCraCustomer")
				.on('click', ".exportCraCustomer", function(event){
					event.preventDefault();
					$(window).trigger("PayView:exportPay", {"type":"CUSTOMER", "customerId": $(this).data("customer-id")});
				});
			return this;
		},
		show: function(data){
			this.$el.show();
			$(window).trigger('ShowPay:openCalendar', $("#searchForm").serializeObject());
			$(window).trigger('ShowPay:openFees', {"trigramme": data.employee.trigramme, "year": data.year, "month": data.month});
			return this;
		}
	});
	PayView.formatTime = function(data){
		return (data.asHour) ? data.asHour + "h (" + moment(data.duration, "HH:mm:ss").format("HH:mm") + ")" : "0h";
	};
	PayView.formatAstreinte = function(data){
		return astreinte ? astreinte : astreinte + " jour(s)"
	};
	PayView.formatAmount = function(amount){
		return amount ? amount + "€" : "0€";
	}
})(window);