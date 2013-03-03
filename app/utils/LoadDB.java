package utils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import constants.*;
import leodagdag.play2morphia.MorphiaPlugin;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import play.Application;
import play.Logger;
import security.RoleName;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author f.patin
 */
public class LoadDB extends AbstractLoadDB {

	public static final List<Mission> CUSTOMER_1_MISSIONS = Mission.findByCustomer(Customer.findByName(CUSTOMER_NAME_1));

	public static void loadDB(final Application app) {
		if (app.plugin(MorphiaPlugin.class) != null && app.plugin(MorphiaPlugin.class).enabled()) {

			if (app.isDev()) {
				// Nettoyage BD
				for (String colName : MorphiaPlugin.db().getCollectionNames()) {
					if (!colName.startsWith("system.") && !colName.startsWith("fs.")) {
						MorphiaPlugin.db().getCollection(colName).drop();
					}
				}
			}

			loadProdDB();

			if (app.isDev()) {
				Logger.info("Loading MongoDB development data...");
				createDevCustomersAndMissions();
				createDevUsers();
				createCras();
				MorphiaPlugin.ds().ensureCaps();
				MorphiaPlugin.ds().ensureIndexes();
			}

		}
	}

	private static void loadProdDB() {
		loadFeesParameters();
		loadPayRates();
		loadRoles();
		loadCustomerGenesis();
		loadGenesisMissions();
	}

	private static void loadCustomerGenesis() {
		Customer genesis = Customer.findByName(Genesis.NAME);
		if (genesis == null) {
			genesis = new Customer();
			genesis.name = Genesis.NAME;
			genesis.isGenesis = Boolean.TRUE;
			genesis.insert();
			Logger.warn(String.format("Customer [%s] created...", Genesis.NAME));
		}
	}

	private static void loadGenesisMissions() {

		final Customer genesis = Customer.Genesis();
		Map<String, Mission> genesisMissions = Maps.newHashMap();

		Mission av = new Mission();
		av.code = "AV";
		av.label = "Avant vente";
		av.missionType = MissionType.PRESALES;
		av.customerId = genesis.id;
		genesisMissions.put(av.code, av);

		Mission ti = new Mission();
		ti.code = "TI";
		ti.label = "Travaux internes";
		ti.missionType = MissionType.INTERNAL_WORK;
		ti.customerId = genesis.id;
		genesisMissions.put(ti.code, ti);

		Mission interContrat = new Mission();
		interContrat.code = "IC";
		interContrat.label = "Inter-contrat";
		interContrat.missionType = MissionType.INTERNAL_WORK;
		interContrat.customerId = genesis.id;
		genesisMissions.put(interContrat.code, interContrat);

		Mission formation = new Mission();
		formation.code = "F";
		formation.label = "Formation";
		formation.missionType = MissionType.INTERNAL_WORK;
		formation.customerId = genesis.id;
		formation.allowanceType = MissionAllowanceType.ZONE;
		genesisMissions.put(formation.code, formation);

		Mission cp = new Mission();
		cp.code = "CP";
		cp.label = "Congé payé";
		cp.missionType = MissionType.HOLIDAY;
		cp.customerId = genesis.id;
		genesisMissions.put(cp.code, cp);

		Mission css = new Mission();
		css.code = "CSS";
		css.label = "Congés Sans Solde";
		css.missionType = MissionType.UNPAID;
		css.customerId = genesis.id;
		genesisMissions.put(css.code, css);

		Mission ae = new Mission();
		ae.code = "AE";
		ae.label = "Absences Exceptionnelles";
		ae.missionType = MissionType.HOLIDAY;
		ae.customerId = genesis.id;
		genesisMissions.put(ae.code, ae);

		Mission mm = new Mission();
		mm.code = "MM";
		mm.label = "Maladie Maternité";
		mm.missionType = MissionType.HOLIDAY;
		mm.customerId = genesis.id;
		genesisMissions.put(mm.code, mm);

		Mission recup = new Mission();
		recup.code = "RECUP";
		recup.label = "Récupération";
		recup.missionType = MissionType.HOLIDAY;
		recup.customerId = genesis.id;
		genesisMissions.put(recup.code, recup);

		Mission rtts = new Mission();
		rtts.code = "RTTS";
		rtts.label = "RTT Salarié";
		rtts.missionType = MissionType.HOLIDAY;
		rtts.customerId = genesis.id;
		genesisMissions.put(rtts.code, rtts);

		Mission rtte = new Mission();
		rtte.code = "RTTE";
		rtte.label = "RTT Employeur";
		rtte.missionType = MissionType.HOLIDAY;
		rtte.customerId = genesis.id;
		genesisMissions.put(rtte.code, rtte);

		Mission tp = new Mission();
		tp.code = "TP";
		tp.label = "Temps Partiel";
		tp.missionType = MissionType.UNPAID;
		tp.customerId = genesis.id;
		genesisMissions.put(tp.code, tp);

		for (String code : genesisMissions.keySet()) {
			final Mission mission = genesisMissions.get(code);
			mission.insert();
		}
	}

