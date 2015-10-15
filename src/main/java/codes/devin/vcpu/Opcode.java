package codes.devin.vcpu;

public enum Opcode {

    NOP(0x00, 0),
    MOV(0x01, 2, new ArgumentType[]{ArgumentType.REGISTER, ArgumentType.CONSTANT}, ArgumentType.any()), // destination, source
    ADD(0x02, 2, new ArgumentType[]{ArgumentType.REGISTER}, ArgumentType.any()), // source/destination, value
    SUB(0x03, 2, new ArgumentType[]{ArgumentType.REGISTER}, ArgumentType.any()), // source/destination, value
    MUL(0x04, 2, new ArgumentType[]{ArgumentType.REGISTER}, ArgumentType.any()), // source/destination, value
    DIV(0x05, 2, new ArgumentType[]{ArgumentType.REGISTER}, ArgumentType.any()), // source/destination, value
    PUSH(0x11, 1, new ArgumentType[]{ArgumentType.REGISTER}), // destination
    POP(0x12, 1, new ArgumentType[]{ArgumentType.REGISTER}), // destination
    APUSH(0x21, 0),
    APOP(0x22, 0),
    JMP(0x31, 1, ArgumentType.any()), // destination
    JEZ(0x32, 2, ArgumentType.any(), new ArgumentType[]{ArgumentType.REGISTER}), // destination, test
    JNZ(0x33, 2, ArgumentType.any(), new ArgumentType[]{ArgumentType.REGISTER}), // destination, test
    JLZ(0x34, 2, ArgumentType.any(), new ArgumentType[]{ArgumentType.REGISTER}), // destination, test
    JGZ(0x35, 2, ArgumentType.any(), new ArgumentType[]{ArgumentType.REGISTER}), // destination, test
    HWID(0xF0, 0),
    INT(0xFE, 1, ArgumentType.any()), // interrupt number
    HLT(0xFF, 0);

    public final byte opcode;
    public final int argumentCount;
    public final ArgumentType[][] argumentTypes;

    private Opcode(int opcode, int argumentCount, ArgumentType[]... argumentTypes) {
        this.opcode = (byte) opcode;
        this.argumentCount = argumentCount;
        this.argumentTypes = argumentTypes;
    }

    public static Opcode get(byte id) {
        for (Opcode opcode : Opcode.values()) {
            if (opcode.opcode == id) {
                return opcode;
            }
        }

        return Opcode.NOP;
    }
}
