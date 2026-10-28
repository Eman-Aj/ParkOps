import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.net.http.HttpTimeoutException;

/** Minimal D1 application boundary over the protected domain model. */
public final class StudentApplication {
    private static final String BOOKING_ID = "B1";
    private static final Set<String> TOOLS = Set.of(
            "READ_BOOKING", "LIST_ALTERNATIVES", "PROPOSE_REASSIGNMENT", "FINISH");
    private static final String SYSTEM_PROMPT = "You are a supervised parking assistant. Use exactly one JSON object per response with exactly three string fields: tool, bookingId, targetId. Allowed tools: READ_BOOKING, LIST_ALTERNATIVES, PROPOSE_REASSIGNMENT, FINISH. For READ_BOOKING and LIST_ALTERNATIVES use the goal booking ID and an empty targetId. Read the booking, then list alternatives, then you may propose a target. A proposal only creates a pending proposal; never claim approval or execution. Use FINISH with the goal booking ID and empty targetId when done. Tool results arrive as observations in later conversation messages.";
    //I Just hard coded the same data that was in the Fixture.java file, to speed things up
    private final Booking booking = new Booking("B1", 
    "A17", 
    3, 
    0, 
    true);
    private final List<ParkingSpace> spaces = List.of(
            new ParkingSpace("A17", false, true, false),
            new ParkingSpace("B12", true, false, false),
            new ParkingSpace("C03", true, false, true),
            new ParkingSpace("D09", true, true, true));
            
    //Passes the fixture data to the proposal service so it knows the relvant data to use in this scenario
    private final ProposalService proposalService = new ProposalService(booking, spaces);

    // This snapshot method is passed to the main - the mapping can then become a json
    public Map<String, Object> bookingSnapshot() { return booking.snapshot(); }

    public ProposalView propose(String targetId) {
        return proposalService.propose(targetId).view();
    }

    public String operatorDecision(String proposalId, boolean approve) {
        return proposalService.decide(proposalId, approve);
    }

    public ProposalView proposal(String proposalId) { return proposalService.proposal(proposalId); }

    public int notificationCount() { return proposalService.notificationCount(); }

    public String run(ModelProvider provider, int limit) {
        if (provider == null) throw new IllegalArgumentException("provider required");
        if (limit < 1 || limit > 6) throw new IllegalArgumentException("call limit must be between 1 and 6");

        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage("system", SYSTEM_PROMPT));
        history.add(new ChatMessage("user", "Assess booking " + BOOKING_ID + " and propose a suitable replacement if needed. Stop at a pending proposal."));
        int invalidOutputs = 0;
        int reads = 0;
        int listings = 0;
        String lastFailure = "";

        for (int call = 1; call <= limit; call++) {
            final String raw;
            try {
                raw = provider.next(List.copyOf(history));
            } catch (HttpTimeoutException ex) {
                return outcome("TIMEOUT", call, invalidOutputs, "Model request timed out");
            } catch (Exception ex) {
                String message = ex.getMessage() == null ? "Model request failed" : ex.getMessage();
                String kind = message.toLowerCase().contains("exhaust") ? "ATTEMPTS_EXHAUSTED" : "MODEL_ERROR";
                return outcome(kind, call, invalidOutputs, message);
            }

            history.add(new ChatMessage("assistant", raw == null ? "" : raw));
            final Map<String, Object> request;
            try {
                request = MiniJson.object(MiniJson.parse(raw));
                if (!request.keySet().equals(Set.of("tool", "bookingId", "targetId")))
                    throw new IllegalArgumentException("request must contain exactly tool, bookingId, and targetId");
                if (!(request.get("tool") instanceof String) || !(request.get("bookingId") instanceof String)
                        || !(request.get("targetId") instanceof String))
                    throw new IllegalArgumentException("all request fields must be strings");
            } catch (IllegalArgumentException ex) {
                invalidOutputs++;
                lastFailure = "MALFORMED_REQUEST: " + ex.getMessage();
                history.add(new ChatMessage("user", observation("MALFORMED_REQUEST", ex.getMessage())));
                continue;
            }

            String tool = (String) request.get("tool");
            String bookingId = (String) request.get("bookingId");
            String targetId = (String) request.get("targetId");
            if (!TOOLS.contains(tool)) {
                invalidOutputs++;
                lastFailure = "UNKNOWN_TOOL: " + tool;
                history.add(new ChatMessage("user", observation("UNKNOWN_TOOL", tool)));
                continue;
            }
            if (!BOOKING_ID.equals(bookingId)) {
                invalidOutputs++;
                lastFailure = "WRONG_GOAL: bookingId must be " + BOOKING_ID;
                history.add(new ChatMessage("user", observation("WRONG_GOAL", "bookingId must be " + BOOKING_ID)));
                continue;
            }

            try {
                switch (tool) {
                    case "READ_BOOKING" -> {
                        requireEmptyTarget(targetId); reads++;
                        history.add(new ChatMessage("user", observation("READ_BOOKING", bookingSnapshot())));
                    }
                    case "LIST_ALTERNATIVES" -> {
                        requireEmptyTarget(targetId);
                        if (reads == 0) throw new IllegalArgumentException("READ_BOOKING must come first");
                        listings++;
                        history.add(new ChatMessage("user", observation("LIST_ALTERNATIVES", alternatives())));
                    }
                    case "PROPOSE_REASSIGNMENT" -> {
                        if (reads == 0 || listings == 0) throw new IllegalArgumentException("READ_BOOKING and LIST_ALTERNATIVES must come before proposing");
                        ProposalView proposal = propose(targetId);
                        return outcome("PENDING_APPROVAL", call, invalidOutputs, Map.of(
                                "proposalId", proposal.proposalId(), "bookingId", proposal.bookingId(),
                                "targetId", proposal.targetId(), "bookingVersion", proposal.bookingVersion(),
                                "policyVersion", proposal.policyVersion(), "status", proposal.status()));
                    }
                    case "FINISH" -> {
                        requireEmptyTarget(targetId);
                        return outcome("FINISHED", call, invalidOutputs, "Model finished without a proposal");
                    }
                    default -> throw new IllegalStateException("validated tool missing dispatch");
                }
            } catch (IllegalArgumentException ex) {
                invalidOutputs++;
                lastFailure = "INVALID_REQUEST: " + ex.getMessage();
                history.add(new ChatMessage("user", observation("INVALID_REQUEST", ex.getMessage())));
            }
        }
        return outcome(lastFailure.isEmpty() ? "ATTEMPTS_EXHAUSTED" : "ATTEMPTS_EXHAUSTED", limit,
                invalidOutputs, lastFailure.isEmpty() ? "No terminal action before call limit" : lastFailure);
    }

    private static void requireEmptyTarget(String targetId) {
        if (!targetId.isEmpty()) throw new IllegalArgumentException("targetId must be empty for this tool");
    }

    private List<Map<String, Object>> alternatives() {
        return spaces.stream().map(ParkingSpace::snapshot).toList();
    }

    private static String observation(String type, Object detail) {
        return MiniJson.stringify(Map.of("observation", type, "detail", detail));
    }

    private static String outcome(String status, int calls, int invalidOutputs, Object detail) {
        return MiniJson.stringify(Map.of("status", status, "modelCalls", calls,
                "invalidOutputs", invalidOutputs, "result", detail));
    }
}
