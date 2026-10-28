import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public final class Main {
    public static void main(String[] args) throws Exception {
        String mode=args.length==0?"fixture":args[0];
        if(mode.equals("fixture")) {
            System.out.println("SYNTHETIC FIXTURE; no model invocation or booking mutation.");
            System.out.println(MiniJson.stringify(Fixture.booking()));
            System.out.println(MiniJson.stringify(Fixture.spaces()));
            return;
        }
        StudentApplication app=new StudentApplication();
        try {
            if(mode.equals("scripted")) {
                System.out.println("REPLAY D1 input: not a live model run; approval/execution are not invoked.");
                ReplayProvider replay=new ReplayProvider(List.of(Fixture.proposalRequest("B12")));
                var input=MiniJson.object(MiniJson.parse(replay.next(List.of(new ChatMessage("user","Propose a replacement for B1")))));
                ProposalView p=app.propose((String)input.get("targetId"));
                System.out.println(p);
                System.out.println("Booking: "+MiniJson.stringify(app.bookingSnapshot()));
            } else if(mode.equals("agent-scripted")) {
                System.out.println("REPLAY D2 workflow; not a live model run.");
                runSupervised(app, Fixture.replay());
            } else if(mode.equals("tick-demo")) {
                for (long t : List.of(0L, 59L, 60L)) {
                    PeriodicTickResult result = app.tick(t);
                    System.out.println(MiniJson.stringify(Map.of("timeSeconds", result.timeSeconds(),
                            "scanned", result.scanned(), "workItems", result.workItems().stream().map(w -> Map.of(
                                    "bookingId", w.bookingId(), "bookingVersion", w.bookingVersion(),
                                    "policyVersion", w.policyVersion(), "closedSpaceId", w.closedSpaceId(),
                                    "detectedAtSeconds", w.detectedAtSeconds())).toList())));
                }
                try {
                    app.tick(59);
                    throw new IllegalStateException("backwards tick unexpectedly accepted");
                } catch (IllegalArgumentException expected) {
                    System.out.println("BACKWARDS_CLOCK_REJECTED: " + expected.getMessage());
                }
            } else if(mode.equals("live")) {
                String host=System.getenv("OLLAMA_CHAT_URL"),model=System.getenv("OLLAMA_MODEL");
                if(host==null || model==null || host.isBlank() || model.isBlank()) {
                    System.out.println("LIVE_RUN_PENDING_COURSE_ACCESS: configure the course-provided OLLAMA_CHAT_URL and OLLAMA_MODEL.");
                    System.exit(2);
                }
                System.out.println("LIVE provider selected; outcome must be recorded from actual execution.");
                runSupervised(app, new OllamaProvider(URI.create(host),model,Duration.ofSeconds(30)));
            } else {
                System.err.println("Modes: fixture | scripted (D1) | agent-scripted (D2) | tick-demo (D2) | live (D2)");System.exit(2);
            }
        } catch(UnsupportedOperationException ex) {
            System.err.println("STARTER_NOT_IMPLEMENTED: "+ex.getMessage());System.exit(1);
        }
    }

    private static void runSupervised(StudentApplication app, ModelProvider provider) {
        String output = app.run(provider, 6);
        System.out.println(output);
        Map<String, Object> run = MiniJson.object(MiniJson.parse(output));
        if (!"PENDING_APPROVAL".equals(run.get("status"))) return;
        Map<String, Object> proposal = MiniJson.object(run.get("result"));
        String proposalId = (String) proposal.get("proposalId");
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("Operator decision for " + proposalId + " [y/n]: ");
            if (!input.hasNextLine()) {
                System.out.println("No operator decision received; proposal remains PENDING.");
                return;
            }
            String answer = input.nextLine().trim();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n")) {
                boolean approve = answer.equalsIgnoreCase("y");
                String result = app.operatorDecision(proposalId, approve);
                System.out.println("Operator outcome: " + result);
                System.out.println("Proposal: " + app.proposal(proposalId));
                System.out.println("Booking: " + MiniJson.stringify(app.bookingSnapshot()));
                System.out.println("Notification records: " + app.notificationCount());
                return;
            }
            System.out.println("Enter y or n.");
        }
    }
}
