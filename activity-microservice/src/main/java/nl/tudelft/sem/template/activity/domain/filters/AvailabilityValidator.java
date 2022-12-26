package nl.tudelft.sem.template.activity.domain.filters;

import java.time.LocalDate;
import java.time.ZoneId;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Availability;
import nl.tudelft.sem.template.activity.domain.User;

public class AvailabilityValidator extends BaseValidator {
    @Override
    public boolean handle(User user, Activity activity) {
        for (Availability availability : user.getAvailabilities()) {
            LocalDate activityDate = activity.getActivityDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (availability.getDayOfWeek() == activityDate.getDayOfWeek().getValue()) {
                if (!activity.getStartTime().isBefore(availability.getStartTime())
                    && !activity.getEndTime().isBefore(availability.getStartTime())
                    && !activity.getStartTime().isAfter(availability.getEndTime())
                    && !activity.getEndTime().isAfter(availability.getEndTime())) {
                    return super.checkNext(user, activity);
                }
            }
        }
        return false;
    }
}
