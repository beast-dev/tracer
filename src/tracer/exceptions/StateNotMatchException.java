package tracer.exceptions;

/**
 * @author Walter Xie
 */
public class StateNotMatchException extends IllegalArgumentException {

    public static final String TITLE = "Number of states don't match";

    public StateNotMatchException() {
        super("The number of states in the log file and tree file not match !");
    }
}
