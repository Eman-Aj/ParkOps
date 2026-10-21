// Add focused @org.junit.jupiter.api.Test methods here.
// Create fresh objects for each test and assert results AND unchanged state on rejection.
// The supplied wrapper is not a substitute for your own test design.
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class StudentTests { 

    @Test
    void ValidHours_1_24(){
        Booking b1 = new Booking("B01", "S01", 1, 1, true);
        assertEquals(1, b1.durationHours(), "Should Be 1");
        Booking b2 = new Booking("B02", "S02", 24, 1, true);
        assertEquals(24, b2.durationHours(), "Should Be 24");
    }
    @Test
    void InvalidHours_0_25(){
        //0 Hours throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking("B01", "S01", 0, 1, true);
        });

        //25 Hours throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking("B01", "S01", 25, 1, true);
        });
    }
    @Test
    void Invalid_Identifiers(){
        //incorrect booking Id should through IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking(null, "S01", 1, 1, true);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking("", "S01", 1, 1, true);
        });

        //incorrect space Id should through IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking("B01", null, 3, 1, true);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Booking b1 = new Booking("B01", "", 3, 1, true);
        });
    }
    
    @Test
    void B1Proposed_B12_Policy1(){

    }
    
    @Test
    void B1Proposed_D09(){
        //This should reject because the D09 space should be occupied 
        Booking B01 = new Booking("B01", "D09", 5, 1, false);
        ParkingSpace D09 = new ParkingSpace("D09", true, true, false);
        ParkingSpace D10 = new ParkingSpace("D10", true, false, false);

        // ProposalService

    }

    @Test
    void UnkownTarget(){
        StudentApplication app = new StudentApplication();

        assertThrows(IllegalArgumentException.class, () -> {
            app.propose(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            app.propose("MadeUpTarget");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            app.propose("S11");
        });

    }
    @Test
    void ExternalObtainedSnapShot(){
        Booking B01 = new Booking("B01", "D09", 5, 1, false);
        Map<String, Object> snapshot = B01.snapshot();

        assertThrows(UnsupportedOperationException.class, () -> {
        snapshot.put("id", "B02");
        });

    }
}
