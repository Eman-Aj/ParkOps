/**
 * Immutable proposal to reassign a booking; creating one never changes the
 * booking.
 */
public final class Proposal {
    public enum Status {
        PENDING, REJECTED, STALE, COMMITTED
    }

    private final String proposalId;
    private final String bookingId;
    private final String targetId;
    private final int bookingVersion;
    private final int policyVersion;
    private Status status;

    public Proposal(String proposalId,
            String bookingId,
            String targetId,
            int bookingVersion,
            int policyVersion,
            Status status) {

        // Same guardrails
        DomainRules.requireIdentifier(proposalId);
        DomainRules.requireIdentifier(bookingId);
        DomainRules.requireIdentifier(targetId);
        
        //Version Matching, we don't recieve any stale(old) requests
        if (bookingVersion < 0 || policyVersion < 1 || status == null) {
            throw new IllegalArgumentException("Invalid proposal state");
        }

        this.proposalId = proposalId;
        this.bookingId = bookingId;
        this.targetId = targetId;
        this.bookingVersion = bookingVersion;
        this.policyVersion = policyVersion;
        this.status = status;
    }

    //Getters
    public String proposalId() {
        return proposalId;
    }

    public String bookingId() {
        return bookingId;
    }

    public String targetId() {
        return targetId;
    }

    public int bookingVersion() {
        return bookingVersion;
    }

    public int policyVersion() {
        return policyVersion;
    }

    public Status status() {
        return status;
    }

    //Makes a new proposal view object 
    public ProposalView view() {
        return new ProposalView(proposalId, bookingId, targetId, bookingVersion, policyVersion, status.name());
    }

    void transition(Status next) {
        if (status != Status.PENDING) throw new IllegalStateException("Only a pending proposal can change state");
        if (next == null || next == Status.PENDING) throw new IllegalArgumentException("A terminal proposal state is required");
        status = next;
    }
}
