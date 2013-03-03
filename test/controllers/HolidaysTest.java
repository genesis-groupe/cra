package controllers;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import constants.Genesis;
import constants.MissionType;
import dto.holiday.HolidayDTO;
import dto.holiday.HolidayDataTableDTO;
import models.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import security.RoleName;
import testUtils.AbstractTest;
import testUtils.TestFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.libs.Json.toJson;
import static play.test.Helpers.*;

/**
 * @author LPU
 */
public class HolidaysTest extends AbstractTest {

	private List<Mission> holidayMissions = new ArrayList<>();

	@Override
	protected String getConnexionRole() {
		return RoleName.COLLABORATOR;
	}

	@Override
	protected void postBeforeEachTest() {
		holidayMissions.addAll(Collections2.filter(Mission.findGenesisMission(), new Predicate<Mission>() {
			@Override
			public boolean apply(@Nullable Mission mission) {
				return MissionType.HOLIDAY.equals(mission.missionType);
			}
		}));
	}

	@Override
	protected void preAfterEachTest() {

	}

	/**
	 * Création de 2 demi journées de congé de type différent le même jour
	 */
	@Test
	public void testCreateOneHoliday_TwoDifferentHalfDayHolidayInSameDay() {
	    /* Preparation */
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("comment", "");
		data.put("date", "03/01/2013");  // 03/01/2013
		data.put("morning", "true");
		data.put("afternoon", "false");
		Result result = callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		/* Test */
		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(1).id.toString());
		data.put("comment", "");
		data.put("date", "03/01/2013");  // 03/01/2013
		data.put("morning", "false");
		data.put("afternoon", "true");
		result = callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		result = callAction(
				routes.ref.Holidays.fetchHolidays(collaborator.trigramme, 2012),
				fakeRequest("GET", "/holidays/:trigramme/:year").withCookies(cookie(COOKIE_NAME, connexion))
		);
		HolidayDataTableDTO dtos = Json.fromJson(Json.parse(contentAsString(result)), HolidayDataTableDTO.class);
		assertThat(dtos.holidays.size()).isEqualTo(2);
		final HolidayDTO holidayDTO_0 = dtos.holidays.get(0);
		assertThat(holidayDTO_0.nbDay).isEqualTo(0.5);
		assertThat(holidayDTO_0.start).isEqualTo(new DateTime(2013, 1, 3, 0, 0));
		assertThat(holidayDTO_0.end).isEqualTo(new DateTime(2013, 1, 3, 0, 0));
		assertThat(holidayDTO_0.halfDayType).isEqualTo("MORNING");
		assertThat(holidayDTO_0.missionCode).isEqualTo(holidayMissions.get(0).code);

