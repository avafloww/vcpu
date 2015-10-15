package codes.devin.vcpu.interrupt;

public class NonMaskableInterrupt extends Interrupt {

    public NonMaskableInterrupt() {
    }

    public NonMaskableInterrupt(String message) {
        super(message);
    }

    public NonMaskableInterrupt(String message, Throwable cause) {
        super(message, cause);
    }

    public NonMaskableInterrupt(Throwable cause) {
        super(cause);
    }

    public NonMaskableInterrupt(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
