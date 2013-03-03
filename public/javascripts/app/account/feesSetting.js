/* FeesSetting.js */
;
(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** FeesSettingController */
	var FeesSettingController = window.App.FeesSettingController = function(){
		var _model = new FeesSettingModel();
		var _view = new FeesSettingView();

		this.getModel = function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesSettingController.prototype, {
		bindEvents: function(){
			$(window).on("App:renderFeesSetting", $.proxy(this.onRender, this));
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("FeesSettingController:loadData", element);
		}
	});
	/** FeesSettingModel */
	var FeesSettingModel = window.App.FeesSettingController.FeesSettingModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(FeesSettingModel.prototype, {
		bindEvents: function(){
			$(window).on("FeesSettingView:submit", $.proxy(this.onSubmit, this));
			$(window).on("FeesSettingController:loadData", $.proxy(this.onLoadData, this));
		},
		onSubmit: function(event, data){
			jsRoutes.controllers.Accounts.saveVehicle().ajax({
				data: JSON.stringify(data),
				context: this
			})
				.done(function(data, textStatus, jqXHR){
					$(window).trigger("FeesSettingModel:vehicleSaved", data);
				});
		},
		onLoadData: function(event, data){
			jsRoutes.controllers.Accounts.fetchVehicle().ajax({
				context: this
			})
				.done(function(data, textStatus, jqXHR){
					if (data) {
						$(window).trigger("FeesSettingModel:vehicleLoaded", data);
					} else {
						$(window).trigger("FeesSettingModel:noVehicle", {});
					}
				});
		}
	});
	/** FeesSettingView */
	var FeesSettingView = window.App.FeesSettingController.FeesSettingView = function(){
		this.$el = $("#divFeesSetting");
		this.$form = this.$el.find('#formFees');

		this.$fiscalPower = this.$el.find('#fiscalPower');
		this.$capacity = this.$el.find('#capacity');
		this.$brand = this.$el.find('#brand');
		this.$matriculation = this.$el.find('#matriculation');

		this.$tabHeaderCar = this.$el.find('#tabHeaderCar');
		this.$tabHeaderCar.on('show',{instance:this},function(event){
			event.data.instance.$tabPaneCar.find('#fiscalPower').attr("required", "required");
			event.data.instance.$tabPaneMotorcycle.find('#capacity').removeAttr("required");
		});
		this.$tabPaneCar = this.$el.find('#tabPaneCar');
		this.$tabHeaderMotorcycle = this.$el.find('#tabHeaderMotorcycle');
		this.$tabHeaderMotorcycle.on('show',{instance:this}, function(event){
			event.data.instance.$tabPaneMotorcycle.find('#capacity').attr("required", "required");
			event.data.instance.$tabPaneCar.find('#fiscalPower').removeAttr("required");
		});
		this.$tabPaneMotorcycle = this.$el.find('#tabPaneMotorcycle');


		this.$form.on("submit", {instance: this}, function(event){
			event.preventDefault();
			var data = _.omit($(this).serializeObject(), ["fiscalPower", "capacity"]);
			if (event.data.instance.$tabHeaderCar.hasClass("active")) {
				data.type = "CAR";
				data.power = event.data.instance.$fiscalPower.val();
			} else if (event.data.instance.$tabHeaderMotorcycle.hasClass("active")) {
				data.type = "MOTORCYCLE";
				data.power = event.data.instance.$capacity.val();
			}
			$(window).trigger("FeesSettingView:submit", data);
		});

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(FeesSettingView.prototype, {
		bindEvents: function(){
			$(window).on("FeesSettingModel:vehicleLoaded", $.proxy(this.render, this));
			$(window).on("FeesSettingModel:noVehicle", $.proxy(this.onInit, this));
			$(window).on("FeesSettingModel:vehicleSaved", $.proxy(this.onSaved, this));
		},
		/* Event response*/
		onInit: function(){
			this.$el.show();
		},
		render: function(event, data){
			this
				.renderInput(data)
				.renderTab(data)
				.onInit();
		},
		renderInput: function(data){
			if (data.type === "CAR") {
				this.$fiscalPower.val(data.power);
			} else if (data.type === "MOTORCYCLE") {
				this.$capacity.val(data.power);
			}
			if (data.matriculation) {
				this.$matriculation.val(data.matriculation);
			}
			if (data.brand) {
				this.$brand.find('option[value="' + data.brand.toUpperCase() + '"]').attr("selected", true);
			}
			return this;
		},
		renderTab: function(data){
			if (data.type === "CAR") {
				this.$tabHeaderCar.addClass('active');
				this.$tabPaneCar
					.addClass('active')
					.find('#fiscalPower').attr("required", "required");

				this.$tabHeaderMotorcycle.removeClass('active');
				this.$tabPaneMotorcycle
					.removeClass('active')
					.find('#capacity').removeAttr("required");
			} else if (data.type === "MOTORCYCLE") {
				this.$tabHeaderMotorcycle.addClass('active');
				this.$tabPaneMotorcycle
					.addClass('active')
					.find('#capacity').attr("required", "required");

				this.$tabHeaderCar.removeClass('active');
				this.$tabPaneCar
					.removeClass('active')
					.find('#fiscalPower').removeAttr("required");
			}
			return this;
		},
		onSaved: function(event, data){
			App.showModal({type: "success", title: "Véhicule", message: "Saisie validée", timeout: 3000});
		}
	});
})(window);