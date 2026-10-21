D1.5 Explainations

////Responsibilities
Booking
    - Holds the Id of the booking, id of the space, integer duration for the booking length, whether the booking needs accessibility, and saves it's requests version for validity checks
   
    * Methods
        Booking() - accesses the domain rules to assert naming constraints, and valid duration hours
        Snapshot() - Provides and immutable mapping/dicitonary of the bookings values
        (Supplied Getters and setters) for read only actions
        
Parking Space
    - Privately owns the id of the space, info regarding the space whether it's an accesibility type, and the state of occupacy/- avilability

    * Methods
        ParkingSpace() - Constructor, asserts the correct naming scheme for space id, and initializes the final values
        Snapshot() - Provides and immutable mapping/dicitonary of the parking spaces values.
        (Supplied Getters and setters) for read only actions.
        IsAvailable() - returns a derived boolean making use of whether the space is open and not occupied


Proposal
    - A class that is used to "propose" requests of a speicied booking.
    - Supporting funcitons like returning the state of a proposed booking, created within the proposal service

    * Methods
        Proposal() - Constructor that initilizes a final, unchanging state of the requested proposal
        View() - provides a proposal view of the data stored within the proposal
        (Supplied Getters and setters) for read only actions.

Proposal Service
    - Owns proposal creation in the context of application use.
    - Doesn't change or approve bookings, only creates proposals with unique Id's
    - Rejects null targets

    * Methods
        ProposalService() - Constructuor - Recieves
        Propose() - Creates a proposal object that will be used to pass the status of a proposal request. (Doesn't change anything about the booking nor does it approve), asserts whether a booking space change is possible?d 




Student Application
    - The applications way of making proposals, simulates the actual inputs within an applicaitons
    
    * Methods
        BookingSnapshot() allows the caller to view the status of the booking as Read Only
        Propose() - Talks to the proposal serivce to provide a valid proposal view


Restrictions

Proposal service, restricted in the context of this D1 is hardcoded to a single booking, where as in a proper application should be able to process several bookings for varries proposals

Operation Contracts
    StudentApplication.propose(targetId)
    - Precondition: the target id is valid under domain rules, and isn't the same target of the booking
    - Postcondition: It returns a pending proposal for booking the, it also keeps track of booking policy version
    - Frame condition: It does not change the booking. If the target is invalid or ineligible, it rejects the request and still leaves the booking unchanged.
    
    StudentApplication.bookingSnapshot()
    - NO Precondition 
    - Postcondition: returns an immutable snapshot of the booking
    - Frame condition: Calling it does not change the booking.

Important Current Limitation