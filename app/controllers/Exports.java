package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import constants.ExportFormat;
import helpers.ErrorMsgFactory;
import helpers.binder.BigDecimalW;
import helpers.binder.ObjectIdW;
import helpers.exception.PayIllegalStateException;
import models.Cra;
import models.Customer;
import models.Pay;
import play.api.templates.Html;
import play.libs.Akka;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import vendor.*;
import views.html.exports.pay;

import java.util.concurrent.Callable;

import static constants.Pattern.MONTH;
import static play.libs.Scala.Option;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Exports extends AbstractController {

	public static final String CRA_EMPLOYEE_FILENAME = "Cra_%s_%s";
	public static final String CRA_CUSTOMER_FILENAME = "Cra_%s_%s_%s";
	public static final String PAY_EMPLOYEE_FILENAME = "Remuneration_%s_%s_%s";
	public static final String PAY_CUSTOMER_FILENAME = "Remuneration_%s_%s_%s_%s";

	public static Result craEmployee(final String format, final String trigramme, final Integer year, final Integer month) {
		final String fileName = String.format(CRA_EMPLOYEE_FILENAME, year, MONTH.print(month));
		return redirect(routes.Exports.downloadCraEmployee(format, trigramme, year, month, fileName));
	}

	public static Result downloadCraEmployee(final String format, final String trigramme, final Integer year, final Integer month, final String fileName) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Cra c = Cra.fetch(trigramme, year, month, Boolean.TRUE);
				c.fees = Cra.computeFees(trigramme, year, month, Boolean.TRUE);
				PDFData pdfCra = new PDFCra(c);
				return ok(PDFv2.apply(pdfCra)).as("application/pdf");
			}
		}));
	}

	public static Result craCustomer(final String format, final String trigramme, final Integer year, final Integer month, final ObjectIdW customerId) {
		final String fileName = String.format(CRA_CUSTOMER_FILENAME, year, MONTH.print(month), Customer.findById(customerId.value).name);
		return redirect(routes.Exports.downloadCraCustomer(format, trigramme, year, month, customerId, fileName));
	}

	public static Result downloadCraCustomer(final String format, final String trigramme, final Integer year, final Integer month, final ObjectIdW customerId, final String fileName) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Customer customer = Customer.findById(customerId.value);
				Cra c = Cra.fetch(trigramme, year, month, customer, Boolean.TRUE);
				PDFData pdfCra = new PDFCraCustomer(c, customer);
				return ok(PDFv2.apply(pdfCra)).as("application/pdf");
			}
		}));
	}

	public static Result payEmployee(final String format, final String trigramme, final Integer year, final Integer month, final BigDecimalW hourlyRate) {
		final String fileName = String.format(PAY_EMPLOYEE_FILENAME, trigramme, year, MONTH.print(month));
		return redirect(routes.Exports.downloadPayEmployee(format, trigramme, year, month, hourlyRate, fileName));
	}

	public static Result downloadPayEmployee(final String format, final String trigramme, final Integer year, final Integer month, final BigDecimalW hourlyRate, final String fileName) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				try {
					Pay p = Pay.getForExport(trigramme, year, month, hourlyRate.value);
					Cra cra = Cra.fetch(trigramme, year, month, Boolean.TRUE);
					cra.fees = Cra.computeFees(trigramme, year, month, Boolean.TRUE);
					return export(format, pay.render(p, cra, Option((Customer) null)));
				} catch (PayIllegalStateException e) {
					return badRequest(ErrorMsgFactory.asJson(e.getMessage()));
				}
			}
		}));
	}

	public static Result payCustomer(final String format, final String trigramme, final Integer year, final Integer month, final BigDecimalW hourlyRate, final ObjectIdW customerId) {
		final String fileName = String.format(PAY_CUSTOMER_FILENAME, trigramme, year, MONTH.print(month), Customer.findById(customerId.value).name);
		return redirect(routes.Exports.downloadPayCustomer(format, trigramme, year, month, hourlyRate, customerId, fileName));
	}

	public static Result downloadPayCustomer(final String format, final String trigramme, final Integer year, final Integer month, final BigDecimalW hourlyRate, final ObjectIdW customerId, final String fileName) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				try {
					Customer customer = Customer.findById(customerId.value);
					Pay p = Pay.getForExport(trigramme, year, month, hourlyRate.value);
					Cra cra = Cra.fetch(trigramme, year, month, Boolean.FALSE);
					return export(format, pay.render(p, cra, Option(customer)));
				} catch (PayIllegalStateException e) {
					return badRequest(ErrorMsgFactory.asJson(e.getMessage()));
				}
			}
		}));
	}

	public static Result holidays(final String trigramme, final Integer year, final Integer month) {
		Result result = null;
		return result;
	}

	private static Result export(final String format, final Html html) {
		switch (ExportFormat.byName(format)) {
			case PDF:
				return PDF.ok(html);
			case HTML:
				return ok(html);
			default:
				return badRequest("Format non reconnu.");
		}
	}
}
