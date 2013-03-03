/* Fees.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** FeesController */
	var FeesController = window.App.FeesController = function(){
		var _model   = new FeesModel();
		var _view	= new FeesView();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderFees", $.proxy(this.onRender, this));	
			$(window).on("ShowCreate:loadFeesHistory", $.proxy(this.onLoadHistory, this));
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("FeesController:init", element);
		},
		onLoadHistory: function(event, element){
			$(window).trigger("FeesController:loadHistory", element);
		}
	});
	/** FeesModel */
	var FeesModel = window.App.FeesController.FeesModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesModel.prototype, {
		bindEvents: function(){
			$(window).on("FeesView:submit", $.proxy(this.onSave, this));
			$(window).on("FeesController:loadHistory", $.proxy(this.onLoadHistory, this));	
			$(window).on("FeesView:deleteFee", $.proxy(this.onDeleteFee, this));
		},
		onSave: function(event, fee){
			jsRoutes.controllers.Fees.create().ajax({
	            data: JSON.stringify(fee),
				context: this
	        })
	        .done(function(data, textStatus, jqXHR){
	        	$(window).trigger("FeesModel:feeSaved", data);
	        	var date = moment(fee.date, "DD/MM/YYYY");
	        	var context = {
	        		"trigramme":fee.trigramme,
	        		"year": date.year(),
	        		"month": date.month() + 1
	        	};
	        	this.onLoadHistory(null, context);
	        });
		},
		onLoadHistory: function(event, context){
			jsRoutes.controllers.Fees.fetch(context.trigramme, context.year, context.month).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
	        	$(window).trigger("FeesModel:feesHistoryLoaded", {"fees": data});
	        });
		},
		onDeleteFee: function(event, fee){
			jsRoutes.controllers.Fees.delete().ajax({
				data: JSON.stringify(fee)
			})
			.done(function(data, textStatus, jqXHR){
				$(window).trigger("FeesModel:feeDeleted", data);
			});
		}
	});
	/** FeesView */
	var FeesView = window.App.FeesController.FeesView = function(){
		this.$el = $("#formFees");
		this.$searchForm = $('#searchForm');
		this.$date = this.$el.find('#date');
		this.feeTypes = {};
		this.$tableFees = $("#tableFees").dataTable({
			// Paramètre ajax Source
			"sAjaxDataProp": "fees",
			// Paramètres de tri
			"bSort": true,
			"aaSorting": [[1,"asc"]],
			//Paramètre pour le scroll vertical
			"sScrollY": "400px",
			"bScrollCollapse": true,
			// Paramètres généraux
			"oLanguage": App.I18N_DATA_TABLE,
			"bPaginate": false,			
			"bFilter": false,
			"bAutoWidth": false,
			"fnDrawCallback": function(oSettings){
				if(oSettings.fnRecordsTotal()){
					this.tooltip({"selector":"span[rel=tooltip]"});
				}
			},
			// Paramètre des colonnes
			"aoColumnDefs": [
				{
					"mData": "date",	
					"aTargets": [0],
					"bSortable": true,
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
								return moment(data, "DD/MM/YYYY").format("YYYYMMDD");
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "type" ,
					"aTargets": [1],
					"mRender" : function(data, type, full){
						switch(type) {
							case "sort" :
							case "display" :
								return FEE_TYPES[data];
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "mission" ,
					"aTargets": [2],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								if(data){
									return _.capitalize(data.customerMissionName);	
								} else {
									return "";
								}
							case "sort":
								return _.capitalize(data.customerMissionName);								
							default:
								return data;
						}	
					}
				},			
				{ 
					"mData": "kilometers",
					"aTargets": [3],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								if(data){
									var km =data + " Km";
									if(full.journey.length){
										var $tooltip = $(document.createElement("span"))
											.addClass("badge badge-info")
											.attr("data-original-title", full.journey)
											.attr("rel", "tooltip")
											.append(km);
										return $tooltip[0].outerHTML;
									} else {
										return km;
									}
								} else {
									return "";
								}								
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "amount",
					"aTargets": [4],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								if(data){
									return numeral(data).format('0.00 $');	
								} else {
									return "";
								}
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "description",
					"aTargets": [5],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								if(data.length > 10){
									var $badge = $(document.createElement("span"))
										.attr("data-original-title", data)
										.attr("rel", "tooltip")
										.append(_(data).truncate(10));
									return $badge[0].outerHTML;
								} else {
									return data;
								}
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "total",
					"aTargets": [6],
					"mRender" : function(data, type, full){
						switch(type) {
							case "display" :
								if(data){
									return numeral(data).format('0.00 $');	
								} else {
									return "";
								}
							default:
								return data;
						}
					}
				},
				{ 
					"mData": "date",
					"aTargets": [7],
					"bSortable": false,
					"mRender" : function(data, type, full){
						var firstDayOfMonth = moment().date(1).hours(0).minutes(0).seconds(0).milliseconds(0),
							feeDate = moment(data, "DD/MM/YYYY");
						switch(type) {
							case "display" :
								if(feeDate >= firstDayOfMonth) {
									var $img = $(document.createElement("i"))
											.addClass("icon-trash");
									var $btn = $(document.createElement("button"))
											.addClass("btn btn-danger btn-mini removeLine")
											.attr("id","btnRemove")
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

		this.today = new Date();

		this.$el.on("submit", {instance: this}, function(event){
			event.preventDefault();
			$(window).trigger("FeesView:submit", $(this).serializeObject());
		});

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(FeesView.prototype, {
		bindEvents: function(){
			$(window).on("FeesController:init", $.proxy(this.onInit, this));	
			$(window).on("FeesModel:feeSaved", $.proxy(this.onSaved, this));	
			$(window).on("FeesModel:feesHistoryLoaded", $.proxy(this.onHistoryLoaded, this));	
			$(window).on("FeesModel:feeDeleted", $.proxy(this.onDelete, this));	
		},
		/* Event response*/
		onInit: function(){
			$(this.$date).val(App.formatDate(this.today));
			this.attachEvents();
		},
		attachEvents: function(cra){
			this
				.attachRemoveTableAbsenceRowEvent();
			return this;
		},
		attachRemoveTableAbsenceRowEvent: function(){
			this.$tableFees
				.off("click", ".removeLine")
				.on("click", ".removeLine", {instance: this}, function(event){
					event.preventDefault();
					var msg = "Êtes-vous sur de vouloir supprimer cette note de frais ?",
						tr = $(this).parents("tr")[0];

					bootbox.confirm(msg, function(result) {
						if (result) {
							var fee= {
								"trigramme": event.data.instance.$el.find('#trigramme').val(),
								"fee": event.data.instance.$tableFees.fnGetData(tr)
							};
							$(window).trigger("FeesView:deleteFee", fee);
						}
					});
				});
			return this;
		},
		onSaved: function(event, data){
			App.showModal({type: "success", title:"Frais de déplacement", message: "Saisie validée", timeout:3000});
		},
		onHistoryLoaded: function(event, history){
			this.$tableFees.fnClearTable()
			jsRoutes.controllers.Fees.fetchType().ajax({
				context: this
			})
			.done(function(feeTypes, textStatus, jqXHR){
				FEE_TYPES = feeTypes;
				this.$tableFees.fnAddData(history.fees);
			});
			
		},
		onDelete: function(event, data){
			App.showModal({type: "success", title:"Frais", message: "Frais supprimé", timeout:3000});
			$(window).trigger("FeesController:loadHistory", this.$searchForm.serializeObject());
		}
	});

	var FEE_TYPES = window.App.FeesController.FeesView.FeeTypes = {};
})(window);