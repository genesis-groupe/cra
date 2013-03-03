package controllers;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import models.Cra;
import models.Customer;
import models.Mission;
import org.codehaus.jackson.JsonNode;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import security.RoleName;
import testUtils.AbstractTest;
import testUtils.TestFactory;

import javax.annotation.Nullable;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * @author f.patin
 */
public class CrasTest extends AbstractTest {

	private static final Integer YEAR = 2012;
	private static final Integer MONTH = DateTimeConstants.NOVEMBER;

	@Override
	protected String getConnexionRole() {
		return RoleName.COLLABORATOR;
	}

	@Override
	protected void postBeforeEachTest() {

	}

	@Override
	protected void preAfterEachTest() {
	}

	@Test
	public void testFetchCustomers() {
	    /* Preparation */
		final Customer customer = TestFactory.newCustomer("customer");
		final Mission mission = TestFactory.newMission("M1", "ML1", customer.id);
		collaborator.addMission(mission);
		collaborator.update();
		final Cra cra = TestFactory.newCra(collaborator, YEAR, MONTH);
		TestFactory.addDay(cra, 5, mission);
		TestFactory.addDay(cra, 6, mission);
		TestFactory.addDay(cra, 7, mission);

        /* Test */
		Result result = callAction(
				routes.ref.Cras.fetchCustomers(collaborator.trigramme, 2012, 11),
				fakeRequest("GET", "/cra/:trigramme/:year/:month/customers").withCookies(cookie(COOKIE_NAME, connexion))
		);
		assertThat(status(result)).isEqualTo(OK);
		List<Customer> customers = Lists.transform(Lists.newArrayList(Json.parse(contentAsString(result))), new Function<JsonNode, Customer>() {
			@Override
			public Customer apply(@Nullable JsonNode jsonNodes) {
				return Json.fromJson(jsonNodes, Customer.class);
			}
		});
		assertThat(customers.size()).isEqualTo(1);

	}
}