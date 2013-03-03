package controllers;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import constants.FeeType;
import models.AffectedMission;
import models.Employee;
import models.Fee;
import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import security.RoleName;
import testUtils.AbstractTest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.libs.Json.toJson;
import static play.test.Helpers.*;

/**
 * @author LPU
 */
public class FeesTest extends AbstractTest {

	private static final Function<Fee, FeeType> EXTRACT_FEE_TYPE = new Function<Fee, FeeType>() {
		@Override
		public FeeType apply(@Nullable Fee fee) {
			return fee.type;
		}
	};
	private List<AffectedMission> missionForFee;

	@Override
	protected String getConnexionRole() {
		return RoleName.COLLABORATOR;
	}

	@Override
	protected void postBeforeEachTest() {
		missionForFee = Employee.fetchFeesMission(collaborator.trigramme);
	}

	@Override
	protected void preAfterEachTest() {

	}

	@Test
	public void testCreateFeeWithoutKilometer() {
	    /* Preparation */

        /* Test */
		Map<String, Object> form = Maps.newHashMap();
		form.put("trigramme", collaborator.trigramme);
		form.put("missionId", missionForFee.get(0).mission.id.toStringMongod());
		form.put("date", "12/12/2012");
		form.put("type", FeeType.PARKING.name());
		form.put("amount", "9.6");
		form.put("description", "RDV avec User Collab n°2");

		Result result = callAction(
				routes.ref.Fees.create(),
				fakeRequest("POST", "/fees").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(form))
		);
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(
				routes.ref.Fees.fetch(collaborator.trigramme, 2012, 12),
				fakeRequest("GET", "/fees/:trigramme/:year/:month").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(form))
		);
		assertThat(status(result)).isEqualTo(OK);
		List<Fee> fees = Lists.transform(Lists.newArrayList(Json.parse(contentAsString(result))), new Function<JsonNode, Fee>() {
			@Override
			public Fee apply(@Nullable JsonNode jsonNodes) {
				return Json.fromJson(jsonNodes, Fee.class);
			}
		});
		assertThat(fees.size()).isEqualTo(1);
		Iterable<FeeType> feeTypes = Iterables.transform(fees, EXTRACT_FEE_TYPE);
		assertThat(feeTypes).contains(FeeType.PARKING);

	}

	@Test
	public void testCreateFeeWithKilometer() {
	    /* Preparation */

        /* Test */
		Map<String, Object> form = Maps.newHashMap();
		form.put("trigramme", collaborator.trigramme);
		form.put("missionId", missionForFee.get(0).mission.id.toStringMongod());
		form.put("date", "12/12/2012");
		form.put("kilometers", "15");
		form.put("journey", "Déplacement");
		form.put("type", FeeType.PARKING.name());
		form.put("amount", "9.6");
		form.put("description", "RDV avec User Collab n°2");

		Result result = callAction(
				routes.ref.Fees.create(),
				fakeRequest("POST", "/fees").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(form))
		);
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(
				routes.ref.Fees.fetch(collaborator.trigramme, 2012, 12),
				fakeRequest("GET", "/fees/:trigramme/:year/:month").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(form))
		);
		assertThat(status(result)).isEqualTo(OK);
		List<Fee> fees = Lists.transform(Lists.newArrayList(Json.parse(contentAsString(result))), new Function<JsonNode, Fee>() {
			@Override
			public Fee apply(@Nullable JsonNode jsonNodes) {
				return Json.fromJson(jsonNodes, Fee.class);
			}
		});
		assertThat(fees.size()).isEqualTo(2);
		Iterable<FeeType> feeTypes = Iterables.transform(fees, EXTRACT_FEE_TYPE);
		assertThat(feeTypes).containsOnly(FeeType.KILOMETER, FeeType.PARKING);
	}

	@Test
	public void testDeleteFee() {
        /* Preparation */
		Map<String, Object> form = Maps.newHashMap();
		form.put("trigramme", collaborator.trigramme);
		form.put("missionId", missionForFee.get(0).mission.id.toStringMongod());
		form.put("date", "12/12/2012");
		form.put("type", FeeType.PARKING.name());
		form.put("amount", "9.6");
		form.put("description", "RDV avec User Collab n°2");

		callAction(
				routes.ref.Fees.create(),
				fakeRequest("POST", "/fees").withCookies(cookie(COOKIE_NAME, connexion)).withJsonBody(toJson(form))
		);

        /* Test */

	}

}
