import java.util.regex.Pattern;

/** Shared D1 validation rules for ParkOps domain values. */
public final class DomainRules {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9_-]{0,15}");

    private DomainRules() { }

    //This is how we check if inputs are valid. 
    //This whole class is just a big assertion
    public static void requireDuration(int hours) {
        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException("Duration must be between 1 and 24 hours");
        }
    }

    //We can't have I'd fall outside of  [A-Z][A-Z0-9_-]{0,15} field of values
    public static void requireIdentifier(String id) {
        //Used ai to show if a regular expression passes
        if (id == null || !IDENTIFIER.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
    }
}
