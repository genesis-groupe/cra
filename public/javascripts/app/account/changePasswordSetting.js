/* changePassword.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** ChangePasswordController */
	var ChangePasswordController = window.App.ChangePasswordController = function(){
		var _model   = new ChangePasswordModel();
		var _view	= new ChangePasswordView();

		this.getModel= function(){
			return _model;
		};
		this.getView = function(){
			return _view;
		};
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(ChangePasswordController.prototype,{
		bindEvents: function(){
			$(window).on("App:renderChangePassword", $.proxy(this.onRender, this));	
		},
		/* Event response*/
		onRender: function(event, element){
			$(window).trigger("ChangePasswordController:init", element);
		}
	});
	/** ChangePasswordModel */
	var ChangePasswordModel = window.App.ChangePasswordController.ChangePasswordModel = function(){
		/* Definit les events sur lesquels ecoute la Modele */
		this.bindEvents();
	};
	_.extend(ChangePasswordModel.prototype, {
		bindEvents: function(){
			$(window).on("ChangePasswordView:submit", $.proxy(this.onSubmit, this));	
		},
		onSubmit: function(event, data){
			jsRoutes.controllers.Accounts.changePassword().ajax({
				data: JSON.stringify(data),
				context: this
	        })
	        .done(function(data, textStatus, jqXHR){
	        	$(window).trigger("ChangePasswordModel:passwordChanged", {});
	        });
		}
	});
	/** ChangePasswordView */
	var ChangePasswordView = window.App.ChangePasswordController.ChangePasswordView = function(){
		this.$el = $("#divChangePassword");
		this.$form = this.$el.find('#formChangePassword');

		this.$oldPassword = this.$el.find('#oldPassword');
		this.$newPassword = this.$el.find('#newPassword');
		this.$newPasswordConfirm = this.$el.find('#newPasswordConfirm');
		this.$dataError = this.$el.find('#dataError');


		this.$form.on("submit", {instance: this}, function(event){
			event.preventDefault();
			if(event.data.instance.checkNewPassword() && event.data.instance.checkOldPasswordAndNew()){
				event.data.instance.$oldPassword.parent().parent().removeClass("error");
				var instance = this;
				bootbox.confirm("Etes-vous sûr de vouloir changer votre mot de passe ?", function(result) {
					if (result) {
						$(window).trigger("ChangePasswordView:submit", $(instance).serializeObject());
					}
				});
			}
		});
		this.$newPassword.on("blur", {instance: this}, function(event){
			event.data.instance.checkNewPassword();
		});
		this.$newPasswordConfirm.on("blur", {instance: this}, function(event){
			event.data.instance.checkNewPassword();
		});

		/* Definit les events sur lesquels ecoute la View */
		this.bindEvents();
	};
	_.extend(ChangePasswordView.prototype, {
		bindEvents: function(){
			$(window).on("ChangePasswordController:init", $.proxy(this.onInit, this));	
			$(window).on("ChangePasswordModel:passwordChanged", $.proxy(this.onChanged, this));	
		},
		/* Event response*/
		onInit: function(){
			this.$el.show();

		},
		onChanged: function(){
			$(this.$form)[0].reset();
			App.showModal({type: "success", title:"Mot de passe", message: "Changement effectué", timeout:3000});
		},
		checkOldPasswordAndNew: function(){
			var oldPassword = this.$oldPassword.val(),
				newPassword = this.$newPassword.val();
			if(oldPassword && newPassword){
				if(oldPassword === newPassword){
					this.displayError("Le nouveau mot de passe est le même.");
					this.$oldPassword.parent().parent().addClass("error");
					this.$newPassword.parent().parent().addClass("error");
					return false;
				}else {
					this.$dataError.empty().hide();
					this.$oldPassword.parent().parent().removeClass("error");
					this.$newPassword.parent().parent().removeClass("error");
					return true;
				}
			}
		},
		checkNewPassword: function(){
			var password1 = this.$newPassword.val(),
				password2 = this.$newPasswordConfirm.val();
			if(password1 && password2){
				this.$oldPassword.parent().parent().removeClass("error");
				if(password1 !== password2){
					this.displayError("Les mots de passe ne correspondent pas.");
					this.$newPassword.parent().parent().addClass("error");
					this.$newPasswordConfirm.parent().parent().addClass("error");
					return false;
				}else {
					this.$dataError.empty().hide();
					this.$newPassword.parent().parent().removeClass("error");
					this.$newPasswordConfirm.parent().parent().removeClass("error");
					return true;
				}
			}
		},
		displayError: function(msgError){
			if(msgError){
				this
					.$dataError
					.empty()
					.append(msgError)
					.fadeIn("slow");
			}
		}
	});
})(window);