package codes.devin.vcpu;

public enum ArgumentType {

    CONSTANT(1),
    REGISTER(2),
    MEMORY_VALUE(3);

    public final int index;

    private ArgumentType(int index) {
        this.index = index;
    }

    public static ArgumentType[] any() {
        return ArgumentType.values();
    }

    public static ArgumentType index(int index) {
        for (ArgumentType at : ArgumentType.values()) {
            if (at.index == index) {
                return at;
            }
        }

        return null;
    }
}
