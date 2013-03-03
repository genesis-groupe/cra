/* standby.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** StandBy */
	var StandBy = window.App.StandBy = function(){
		var _data = {};

		var _elementsToRender = [];
		
		this.$el = $('#standby').modal({
				backdrop: "static",
				keyboard: true,
				show: false
			})
			.on("hide", function(){
				$(window).trigger("StandBy:close", {});
			});
		this.$saveStandBy = this.$el.find("#saveStandBy")
			.on("click", {instance: this}, function(event){
				event.preventDefault();
				var $tables = event.data.instance.$el.find(".table");
				var unfilteredTables = _.map($tables, function(table, index, list){
						var chks = _.filter($(table).find('input[type="checkbox"]'), function(chk){
										return $(chk).is(":checked");
									});
						return _.map(chks, function(chk, index, list){
							return {
								"missionId": $(table).attr("data-missionid"),	
								"standByMomentIndex": $(chk).val()
							}
						});
					});
				_data.standBy = _.flatten(_.filter(unfilteredTables, function(table){
					return (table.length);
				}));
				console.log("StandBy.Submit")
				console.log(_data);
				$(window).trigger("StandBy:save", _data);
			});
		this.$title = this.$el.find("#title");
		this.$body = this.$el.find("#body");
		this.$missions = this.$el.find("#missions");

		this.bindEvents();

		this.loadData = function(data){
			console.log("StandBy.loadData", data);
			_data = data;
			return this;
		};
		this.getData = function(){
			return _data;
		};
		this.initElementsToRender = function() {
			_elementsToRender = _.clone(STANDBY_VIEW_ELEMENTS.list);
			return this;
		};
		this.readyToRender = function(element){
			_elementsToRender.splice(_.indexOf(_elementsToRender, element),1);
			return _elementsToRender.length === 0;
		};
	};
	_.extend(StandBy.prototype, {
		bindEvents: function(){
			$(window).on("CalendarController:editStandBy", $.proxy(this.init, this));
			$(window).on("CalendarController:standBySaved", $.proxy(this.onStandBySaved, this));
			$(window).on("StandBy:renderElement", $.proxy(this.show, this));
			$(window).on("StandBy:renderMissions", $.proxy(this.renderButtons, this));
		},
		onStandBySaved: function(event){
			this.$el.modal("hide");
			return this;
		},
		init: function(event, data){
			this
				.initElementsToRender()
				.loadData(data)
				.render(this.getData())

			return this;
		},
		render: function(data){
			this
				.renderTitle(data)
				.renderForm(data)
				.renderMissions(data)
			return this;
		},
		renderTitle: function(data){
			this.$title.empty().append("Astreinte semaine " + data.week)
			$(window).trigger("StandBy:renderElement", STANDBY_VIEW_ELEMENTS.title);
			return this;
		},
		renderForm: function(data){
			$(window).trigger("StandBy:renderElement", STANDBY_VIEW_ELEMENTS.form);

			return this;
		},
		renderMissions: function(data){
			jsRoutes.controllers.Employees.fetchMissionsWithStandby(data.trigramme, data.year, data.week)
				.ajax({
					context: this
				})
				.done(function(affectedMissions, textStatus, jqXHR){
					console.log(affectedMissions);
					// Construct tables
					var tables = _.map(affectedMissions, function(affectedMission, index, list){

						var ths = _.map(affectedMission.mission.standByMoments, function(standByMoment, index, list){
								var th = $(document.createElement("th"))
											.attr("data-index", standByMoment.index)
											.text(standByMoment.label);
								return th;				
							});
						var tds = _.map(affectedMission.mission.standByMoments, function(standByMoment, index, list){
							var chk = $(document.createElement("input"))
										.attr("type","checkbox")
										.attr("name", "standBy")
										.attr("id", "standBy")
										.attr("value", standByMoment.index)
										.attr("data-index", standByMoment.index)
										.attr("data-missionid", affectedMission.mission.id)
							var td = $(document.createElement("td"))
										.append(chk);
							return td;
							});
						var table = $(document.createElement("table"))
							.addClass("table table-striped table-condensed table-bordered")
							.attr("data-missionid",affectedMission.mission.id)
							.attr("name",affectedMission.mission.id)
							.append($(document.createElement("caption"))
								.append($(document.createElement("strong"))
									.text("Client : "))
								.append($(document.createElement("span"))
									.text(affectedMission.mission.customerName))
								.append($(document.createElement("strong"))
									.text(" - Mission : "))
								.append($(document.createElement("span"))
									.text(affectedMission.mission.code))
							)
							.append($(document.createElement("tr"))
								.append(ths))
							.append($(document.createElement("tr"))
								.append(tds));
						return table;
					});
					this.$body.empty().append((tables.length) ? tables : "Vous n'avez pas de missions sujettes aux astreintes.");
					// Check standBys
					_.each(this.getData().standBy, function(standBy, index, list){
						this.$body
							.find("table[data-missionid=" + standBy.missionId + "]")
							.find("input[type=checkbox][data-index=" + standBy.standByMomentIndex + "]")
							.attr("checked","checked")

					}, this);
					$(window).trigger("StandBy:renderElement", STANDBY_VIEW_ELEMENTS.missions);
					$(window).trigger("StandBy:renderMissions", data);
				});
			return this;
		},
		renderButtons: function(event, data){
			this.$saveStandBy.attr("disabled", data.readOnly)
			if(this.$el.find(".table").length){
				this.$saveStandBy.show();
			} else {
				this.$saveStandBy.hide();
			}
			$(window).trigger("StandBy:renderElement", STANDBY_VIEW_ELEMENTS.buttons);

			return this;
		},
		show: function(event, element){
			if(this.readyToRender(element)){
				this.$el.modal("show");
			}
			return this;
		},
		retrieveData: function(){
			return this;
		}
	});
	/** STANDBY_VIEW_ELEMENTS */
	var STANDBY_VIEW_ELEMENTS = window.App.StandBy.STANDBY_VIEW_ELEMENTS = {
		"title": "title",
		"missions": "missions",
		"form": "form",
		"buttons": "buttons",
		"list":[
			"title",
			"mission",
			"form",
			"buttons"
		]
	};
})(window);