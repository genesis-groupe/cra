package dto.employee;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import models.AffectedMission;
import models.Employee;
import models.Role;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author f.patin
 */
public class EmployeeLightDTO {
    @JsonSerialize(using = ObjectIdSerializer.class)
    public final ObjectId id;
    public final String username;
    public final String firstName;
    public final String lastName;
    public final String trigramme;
    public final String email;
    public final Role role;
    public final Boolean isManager;
    public final List<AffectedMissionDTO> affectedMissions = Lists.newArrayList();

    public EmployeeLightDTO(final Employee employee) {
        this.id = employee.id;
        this.username = employee.username;
        this.firstName = employee.firstName;
        this.lastName = employee.lastName;
        this.trigramme = employee.trigramme;
        this.email = employee.email;
        this.role = employee.role;
        this.isManager = employee.isManager;
        this.affectedMissions.addAll(Lists.transform(employee.affectedMissions, new Function<AffectedMission, AffectedMissionDTO>() {
            @Override
            public AffectedMissionDTO apply(@Nullable AffectedMission affectedMission) {
                return new AffectedMissionDTO(affectedMission);
            }
        }));
    }

	public static EmployeeLightDTO of(final Employee employee) {
		return new EmployeeLightDTO(employee);
	}
}
