/* GlobalSettings.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** GlobalSettingsController */
	var GlobalSettingsController = window.App.GlobalSettingsController = function(){
		var _model   = new GlobalSettingsModel();
		var _view	= new GlobalSettingsView();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(GlobalSettingsController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderGlobalSettings", $.proxy(this.onRender, this));	
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("GlobalSettingsController:loadData", element);
		}
	});
	/** GlobalSettingsModel */
	var GlobalSettingsModel = window.App.GlobalSettingsController.GlobalSettingsModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(GlobalSettingsModel.prototype, {
		bindEvents: function(){
			$(window).on("GlobalSettingsView:submit", $.proxy(this.onSubmit, this));	
			$(window).on("GlobalSettingsController:loadData", $.proxy(this.onLoad, this));	
			$(window).on("GlobalSettingsController:saveManager", $.proxy(this.onSaveManager, this));	
			$(window).on("GlobalSettingsController:saveEmail", $.proxy(this.onSaveEmail, this));	
			$(window).on("GlobalSettingsController:saveShowStandBy", $.proxy(this.onSaveStandBy, this));	
		},
		onSaveManager: function(event, data){
			jsRoutes.controllers.Accounts.saveManager(data.manager).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
	        	$(window).trigger("GlobalSettingsModel:managerSaved", data);
	        });
		},
		onSaveEmail: function(event, data){
			jsRoutes.controllers.Accounts.saveEmail(data.email).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
	        	$(window).trigger("GlobalSettingsModel:emailSaved", data);
	        });
		},
		onSaveStandBy: function(event, data){
			jsRoutes.controllers.Accounts.changeStandByOption(data).ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
	        	$(window).trigger("GlobalSettingsModel:standBySaved", data);
	        });
		},
		onLoad: function(event, data){
			jsRoutes.controllers.Accounts.fetchGlobalSetting().ajax({
				context: this
			})
			.done(function(data, textStatus, jqXHR){
	        	$(window).trigger("GlobalSettingsModel:dataLoaded", data);
	        });
		}
	});
	/** GlobalSettingsView */
	var GlobalSettingsView = window.App.GlobalSettingsController.GlobalSettingsView = function(){
		this.$el = $("#divGlobalSettings");

		this.$formEmail = this.$el.find('#formEmail');
		this.$email = this.$formEmail.find('#email');
		this.$btnEmail = this.$formEmail.find('#btnEmail');
		this.$btnResetEmail = this.$formEmail.find('#btnResetEmail');
		this.emailIsOnEdit = false;

		this.$formManager = this.$el.find('#formManager');
		this.$manager = this.$formManager.find('#manager');
		this.$btnManager = this.$formManager.find('#btnManager');
		this.$btnResetManager = this.$formManager.find('#btnResetManager');
		this.managerIsOnEdit = false

		this.$formStandBy = this.$el.find('#formStandBy');
		this.$showStandBy = this.$formStandBy.find('#showStandBy');
		this.$btnShowStandBy = this.$formStandBy.find('#btnShowStandBy');
		this.$btnResetShowStandBy = this.$formStandBy.find('#btnResetShowStandBy');
		this.showStandByIsOnEdit = false

		this.$btnManager.on("click", {instance: this}, function(event){
			if(!event.data.instance.managerIsOnEdit) {
				event.data.instance.edit("manager");
			} else {
				$(window).trigger("GlobalSettingsController:saveManager", $(event.data.instance.$formManager).serializeObject());
			}
		});
		this.$btnResetManager.on("click", {instance: this}, function(event){
			event.data.instance.cancel("manager");	
		});
		this.$btnEmail.on("click", {instance: this}, function(event){
			if(!event.data.instance.emailIsOnEdit) {
				event.data.instance.edit("email");
			} else {
				$(window).trigger("GlobalSettingsController:saveEmail", $(event.data.instance.$formEmail).serializeObject());
			}
		});
		this.$btnResetEmail.on("click", {instance: this}, function(event){
			event.data.instance.cancel("email");	
		});
		this.$btnShowStandBy.on("click", {instance: this}, function(event){
			if(!event.data.instance.showStandByIsOnEdit) {
				event.data.instance.edit("showStandBy");
			} else {
				$(window).trigger("GlobalSettingsController:saveShowStandBy"
								, $(event.data.instance.$showStandBy).attr('checked') === "checked" ? true : false);
			}
		});
		this.$btnResetShowStandBy.on("click", {instance: this}, function(event){
			event.data.instance.cancel("showStandBy");	
		});

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(GlobalSettingsView.prototype, {
		bindEvents: function(){
			$(window).on("GlobalSettingsModel:dataLoaded", $.proxy(this.renderForms, this));	
			$(window).on("GlobalSettingsModel:managerSaved", $.proxy(this.onManagerSaved, this));	
			$(window).on("GlobalSettingsModel:emailSaved", $.proxy(this.onEmailSaved, this));	
			$(window).on("GlobalSettingsModel:standBySaved", $.proxy(this.onStandBySaved, this));	
		},
		/* Event response*/
		show: function(){
			this.$el.show();
		},
		renderForms: function(event, data){
			this
				.renderEmail(data.email)
				.renderManager(data.manager)
				.renderShowStandBy(data.showStandBy)
				.show();
		},
		renderEmail: function(email){
			if(email){
				this.$email.val(email);	
			} else {
				this.edit("email");
			}
			
			return this;
		},
		renderManager: function(manager){
			if(manager) {
				this.$manager.find('option[value="' + manager.trigramme + '"]').attr("selected", true);
			} else {
				this.edit("manager");
			}
			return this;
		},
		renderShowStandBy: function(showStandBy){
			if(showStandBy) {
				this.$showStandBy.attr("checked", true);
			} else {
				this.edit("showStandBy");
			}
			return this;
		},
		onManagerSaved: function(event, data){
			App.showModal({type: "success", title:"Responsable", message: "Changement effectué", timeout:3000});
			this.cancel("manager");
		},
		onEmailSaved: function(event, data){
			App.showModal({type: "success", title:"Mail", message: "Changement effectué", timeout:3000});
			this.cancel("email");
		},
		onStandBySaved: function(event, data){
			App.showModal({type: "success", title:"Astreintes", message: "Changement effectué", timeout:3000});
			this.cancel("showStandBy");
		},
		edit: function(zoneName){
			if(zoneName === "manager") {
				if(!this.managerIsOnEdit) {
					this.$manager.attr("disabled", false);
					this.$btnResetManager.show();
					this.$btnManager.text('Sauvegarder');
					this.managerIsOnEdit = true;
				}
			} else if (zoneName === "email") {
				if(!this.emailIsOnEdit) {
					this.$email.attr("disabled", false);
					this.$btnResetEmail.show();
					this.$btnEmail.text('Sauvegarder');
					this.emailIsOnEdit = true;
				}
			} else if (zoneName === "showStandBy") {
				if(!this.showStandByIsOnEdit) {
					this.$showStandBy.attr("disabled", false);
					this.$btnResetShowStandBy.show();
					this.$btnShowStandBy.text('Sauvegarder');
					this.showStandByIsOnEdit = true;
				}
			}		
		},
		cancel: function(zoneName){
			if(zoneName === "manager") {
				if(this.managerIsOnEdit) {
					this.$manager.attr("disabled", true);
					this.$btnResetManager.hide();
					this.$btnManager.text('Editer');
					this.managerIsOnEdit = false;
				}
			} else if (zoneName === "email") {
				if(this.emailIsOnEdit) {
					this.$email.attr("disabled", true);
					this.$btnResetEmail.hide();
					this.$btnEmail.text('Editer');
					this.emailIsOnEdit = false;
				}
			} else if (zoneName === "showStandBy") {
				if(this.showStandByIsOnEdit) {
					this.$showStandBy.attr("disabled", true);
					this.$btnResetShowStandBy.hide();
					this.$btnShowStandBy.text('Editer');
					this.showStandByIsOnEdit = false;
				}
			}
		}
	});
})(window);
