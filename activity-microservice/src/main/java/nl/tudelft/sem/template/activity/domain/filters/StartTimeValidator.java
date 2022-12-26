package nl.tudelft.sem.template.activity.domain.filters;

import java.time.ZoneId;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.MyClock;
import nl.tudelft.sem.template.activity.domain.User;

@Data
@NoArgsConstructor
public class StartTimeValidator extends BaseValidator {
    MyClock myClock;

    private final long trainingTime = 30;

    private final long competitionTime = 24 * 60;

    @Override
    public boolean handle(User user, Activity activity) {
        long activityDateWithTime = activity.getActivityDate().toInstant()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        activityDateWithTime +=
                (activity.getStartTime().getHour() * 60 + activity.getStartTime().getMinute()) * 60 * 1000;
        long currentDate = myClock.getCurrentTime();

        long diff = (activityDateWithTime - currentDate) / (60 * 1000);

        if (activity instanceof Competition) {
            if (diff < competitionTime) {
                return false;
            }
        } else {
            if (diff < trainingTime) {
                return false;
            }
        }
        return super.checkNext(user, activity);
    }

}
