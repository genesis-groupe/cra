package dto.customer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.ObjectIdSerializer;
import models.Customer;
import models.Mission;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author f.patin
 */
public class CustomerLightDTO {

	public static class MissionDTO {
		public static final Function<Mission, MissionDTO> TO_MISSION_DTO = new Function<Mission, MissionDTO>() {
			@Override
			public MissionDTO apply(@Nullable final Mission mission) {
				return new MissionDTO(mission);
			}
		};

		@JsonSerialize(using = ObjectIdSerializer.class)
		public ObjectId id;
		public String code;
		public String label;
		@JsonSerialize(using = DateTimeSerializer.class)
		public DateTime startDate;
		@JsonSerialize(using = DateTimeSerializer.class)
		public DateTime endDate;

		public MissionDTO(final Mission mission) {
			this.id = mission.id;
			this.code = mission.code;
			this.label = mission.label;
			this.startDate = mission.startDate;
			this.endDate = mission.endDate;
		}

		public static MissionDTO of(final Mission mission) {
			return new MissionDTO(mission);
		}

		public static List<MissionDTO> of(final List<Mission> missions) {
			return Lists.transform(missions, TO_MISSION_DTO);
		}
	}

	@JsonSerialize(using = ObjectIdSerializer.class)
	public ObjectId id;

	public String code;

	public String name;

	public String finalCustomer;

	public Boolean isGenesis = Boolean.FALSE;

	public CustomerLightDTO(final Customer customer) {
		this.id = customer.id;
		this.code = customer.code;
		this.name = customer.name;
		this.finalCustomer = customer.finalCustomer;
		this.isGenesis = customer.isGenesis;
	}

	public static CustomerLightDTO of(final Customer customer) {
		return new CustomerLightDTO(customer);
	}

	public static List<CustomerLightDTO> of(final List<Customer> customers) {
		return Lists.transform(customers, new Function<Customer, CustomerLightDTO>() {
			@Override
			public CustomerLightDTO apply(@Nullable final Customer customer) {
				return CustomerLightDTO.of(customer);
			}
		});
	}
}
