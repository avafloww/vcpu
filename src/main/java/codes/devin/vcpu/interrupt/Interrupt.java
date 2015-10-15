package codes.devin.vcpu.interrupt;

public abstract class Interrupt extends RuntimeException {

    public Interrupt() {
    }

    public Interrupt(String message) {
        super(message);
    }

    public Interrupt(String message, Throwable cause) {
        super(message, cause);
    }

    public Interrupt(Throwable cause) {
        super(cause);
    }

    public Interrupt(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
