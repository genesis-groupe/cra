/* config.js */
;(function(window, undefined){
	"use strict";
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;
	/** Définition de $ = jQuery et _ = Underscore */
	var $ = window.jQuery,
		_ = window._,
		Handlebars = window.Handlebars,
		numeral = window.numeral,
		moment = window.moment,
		bootbox = window.bootbox;

	/** Init plugins */
	/* Avoid `console` errors in browsers that lack a console. */
	if(!window.console){
		var method,
			noop = function noop() {},
			methods = [
				'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
				'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
				'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
				'timeStamp', 'trace', 'warn'
			],
			length = methods.length,
			console = (window.console = window.console || {});

		while (length--) {
			method = methods[length];

			// Only stub undefined methods.
			if (!console[method]) {
				console[method] = noop;
			}
		}
	}
	/** DatePicker */
	$(".datepicker").datepicker({
		format: "dd/mm/yyyy",
		weekStart: 1
	})
	.on("blur", function(event){
		$(".datepicker").datepicker("hide");
	});
	/** Numeral **/
	numeral.language('fr');
	/** Time input mask */
	$(".timeInput").mask("99:99",{placeholder: " "});
	/** Extend jQuery */
	$.fn.serializeObject = function() {
		var arrayData, objectData;
		arrayData = this.serializeArray();
		objectData = {};

		$.each(arrayData, function() {
			var value = this.value != null ? this.value : "";

			if (objectData[this.name] != null) {
				if (!objectData[this.name].push) {
					objectData[this.name] = [objectData[this.name]];
				}
				objectData[this.name].push(value);
			} else {
				objectData[this.name] = value;
			}
		});

		return objectData;
	};

	$.fn.clearForm = function() {
		// textarea
		this.find('textarea').val('');
		// inputs
		this.find('input').each(function(){
			var $element = $(this);
			if($element.attr('type') === 'checkbox'){
				$element.attr('checked',false);
			} else if($element.attr('type') !== "submit" && $element.attr('type') !== "button"){
				$element.val('');
			}
		});
		this.find('select').each(function(){
			$(this).find("option[value='']").attr('selected','selected');
		});
		return this;
	};
	$.fn.populateForm = function(obj){
		for (var key in obj){
			var $el = $(this.find('#'+key)),
				el = $el[0];

			if(el){
				if(el.tagName === "INPUT"){
					if(el.type === 'checkbox' || el.type === 'radio'){
						$el.attr('checked',obj[key]);
					} else {
						$el.val(obj[key]);
					}
				} else if(el.tagName === "SELECT"){
					$el.find("option[value='" + obj[key] + "']").attr('selected','selected');
				} else if(el.tagName === "TEXTAREA"){
					$el.val(obj[key]);
				}
			}
		}
		
	}
	/** Extend _ (Underscore) with Underscore.string */
	_.mixin(_.string.exports());
	/** Handlebars helpers */
	Handlebars.registerHelper("iter", function(context, options) {
		var fn = options.fn, inverse = options.inverse;
		var ret = "";

		if(context && context.length > 0) {
			for(var i=0, j=context.length; i<j; i++) {
				ret = ret + fn(_.extend({}, context[i], { i: i, iPlus1: i + 1 }));
			}
		} else {
			ret = inverse(this);
		}
		return ret;
	});
	Handlebars.registerHelper("calDayIndex", function(context, options){
		return 7 * context.id + options;
	});
	/** Configure bootbox */
	bootbox.animate(false);
})(window);