	private static void loadRoles() {
		Map<String, String> map = Maps.newHashMapWithExpectedSize(3);
		map.put(RoleName.ADMINISTRATOR, "Administrateur");
		map.put(RoleName.GESTION, "Gestionnaire");
		map.put(RoleName.COLLABORATOR, "Collaborateur");
		for (String roleName : map.keySet()) {
			if (Role.findByRoleName(roleName) == null) {
				Role role = new Role();
				role.roleName = roleName;
				role.label = map.get(roleName);
				role.insert();
			}
		}
	}

	private static void loadPayRates() {
		PayRate payRate = new PayRate();
		payRate.extraTime125Multiplier = new BigDecimal(1.25);
		payRate.extraTime150Multiplier = new BigDecimal(1.5);
		payRate.sundayRateMultiplier = new BigDecimal(2D);
		payRate.nightlyRateMultiplier = new BigDecimal(1.5);
		payRate.dayOffMultiplier = new BigDecimal(2D);
		payRate.insert();
	}

	private static void loadFeesParameters() {
		FeesParameter feesParameters = new FeesParameter();
		feesParameters.startDate = new DateTime(2012, 4, 1, 0, 0);
		feesParameters.endDate = new DateTime(2013, 3, 31, 0, 0);

		feesParameters.cars.put(0, new BigDecimal("0.33"));
		feesParameters.cars.put(5, new BigDecimal("0.4"));
		feesParameters.cars.put(8, new BigDecimal("0.43"));
		feesParameters.cars.put(11, new BigDecimal("0.44"));

		feesParameters.motorcycles.put(0, new BigDecimal("0.33"));
		feesParameters.motorcycles.put(501, new BigDecimal("0.4"));

		feesParameters.farTravels.put(75, new BigDecimal("62.2"));
		feesParameters.farTravels.put(92, new BigDecimal("62.2"));
		feesParameters.farTravels.put(93, new BigDecimal("62.2"));
		feesParameters.farTravels.put(94, new BigDecimal("62.2"));

		feesParameters.provinceTravel = new BigDecimal("46.2");

		feesParameters.feeAllowances.add(new FeeAllowance(MissionAllowanceType.NONE, BigDecimal.ZERO));
		feesParameters.feeAllowances.add(new FeeAllowance(MissionAllowanceType.ZONE, new BigDecimal("4.70")));
		feesParameters.feeAllowances.add(new FeeAllowance(MissionAllowanceType.CASINO, new BigDecimal("1000")));
		feesParameters.feeAllowances.add(new FeeAllowance(MissionAllowanceType.REAL, null));

		feesParameters.insert();
	}

	private static void createDevCustomersAndMissions() {
		Logger.debug("Create customers and missions...");
		createOneCustomerAndMissions(CUSTOMER_NAME_1);
		createOneCustomerAndMissions(CUSTOMER_NAME_2);
	}

