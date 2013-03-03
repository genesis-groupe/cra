/* day.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** Day */
	var Day = window.App.Day = function(){
		var _data = {};
		var _isSpecial = false;

		this.$el = $("#day").modal({
			backdrop: "static",
			keyboard: true,
			show: false
		})
		.on("hide", function(){
			$(window).trigger("Day:close", {});
		});

		this.$year			 = this.$el.find("#year");
		this.$month			= this.$el.find("#month");
		this.$days			 = this.$el.find("#days");
		this.$missionLists	 = this.$el.find(".missionList");
		this.$tabNormal		= this.$el.find("a[href=\"#tabPaneNormal\"]");
		this.$tabSpecial	   = this.$el.find("a[href=\"#tabPaneSpecial\"]");
		this.$saveBtn		  = this.$el.find("#saveBtn");
		this.$formNormal	   = this.$el.find("#formNormal");
		this.$formSpecial	  = this.$el.find("#formSpecial");
		this.$radioPeriodMorning  = this.$formSpecial.find("#periodMorning");
		this.$radioPeriodAfternoon  = this.$formSpecial.find("#periodAfternoon");
		this.$radioPeriodSpecial = this.$formSpecial.find("#periodSpecial");

		this.$overlapPeriodLine = this.$formSpecial.find("#overlapPeriodLine");
		this.$overlapPeriod = this.$formSpecial.find("#overlapPeriod");
		this.$overlapPeriodMsg = this.$formSpecial.find("#overlapPeriodMsg");
		this.$closeOverlapPeriod  = this.$formSpecial.find("#closeOverlapPeriod");

		this.elementsToRender = [];

		this.$formNormal.on("submit", {instance: this}, function(event){
			event.preventDefault();
			var day = event.data.instance.retrieveData();
			$(window).trigger("Day:saveDays", day);
		});

		this.$formSpecial.on("submit", {instance: this}, function(event){
			event.preventDefault();
			event.data.instance.$overlapPeriodLine.hide();

			var instance = event.data.instance,
				data = $(this).serializeObject(),
				mission = instance.$formSpecial.find("#specialMission").find("option:selected"),
				tableData = instance.$tableSpecialPeriods.fnGetData(),
				msg;
			/* Détection des chevauchemetns et inversements d"heure */
			switch(data.periodType){
				case "MORNING":
					var r = _.filter(tableData, function(line){
						return (line.startTime < "12:00:00");
					});
					if(r.length){
						msg = "Chevauchement interdit : des périodes sont déjà saisies le matin.";
					}
					if(!msg){
						var r = _.filter(tableData, function(line){
							return (line.halfDayType === "MORNING");
						});
						if(r.length){
							msg = "Chevauchement interdit : le matin est déjà saisie.";
						}
					}
					break;
				case "AFTERNOON":
					var r = _.filter(tableData, function(line){
						return (line.startTime > "11:59:59");
					});
					if(r.length){
						msg = "Chevauchement interdit : des périodes sont déjà saisies l'après-midi.";
					}
					if(!msg){
						var r = _.filter(tableData, function(line){
							return (line.halfDayType === "AFTERNOON");
						});
						if(r.length){
							msg = "Chevauchement interdit : l'après-midi est déjà saisie.";
						}
					}
					break;
				case "SPECIAL":
					if(data.startTimePeriodSpecial === data.endTimePeriodSpecial || data.startTimePeriodSpecial > data.endTimePeriodSpecial) {
						msg = "L'heure de début doit être inférieur à l'heure de fin";
					} else if(data.startTimePeriodSpecial < "12:00:00"){
						var r = _.filter(tableData, function(line){
							return (line.halfDayType === "MORNING" && !line.startTime);
						});
						if(r.length){
							msg = "Chevauchement interdit : le matin est déjà saisie.";
						}
					} else if(data.startTimePeriodSpecial > "11:59:59"){
						var r = _.filter(tableData, function(line){
							return (line.halfDayType === "AFTERNOON" && !line.startTime);
						});
						if(r.length){
							msg = "Chevauchement interdit : l'après-midi est déjà saisie.";
						}
					} else if(data.startTimePeriodSpecial < "12:00:00" && data.endTimePeriodSpecial > "12:00:00"){
						var r = _.filter(tableData, function(line){
							return (line.halfDayType === "AFTERNOON" || line.halfDayType === "MORNING"  && !line.startTime);
						});
						if(r.length){
							msg = "Chevauchement interdit : le matin et l'après-midi sont déjà saisis.";
						}
					} else {
						var r = _.filter(tableData, function(line){
							return line.startTime < this.start && this.end < line.endTime 
								|| this.start < line.startTime && line.endTime < this.end
								|| this.start < line.startTime && line.startTime < this.end
								|| this.start < line.endTime && line.endTime < this.end
								|| line.startTime === this.start
								|| line.endTime === this.start
								|| line.startTime === this.end
								|| line.endTime === this.end
						},{start:data.startTimePeriodSpecial ,end:data.endTimePeriodSpecial});
						if(r.length){
							msg = "Chevauchement interdit : Une période existe déjà dans cette intervalle de temps.";
						}
					}
					break;
			}
			if(msg){
				instance.$overlapPeriodMsg.empty().append(msg);
				instance.$overlapPeriod.removeClass().addClass("label label-important");
				instance.$overlapPeriodLine.slideDown("slow", function(){
					setTimeout(function(){
						instance.$overlapPeriodLine.hide()
					},5000);
				});
			} else {
				var periods = [];
				if(data.periodType === "SPECIAL" && data.startTimePeriodSpecial < "12:00:00" && data.endTimePeriodSpecial > "12:00:00"){
					periods.push({
							"idMission": data.specialMission,
							"label": mission.text(),
							"halfDayType": "MORNING",
							"startTime": data.startTimePeriodSpecial,
							"endTime": "12:00:00",
							"isSpecial":true
						},
						{
							"idMission": data.specialMission,
							"label": mission.text(),
							"halfDayType": "AFTERNOON",
							"startTime": "12:00:00",
							"endTime": data.endTimePeriodSpecial,
							"isSpecial":true
						}
					);
					instance.$overlapPeriodMsg.empty().append("La période a été découpée en 2 périodes");
					instance.$overlapPeriod.removeClass().addClass("label label-warning");
					instance.$overlapPeriodLine.slideDown("slow", function(){
						setTimeout(function(){
							instance.$overlapPeriodLine.hide()
						},5000);
					});
				} else {
					var halfDayType;
					if(data.periodType === "SPECIAL"){
						if(data.endTimePeriodSpecial <= "12:00:00") {
							halfDayType = "MORNING";
						} else {
							halfDayType = "AFTERNOON";
						}
					} else {
						halfDayType = data.periodType;
					}
					periods.push({
						"idMission": data.specialMission,
						"label": mission.text(),
						"halfDayType": halfDayType,
						"startTime": data.startTimePeriodSpecial || null,
						"endTime": data.endTimePeriodSpecial || null,
						"isSpecial": data.periodType === "SPECIAL"
					});
				}
				_.each(periods, function(period,index,list){
					this.$tableSpecialPeriods.fnAddData(period);
				}, instance)
			}
		});

		this.$tableSpecialPeriods = this.$el.find("#tableSpecialPeriods").dataTable({
			"sDom": "rt",
			"aaSorting": [[2,"asc"]],
			"bPaginate": false,
			"bSort": true,
			"bInfo": false,
			"bFilter": false,
			"bProcessing": true,
			"oLanguage": App.I18N_DATA_TABLE,
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull){
				$(nRow).removeClass("specialPeriodProperties");
				if(aData.isSpecial){
					$(nRow).addClass("specialPeriodProperties");
				}
			},
			"aoColumnDefs": [
				{
					"aTargets": [0],
					"mData"   : "idMission",
					"mRender" : function(data, type, full){
						return _(full.label).prune(30);
					}
				},
				{
					"aTargets": [1],
					"mData"   : "halfDayType",
					"mRender" : function(data, type, full){
						if(type === "display"){
							switch (data){
								case "MORNING":
									return "Matin";
									break;
								case "AFTERNOON":
									return "Après-midi";
									break;
								default:
									return "";
							}
						} else {
							return data;
						}
					}
				},
				{
					"aTargets": [2],
					"mData"   : "startTime",
					"bSortable": true,
					"mRender" : function(data, type, full){
						if(type === "sort"){
								switch (full.halfDayType){
									case "MORNING":
										return "00:00:00";
										break;
									case "AFTERNOON":
										return "12:00:00";
										break;
									default:
										return data
								}
						} else {
							return data;
						}
					}
				},
				{
					"aTargets": [3],
					"mData"   : "endTime",
					"bSortable": true,
					"mRender" : function(data, type, full){
						if(type === "sort"){
								switch (full.halfDayType){
									case "MORNING":
										return "11:59:59";
										break;
									case "AFTERNOON":
										return "23:59:59";
										break;
									default:
										return data
								}
						} else {
							return data;
						}
					}
				},
				{ 
					"aTargets": [4],
					"mData"   : "idMission",
					"mRender" : function (data, type, full) {
						var $img = $(document.createElement("i"))
									.addClass("icon-trash");
						var $btn = $(document.createElement("button"))
									.addClass("btn btn-danger btn-mini removeLine")
									.html($img);
						return $btn[0].outerHTML;
					 },
					 "bSortable": false
				}
			]
		});

		this.bindEvents();

		this.getData= function(){
			return _data;
		};
		this.loadData = function(cra){
			_data.day = $.extend(true, {}, _.first(_.sortBy(cra.days, "date")));
			_data.dates = _.pluck(cra.days, "date");
			_data.cra = $.extend(true,{},cra);
			_data.employee = $.extend(true,{},cra.employee);
			var morning = _data.day.morning,
				afternoon = _data.day.afternoon,
				isMorningSpecial = morning && morning.isSpecial,
				isAfternoonSpecial = afternoon && afternoon.isSpecial;
			_isSpecial= isMorningSpecial || isAfternoonSpecial;
			return this;
		};
		this.isSpecial = function(){
			return _isSpecial;
		};
		this.setSpecial = function(isSpecial){
			_isSpecial = isSpecial;
		};
	};
	_.extend(Day.prototype, {
		bindEvents: function(){
			$(window).on("CalendarController:editDays", $.proxy(this.init, this));
			$(window).on("Day:renderElement", $.proxy(this.show, this));
			$(window).on("CalendarController:daysSaved", $.proxy(this.onDaysSaved, this));
		},
		init: function(event, cra){
			this.elementsToRender = _.clone(DAY_VIEW_ELEMENTS.list);

			this
				.loadData(cra)
				.clean()
				.attachEvents()
				.handleEvents()
				.render(this.getData());
			return this;
		},
		onDaysSaved: function(event, data){
			this.$el.modal("hide");
			return this;
		},
		clean: function(){
			this.$tableSpecialPeriods.fnClearTable();
			this.$formSpecial.find("#startTimePeriodSpecial").val("");
			this.$formSpecial.find("#endTimePeriodSpecial").val("")
			return this;
		},
		render: function(data){
			this
				.renderMissions(data)
				.renderTitle(data)
				.renderTab(data.day)
				.renderFormSpecial()
				.renderTableSpecial(data.day);
			return this;
		},
		renderMissions: function(data){
			jsRoutes.controllers.Employees.fetchMissions(data.employee.trigramme, data.cra.year, data.cra.month.value)
				.ajax({
					context: {"instance":this,"data":data}
				})
				.done(function(affectedMissions, textStatus, jqXHR){
					var $spanHoliday = this.instance.$formNormal.find('span[id$="Mission"]'); 
					if($spanHoliday.length != 0){
						$spanHoliday.replaceWith(
							$(document.createElement("select"))
								.addClass("input-xlarge missionList")
								.attr('name', $spanHoliday.attr('id'))
								.attr('id', $spanHoliday.attr('id'))
								.attr('data-halfType', _($spanHoliday.attr('id')).strLeft('Mission').toUpperCase())
							);
						this.instance.$missionLists	= this.instance.$el.find(".missionList");
					}

					var m = _.map(affectedMissions, function(affectedMission, index, list){
							return $(document.createElement("option"))
									.val(affectedMission.mission.id)
									.text(_.capitalize(affectedMission.mission.customerMissionName));
						}),
						morning = this.data.day.morning,
						afternoon = this.data.day.afternoon;

					this.instance.$missionLists.empty().append(App.createSelectPlaceholder({label:"<Activité>"})).append(m);
					
					_.each([{"halfDay": morning, "halfDayType":"MORNING"},{"halfDay": afternoon, "halfDayType":"AFTERNOON"}], function(element, index, list){
						var halfDay = element.halfDay;
						if(halfDay && !halfDay.isSpecial){
							if(halfDay.mission.missionType === "HOLIDAY" || halfDay.mission.code === "CSS"){
								this.$formNormal.find("select[data-halfType=\"" + element.halfDayType + "\"]")
									.replaceWith(
										$(document.createElement("span"))
											.append(_.capitalize(halfDay.mission.customerMissionName))
											.addClass("uneditable-input input-xlarge")
											.attr('id', element.halfDayType.toLowerCase() + "Mission")
											.attr('value', halfDay.mission.id)
								);
							} else {
								this.$formNormal.find("select[data-halfType=\"" + element.halfDayType + "\"]")
									.children("option[value=\"" + halfDay.mission.id + "\"]")
									.attr("selected","selected");							
							}
						}
					}, this.instance);
					
					$(window).trigger("Day:renderElement", DAY_VIEW_ELEMENTS.mission);
				});
			return this;
		},
		renderTitle: function(data){
			var cra   = data.cra,
				days = _.map(data.dates, function(date, key, list){
					return date.split("/")[0];
				})
				.join("-");

			this.$year.empty().append(cra.year);
			this.$month.empty().append(cra.month.label);
			this.$days.empty().append(days);
			$(window).trigger("Day:renderElement", DAY_VIEW_ELEMENTS.title);
			return this;
		},
		renderTab: function() {
			if(this.isSpecial()) {
				this.$tabSpecial.tab("show");
			} else {
				this.$tabNormal.tab("show");
			}
			$(window).trigger("Day:renderElement", DAY_VIEW_ELEMENTS.tab);
			return this;
		},
		renderFormSpecial: function(){
			if(this.isSpecial()) {
				this.$radioPeriodSpecial.trigger("click");
			}
			$(window).trigger("Day:renderElement", DAY_VIEW_ELEMENTS.formSpecial);
			return this;
		},
		renderTableSpecial: function(day){
			var halfDays = [];
			if(day.morning) {
				day.morning.halfDayType = "MORNING";
				halfDays.push(day.morning);
			}
			if(day.afternoon){
				day.afternoon.halfDayType = "AFTERNOON";
				halfDays.push(day.afternoon);
			}
			if(halfDays.length) {
				var	lines = _.flatten(
					_.map(halfDays, function(halfDay, index, list){
						if(halfDay.periods.length) {
							return _.map(halfDay.periods, function(period, index, list){
								return {
									"idMission": period.mission.id,
									"label": period.mission.customerMissionName,
									"halfDayType": this.halfDayType,
									"startTime": period.startTime || null,
									"endTime": period.endTime || null,
									"isSpecial": this.isSpecial
								};
							}, halfDay);
						} else {
							return {
								"idMission": halfDay.mission.id,
								"label": halfDay.mission.customerMissionName,
								"halfDayType": halfDay.halfDayType,
								"startTime": null,
								"endTime": null,
								"isSpecial": halfDay.isSpecial
							};
						}
					})
				);
				_.each(lines, function(line, index, list){
					this.fnAddData(line);
				}, this.$tableSpecialPeriods);
			}
			$(window).trigger("Day:renderElement", DAY_VIEW_ELEMENTS.tableSpecial);
			return this;
		},
		show: function(event, element){
			this.elementsToRender.splice(_.indexOf(this.elementsToRender,element),1);
			if (this.elementsToRender.length === 0){
				this.$el.modal("show");
			}
			return this;
		},
		handleEvents: function(){
			this
				.handleTabEvents();
			return this;
		},
		handleTabEvents: function(){
			this.$el
				.off("show")
				.on("show", "a[data-toggle=\"tab\"]", {instance: this}, function (event) {
					event.data.instance.setSpecial(event.target.id === "tabSpecial");
					/*TODO clear event.relatedTarget */
				});
			return this;
		},
		attachEvents: function(){
			this
				.attachSaveBtnEvent()
				.attachFormNormalRadioButtonEvent()
				.attachRemoveTableSpecialRowEvent()
				.attacheCloseOverlapPeriodEvent();
			return this;
		},
		attachSaveBtnEvent: function(){
			this.$saveBtn.off("click").on("click", {instance: this}, function(event){
				var data = event.data.instance.retrieveData();
				$(window).trigger("Day:saveDays", data);
			});
			return this;
		},
		attachFormNormalRadioButtonEvent: function(){
			this.$radioPeriodMorning.off("click").on("click", function(event){
				$("#startTimePeriodSpecial").val("").attr("disabled",true);
				$("#endTimePeriodSpecial").val("").attr("disabled",true);
			});
			this.$radioPeriodAfternoon.off("click").on("click", function(event){
				$("#startTimePeriodSpecial").val("").attr("disabled",true);
				$("#endTimePeriodSpecial").val("").attr("disabled",true);
			});
			this.$radioPeriodSpecial.off("click").on("click", function(event){
				$("#startTimePeriodSpecial").attr("disabled",false);
				$("#endTimePeriodSpecial").attr("disabled",false);
			});
			return this;
		},
		attachRemoveTableSpecialRowEvent: function(){
			this.$tableSpecialPeriods.off("click").on("click", ".removeLine", {instance: this}, function(event){
				event.preventDefault();
				var $trs = $(this).parents("tr");
				event.data.instance.$tableSpecialPeriods.fnDeleteRow($trs[0]);
			});
			return this;
		},
		attacheCloseOverlapPeriodEvent: function(){
			this.$closeOverlapPeriod.off("click").on("click", {instance: this}, function(event){
				event.preventDefault();
				event.data.instance.$overlapPeriodLine.hide();
				event.data.instance.$overlapPeriodMsg.empty();
			});
			return this;
		},
		retrieveData: function(){
			var data;
			if(this.isSpecial()){
				data = this.$tableSpecialPeriods.fnGetData();
			} else {
				data = {
					"morningMission": this.$formNormal.find('#morningMission').val(),
					"afternoonMission": this.$formNormal.find('#afternoonMission').val(),
				};
			}
			return _.omit(this.mergeDays(this.formatData(data)), ["fees", "standBys", "dayCounter","comment"]);
		},
		formatData: function(rawDays){
			var day = _.omit(this.getData().day, "morning", "afternoon");
			var table = [];
			if(this.isSpecial()){
				table = $.extend(true, [], rawDays);
				_.each(table, function(line, index, list){
					if(!this[line.halfDayType.toLowerCase()]){
						this[line.halfDayType.toLowerCase()] = Day.initHalfDay(line.isSpecial, line.halfDayType);
					}
				}, day);

				_.each(table, function(line, index, list){
					if(line.isSpecial){
						this[line.halfDayType.toLowerCase()].periods.push(Day.initPeriod(line.halfDayType, line.idMission, line.startTime, line.endTime));
					} else {
						this[line.halfDayType.toLowerCase()] = Day.initHalfDay(false, line.halfDayType, line.idMission);
					}
					
				},day);
			} else {
				if(rawDays.morningMission){
					table.push({"idMission":rawDays.morningMission, halfDayType: "MORNING"});
				}
				if(rawDays.afternoonMission){
					table.push({"idMission":rawDays.afternoonMission, halfDayType: "AFTERNOON"});
				}
				_.each(table, function(line, index, list){
					this[line.halfDayType.toLowerCase()] = Day.initHalfDay(false, line.halfDayType, line.idMission);
				},day);
			}

			return day;
		},
		mergeDays: function(formattedDay){
			var mergedCra = $.extend(true, {}, this.getData().cra);
			_.each(mergedCra.days,function(day, index, list){
				day.morning = this.morning;
				day.afternoon = this.afternoon;
			}, formattedDay);
			return mergedCra;
		}
	});
	Day.initHalfDay = function(isSpecial, halfDayType, idMission){
		var halfDay = {
			"halfDayType": halfDayType,
			"isSpecial": isSpecial
		};
		if(isSpecial){
			halfDay.periods = [];
		} else {
			halfDay.mission = {"id":idMission};
		}
		return halfDay;
	};
	Day.initPeriod = function(halfDayType, idMission, startTime, endTime){
		return {
			"startTime": startTime,
			"endTime": endTime,
			"mission": {
				"id": idMission
			}
		};
	};
	/** DAY_VIEW_ELEMENTS */
	var DAY_VIEW_ELEMENTS = window.App.Day.DAY_VIEW_ELEMENTS = {
		"title": "title",
		"mission": "mission",
		"tab": "tab",
		"formSpecial": "formSpecial",
		"tableSpecial": "tableSpecial",
		"list":[
			"title",
			"mission",
			"tab",
			"formSpecial",
			"tableSpecial"
		]
	};
})(window);
