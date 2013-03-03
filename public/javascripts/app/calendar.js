/* calendar.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** CalendarController */
	var CalendarController = window.App.CalendarController = function(readOnly){
		var _readOnly = readOnly || false;
		var _model   = new CalendarModel(_readOnly);
		var _view	= new CalendarView(_readOnly);
		var _dayCtrl = new App.Day();
		var _standbyCtrl = new App.StandBy();
		var _feesCtrl = new App.FeesCraController();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		this.isReadOnly = function(){
			return _readOnly;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(CalendarController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderCalendar", $.proxy(this.onRender, this));

			$(window).on("CalendarView:onEditDays", $.proxy(this.onEditDays, this));
			$(window).on("CalendarView:editStandBy", $.proxy(this.onEditStandBy, this));
			$(window).on("CalendarView:openCraFees", $.proxy(this.onOpenFees, this));

			$(window).on("CalendarModel:daysSaved", $.proxy(this.onDaysSaved, this));
			$(window).on("CalendarModel:standBySaved", $.proxy(this.onStandBySaved, this));

			$(window).on("Day:close", $.proxy(this.onCloseDay, this));
			$(window).on("Day:saveDays", $.proxy(this.onSaveDays, this));

			$(window).on("StandBy:save", $.proxy(this.onSaveStandBy, this));
		},
		/* Event response*/
		onRender: function(event, data){
			$(window).trigger("CalendarController:loadData", data);
		},
		onEditDays: function(event, data){
			$(window).trigger("CalendarController:editDays", this.getModel().filterDays(data.indexes));
		},
		onEditStandBy: function(event, week){
			var cra_clone = _.pick(this.getModel().getData(), "employee", "year", "month", "standBys", "isValidated", "isValidated")
			var data = {
				"trigramme": cra_clone.employee.trigramme,
				"year": cra_clone.year,
				"month": cra_clone.month.value,
				"standBy": cra_clone.standBys[week],
				"week" : week,
				"readOnly": cra_clone.isValidated || cra_clone.isValidated
			}
			$(window).trigger("CalendarController:editStandBy", data);
		},
		onOpenFees: function(event, data){
			$(window).trigger("CalendarController:openCraFees", data);
		},
		onCloseDay: function(event, data){
			$(window).trigger("CalendarController:closeDay", {});
		},
		onSaveDays: function(event, newCra){
			$(window).trigger("CalendarController:saveDays", newCra);
		},
		onSaveStandBy: function(event, standBy){
			$(window).trigger("CalendarController:saveStandBy", standBy);
		},
		onDaysSaved: function(event, indexes){
			$(window).trigger("CalendarController:daysSaved", days);
			return this;
		},
		onStandBySaved: function(event, indexes){
			$(window).trigger("CalendarController:standBySaved", days);
			return this;
		}
	});
	/** CalendarModel */
	var CalendarModel = window.App.CalendarController.CalendarModel = function(readOnly){
		var _readOnly = readOnly || false;
		var _cra = {};
		this.isReadOnly = function(){
			return _readOnly;
		};
		this.getData= function(){
			return _cra;
		};
		this.getDays = function(indexes){
			var i = _.map(indexes, function(value, key, list) {
				return this[value];
			}, _cra.days);
			return i;
		};
		this.filterDays = function(indexes){
			var cra = _.pick(_cra, _.without(_.keys(_cra), "days"));
			cra.days = _.map(indexes, function(index, key, list){
				return this[index];
			}, _cra.days);
			return cra;
		};
		this.isEmptyDay = function(indexes) {
			console.log(indexes);
			var result = false;
			_.each(indexes, function(i, index,list){
				result = (this.days[i].morning) || (this.days[i].afternoon) ? false : true;
			}, _cra);
			return result;
		};
		this.setData = function(data){
			_cra = data;
			_readOnly = data.isValidated;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(CalendarModel.prototype, {
		bindEvents: function(){
			$(window).on("CalendarController:loadData", $.proxy(this.onLoadData, this));
			$(window).on("CalendarController:saveDays", $.proxy(this.onSaveDays, this));
			$(window).on("CalendarController:saveStandBy", $.proxy(this.onSaveStandBy, this));

			$(window).on("CalendarView:deleteDay", $.proxy(this.onDeleteDay, this));
			$(window).on("CalendarView:validateCra", $.proxy(this.onValidateCra, this));
			$(window).on("CalendarView:invalidateCra", $.proxy(this.onInvalidateCra, this));
			$(window).on("CalendarView:saveComment", $.proxy(this.onSaveComment, this));
			$(window).on("CalendarView:deleteComment", $.proxy(this.onDeleteComment, this));
		},
		/* Event response*/
		onLoadData: function(event, criterias){
			jsRoutes.controllers.Cras.fetch(criterias.trigramme, criterias.year, criterias.month)
				.ajax({
					context : this
				})
				.done(function(cra, textStatus, jqXHR){
					console.log("onLoadData", cra);
					this.setData(cra);
					$(window).trigger("CalendarModel:dataLoaded", cra);
				});
		},
		onSaveDays: function(event, newCra){
			console.log("onSaveDays",newCra);
			jsRoutes.controllers.Cras.save().ajax({
				data: JSON.stringify(newCra),
				context: this
			})
			.done(function(cra, textStatus, jqXHR){
				console.log("onSaveDays");
				console.log(cra);
				this.setData(cra);
				$(window).trigger("CalendarModel:daysSaved", cra);
			})
			.fail(function(jqXHR, settings, errorThrown){

			});
		},
		onSaveStandBy: function(event, standBy){
			console.log("CalendarModel.onSaveStandBy")
			console.log(standBy);
			jsRoutes.controllers.Cras.saveStandBys().ajax({
				data: JSON.stringify(standBy),
				context: this
			})
			.done(function(cra, textStatus, jqXHR){
				console.log("onSaveStandBys");
				console.log(cra);
				this.setData(cra);
				$(window).trigger("CalendarModel:standBySaved", cra);
			})
			.fail(function(jqXHR, settings, errorThrown){

			});
		},
		onDeleteDay: function(event, data){
			var cra = this.getData(),
				trigramme = cra.employee.trigramme,
				date = cra.days[data.index].date.split("/"),
				year = date[2],
				month = date[1],
				day = date[0];

			jsRoutes.controllers.Cras.deleteDay(trigramme, year, month, day)
			.ajax({
				context: this
			})
			.done(function(cra, textStatus, jqXHR){
				console.log("onDeleteDay");
				console.log(cra);
				this.setData(cra);
				$(window).trigger("CalendarModel:dayDeleted", cra);
			});
		},
		onValidateCra: function(event, data){
			jsRoutes.controllers.Cras.validate(this.getData().id).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				this.getData().isValidated = true;
				$(window).trigger("CalendarModel:craValidated", this.getData());
			});
			return this;
		},
		onInvalidateCra: function(event,data){
			jsRoutes.controllers.Cras.invalidate(this.getData().id).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				this.getData().isValidated = false;
				$(window).trigger("CalendarModel:craInvalidated", this.getData());
			});
			return this;	
		},
		onSaveComment: function(event, comment){
			jsRoutes.controllers.Cras.saveComment()
			.ajax({
				data: JSON.stringify(comment),
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				// Mise à jour du modele
				this.getData().comment = comment.comment;
				$(window).trigger("CalendarModel:commentSaved", this.getData());
			});
		},
		onDeleteComment: function(event, data){
			jsRoutes.controllers.Cras.deleteComment(data.id)
			.ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
				// Mise à jour du modele
				this.getData().comment = null;
				$(window).trigger("CalendarModel:commentDeleted", this.getData());
			});
		}
	});
	/** CalendarView */
	var CalendarView = window.App.CalendarController.CalendarView = function(readOnly){
		var _readOnly = readOnly || false;
		this.$el	   = $("#craResult");
		this.$calendar = this.$el.find("#calendar");
		this.$toolbar = this.$el.find("#toolbar");
		this.$counters = null;
		this.$printInfos = this.$el.find("#printInfos");

		this.$legend = this.$el.find("#legend");
		this.$legendBtn = this.$legend.find("#legendBtn");
		this.$legendContent = this.$legend.find("#legendContent");

		this.elementsRendered = [];
		/* Compilation du template */
		this.template = Handlebars.compile($("#calendar-template").html());
		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
		this.isReadOnly = function(){
			return _readOnly;
		};
		this.setReadOnly = function(readOnly){
			_readOnly = readOnly;
		};
	};
	_.extend(CalendarView.prototype, {
		bindEvents: function(){
			$(window).on("CalendarController:closeDay", $.proxy(this.onCloseDay, this));
			$(window).on("CalendarModel:dataLoaded CalendarModel:dayDeleted CalendarModel:daysSaved CalendarModel:standBySaved", $.proxy(this.render, this));
			$(window).on("CalendarModel:craValidated", $.proxy(this.onCraValidated, this));
			$(window).on("CalendarModel:craInvalidated", $.proxy(this.onCraInvalidated, this));
			$(window).on("CalendarModel:commentSaved", $.proxy(this.onCommentSaved, this));
			$(window).on("CalendarModel:commentDeleted", $.proxy(this.onCommentDeleted, this));
		},
		/* Event response*/
		onCloseDay: function(event, data){
			this.uncheckAll();
		},
		onCraValidated: function(event, cra){
			this.setReadOnly(true);
			this.render(null, cra);
		},
		onCraInvalidated: function(event, cra){
			this.setReadOnly(false);
			this.render(null, cra);
		},
		onCommentSaved: function(event, cra){
			this.setReadOnly(false);
			this.renderToolbar(cra);
		},
		onCommentDeleted: function(event, cra){
			this.setReadOnly(false);
			this.renderToolbar(cra);
		},
		/* Renders */
		render: function(event, cra){
			this.setReadOnly(cra.isValidated);
			var weeksId = [];
			for(var i = 0, len=(cra.days.length / 7); i <= len-1; i++){
				weeksId.push({"id":i});
			}
			this.$calendar.empty().append(this.template({"weeks":weeksId}));
			this.$counters = this.$calendar.find("#counters");

			if(this.isReadOnly()){
				this.$calendar.find('input[type=checkbox]').remove();
			}
			this
				.renderWeek(cra, this.isReadOnly())
				.renderDays(cra.days, this.isReadOnly())
				.renderToolbar(cra, this.isReadOnly())
				.renderCounters(cra.dayCounter, this.isReadOnly())
				.renderForPrint(cra.employee, cra.month.label, cra.year, this.isReadOnly())
				.attachEvents(cra, this.isReadOnly())
				.show();
		},
		show: function(){
			this.$el.show();
			if(this.$toolbar) {
				this.$toolbar.show();
			}
			return this;
		},
		renderDays: function(days, readOnly){
			_.each(days, function(day, index, list){
				this.renderDay(day, index, readOnly);
			}, this);			
			return this;
		},
		renderToolbar: function(cra, readOnly){
			if(this.$toolbar) {
				if(cra.isLocked){
					this.$toolbar.find("#validate").find(".btn").prop("disabled", true);
					this.$toolbar.find("#invalidate").find(".btn").prop("disabled", true);
				} 
				if(cra.isValidated){
					this.$toolbar.find("#validate").hide();
					this.$toolbar.find("#invalidate").show();
				} else {
					this.$toolbar.find("#validate").show();
					this.$toolbar.find("#invalidate").hide();
				}
				if(readOnly){
					this.$toolbar.find("#btn-comment").prop('disabled', (cra.comment ? false : true));
				} else {
					this.$toolbar.find("#btn-comment").prop('disabled', false);
				}
				this.$toolbar.find("#btn-comment").toggleClass("btn-info", (cra.comment ? true : false));

				jsRoutes.controllers.Cras.fetchCustomers(cra.employee.trigramme, cra.year, cra.month.value)
					.ajax({
						context: this
					})
					.done(function(customers, textStatus, jqXHR){
						var m = _.map(customers, function(customer, index, list){
							return $(document.createElement("li"))
									.append($(document.createElement("a"))
										.addClass("exportCraCustomer")
										.attr("data-customer-id", customer.id)
										.attr("data-customer-name", customer.name)
										.attr("href", "#")
										.text(_.capitalize(customer.name))
									);
						}, this);
						this.$toolbar.find('#printCustomerList').empty().append(m);
					});
			}
			return this;
		},
		renderCounters: function(counter, readOnly){
			_.each(counter, function(count, key, list){
				this.$counters.find("#" + key).empty().text(count);
			}, this);
			return this;
		},
		renderForPrint : function(employee, month, year){
			var employeeName = _.join(" ", _.capitalize(employee.firstName), _.capitalize(employee.lastName));
			var monthYear = _.join(" ", _.capitalize(month), year);
			this.$printInfos.empty().append(_.join(" - ", employeeName, monthYear));
			return this;
		},
		renderWeek: function(cra, readOnly){
			// Week number
			var firstWeekDays = _.filter(cra.days, function(day, index, list){
				return index % 7 === 0;
			});
			_.each(firstWeekDays, function(firstDays, index, list){
				this.$calendar.find("#weekDay_" + index).empty()
					.append(firstDays.week);
				// StandBy button
				if(cra.employee.showStandBy){
					var $standBy = $(document.createElement("i"))
					.addClass("icon-time pull-left standBy")
					.attr("data-original-title", "Astreinte")
					.attr("rel", "tooltip")
					.attr("data-week", firstDays.week)
					.tooltip()
					if(this.standBy[firstDays.week]){
						$standBy.addClass("hightlight")
					}
					this.$calendar.find("#weekDay_" + index)
						.append("<br/>")
						.append($standBy);
				}	
			}, {"$calendar": this.$calendar, "standBy": cra.standBys});

			return this;
		},
		renderDay: function(day, index, readOnly){
			var $header	= this.$calendar.find("#date_" + index);
			var $body	  = this.$calendar.find("#day_" + index)
			
			return this
				.renderDayHeader($header, day, index, readOnly)
				.renderDayBody($body, day, index, readOnly);
		},
		renderDayHeader: function($header, day, index, readOnly){
			return this
				.renderDayHeaderClasses($header, day, readOnly)
				.renderDayHeaderContent($header, day, index, readOnly);
		},
		renderDayHeaderClasses: function($header, day, readOnly){
			/* reset des classes */
			$header.removeClass().addClass("dayHeader");
			/* Calcul des classes */
			var classes = [];
			if(day.isInPastOrFuture){
				classes.push("pastOrFuture");
			}
			if(day.isDayOff){
				classes.push("dayOff");	
			}
			if(day.isSaturday){
				classes.push("saturday");
			} else if(day.isSunday){
				classes.push("sunday");
			} else {
				classes.push("date");
			}
			$header.addClass(classes.join(" "));
			return this;
		},
		renderDayHeaderContent: function($header, day, index, readOnly){
			/* reset de la zone */
			$header.empty();
			/* Calcul du contenu */
			if(day.isInPastOrFuture){
				$header.append(day.date);
			} else{
				$header.append(day.date);

				if(!readOnly) {
					var $chk = $(document.createElement("input"))
						.attr("data-index", index)
						.attr("type", "checkbox")
						.addClass("dayChk noPrint");
					if(day.isSaturday) {
						$chk.addClass("saturday");
					}
					if(day.isSunday) {
						$chk.addClass("sunday");
					}
					if(day.isSaturday) {
						$chk.addClass("saturday");
					}
					if(day.isDayOff) {
						$chk.addClass("dayOff");
					}
					$header.prepend($chk);

					if(day.morning || day.afternoon){
						var $trash = $(document.createElement("i"))
							.addClass("pull-right icon-trash deleteDay noPrint")
							.attr("data-original-title","Effacer la journée")
							.attr("data-index", index)
							.tooltip()
						$header.append($trash);
					}
				}
			}
			return this;
		},
		renderDayBody: function($body, day, index, readOnly){
			var $morning   = $body.find("#day_" + index + "_m");
			var $afternoon = $body.find("#day_" + index + "_a");
			return this
				.renderDayBodyClasses($body, day, readOnly)
				.renderDayBodyContent($body, day, readOnly)
				.renderHalfDay($morning, day.morning, index, readOnly)
				.renderHalfDay($afternoon, day.afternoon, index, readOnly);
		},
		renderDayBodyClasses: function($body, day, readOnly){
			/* reset des classes */
			$body.removeClass().addClass("dayBody");
			/* Calcul des classes */
			if(day.isInPastOrFuture){
				$body.addClass("pastOrFuture");
			}
			return this;
		},
		renderDayBodyContent: function($body, day, readOnly){
			if(day.morning || day.afternoon){
				$body
					.attr("rel","popover")
					.attr("data-original-title","<strong>" + day.date + "</strong>")
					.attr("data-html","true")
					.attr("data-trigger","hover")
					.attr("data-placement","top");
				/** Popover */
				$($body)
					.popover({
						"content":function(){
							var morningMsg = "<strong>Matin:</strong> ",
								afternoonMsg = "<strong>Après midi:</strong> ";
							if(day.morning){
								if(day.morning.isSpecial){
									morningMsg = morningMsg + "<ul>" + _.map(day.morning.periods, function(period, index, list){
											return "<li class='specialDay'>" + period.mission.customerMissionName + "(" + period.startTime + " -> " + period.endTime + ")" + "</li>";
										}).join('<br>') + "</ul>";
								} else {
									morningMsg = morningMsg + day.morning.mission.customerMissionName;
								}
							}
							if(day.afternoon){
								if(day.afternoon.isSpecial){
									morningMsg = morningMsg + "<ul>" + _.map(day.afternoon.periods, function(period, index, list){
											return "<li class='specialDay'>" + period.mission.customerMissionName + "(" + period.startTime + " -> " + period.endTime + ")" + "</li>";
										}).join('<br>') + "</ul>";
								} else {
									afternoonMsg = afternoonMsg + day.afternoon.mission.customerMissionName;
								}
							}
							return morningMsg + "<br>" + afternoonMsg;
						}
					})
					.on("click",function(event) {
						event.preventDefault()
					});
			}
			return this;
		},
		renderHalfDay: function($halfday, halfday, index, readOnly){
			return this
				.renderHalfDayClasses($halfday, halfday, readOnly)
				.renderHalfDayContent($halfday, halfday, index, readOnly);
		},
		renderHalfDayClasses: function($halfday, halfday, readOnly){
			/* reset des classes */
			$halfday.removeClass();

			if(!halfday){
				return this;
			}
			/* Calcul des classes */
			if(halfday.isSpecial){
				$halfday.addClass("special");
			} else {
				switch(halfday.mission.missionType){
					case "CUSTOMER":
						$halfday.addClass("customer");
						break;
					case "INTERNAL_WORK":
						$halfday.addClass("internalWork");
						break;
					case "PRESALES":
						$halfday.addClass("presales");
						break;
					case "HOLIDAY":
						$halfday.addClass("holiday");
						break;
					case "UNPAID":
						$halfday.addClass("unpaid");
						break;
				}
			}
			return this;
		},
		renderHalfDayContent: function($halfday, halfday, index, readOnly){
			/*  reset de la zone */
			$halfday.empty();

			if(!halfday){
				return this;
			}
			
			/* Calcul du contenu */
			$halfday.attr("data-index", index);
			if(halfday.isSpecial){
				$halfday.addClass("special");
			} else {
				if(halfday.mission){
					if(halfday.mission.missionType === "CUSTOMER"){
						$halfday.append(_(halfday.mission.code + "/" + halfday.mission.customerName).truncate(10));
					} else {
						$halfday.append(halfday.mission.code);
					}
				}
			}
			return this;
		},
		/* Events */
		attachEvents: function(cra, readOnly){
			this
				.attachCalendarEvents(cra, readOnly)
				.attachToolbarEvents(cra, readOnly);
			return this;
		},
		attachCalendarEvents: function(cra, readOnly){
			/* attach events */
			this.$calendar.off("click");
			if(!(cra.isValidated || cra.isLocked)) {
				this.$calendar
					.on("click", {instance:this}, function(event){
						var $target = $(event.target),
							$calendar = event.data.instance.$calendar;

						event.data.instance
							.handleDeleteDayEvt($target, readOnly)
							.handleStandByEvt($target, readOnly)
							.handleCheckboxesEvt($target, $calendar, readOnly)
							.handleDayBodyEvent(cra, $target, $calendar, readOnly);
					});
			} else {
				this.$calendar
					.on("click", {instance:this}, function(event){
						var $target = $(event.target),
							$calendar = event.data.instance.$calendar;

						event.data.instance
							.handleStandByEvt($target, readOnly)
					});
			}
			this.$legendBtn
				.off("click")
				.on("click", {instance: this}, function(event){
					event.data.instance.$legendContent.fadeToggle("slow");
				});
			return this;
		},
		attachToolbarEvents: function(cra, readOnly){
			if(this.$toolbar) {
				this.$toolbar.find("#validate").find(".btn")
					.off("click")
					.on("click", function(event){
						event.preventDefault();
						var msg = "Êtes-vous sur de vouloir valider votre CRA ?";
						bootbox.confirm(msg, function(result) {
							if (result) {
								$(window).trigger("CalendarView:validateCra", {});
							}
						});
					});
				this.$toolbar.find("#invalidate").find(".btn")
					.off("click")
					.on("click", function(event){
						event.preventDefault();
						var msg = "Êtes-vous sur de vouloir invalider votre CRA ?";
						bootbox.confirm(msg, function(result) {
							if (result) {
								$(window).trigger("CalendarView:invalidateCra", {});
							}
						});
					});
				this.$toolbar.find("#btn-comment")
					.off("click")
					.on("click", {instance:this}, function(event) {
						App.commentDialog("Commentaire pour le CRA de " + cra.month.label + " " + cra.year, cra.comment ,function(result){
							var evt,
								data = {"id": cra.id};
							switch (result.action){
								case "save":
									data.comment = result.comment;
									evt = "CalendarView:saveComment";
									break;
								case "delete":
									evt = "CalendarView:deleteComment";
									break;
							}
							if(evt){
								$(window).trigger(evt, data);
							}
						}, event.data.instance.isReadOnly());
						
					});
				this.$toolbar.find("#btn-print-complete")
					.off("click")
					.on("click", function(event) {
						event.preventDefault();
						window.open(jsRoutes.controllers.Exports.craEmployee("pdf", cra.employee.trigramme, cra.year, cra.month.value).url);
					});
				this.$toolbar
					.off("click",".exportCraCustomer")
					.on('click', ".exportCraCustomer", function(event){
						event.preventDefault();
						window.open(jsRoutes.controllers.Exports.craCustomer("pdf", cra.employee.trigramme, cra.year, cra.month.value, $(this).attr("data-customer-id")).url);
					});
				this.$toolbar.find("#btn-fees")
					.off("click")
					.on("click", function(event){
						event.preventDefault();
						$(window).trigger("CalendarView:openCraFees", {"trigramme": cra.employee.trigramme, "year": cra.year, "month": cra.month});
					});
			}
			return this;
		},
		handleDeleteDayEvt: function($target, readOnly){
			if($target.hasClass("deleteDay")){
				var msg = "Êtes-vous sur de vouloir supprimer cette journée ?";
				bootbox.confirm(msg, function(result) {
					if (result) {
						var index = $target.parents("td").attr("data-index");
						$(window).trigger("CalendarView:deleteDay", {index: index});
					}
				});
			}
			return this;
		},
		handleStandByEvt: function($target, readOnly){
			if($target.hasClass("standBy")){
				var week = $target.attr("data-week")
				$(window).trigger("CalendarView:editStandBy", week);
			}
			return this;
		},
		handleCheckboxesEvt: function($target, $calendar, readOnly){
			if($target.attr("type") === "checkbox") {
				var $elements = [],
					toChecked = $($target).is(":checked");
				if($target.attr("id") === "monthChk"){
					$elements = $calendar.find("#monthChk, .weekChk, .dayChk").not(".saturday, .sunday, .dayOff");
				} else if($target.hasClass("weekChk")){
					$elements = $target.parents("tr").find(".weekChk, .dayChk").not(".saturday, .sunday, .dayOff")
				}
				_.each($elements, function(element, index, list){
					var $element = $(element);
					if($element.hasClass('dayChk')) {
						if(!$element.siblings('.icon-trash').length){
							$element.attr("checked", this.valueOf());
						}
					} else {
						$element.attr("checked", this.valueOf());
					}
				}, toChecked);
			}
			return this;
		},
		handleDayBodyEvent: function(cra, $target, $calendar, readOnly){
			var $element = null,
				$parent = null,
				index = 0;
			if($target.hasClass("dayBody") &&  !$target.hasClass("pastOrFuture")){ /* TD */
				$element = $target;
				$parent = $target;

			} else if($target.parent("td").hasClass("dayBody") &&  !$target.parent("td").hasClass("pastOrFuture")){ /* DIV */
				$element = $target;
				$parent = $target.parent();
			}
			if($element){
				var index = $element.attr("data-index");
				this.checkDay(index);
				var indexes = {};
				if(this.isReadOnly()){
					indexes = {"indexes":[index]}
				} else {
						indexes = {"indexes":this.getSelectedDaysIndexes()}
				}
				$(window).trigger("CalendarView:onEditDays", indexes);
			}
			return this;
		},
		getSelectedDaysIndexes: function(){
			var days = _.filter(this.$calendar.find(".dayChk"), function(day){
				return $(day).is(":checked");
			});
			return _.map(days, function(day, index, list){
				return $(day).attr("data-index");
			});
		},
		/**
		 * Check all corresponding checkbox
		 */
		checkDay: function(index){
			this.$calendar.find(".dayChk").filter("[data-index=\"" + index + "\"]").attr("checked", true)
		},
		/**
		 * Uncheck all checkbox
		 */
		uncheckAll: function(){
			this.$calendar.find("#monthChk, .weekChk, .dayChk").attr("checked", false);
		}
	});
})(window);