	private static void createOneCustomerAndMissions(String customerName) {
		Customer customer = new Customer();
		customer.name = customerName;
		customer.insert();
		for (int i = 1; i < 3; i++) {
			final Mission mission = new Mission();
			mission.code = customer.name + "_" + MISSION_CODE + i;
			mission.label = customer.name + "_" + MISSION_LABEL + i;
			mission.missionType = MissionType.CUSTOMER;
			mission.customerId = customer.id;
			mission.allowanceType = MissionAllowanceType.ZONE;
			switch (i) {
				case 1:
					mission.startDate = new DateTime(2012, 5, 12, 0, 0);
					mission.endDate = new DateTime(2012, 11, 30, 0, 0);
					mission.standByMoments.add(new StandByMoment(0, "lundi nuit"));
					mission.standByMoments.add(new StandByMoment(1, "mardi nuit"));
					mission.standByMoments.add(new StandByMoment(2, "mercredi nuit"));
					mission.standByMoments.add(new StandByMoment(3, "jeudi nuit"));
					mission.standByMoments.add(new StandByMoment(4, "vendredi nuit"));
					mission.standByMoments.add(new StandByMoment(5, "semaine nuit"));
					mission.standByMoments.add(new StandByMoment(6, "samedi jour"));
					mission.standByMoments.add(new StandByMoment(7, "samedi nuit"));
					mission.standByMoments.add(new StandByMoment(8, "dimanche jour"));
					mission.standByMoments.add(new StandByMoment(9, "dimanche nuit"));
					mission.standByMoments.add(new StandByMoment(10, "week-end"));
					mission.standByMoments.add(new StandByMoment(11, "jour férié"));
					break;
				case 2:
					mission.startDate = new DateTime(2012, 11, 2, 0, 0);
					mission.endDate = new DateTime(2013, 12, 31, 0, 0);
					break;
			}
			mission.insert();
			Logger.debug(String.format("Mission [%s:%s] created.", mission, mission.code));
		}
		customer.update();
	}

