package testUtils;

import models.*;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;

/**
 * @author f.patin
 */
public class TestFactory {

	public static Customer newCustomer(final String customerName) {
		return ((Customer) new Customer(customerName).insert());
	}

	public static Mission newMission(final String code, final String label, final ObjectId customerId) {
		Mission mission = new Mission();
		mission.code = code;
		mission.label = label;
		mission.startDate = DateTime.now();
		mission.customerId = customerId;
		return ((Mission) mission.insert());
	}

	public static Cra newCra(final Employee employee, final Integer year, final Integer month) {
		Cra cra = new Cra(employee, year, new Month(month));
		return (Cra) cra.insert();
	}

	public static void addDay(final Cra cra, final Integer dayOfMonth, final Mission mission) {
		DateTime date = new DateTime(cra.year, cra.month.value, dayOfMonth, 0, 0);
		Day day = new Day(date);
		day.morning = new HalfDay(mission);
		day.afternoon = new HalfDay(mission);
		cra.updateDay(day);
	}
}