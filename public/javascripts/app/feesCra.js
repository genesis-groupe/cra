/* feesCra.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** FeesCraController */
	var FeesCraController = window.App.FeesCraController = function(){
		var _model   = new FeesCraModel();
		var _view	= new FeesCraView();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesCraController.prototype,{
		bindEvents: function(){
			$(window).on("CalendarController:openCraFees ShowPay:openFees", $.proxy(this.onOpen, this));		
		},
		/* Event response*/
		onOpen: function(event, element){
			element.trigger = event.type;
			$(window).trigger("FeesCraController:loadCraFees", element);
		}
	});
	/** FeesCraModel */
	var FeesCraModel = window.App.FeesCraController.FeesCraModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesCraModel.prototype, {
		bindEvents: function(){
			$(window).on("FeesCraController:loadCraFees", $.proxy(this.onLoad, this));	
		},
		onLoad: function(event, data){
			var triggerer = data.trigger;
			jsRoutes.controllers.Cras.fetchFees(data.trigramme, data.year, data.month.value).ajax({
				context: {
					"year": data.year,
					"month": data.month
				}
			})
			.done(function(data, textStatus, jqXHR){
				console.log("Fees.fetch", data)
				if(triggerer === "ShowPay:openFees") {
					$(window).trigger("FeesCraModel:renderTable", {"context": this, "fees": data});	
				} else {
	        		$(window).trigger("FeesCraModel:craFeesLoaded", {"context": this, "fees": data});
	    		}
	        });
		}

	});
	/** FeesCraView */
	var FeesCraView = window.App.FeesCraController.FeesCraView = function(){
		this.$el = $('#fees').modal({
				show: false
			});
		this.$feesTable = $('#feesTable');
		this.$title = this.$el.find('#title')


		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(FeesCraView.prototype, {
		bindEvents: function(){
			$(window).on("FeesCraModel:renderTable", $.proxy(this.render, this));	
			$(window).on("FeesCraModel:craFeesLoaded", $.proxy(this.show, this));	
		},
		/* Event response*/
		show: function(event, data){
			this.render(null, data);
			var title = [data.context.month.label, data.context.year].join(" ");
			this.$title.empty().append(title);
			this.$el.modal("show");
		},
		render: function(event, data){
			var cra = data.context,
				fees = data.fees,
				totalByType = {},
				totalByWeek = {},
				lines = {},
				totalGlobal = 0,
				feesByWeek = _.groupBy(fees,"week"),
				feesByType = _.groupBy(fees, 'type'),
				feesByWeekByType = _.map(feesByWeek, function(feeByWeek, index, list){
					return {"week": index, "byType" : _.groupBy(feeByWeek, 'type')}
				});

			// Compute Data
			_.each(feesByWeekByType, function(elByWeek, idxbyWeek, listByWeek){
				totalByWeek[elByWeek.week] = 0;
				_.each(elByWeek.byType, function(elByType, idxBytype, listByType){
					if(!_.has(totalByType,idxBytype)){
						totalByType[idxBytype] = 0;
					}
					if(!_.has(lines,idxBytype)){
						lines[idxBytype] = {};
					}
					if(!_.has(lines[idxBytype],elByWeek.week)){
						lines[idxBytype][elByWeek.week] = 0
					}
					_.each(elByType, function(el, idx, lst){
						lines[el.type][el.week] = lines[el.type][el.week] + el.total;
						totalByWeek[el.week] += el.total;
						totalByType[el.type] =  totalByType[el.type] + el.total;
						totalGlobal += el.total;
					});
				});
			});

			// Create HTML
			var $table = $(document.createElement("table")).addClass("table table-striped table-condensed table-bordered"),
				$tHeader = $(document.createElement("thead")),
				$trHeader = $(document.createElement("tr")).append($(document.createElement("th"))
																		.text("Semaines")),
				$tBody = $(document.createElement("tbody")),
				$tFooter = $(document.createElement("tfoot")),
				$trFooter = $(document.createElement("tr")).append($(document.createElement("th"))
																		.text("Total"));
			var linesByType = {},
				sortedTypes = _.sortBy(_.keys(lines), function(lineType){
					return window.App.FEE_TYPE.getOrder(lineType);
				});
			_.each(sortedTypes, function(byType, byTypeIdx, byTypeList){
				linesByType[byType] = [];
				linesByType[byType].push($(document.createElement("th")).text(App.FEE_TYPE.getLabel(byType)));
				_.each(totalByWeek, function(byWeek, byWeekIdx, byWeekList){
					var val = (lines[byType][byWeekIdx])? numeral(lines[byType][byWeekIdx]).format('0.00 $') : ""
					linesByType[byType].push($(document.createElement("td")).text(val));
				});
			});

			
			_.each(linesByType, function(lineByType, rIdx, rLst){
				$tBody.append($(document.createElement("tr"))
									.append(lineByType) // Line by Type
									.append($(document.createElement("th")) // total By Type
												.text(numeral(totalByType[rIdx]).format('0.00 $'))));
			})

			// Header + Footer
			_.each(totalByWeek, function(td, idx, lst){
				$trHeader.append($(document.createElement("th")).text(idx))
				$trFooter.append($(document.createElement("th")).text(numeral(td).format('0.00 $')));
			});
			$trHeader.append($(document.createElement("th")).text("Total"));
			$trFooter.append($(document.createElement("th")).append($(document.createElement("span")).addClass("total").text(numeral(totalGlobal).format('0.00 $'))));
			$table.append($tHeader.append($trHeader)).append($tBody).append($tFooter.append($trFooter));
			this.$feesTable.empty().append($table);
			return this;
		}
	});
})(window);