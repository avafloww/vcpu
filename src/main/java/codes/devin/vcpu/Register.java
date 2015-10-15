package codes.devin.vcpu;

public enum Register {

    EAX(1),
    EBX(2),
    ECX(3),
    EDX(4),
    SP(254),
    IP(255);

    public final int index;

    private Register(int index) {
        this.index = index;
    }

    public static Register index(int index) {
        for (Register register : Register.values()) {
            if (register.index == index) {
                return register;
            }
        }

        return null;
    }
}
