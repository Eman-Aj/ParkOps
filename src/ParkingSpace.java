import java.util.Map;

/** A parking space and its availability state. */
public final class ParkingSpace {
    private final String id;
    private final boolean open;
    private final boolean occupied;
    private final boolean accessible;

    public ParkingSpace(String id, boolean open, boolean occupied, boolean accessible) {
        DomainRules.requireIdentifier(id);
        this.id = id;
        this.open = open;
        this.occupied = occupied;
        this.accessible = accessible;
    }

    public String id() { return id; }
    public boolean isOpen() { return open; }
    public boolean isOccupied() { return occupied; }
    public boolean isAccessible() { return accessible; }
    public boolean isAvailable() { return open && !occupied; }

    // This snapshot method is passed from the student application to the main - the mapping can then become a json
    public Map<String, Object> snapshot() {
        return Map.of("id", id, "open", open, "occupied", occupied, "accessible", accessible);
    }
}
