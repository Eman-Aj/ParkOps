import java.util.List;
import java.util.Map;

/** Minimal D1 application boundary over the protected domain model. */
public final class StudentApplication {
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

    public String run(ModelProvider provider, int limit) {
        throw new UnsupportedOperationException("D2 model/tool coordination");
    }
}