	private static void createDevUsers() {
		User homer = new User();
		homer.username = HOMER_USERNAME;
		homer.password = HOMER_PASSWORD;
		homer.trigramme = HOMER_TRIGRAMME;
		homer.email = HOMER_EMAIL;
		homer.firstName = HOMER_FIRSTNAME;
		homer.lastName = HOMER_LASTNAME;
		homer.role = Role.findByRoleName(RoleName.ADMINISTRATOR);
		homer.insert();

		User lisa = new User();
		lisa.username = LISA_USERNAME;
		lisa.password = LISA_PASSWORD;
		lisa.trigramme = LISA_TRIGRAMME;
		lisa.email = LISA_EMAIL;
		lisa.firstName = LISA_FIRSTNAME;
		lisa.lastName = LISA_LASTNAME;
		lisa.role = Role.findByRoleName(RoleName.GESTION);
		lisa.insert();

		Employee marge = new Employee(MARGE_USERNAME);
		marge.password = MARGE_PASSWORD;
		marge.trigramme = MARGE_TRIGRAMME;
		marge.email = MARGE_EMAIL;
		marge.firstName = MARGE_FIRSTNAME;
		marge.lastName = MARGE_LASTNAME;
		marge.role = Role.findByRoleName(RoleName.COLLABORATOR);
		marge.insert();

		Employee ned = new Employee(NED_USERNAME);
		ned.password = NED_PASSWORD;
		ned.trigramme = NED_TRIGRAMME;
		ned.email = NED_EMAIL;
		ned.firstName = NED_FIRSTNAME;
		ned.lastName = NED_LASTNAME;
		ned.role = Role.findByRoleName(RoleName.COLLABORATOR);
		ned.insert();

		Employee bart = new Employee(BART_USERNAME);
		bart.password = BART_PASSWORD;
		bart.trigramme = BART_TRIGRAMME;
		bart.email = BART_EMAIL;
		bart.firstName = BART_FIRSTNAME;
		bart.lastName = BART_LASTNAME;
		bart.role = Role.findByRoleName(RoleName.COLLABORATOR);
		bart.insert();

		Employee moe = new Employee(MOE_USERNAME);
		moe.password = MOE_PASSWORD;
		moe.trigramme = MOE_TRIGRAMME;
		moe.email = MOE_EMAIL;
		moe.firstName = MOE_FIRSTNAME;
		moe.lastName = MOE_LASTNAME;
		moe.role = Role.findByRoleName(RoleName.COLLABORATOR);
		moe.addMissions(CUSTOMER_1_MISSIONS);
		moe.showStandBy = Boolean.TRUE;

        /* PartTime */
		PartTime partTime = new PartTime();
		partTime.startPartTime = new DateTime(2012, 10, 1, 0, 0);
		partTime.daysOfWeek = Lists.newArrayList(DateTimeConstants.WEDNESDAY); // All wednesday
		partTime.morning = Boolean.TRUE;
		partTime.afternoon = Boolean.TRUE;
		partTime.repeatEveryXWeeks = 2;
		partTime.isActive = true;
		moe.partTimes.add(partTime);

        /* Vehicle */
		Vehicle vehicle = new Vehicle();
		vehicle.type = Vehicles.VehicleType.CAR;
		vehicle.power = 5;
		vehicle.brand = Vehicles.Brand.FERRARI.label;
		vehicle.matriculation = "280CRA69";
		moe.vehicle = vehicle;

		moe.insert();

        /* Manager */
		Employee teddy = new Employee(TEDDY_USERNAME);
		teddy.password = TEDDY_PASSWORD;
		teddy.trigramme = TEDDY_TRIGRAMME;
		teddy.email = TEDDY_EMAIL;
		teddy.firstName = TEDDY_FIRSTNAME;
		teddy.lastName = TEDDY_LASTNAME;
		teddy.role = Role.findByRoleName(RoleName.COLLABORATOR);
		teddy.isManager = Boolean.TRUE;
		teddy.insert();

		Employee patrick = new Employee(PATRICK_USERNAME);
		patrick.password = PATRICK_PASSWORD;
		patrick.trigramme = PATRICK_TRIGRAMME;
		patrick.email = PATRICK_EMAIL;
		patrick.firstName = PATRICK_FIRSTNAME;
		patrick.lastName = PATRICK_LASTNAME;
		patrick.role = Role.findByRoleName(RoleName.COLLABORATOR);
		patrick.isManager = Boolean.TRUE;
		patrick.insert();

		moe.manager = teddy;
		moe.update();
	}

	private static void createCras() {

		Logger.info("Create Cra with Periods...");
		final Employee moe = Employee.findByTrigramme(MOE_TRIGRAMME);

		ImmutableMap<String, Mission> bdMissions = Maps.uniqueIndex(Mission.findByCustomer(Customer.findByName(Genesis.NAME)), new Function<Mission, String>() {
			@Override
			public String apply(@Nullable Mission mission) {
				return mission.code;
			}
		});
		if (!Cra.existForEmployee(moe)) {
		    /* Mai 2012 */
		    /* 08/05/2012 */
			final Period p_1_m_05_08 = new Period();
			p_1_m_05_08.mission = CUSTOMER_1_MISSIONS.get(0);
			p_1_m_05_08.startTime = new LocalTime(1, 0);
			p_1_m_05_08.endTime = new LocalTime(2, 0);
			final HalfDay m_05_08 = new HalfDay();
			m_05_08.periods.add(p_1_m_05_08);
			final Day d_05_08 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.MAY, 8, 0, 0));
			d_05_08.morning = m_05_08;

