package dto.employee;

import controllers.Employees;
import dto.mission.MissionLightDTO;
import helpers.serializer.DateTimeSerializer;
import models.AffectedMission;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

/**
* @author f.patin
*/
public class AffectedMissionDTO {
    public final MissionLightDTO mission;
    @JsonSerialize(using = DateTimeSerializer.class)
    public final DateTime startDate;
    @JsonSerialize(using = DateTimeSerializer.class)
    public final DateTime endDate;

    public AffectedMissionDTO(final AffectedMission affectedMission) {
        this.mission = new MissionLightDTO(affectedMission.mission);
        this.startDate = affectedMission.startDate;
        this.endDate = affectedMission.endDate;
    }

    public static AffectedMissionDTO of(final AffectedMission affectedMission) {
        return new AffectedMissionDTO(affectedMission);
    }
}
