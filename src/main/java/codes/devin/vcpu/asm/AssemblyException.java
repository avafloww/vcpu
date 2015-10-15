package codes.devin.vcpu.asm;

public class AssemblyException extends Exception {

    public AssemblyException() {
    }

    public AssemblyException(String message) {
        super(message);
    }

    public AssemblyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssemblyException(Throwable cause) {
        super(cause);
    }

    public AssemblyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AssemblyException(int lineNumber, String line, String message) {
        this("line " + lineNumber + ": " + line + ": " + message);
    }
}
