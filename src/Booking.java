import java.util.Map;

/** Protected domain representation of a parking booking. */
public final class Booking {
    //Base input fields for the domain
    private final String id;
    private String spaceId;
    private final int durationHours;
    private int version;
    private final boolean requiresAccessible;

    public Booking(String id, String spaceId, int durationHours, int version, boolean requiresAccessible) {
        // Guard Rails to ensure correct timing and id Naming conventions
        DomainRules.requireIdentifier(id);
        DomainRules.requireIdentifier(spaceId);
        DomainRules.requireDuration(durationHours);

        //Version checking to prevent 
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        this.id = id;
        this.spaceId = spaceId;
        this.durationHours = durationHours;
        this.version = version;
        this.requiresAccessible = requiresAccessible;
    }

    public String id() { return id; }
    public String spaceId() { return spaceId; }
    public int durationHours() { return durationHours; }
    public int version() { return version; }
    public boolean requiresAccessible() { return requiresAccessible; }

    // This snapshot method is passed from the student application to the main - the mapping can then become a json
    public Map<String, Object> snapshot() {
        return Map.of("id", id, "spaceId", spaceId, "durationHours", durationHours,
                "version", version, "requiresAccessible", requiresAccessible);
    }

    /** Commit a reassignment after application-level approval checks have passed. */
    void reassign(String newSpaceId) {
        DomainRules.requireIdentifier(newSpaceId);
        if (newSpaceId.equals(spaceId)) throw new IllegalArgumentException("Booking is already assigned to that space");
        spaceId = newSpaceId;
        version++;
    }
}