			final Period p_1_m_05_17 = new Period();
			p_1_m_05_17.mission = CUSTOMER_1_MISSIONS.get(0);
			p_1_m_05_17.startTime = new LocalTime(22, 0);
			p_1_m_05_17.endTime = new LocalTime(23, 0);
			final HalfDay m_05_17 = new HalfDay();
			m_05_17.periods.add(p_1_m_05_17);
			final Day d_05_17 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.MAY, 17, 0, 0));
			d_05_17.afternoon = m_05_17;

			final Cra c_05 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.MAY);
			c_05.isValidated = Boolean.TRUE;
			c_05.days.add(d_05_08);
			c_05.days.add(d_05_17);
			c_05.insert();

            /* Juillet 2012 */
            /* 30/07/2012 */
			final Day d_07_30 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.JULY, 30, 0, 0));
			d_07_30.morning = new Holiday(bdMissions.get("MM"), d_07_30.date, HalfDayType.MORNING);
			d_07_30.afternoon = new Holiday(bdMissions.get("MM"), d_07_30.date, HalfDayType.AFTERNOON);
            /* 31/07/2012 */
			final Day d_07_31 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.JULY, 31, 0, 0));
			d_07_31.morning = new Holiday(bdMissions.get("MM"), d_07_31.date, HalfDayType.MORNING);
			d_07_31.afternoon = new Holiday(bdMissions.get("MM"), d_07_31.date, HalfDayType.AFTERNOON);

			final Cra c_07 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.JULY);
			c_07.days.add(d_07_30);
			c_07.days.add(d_07_31);
			c_07.insert();

            /* Août 2012 */
            /* 01/08/2012 */
			final Day d_08_01 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 1, 0, 0));
			d_08_01.morning = new Holiday(bdMissions.get("MM"), d_08_01.date, HalfDayType.MORNING);
			d_08_01.afternoon = new Holiday(bdMissions.get("MM"), d_08_01.date, HalfDayType.AFTERNOON);
            /* 06/08/2012 */
			final Day d_08_06 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 6, 0, 0));
			d_08_06.morning = new Holiday(bdMissions.get("MM"), d_08_06.date, HalfDayType.MORNING);
			d_08_06.afternoon = new Holiday(bdMissions.get("MM"), d_08_06.date, HalfDayType.AFTERNOON);
            /* 07/08/2012 */
			final Day d_08_07 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 7, 0, 0));
			d_08_07.morning = new Holiday(bdMissions.get("MM"), d_08_07.date, HalfDayType.MORNING);
			d_08_07.afternoon = new Holiday(bdMissions.get("MM"), d_08_07.date, HalfDayType.AFTERNOON);
            /* 13/08/2012 */
			final Day d_08_13 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 13, 0, 0));
			d_08_13.morning = new Holiday(bdMissions.get("MM"), d_08_13.date, HalfDayType.MORNING);
			d_08_13.afternoon = new Holiday(bdMissions.get("MM"), d_08_13.date, HalfDayType.AFTERNOON);
            /* 14/08/2012 */
			final Day d_08_14 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 14, 0, 0));
			d_08_14.morning = new Holiday(bdMissions.get("MM"), d_08_14.date, HalfDayType.MORNING);
			d_08_14.afternoon = new Holiday(bdMissions.get("MM"), d_08_14.date, HalfDayType.AFTERNOON);
            /* 20/08/2012 */
			final Day d_08_20 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 20, 0, 0));
			d_08_20.morning = new Holiday(bdMissions.get("MM"), d_08_20.date, HalfDayType.MORNING);
			d_08_20.afternoon = new Holiday(bdMissions.get("MM"), d_08_20.date, HalfDayType.AFTERNOON);
            /* 21/08/2012 */
			final Day d_08_21 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 21, 0, 0));
			d_08_21.morning = new Holiday(bdMissions.get("MM"), d_08_21.date, HalfDayType.MORNING);
			d_08_21.afternoon = new Holiday(bdMissions.get("MM"), d_08_21.date, HalfDayType.AFTERNOON);
            /* 27/08/2012 */
			final Day d_08_27 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 27, 0, 0));
			d_08_27.morning = new Holiday(bdMissions.get("MM"), d_08_27.date, HalfDayType.MORNING);
			d_08_27.afternoon = new Holiday(bdMissions.get("MM"), d_08_27.date, HalfDayType.AFTERNOON);
            /* 28/08/2012 */
			final Day d_08_28 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 28, 0, 0));
			d_08_28.morning = new Holiday(bdMissions.get("MM"), d_08_28.date, HalfDayType.MORNING);
			d_08_28.afternoon = new Holiday(bdMissions.get("MM"), d_08_28.date, HalfDayType.AFTERNOON);
            /* 29/08/2012 */
			final Day d_08_29 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 29, 0, 0));
			d_08_29.morning = new Holiday(bdMissions.get("MM"), d_08_29.date, HalfDayType.MORNING);
			d_08_29.afternoon = new Holiday(bdMissions.get("MM"), d_08_29.date, HalfDayType.AFTERNOON);
            /* 31/08/2012 */
			final Day d_08_31 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.AUGUST, 31, 0, 0));
			d_08_31.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_08_31.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));

			final Cra c_08 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.AUGUST);
			c_08.days.add(d_08_01);
			c_08.days.add(d_08_06);
			c_08.days.add(d_08_07);
			c_08.days.add(d_08_13);
			c_08.days.add(d_08_14);
			c_08.days.add(d_08_20);
			c_08.days.add(d_08_21);
			c_08.days.add(d_08_27);
			c_08.days.add(d_08_28);
			c_08.days.add(d_08_29);
			c_08.days.add(d_08_31);
			c_08.insert();

            /* Septembre 2012 */
            /* 03/09/2012 */
			final HalfDay m_09_03 = new HalfDay();
			m_09_03.mission = CUSTOMER_1_MISSIONS.get(0);
			final HalfDay a_09_03 = new HalfDay();
			a_09_03.mission = CUSTOMER_1_MISSIONS.get(0);
			final Day d_09_03 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 3, 0, 0));
			d_09_03.morning = m_09_03;
			d_09_03.afternoon = a_09_03;
            /* 10/09/2012 */
			final Day d_09_10 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 10, 0, 0));
			d_09_10.morning = new Holiday(bdMissions.get("CP"), d_09_10.date, HalfDayType.MORNING);
			d_09_10.afternoon = new Holiday(bdMissions.get("CP"), d_09_10.date, HalfDayType.AFTERNOON);
            /* 11/09/2012 */
			final Day d_09_11 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 11, 0, 0));
			d_09_11.morning = new Holiday(bdMissions.get("CP"), d_09_11.date, HalfDayType.MORNING);
			d_09_11.afternoon = new Holiday(bdMissions.get("CP"), d_09_11.date, HalfDayType.AFTERNOON);
            /* 12/09/2012 */
			final Day d_09_12 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 12, 0, 0));
			d_09_12.morning = new Holiday(bdMissions.get("CP"), d_09_12.date, HalfDayType.MORNING);
			d_09_12.afternoon = new Holiday(bdMissions.get("CP"), d_09_12.date, HalfDayType.AFTERNOON);
            /* 13/09/2012 */
			final Day d_09_13 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 13, 0, 0));
			d_09_13.morning = new Holiday(bdMissions.get("CP"), d_09_13.date, HalfDayType.MORNING);
			d_09_13.afternoon = new Holiday(bdMissions.get("CP"), d_09_13.date, HalfDayType.AFTERNOON);
            /* 14/09/2012 */
			final Day d_09_14 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 14, 0, 0));
			d_09_14.morning = new Holiday(bdMissions.get("CP"), d_09_14.date, HalfDayType.MORNING);
			d_09_14.afternoon = new Holiday(bdMissions.get("CP"), d_09_14.date, HalfDayType.AFTERNOON);
            /* 28/09/2012 */
			final HalfDay m_09_28 = new HalfDay();
			m_09_28.mission = CUSTOMER_1_MISSIONS.get(0);
			final HalfDay a_09_28 = new HalfDay();
			a_09_28.mission = CUSTOMER_1_MISSIONS.get(0);
			final Day d_09_28 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER, 28, 0, 0));
			d_09_28.morning = m_09_28;
			d_09_28.afternoon = a_09_28;
			final Cra c_09 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.SEPTEMBER);
			c_09.days.add(d_09_03);
			c_09.days.add(d_09_10);
			c_09.days.add(d_09_11);
			c_09.days.add(d_09_12);
			c_09.days.add(d_09_13);
			c_09.days.add(d_09_14);
			c_09.days.add(d_09_28);
			c_09.insert();

            /* Octobre 2012 */
            /* 01/10/2012 */
			final Day d_10_01 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 1, 0, 0));
			d_10_01.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_10_01.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 02/10/2012 */
			final Period p_1_m_10_02 = new Period();
			p_1_m_10_02.mission = CUSTOMER_1_MISSIONS.get(0);
			p_1_m_10_02.startTime = new LocalTime(4, 0);
			p_1_m_10_02.endTime = new LocalTime(8, 0);
			final Period p_2_m_10_02 = new Period();
			p_2_m_10_02.mission = CUSTOMER_1_MISSIONS.get(0);
			p_2_m_10_02.startTime = new LocalTime(9, 0);
			p_2_m_10_02.endTime = new LocalTime(11, 0);
			final HalfDay m_10_02 = new HalfDay();
			m_10_02.periods.add(p_1_m_10_02);
			m_10_02.periods.add(p_2_m_10_02);
			final Day d_10_02 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 2, 0, 0));
			d_10_02.morning = m_10_02;
			d_10_02.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 08/10/2012 */
			final Day d_10_08 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 8, 0, 0));
			d_10_08.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_10_08.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 09/10/2012 */
			final Day d_10_09 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 9, 0, 0));
			d_10_09.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_10_09.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 15/10/2012 */
			final Day d_10_15 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 15, 0, 0));
			d_10_15.morning = new Holiday(bdMissions.get("CP"), d_10_15.date, HalfDayType.MORNING, "Enfant malade");
			d_10_15.afternoon = new Holiday(bdMissions.get("CP"), d_10_15.date, HalfDayType.AFTERNOON);

            /* 18/10/2012 */
			final Day d_10_18 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 18, 0, 0));
			d_10_18.morning = new Holiday(bdMissions.get("CP"), d_10_18.date, HalfDayType.MORNING);
            /* 29/10/2012 */
			final Day d_10_29 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 29, 0, 0));
			d_10_29.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_10_29.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 30/10/2012 */
			final Day d_10_30 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.OCTOBER, 30, 0, 0));
			d_10_30.morning = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			d_10_30.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));

			Cra c_10 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.OCTOBER);
			c_10.days.add(d_10_01);
			c_10.days.add(d_10_02);
			c_10.days.add(d_10_08);
			c_10.days.add(d_10_09);
			c_10.days.add(d_10_15);
			c_10.days.add(d_10_18);
			c_10.days.add(d_10_29);
			c_10.days.add(d_10_30);
			c_10.insert();
			Cra.validate(c_10.id);

            /* Novembre 2012 */
            /* 01/11/2012 */
			final Period p_1_m_11_01 = new Period();
			p_1_m_11_01.mission = CUSTOMER_1_MISSIONS.get(0);
			p_1_m_11_01.startTime = new LocalTime(4, 0);
			p_1_m_11_01.endTime = new LocalTime(8, 0);
			final Period p_2_m_11_01 = new Period();
			p_2_m_11_01.mission = CUSTOMER_1_MISSIONS.get(0);
			p_2_m_11_01.startTime = new LocalTime(9, 0);
			p_2_m_11_01.endTime = new LocalTime(11, 0);
			final HalfDay m_11_01 = new HalfDay();
			m_11_01.periods.add(p_1_m_11_01);
			m_11_01.periods.add(p_2_m_11_01);
			final Day d_11_01 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.NOVEMBER, 1, 0, 0));
			d_11_01.morning = m_11_01;
			d_11_01.afternoon = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
            /* 02/11/2012 */
			final HalfDay m_11_02 = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			final HalfDay a_11_02 = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			final Day d_11_02 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.NOVEMBER, 2, 0, 0));
			d_11_02.morning = m_11_02;
			d_11_02.afternoon = a_11_02;
			/* 05/11/2012 */
			final HalfDay m_11_05 = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			final HalfDay a_11_05 = new HalfDay(CUSTOMER_1_MISSIONS.get(0));
			final Day d_11_05 = new Day(new DateTime(PERIOD_YEAR_2012, DateTimeConstants.NOVEMBER, 5, 0, 0));
			d_11_05.morning = m_11_05;
			d_11_05.afternoon = a_11_05;
            /* StandBy Moments */
            /* Vendredi nuit */
			final StandByMoment sbm_v = CUSTOMER_1_MISSIONS.get(0).standByMoments.get(4);
            /* Samedi jour */
			final StandByMoment sbm_sj = CUSTOMER_1_MISSIONS.get(0).standByMoments.get(6);
            /* Samedi jour */
			final StandByMoment sbm_we = CUSTOMER_1_MISSIONS.get(0).standByMoments.get(10);
            /* StandBy week 44 */
			final StandBy sb_44_v = new StandBy(CUSTOMER_1_MISSIONS.get(0).id, sbm_v.index);
			final StandBy sb_44_sj = new StandBy(CUSTOMER_1_MISSIONS.get(0).id, sbm_sj.index);
            /* StandBy week 45 */
			final StandBy sb_45_we = new StandBy(CUSTOMER_1_MISSIONS.get(0).id, sbm_we.index);
            /* Fees */
			final Fee fee_11_09__1 = new Fee();
			fee_11_09__1.date = new DateTime(2012, 11, 9, 0, 0);
			fee_11_09__1.week = fee_11_09__1.date.getWeekOfWeekyear();
			fee_11_09__1.type = FeeType.PARKING;
			fee_11_09__1.description = "RDV avec Cécile Caprio (péage)";
			fee_11_09__1.amount = new BigDecimal("2");
			fee_11_09__1.compute(moe.trigramme);
			final Fee fee_11_09__2 = new Fee();
			fee_11_09__2.date = new DateTime(2012, 11, 9, 0, 0);
			fee_11_09__2.week = fee_11_09__1.date.getWeekOfWeekyear();
			fee_11_09__2.type = FeeType.KILOMETER;
			fee_11_09__2.kilometers = new BigDecimal("25");
			fee_11_09__2.journey = "Genesis Brenntag";
			fee_11_09__2.description = "RDV avec Cécile Caprio (péage)";
			fee_11_09__2.compute(moe.trigramme);

			final Fee fee_11_15__1 = new Fee();
			fee_11_15__1.date = new DateTime(2012, 11, 15, 0, 0);
			fee_11_15__1.week = fee_11_15__1.date.getWeekOfWeekyear();
			fee_11_15__1.type = FeeType.PARKING;
			fee_11_15__1.description = "RDV avec Frédéric Patin";
			fee_11_15__1.amount = new BigDecimal("9");
			fee_11_15__1.compute(moe.trigramme);
			final Fee fee_11_15__2 = new Fee();
			fee_11_15__2.date = new DateTime(2012, 11, 15, 0, 0);
			fee_11_15__2.week = fee_11_15__2.date.getWeekOfWeekyear();
			fee_11_15__2.type = FeeType.KILOMETER;
			fee_11_15__2.kilometers = new BigDecimal("10");
			fee_11_15__2.journey = "Genesis Partdieu";
			fee_11_15__2.description = "RDV avec Frédéric Patin";
			fee_11_15__2.compute(moe.trigramme);
            /* CRA Novembre 2012 */
			final Cra c_11 = Cra.getOrCreate(moe.trigramme, PERIOD_YEAR_2012, DateTimeConstants.NOVEMBER);
			c_11.isValidated = Boolean.TRUE;
			c_11.addStandBy(44, sb_44_v);
			c_11.addStandBy(44, sb_44_sj);
			c_11.addStandBy(45, sb_45_we);
			c_11.days.add(d_11_01);
			c_11.days.add(d_11_02);
			c_11.days.add(d_11_05);
			c_11.fees.add(fee_11_09__1);
			c_11.fees.add(fee_11_09__2);
			c_11.fees.add(fee_11_15__1);
			c_11.fees.add(fee_11_15__2);
			c_11.insert();
			//Cra.validate(c_11.id);

			Logger.debug("Cras created !");
		}

	}
}
