package testUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import constants.Genesis;
import constants.MissionType;
import helpers.SecurityHelper;
import leodagdag.play2morphia.MorphiaPlugin;
import models.Customer;
import models.Employee;
import models.Mission;
import models.Role;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import play.mvc.Result;
import play.test.FakeApplication;
import security.RoleName;

import javax.annotation.Nullable;
import java.util.Map;

import static play.test.Helpers.*;

/**
 * @author f.patin
 */
public abstract class AbstractTest {

	public static final String COOKIE_NAME = "GENESIS_SESSION";

	protected Employee collaborator;
	protected Employee gestion;
	protected Employee admin;
	protected Result connexion;
	private static final String PASSWORD = "fake_password";
	private static final String MD5_PASSWORD = SecurityHelper.md5(PASSWORD);
	private static FakeApplication application;

	/**
	 * Indicate the roleName use for connexion
	 *
	 * @return a RoleName
	 */
	protected abstract String getConnexionRole();

	/**
	 * Execute a task before a test
	 */
	protected abstract void postBeforeEachTest();

	/**
	 * Execute task after a test
	 */
	protected abstract void preAfterEachTest();

	@BeforeClass
	public static void beforeClass(){
		final Map<String, String> additionalConfiguration = Maps.newHashMap();
		additionalConfiguration.put("morphia.db.name","cra_test");
		application = fakeApplication(additionalConfiguration);
		start(application);
	}

	@Before
	public void setUp() {
		initDB();

		collaborator = createCollaborator();
		gestion = createGestion();
		admin = createAdministrator();

		if (getConnexionRole() != null) {
			String username = null;
			switch (getConnexionRole()) {
				case RoleName.COLLABORATOR:
					username = collaborator.username;
					break;
				case RoleName.GESTION:
					username = gestion.username;
					break;
				case RoleName.ADMINISTRATOR:
					username = admin.username;
					break;
				default:
					break;
			}
			connexion = AutenticationFactory.doAuthentication(username, PASSWORD);
		}
		postBeforeEachTest();
	}

	@After
	public void tearDown() {
		preAfterEachTest();
		cleanBD();
	}

	@AfterClass
	public static void afterClass(){
		stop(application);
	}
	private void cleanBD() {
		// Nettoyage BD
		for (String colName : MorphiaPlugin.db().getCollectionNames()) {
			if (!colName.startsWith("system.") && !colName.startsWith("fs.")) {
				MorphiaPlugin.db().getCollection(colName).drop();
			}
		}
		MorphiaPlugin.ds().ensureCaps();
		MorphiaPlugin.ds().ensureIndexes();
	}

	private Employee createCollaborator() {
		Employee userCollab = new Employee();
		userCollab.username = "collab";
		userCollab.password = MD5_PASSWORD;  //userCollab
		userCollab.trigramme = "UGG";
		userCollab.email = "user@collab.com";
		userCollab.firstName = "user";
		userCollab.lastName = "Collab";
		userCollab.role = Role.findByRoleName(RoleName.COLLABORATOR);
		userCollab.addMissions(Mission.getGenesisMissions());
		return (Employee) userCollab.insert();
	}

	private Employee createGestion() {
		Employee userCollab = new Employee();
		userCollab.username = "gestion";
		userCollab.password = MD5_PASSWORD;  //userCollab
		userCollab.trigramme = "GGG";
		userCollab.email = "user@collab.com";
		userCollab.firstName = "user";
		userCollab.lastName = "Gestion";
		userCollab.role = Role.findByRoleName(RoleName.GESTION);
		return (Employee) userCollab.insert();
	}

	private Employee createAdministrator() {
		Employee userCollab = new Employee();
		userCollab.username = "admin";
		userCollab.password = MD5_PASSWORD;  //userCollab
		userCollab.trigramme = "AGG";
		userCollab.email = "admin@genesis-groupe.com";
		userCollab.firstName = "user";
		userCollab.lastName = "Admin";
		userCollab.role = Role.findByRoleName(RoleName.ADMINISTRATOR);
		userCollab.addMissions(Mission.getGenesisMissions());
		return (Employee) userCollab.insert();
	}

	private void loadCustomerGenesis() {
		if (Customer.findByName(Genesis.NAME) == null) {
			Customer genesis = new Customer(Genesis.NAME);
			genesis.isGenesis= Boolean.TRUE;
			genesis.insert();
		}
	}

	private void loadGenesisMissions() {

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

		Mission rompus = new Mission();
		rompus.code = "R";
		rompus.label = "Rompus";
		rompus.missionType = MissionType.INTERNAL_WORK;
		rompus.customerId = genesis.id;
		genesisMissions.put(rompus.code, rompus);

		Mission formation = new Mission();
		formation.code = "F";
		formation.label = "Formation";
		formation.missionType = MissionType.INTERNAL_WORK;
		formation.customerId = genesis.id;
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

		ImmutableMap<String, Mission> bdMissions = Maps.uniqueIndex(Mission.getGenesisMissions(), new Function<Mission, String>() {
			@Override
			public String apply(@Nullable Mission mission) {
				return mission.code;
			}
		});
		for (String code : genesisMissions.keySet()) {
			if (!bdMissions.keySet().contains(code)) {
				final Mission mission = genesisMissions.get(code);
				mission.insert();
			}
		}

	}

	private void loadRoles() {
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

	private void initDB() {
		cleanBD();
		loadRoles();
		loadCustomerGenesis();
		loadGenesisMissions();

	}


}