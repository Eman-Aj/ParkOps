import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Explicit, deterministic 60-second detector; it never creates a thread or sleeps. */
public final class PeriodicTaskDetector {
    private record StateKey(String bookingId, int bookingVersion, int policyVersion) { }

    private final Booking booking;
    private final List<ParkingSpace> spaces;
    private final ProposalService proposals;
    private final long periodSeconds;
    private final Set<StateKey> emitted = new HashSet<>();
    private final Set<StateKey> explicitlyRetried = new HashSet<>();
    private Long previousTick;
    private long nextBoundary;

    public PeriodicTaskDetector(Booking booking, List<ParkingSpace> spaces,
                                ProposalService proposals, long periodSeconds) {
        if (booking == null || spaces == null || proposals == null || periodSeconds <= 0)
            throw new IllegalArgumentException("booking, spaces, proposals and positive period are required");
        this.booking = booking;
        this.spaces = List.copyOf(spaces);
        this.proposals = proposals;
        this.periodSeconds = periodSeconds;
    }

    /** First tick scans immediately; later scans occur on the 60-second boundary. */
    public synchronized PeriodicTickResult tick(long nowSeconds) {
        if (nowSeconds < 0) throw new IllegalArgumentException("tick time cannot be negative");
        if (previousTick != null && nowSeconds < previousTick)
            throw new IllegalArgumentException("clock moved backwards");
        boolean firstTick = previousTick == null;
        previousTick = nowSeconds;

        if (!firstTick && nowSeconds < nextBoundary) return new PeriodicTickResult(nowSeconds, false, List.of());
        nextBoundary = Math.addExact(nowSeconds, periodSeconds);

        ParkingSpace assigned = spaces.stream().filter(s -> s.id().equals(booking.spaceId())).findFirst().orElse(null);
        if (assigned == null || assigned.isOpen()) return new PeriodicTickResult(nowSeconds, true, List.of());

        StateKey key = new StateKey(booking.id(), booking.version(), ProposalService.currentPolicyVersion());
        if (proposals.hasActiveProposal(key.bookingId(), key.bookingVersion(), key.policyVersion()))
            return new PeriodicTickResult(nowSeconds, true, List.of());
        boolean retry = explicitlyRetried.remove(key);
        if (emitted.contains(key) && !retry) return new PeriodicTickResult(nowSeconds, true, List.of());
        if (proposals.hasProposalForVersion(key.bookingId(), key.bookingVersion(), key.policyVersion())
                && !retry) return new PeriodicTickResult(nowSeconds, true, List.of());

        emitted.add(key);
        return new PeriodicTickResult(nowSeconds, true, List.of(new PeriodicWorkItem(booking.id(), booking.version(), key.policyVersion(),
                assigned.id(), nowSeconds)));
    }

    /** Allows one new automatic work item for the current state after an operator retry. */
    public synchronized void retryCurrentState(String bookingId) {
        if (!booking.id().equals(bookingId)) throw new IllegalArgumentException("unknown booking: " + bookingId);
        explicitlyRetried.add(new StateKey(booking.id(), booking.version(), ProposalService.currentPolicyVersion()));
    }
}
