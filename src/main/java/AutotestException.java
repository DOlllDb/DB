/**
 * Created by Maksim Nikelman on 26.11.17.
 */

/**
 * My custom exception
 */
public class AutotestException extends RuntimeException {
    public AutotestException(Exception e) {
        super(e);
    }
    public AutotestException(String msg) {
        super(msg);
    }
    public AutotestException(Throwable e) {
        super(e);
    }
}
