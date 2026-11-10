import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentTests {
    private static Map<String, Object> result(String json) {
        return MiniJson.object(MiniJson.parse(json));
    }

    private static int intField(Map<String, Object> object, String field) {
        return ((Number) object.get(field)).intValue();
    }

    private static String proposalId(ProposalView proposal) {
        return proposal.proposalId();
    }

    @Test
    void ValidHours_1_24() {
        Booking b1 = new Booking("B01", "S01", 1, 1, true);
        assertEquals(1, b1.durationHours(), "Should Be 1");
        Booking b2 = new Booking("B02", "S02", 24, 1, true);
        assertEquals(24, b2.durationHours(), "Should Be 24");
    }

    @Test
    void InvalidHours_0_25() {
        assertThrows(IllegalArgumentException.class, () -> new Booking("B01", "S01", 0, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Booking("B01", "S01", 25, 1, true));
    }

    @Test
    void Invalid_Identifiers() {
        assertThrows(IllegalArgumentException.class, () -> new Booking(null, "S01", 1, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Booking("", "S01", 1, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Booking("B01", null, 3, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Booking("B01", "", 3, 1, true));
    }

    @Test
    void B1Proposed_B12_Policy1() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        List<Map<String, Object>> spacesBefore = app.spacesSnapshot();
        ReplayProvider replay = Fixture.replay();

        Map<String, Object> outcome = result(app.run(replay, 6));
        Map<String, Object> proposal = MiniJson.object(outcome.get("result"));
        assertEquals("PENDING_APPROVAL", outcome.get("status"));
        assertEquals(3, intField(outcome, "modelCalls"));
        assertEquals(0, intField(outcome, "invalidOutputs"));
        assertEquals("B1", proposal.get("bookingId"));
        assertEquals("B12", proposal.get("targetId"));
        assertEquals(0, intField(proposal, "bookingVersion"));
        assertEquals(1, intField(proposal, "policyVersion"));
        assertEquals("PENDING", proposal.get("status"));
        assertEquals("PENDING", app.proposal((String) proposal.get("proposalId")).status());
        assertEquals(before, app.bookingSnapshot(), "Replay must not execute the proposal");
        assertEquals(spacesBefore, app.spacesSnapshot(), "Replay must not mutate parking spaces");
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());

        assertEquals(2, replay.histories().get(0).size());
        assertEquals(4, replay.histories().get(1).size());
        assertEquals(6, replay.histories().get(2).size());
        assertTrue(replay.histories().get(1).stream().anyMatch(m -> m.content().contains("READ_BOOKING")));
        assertTrue(replay.histories().get(2).stream().anyMatch(m -> m.content().contains("LIST_ALTERNATIVES")));
    }

    @Test
    void B1Proposed_D09() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        assertThrows(IllegalArgumentException.class, () -> app.propose("D09"));
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
        assertNull(app.proposal("P0"));
    }

    @Test
    void UnkownTarget() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        for (String target : new String[]{null, "MadeUpTarget", "S11"}) {
            assertThrows(IllegalArgumentException.class, () -> app.propose(target));
            assertEquals(before, app.bookingSnapshot(), "Rejected target must not change booking");
        }
        assertEquals(0, app.commitCount());
    }

    @Test
    void ExternalObtainedSnapShot() {
        Booking booking = new Booking("B01", "D09", 5, 1, false);
        Map<String, Object> snapshot = booking.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("id", "B02"));
        assertEquals("B01", booking.snapshot().get("id"));
        StudentApplication app = new StudentApplication();
        Map<String, Object> appSnapshot = app.bookingSnapshot();
        assertThrows(UnsupportedOperationException.class, () -> appSnapshot.put("spaceId", "D09"));
        assertEquals("A17", app.bookingSnapshot().get("spaceId"));
    }

    @Test
    void D2B_ExecutePendingProposalIsNotApprovedAndDoesNotMutate() {
        StudentApplication app = new StudentApplication();
        ProposalView pending = app.propose("B12");
        Map<String, Object> before = app.bookingSnapshot();
        List<Map<String, Object>> spacesBefore = app.spacesSnapshot();

        assertEquals("NOT_APPROVED", app.executeProposal(proposalId(pending)));
        assertEquals("PENDING", app.proposal(proposalId(pending)).status());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(spacesBefore, app.spacesSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }

    @Test
    void D2C_ApproveThenExecuteCommitsOnce() {
        StudentApplication app = new StudentApplication();
        ProposalView pending = app.propose("B12");
        String id = proposalId(pending);
        Map<String, Object> before = app.bookingSnapshot();
        List<Map<String, Object>> spacesBefore = app.spacesSnapshot();

        assertEquals("APPROVED", app.approveProposal(id));
        assertEquals("APPROVED", app.proposal(id).status());
        assertEquals(before, app.bookingSnapshot(), "Approval alone must not execute");
        assertEquals(spacesBefore, app.spacesSnapshot(), "Approval alone must not change space occupancy");
        assertEquals("COMMITTED", app.executeProposal(id));
        assertEquals("EXECUTED", app.proposal(id).status());
        assertEquals(Map.of("id", "B1", "spaceId", "B12", "durationHours", 3,
                "version", 1, "requiresAccessible", true), app.bookingSnapshot());
        assertEquals(1, app.commitCount());
        assertEquals(1, app.notificationCount());
        assertEquals(false, app.spacesSnapshot().get(0).get("occupied"));
        assertEquals(true, app.spacesSnapshot().get(1).get("occupied"));
    }

    @Test
    void D2D_RepeatedExecutionIsDuplicateWithoutSecondCommitOrNotification() {
        StudentApplication app = new StudentApplication();
        ProposalView proposal = app.propose("B12");
        String id = proposalId(proposal);
        assertEquals("APPROVED", app.approveProposal(id));
        assertEquals("COMMITTED", app.executeProposal(id));
        Map<String, Object> afterCommit = app.bookingSnapshot();
        List<Map<String, Object>> spacesAfterCommit = app.spacesSnapshot();

        assertEquals("DUPLICATE", app.executeProposal(id));
        assertEquals("EXECUTED", app.proposal(id).status());
        assertEquals(afterCommit, app.bookingSnapshot());
        assertEquals(spacesAfterCommit, app.spacesSnapshot());
        assertEquals(1, app.commitCount());
        assertEquals(1, app.notificationCount());
    }

    @Test
    void D2E_RejectThenExecuteDoesNotCommit() {
        StudentApplication app = new StudentApplication();
        ProposalView proposal = app.propose("B12");
        String id = proposalId(proposal);
        Map<String, Object> before = app.bookingSnapshot();
        List<Map<String, Object>> spacesBefore = app.spacesSnapshot();

        assertEquals("REJECTED", app.rejectProposal(id));
        assertEquals("NOT_APPROVED", app.executeProposal(id));
        assertEquals("REJECTED", app.proposal(id).status());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(spacesBefore, app.spacesSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }

    @Test
    void D2F_OldBookingVersionBecomesStaleAfterAnotherCommit() {
        StudentApplication app = new StudentApplication();
        ProposalView first = app.propose("B12");
        ProposalView second = app.propose("C03");
        assertEquals("APPROVED", app.approveProposal(first.proposalId()));
        assertEquals("COMMITTED", app.executeProposal(first.proposalId()));
        Map<String, Object> afterFirstCommit = app.bookingSnapshot();
        List<Map<String, Object>> spacesAfterFirstCommit = app.spacesSnapshot();
        assertEquals("APPROVED", app.approveProposal(second.proposalId()));

        assertEquals("STALE", app.executeProposal(second.proposalId()));
        assertEquals("STALE", app.proposal(second.proposalId()).status());
        assertEquals(afterFirstCommit, app.bookingSnapshot());
        assertEquals(spacesAfterFirstCommit, app.spacesSnapshot());
        assertEquals(1, app.commitCount());
        assertEquals(1, app.notificationCount());
    }

    @Test
    void D2G_TargetBecomingUnavailablePreventsExecution() {
        StudentApplication app = new StudentApplication();
        ProposalView proposal = app.propose("B12");
        assertEquals("APPROVED", app.approveProposal(proposal.proposalId()));
        Map<String, Object> before = app.bookingSnapshot();
        List<Map<String, Object>> spacesBefore = app.spacesSnapshot();
        app.updateSpaceOperationalState("B12", true, true);
        List<Map<String, Object>> unavailableState = app.spacesSnapshot();

        assertEquals("UNAVAILABLE_OR_INELIGIBLE", app.executeProposal(proposal.proposalId()));
        assertEquals("STALE", app.proposal(proposal.proposalId()).status());
        assertEquals(before, app.bookingSnapshot());
        assertNotEquals(spacesBefore, unavailableState, "The test setup changes the target before execution");
        assertEquals(unavailableState, app.spacesSnapshot(), "Rejected execution must preserve the changed target state");
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }

    @Test
    void D2H_MalformedAndApprovalFieldRequestsAreCountedAndHaveNoAuthority() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        ReplayProvider replay = new ReplayProvider(List.of(
                "{bad json",
                "{\"tool\":\"PROPOSE_REASSIGNMENT\",\"bookingId\":\"B1\",\"targetId\":\"B12\",\"approved\":true}",
                "{\"tool\":\"FINISH\",\"bookingId\":\"B1\",\"targetId\":\"\"}"));

        Map<String, Object> outcome = result(app.run(replay, 6));
        assertEquals("FINISHED", outcome.get("status"));
        assertEquals(3, replay.calls());
        assertEquals(2, intField(outcome, "invalidOutputs"));
        assertTrue(replay.histories().get(1).stream().anyMatch(m -> m.content().contains("MALFORMED_REQUEST")));
        assertTrue(replay.histories().get(2).stream().anyMatch(m -> m.content().contains("MALFORMED_REQUEST")));
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
        assertNull(app.proposal("P0"));
    }

    @Test
    void D2I_UnknownToolAndWrongGoalAreRejected() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        ReplayProvider replay = new ReplayProvider(List.of(
                "{\"tool\":\"DELETE_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B9\",\"targetId\":\"\"}",
                "{\"tool\":\"FINISH\",\"bookingId\":\"B1\",\"targetId\":\"\"}"));

        Map<String, Object> outcome = result(app.run(replay, 6));
        assertEquals("FINISHED", outcome.get("status"));
        assertEquals(2, intField(outcome, "invalidOutputs"));
        assertTrue(replay.histories().get(1).stream().anyMatch(m -> m.content().contains("UNKNOWN_TOOL")));
        assertTrue(replay.histories().get(2).stream().anyMatch(m -> m.content().contains("WRONG_GOAL")));
        assertEquals(before, app.bookingSnapshot());
        assertNull(app.proposal("P0"));
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }

    @Test
    void D2J_SixInvalidResponsesReturnStepLimitAfterSixCalls() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        ReplayProvider replay = new ReplayProvider(List.of("not json", "[]", "{", "null", "{}", "true"));

        Map<String, Object> outcome = result(app.run(replay, 6));
        assertEquals("STEP_LIMIT", outcome.get("status"));
        assertEquals(6, replay.calls());
        assertEquals(6, intField(outcome, "modelCalls"));
        assertEquals(6, intField(outcome, "invalidOutputs"));
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }

    @Test
    void D2_CallLimitMustStayWithinOneAndSix() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        AtomicInteger calls = new AtomicInteger();
        ModelProvider provider = history -> { calls.incrementAndGet(); return "{}"; };

        assertThrows(IllegalArgumentException.class, () -> app.run(provider, 0));
        assertThrows(IllegalArgumentException.class, () -> app.run(provider, 7));
        assertEquals(0, calls.get());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
    }

    @Test
    void D2K_ProviderExceptionAndTimeoutReturnControlledProviderError() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        AtomicInteger calls = new AtomicInteger();
        ModelProvider throwsProvider = history -> {
            calls.incrementAndGet();
            throw new IOException("synthetic provider failure");
        };
        Map<String, Object> failure = result(app.run(throwsProvider, 6));
        assertEquals("PROVIDER_ERROR", failure.get("status"));
        assertEquals(1, intField(failure, "modelCalls"));
        assertEquals(1, calls.get());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());

        StudentApplication timeoutApp = new StudentApplication();
        Map<String, Object> timeoutBefore = timeoutApp.bookingSnapshot();
        ModelProvider timeoutProvider = history -> { throw new HttpTimeoutException("synthetic timeout"); };
        Map<String, Object> timeout = result(timeoutApp.run(timeoutProvider, 6));
        assertEquals("PROVIDER_ERROR", timeout.get("status"));
        assertEquals(1, intField(timeout, "modelCalls"));
        assertEquals(timeoutBefore, timeoutApp.bookingSnapshot());
        assertEquals(0, timeoutApp.commitCount());
        assertEquals(0, timeoutApp.notificationCount());
    }

    @Test
    void D2L_TicksAtZeroFiftyNineAndSixtyEmitAtMostOneWorkItem() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();

        PeriodicTickResult atZero = app.tick(0);
        PeriodicTickResult atFiftyNine = app.tick(59);
        PeriodicTickResult atSixty = app.tick(60);
        List<PeriodicWorkItem> allWork = new ArrayList<>();
        allWork.addAll(atZero.workItems());
        allWork.addAll(atFiftyNine.workItems());
        allWork.addAll(atSixty.workItems());

        assertTrue(atZero.scanned());
        assertEquals(1, atZero.workItems().size());
        assertFalse(atFiftyNine.scanned());
        assertTrue(atFiftyNine.workItems().isEmpty());
        assertTrue(atSixty.scanned());
        assertTrue(atSixty.workItems().isEmpty());
        assertEquals(1, allWork.size());
        assertEquals("B1", allWork.get(0).bookingId());
        assertEquals(0, allWork.get(0).bookingVersion());
        assertEquals(1, allWork.get(0).policyVersion());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
        assertEquals(0, intField(app.bookingSnapshot(), "version"));
    }

    @Test
    void D2M_BackwardsTickIsRejectedWithoutChangingBooking() {
        StudentApplication app = new StudentApplication();
        Map<String, Object> before = app.bookingSnapshot();
        app.tick(0);
        app.tick(60);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> app.tick(59));
        assertEquals("clock moved backwards", error.getMessage());
        assertEquals(before, app.bookingSnapshot());
        assertEquals(0, intField(app.bookingSnapshot(), "version"));
        assertEquals(0, app.commitCount());
        assertEquals(0, app.notificationCount());
    }
}
