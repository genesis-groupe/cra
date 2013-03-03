package mailer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import constants.ConfigurationKey;
import constants.DaysOfWeek;
import constants.Pattern;
import models.Employee;
import models.Mission;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.Configuration;
import play.Play;
import views.html.mails.holiday;
import views.html.mails.holidayRecurrent;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: LPU
 * Date: 20/11/12
 */
public class Mailer {

	private static final MailerPlugin MAILER = Play.application().plugin(MailerPlugin.class);

	private static MailerAPI createMail(String from, String to, String subject, String... cc) {
		return MAILER.email()
				.addFrom(from)
				.addRecipient(to)
				.addCc(cc)
				.setSubject(subject);
	}

	/**
	 * Send Holiday Email : OneDay and Period
	 */
	public static void sendHolidayEmail(final Employee employee, final Mission mission, final DateTime beginning, final DateTime end, /**/
	                                    final Double nbDays, final String comment, final Boolean morning, final Boolean afternoon) {
		checkNotNull(employee, "employee must be not null");
		checkNotNull(mission, "mission must be not null");
		checkNotNull(beginning, "beginning must be not null");

		final Configuration emailConfig = Configuration.root().getConfig(ConfigurationKey.EMAIL.mainKey);
		final String employeeName = formatEmployeeName(employee);
		//Remplissage du contenu
		final String emailBody = holiday
				.render(employee, mission, beginning.toString(Pattern.DATE), (end == null) ? null : end.toString(Pattern.DATE), nbDays, comment, morning, afternoon)
				.body();
		final List<String> cc = Lists.newArrayList(employee.manager.email, employee.email);
		final String subject = "Demande d'absence : " + employeeName;

		Mailer
				.createMail(emailConfig.getString("sender"), emailConfig.getString("absence"), subject, cc.toArray(new String[0]))
				.sendHtml(emailBody);
	}

	/**
	 * Send Holiday Email : Recurrent
	 */
	public static void sendHolidayRecurrentEmail(final Employee employee, final Mission mission, final DateTime beginning, final DateTime end, /**/
	                                             final Integer repeatEveryXWeeks, final List<Integer> daysOfWeek, final Integer nbOfOccurences, /**/
	                                             final Boolean morning, final Boolean afternoon, final String comment, final Double nbDays) {
		checkNotNull(employee, "employee must be not null");
		checkNotNull(mission, "mission must be not null");
		checkNotNull(beginning, "beginning must be not null");
		final Configuration emailConfig = Configuration.root().getConfig(ConfigurationKey.EMAIL.mainKey);
		final String employeeName = formatEmployeeName(employee);
		//Remplissage du contenu
		List<String> daysOfWeekStr = Lists.transform(daysOfWeek, new Function<Integer, String>() {
			@Override
			public String apply(@Nullable Integer integer) {
				return DaysOfWeek.getNameByValue(integer);
			}
		});
		final String emailBody = holidayRecurrent
				.render(employee, mission, beginning.toString(Pattern.DATE),
						(end == null) ? null : end.toString(Pattern.DATE), repeatEveryXWeeks, daysOfWeekStr, nbOfOccurences, morning, afternoon, comment, nbDays)
				.body();
		final List<String> cc = Lists.newArrayList(employee.manager.email, employee.email);
		final String subject = "Demande d'absence : " + employeeName;
		Mailer
				.createMail(emailConfig.getString("sender"), emailConfig.getString("absence"), subject, cc.toArray(new String[0]))
				.sendHtml(emailBody);

	}

	private static String formatEmployeeName(Employee employee) {
		return StringUtils.capitalize(employee.firstName) /**/
				+ " " + StringUtils.capitalize(employee.lastName)/**/
				+ " (" + StringUtils.capitalize(employee.trigramme) + ")";
	}
}