		final HolidayDTO holidayDTO_1 = dtos.holidays.get(1);
		assertThat(holidayDTO_1.nbDay).isEqualTo(0.5);
		assertThat(holidayDTO_1.start).isEqualTo(new DateTime(2013, 1, 3, 0, 0));
		assertThat(holidayDTO_1.end).isEqualTo(new DateTime(2013, 1, 3, 0, 0));
		assertThat(holidayDTO_1.halfDayType).isEqualTo("AFTERNOON");
		assertThat(holidayDTO_1.missionCode).isEqualTo(holidayMissions.get(1).code);
	}

	/**
	 * Création de 1 demi journées de congé en remplacement d'une demi journée existante
	 */
	@Test
	public void testCreateOneHoliday_OneHalfDayHolidayInExistingDay() {
		/* Preparation */
		final Integer testYear = 2013;
		final Integer testMonth = DateTimeConstants.JANUARY;
		final Integer dateDay = 3;
		final DateTime testDate = new DateTime(testYear, testMonth, dateDay, 0, 0);
		final Customer customer = TestFactory.newCustomer("customer");
		final Mission mission = TestFactory.newMission("M1", "ML1", customer.id);
		collaborator.addMission(mission);
		collaborator.update();
		Cra cra = TestFactory.newCra(collaborator, testYear, testMonth);
		TestFactory.addDay(cra, dateDay, mission);
		ObjectId craId = cra.id;

	    /* Test */
		Map<String, String> data = Maps.newHashMap();
		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(1).id.toString());
		data.put("comment", "");
		data.put("date", dateDay + "/" + testMonth + "/" + testYear);  // 03/01/2013
		data.put("morning", "false");
		data.put("afternoon", "true");
		Result result = callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		/* Check */
		result = callAction(
				routes.ref.Holidays.fetchHolidays(collaborator.trigramme, testYear),
				fakeRequest("GET", "/holidays/:trigramme/:year").withCookies(cookie(COOKIE_NAME, connexion))
		);
		HolidayDataTableDTO dtos = Json.fromJson(Json.parse(contentAsString(result)), HolidayDataTableDTO.class);
		assertThat(dtos.holidays.size()).isEqualTo(1);
		final HolidayDTO holidayDTO_0 = dtos.holidays.get(0);
		assertThat(holidayDTO_0.nbDay).isEqualTo(0.5);
		assertThat(holidayDTO_0.start).isEqualTo(testDate);
		assertThat(holidayDTO_0.end).isEqualTo(testDate);
		assertThat(holidayDTO_0.halfDayType).isEqualTo("AFTERNOON");
		assertThat(holidayDTO_0.missionCode).isEqualTo(holidayMissions.get(1).code);

		callAction(
				routes.ref.Cras.fetch(collaborator.trigramme, testYear, testMonth),
				fakeRequest("GET", "/cra/:trigramme/:year/:month").withCookies(cookie(COOKIE_NAME, connexion))
		);

		cra = Cra.findById(craId);
		assertThat(cra.days.size()).isEqualTo(1);
		final Day day = cra.days.get(0);
		assertThat(day.date).isEqualTo(testDate);
		final HalfDay morning = day.morning;
		assertThat(morning).isNotNull();
		assertThat(morning.mission.id).isEqualTo(mission.id);
		final HalfDay afternoon = day.afternoon;
		assertThat(afternoon).isNotNull();
		assertThat(afternoon.mission.id).isEqualTo(holidayMissions.get(1).id);
	}

	/**
	 * Demande de congés pour le 1er novembre jour férié
	 *
	 * @result le controller retourne une <code>BAD_REQUEST (400)</code>
	 * @result le controller répond "Veuillez saisir un jour ouvré"
	 */
	@Test
	public void testCreateOneHoliday_InDayOff() {
	     /* Test*/
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "01/11/2012");  //1er novembre
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");
		Result result = callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(BAD_REQUEST);
		String[] response = StringUtils.split(contentAsString(result), "\"");
		assertThat(response[2]).isEqualTo("Veuillez saisir un jour ouvré.");
	}

	/**
	 * Demande de congés pour le 2 novembre
	 *
	 * @result le controller renvoie la journée créée
	 */
	@Test
	public void testCreateOneDayHoliday() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "02/11/2012");  //2 novembre
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");

		Result result = callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		HolidayDTO dto = Json.fromJson(Json.parse(contentAsString(result)), HolidayDTO.class);
		assertThat(dto).isNotNull();
		assertThat(dto.start).isEqualTo(new DateTime(2012, 11, 2, 0, 0));

	}

	/**
	 * Suppression d'une journée de congés
	 *
	 * @result On récupère la liste des congés, la taille de la liste doit être de 2
	 */
	@Test
	public void testDeleteHoliday_OneDay() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "02/11/2012");  //2 novembre
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");
		callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);

        /* Test */
		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("startHoliday", "02/11/2012");
		data.put("endHoliday", "02/11/2012");
		data.put("isRecurrent", "false");
		Result result = callAction(
				routes.ref.Holidays.deleteHoliday(),
				fakeRequest("POST", "/deleteHoliday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		result = callAction(
				routes.ref.Holidays.fetchHolidays(collaborator.trigramme, 2012),
				fakeRequest("GET", "/holidays/:trigramme/:year").withCookies(cookie(COOKIE_NAME, connexion))
		);
		assertThat(status(result)).isEqualTo(OK);
		HolidayDataTableDTO dtos = Json.fromJson(Json.parse(contentAsString(result)), HolidayDataTableDTO.class);
		assertThat(dtos.holidays.size()).isEqualTo(0);
	}

	/**
	 * Demande 3 jours de congés
	 *
	 * @result le controller renvoie la liste des congés du mois :
	 * <p/>
	 * Début doit être égal au 2 Novembre 2012
	 * Fin au 6 Novembre 2012
	 * Pour un total de 3.0 jours.
	 */
	@Test
	public void testCreateOneDayHoliday_ThreeHolidays() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "02/11/2012");  //2 novembre
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");
		callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "05/11/2012");
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");
		callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("date", "06/11/2012");
		data.put("morning", "true");
		data.put("afternoon", "true");
		data.put("comment", "");
		callAction(
				routes.ref.Holidays.createOneDayHoliday(),
				fakeRequest("POST", "/holiday/oneday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		Result result = callAction(
				routes.ref.Holidays.fetchHolidays(collaborator.trigramme, 2012),
				fakeRequest("GET", "/holidays/:trigramme/:year").withCookies(cookie(COOKIE_NAME, connexion))
		);
		assertThat(status(result)).isEqualTo(OK);
		HolidayDTO dto = Json.fromJson(Json.parse(contentAsString(result)), HolidayDataTableDTO.class).holidays.get(0);
		assertThat(dto.nbDay).isEqualTo(3.0);
		assertThat(dto.start).isEqualTo(new DateTime(2012, 11, 2, 0, 0));
		assertThat(dto.end).isEqualTo(new DateTime(2012, 11, 6, 0, 0));
	}

	/**
	 * Creation de congés du 05/11/2012(lundi) au 11/11/2012(dimanche)
	 *
	 * @result nombre de jour posé 5.
	 * Début le 5 Novembre
	 * Fin le 9 Novembre
	 */
	@Test
	public void testCreateOnePeriodHoliday_1() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("startHoliday", "05/11/2012");  //5 novembre
		data.put("endHoliday", "11/11/2012");  //11 novembre
		data.put("startMorning", "true");
		data.put("endAfternoon", "true");
		data.put("comment", "");
		Result result = callAction(
				routes.ref.Holidays.createHolidayPeriod(),
				fakeRequest("POST", "/holiday/period").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		HolidayDTO dto = Json.fromJson(Json.parse(contentAsString(result)), HolidayDTO.class);
		assertThat(dto).isNotNull();
		assertThat(dto.nbDay).isEqualTo(5);
		assertThat(dto.start).isEqualTo(new DateTime(2012, 11, 5, 0, 0));
		assertThat(dto.end).isEqualTo(new DateTime(2012, 11, 9, 0, 0));
	}

	/**
	 * Creation de congés du 05/11/2012 après-midi (lundi) au 11/11/2012(dimanche)
	 *
	 * @result nombre de jour posé 4.5.
	 * Début le 5 Novembre
	 * Fin le 9 Novembre
	 */
	@Test
	public void testCreateOnePeriodHoliday_2() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("startHoliday", "05/11/2012");  //5 novembre
		data.put("endHoliday", "11/11/2012");  //11 novembre
		data.put("startMorning", "false");
		data.put("endAfternoon", "true");
		data.put("comment", "");
		Result result = callAction(
				routes.ref.Holidays.createHolidayPeriod(),
				fakeRequest("POST", "/holiday/period").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		HolidayDTO dto = Json.fromJson(Json.parse(contentAsString(result)), HolidayDTO.class);
		assertThat(dto).isNotNull();
		assertThat(dto.nbDay).isEqualTo(4.5);
		assertThat(dto.start).isEqualTo(new DateTime(2012, 11, 5, 0, 0));
		assertThat(dto.end).isEqualTo(new DateTime(2012, 11, 9, 0, 0));
	}

	/**
	 * Suppression de la période de congés
	 *
	 * @result On récupère la liste des congés, la taille de la liste doit être de 2
	 */
	@Test
	public void testDeleteHolidayPeriod() {
		Map<String, String> data = Maps.newHashMap();
		data.put("trigramme", collaborator.trigramme);
		data.put("holidayMissionId", holidayMissions.get(0).id.toString());
		data.put("startHoliday", "05/11/2012");  //5 novembre
		data.put("endHoliday", "09/11/2012");  //9 novembre
		data.put("startMorning", "true");
		data.put("endAfternoon", "true");
		data.put("comment", "");
		callAction(
				routes.ref.Holidays.createHolidayPeriod(),
				fakeRequest("POST", "/holiday/period").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);

		data.clear();
		data.put("trigramme", collaborator.trigramme);
		data.put("startHoliday", "05/11/2012");
		data.put("endHoliday", "09/11/2012");
		data.put("isRecurrent", "false");
		Result result = callAction(
				routes.ref.Holidays.deleteHoliday(),
				fakeRequest("POST", "/deleteHoliday").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(data))
		);
		assertThat(status(result)).isEqualTo(OK);
		result = callAction(
				routes.ref.Holidays.fetchHolidays(collaborator.trigramme, 2012),
				fakeRequest("GET", "/holidays/:trigramme/:year").withCookies(cookie(COOKIE_NAME, connexion))
		);
		HolidayDataTableDTO dtos = Json.fromJson(Json.parse(contentAsString(result)), HolidayDataTableDTO.class);
		assertThat(dtos.holidays.size()).isEqualTo(0);
	}

}