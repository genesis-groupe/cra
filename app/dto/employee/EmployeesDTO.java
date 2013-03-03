package dto.employee;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import helpers.serializer.ObjectIdSerializer;
import models.Employee;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author f.patin
 */
public class EmployeesDTO {

	public static final Function<Employee, EmployeesDTO> TO_EMPLOYEES_DTO = new Function<Employee, EmployeesDTO>() {
		@Override
		public EmployeesDTO apply(@Nullable Employee employee) {
			return new EmployeesDTO(employee);
		}
	};

	@JsonSerialize(using = ObjectIdSerializer.class)
	public ObjectId id;
	public String username;
	public String trigramme;
	public String firstName;
	public String lastName;

	public EmployeesDTO(final Employee employee) {
		this.id = employee.id;
		this.username = employee.username;
		this.trigramme = employee.trigramme;
		this.firstName = employee.firstName;
		this.lastName = employee.lastName;
	}

	public static EmployeesDTO of(final Employee employee) {
		return new EmployeesDTO(employee);
	}

	public static List<EmployeesDTO> of(final List<Employee> employees) {
		return Lists.transform(employees, TO_EMPLOYEES_DTO);
	}

}
