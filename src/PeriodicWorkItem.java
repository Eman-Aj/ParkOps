/** One detected booking/policy state that needs supervised resolution. */
public record PeriodicWorkItem(String bookingId, int bookingVersion, int policyVersion,
                               String closedSpaceId, long detectedAtSeconds) { }
