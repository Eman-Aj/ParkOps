import java.util.List;

/** Describes whether an explicit clock tick reached a scan boundary. */
public record PeriodicTickResult(long timeSeconds, boolean scanned, List<PeriodicWorkItem> workItems) {
    public PeriodicTickResult { workItems = List.copyOf(workItems); }
}
