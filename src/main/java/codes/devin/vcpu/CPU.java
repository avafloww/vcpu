package codes.devin.vcpu;

import codes.devin.vcpu.interrupt.NonMaskableInterrupt;

public class CPU implements Runnable {

    /**
     * Instruction Pointer
     */
    public int ip = 0x0000;

    /**
     * Stack Pointer
     */
    public int sp = 0xf000;

    public int eax;
    public int ebx;
    public int ecx;
    public int edx;

    public boolean halted = false;

    public byte[] memory = new byte[0x10000];

    public CPU(byte[] program) {
        System.arraycopy(program, 0, memory, 0, program.length);
    }

    @Override
    public void run() {
        try {
            while (!halted) {
                cycle();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            halt();
        }
    }

    public void cycle() {
        if (halted) {
            return;
        }

        try {
            if (ip >= memory.length) {
                // Loop back to the beginning of memory
                ip = 0;
            }

            Opcode opcode = Opcode.get(memory[ip++]);
            byte flags = 0;
            if (opcode.argumentCount > 0) {
                flags = memory[ip++];
            }

            Register reg;
            switch (opcode) {
                case NOP:
                    break;
                case MOV:
                    if (argumentType(flags, 0) == ArgumentType.CONSTANT) {
                        // Memory address
                        // Note - only the least significant byte is set here
                        int address = argument(flags, 0);
                        int value = argument(flags, 1);
                        memory[address] = (byte) value;
                    } else if (argumentType(flags, 0) == ArgumentType.REGISTER) {
                        reg = Register.index(memory[ip++]);
                        int value = argument(flags, 1);
                        register(reg, value);
                    } else {
                        throw new NonMaskableInterrupt("invalid argument type");
                    }

                    break;
                case ADD:
                    reg = Register.index(memory[ip++]);
                    register(reg, register(reg) + argument(flags, 1));
                    break;
                case SUB:
                    reg = Register.index(memory[ip++]);
                    register(reg, register(reg) - argument(flags, 1));
                    break;
                case MUL:
                    reg = Register.index(memory[ip++]);
                    register(reg, register(reg) * argument(flags, 1));
                    break;
                case DIV:
                    reg = Register.index(memory[ip++]);
                    register(reg, register(reg) / argument(flags, 1));
                    break;
                case PUSH:
                    push(Register.index(memory[ip++]));
                    break;
                case POP:
                    pop(Register.index(memory[ip++]));
                    break;
                case APUSH:
                    push(Register.EDX);
                    push(Register.ECX);
                    push(Register.EBX);
                    push(Register.EAX);
                    break;
                case APOP:
                    pop(Register.EAX);
                    pop(Register.EBX);
                    pop(Register.ECX);
                    pop(Register.EDX);
                    break;
                case JMP:
                    ip = argument(flags, 0);
                    break;
                case JEZ:
                    int jezDest = argument(flags, 0);
                    if (argument(flags, 1) == 0) {
                        ip = jezDest;
                    }
                    break;
                case JNZ:
                    int jnzDest = argument(flags, 0);
                    if (argument(flags, 1) != 0) {
                        ip = jnzDest;
                    }
                    break;
                case JLZ:
                    int jlzDest = argument(flags, 0);
                    if (argument(flags, 1) < 0) {
                        ip = jlzDest;
                    }
                    break;
                case JGZ:
                    int jgzDest = argument(flags, 0);
                    if (argument(flags, 1) > 0) {
                        ip = jgzDest;
                    }
                    break;
                case HWID:
                    break;
                case HLT:
                    halt();
                    break;
            }
        } catch (Exception ex) {
            throw new NonMaskableInterrupt(ex);
        }
    }

    private ArgumentType argumentType(byte flags, int index) {
        // 0: >> 6
        // 1: >> 4
        // 2: >> 2
        // 3: >> 0
        int shr = 8 - ((index + 1) * 2);
        
        // 0: << 0
        // 1: << 2
        // 2: << 4
        // 3: << 6
        int shl = index * 2;
        
        int flag = (((byte) flags << shl) & 0xFF) >> (shr + shl);
        return ArgumentType.index(flag);
    }

    private int argument(byte flags, int index) {
        ArgumentType type = argumentType(flags, index);

        switch (type) {
            case CONSTANT:
                int value = ((memory[ip] << 24) + (memory[ip + 1] << 16) + (memory[ip + 2] << 8) + (memory[ip + 3]));
                ip += 4;
                return value;
            case REGISTER:
                return register(Register.index(memory[ip++]));
            case MEMORY_VALUE:
                int address = ((memory[ip] << 24) + (memory[ip + 1] << 16) + (memory[ip + 2] << 8) + (memory[ip + 3]));
                ip += 4;
                return memory[address];
            default:
                throw new NonMaskableInterrupt("invalid argument type");
        }
    }

    private int register(byte flags, int index) {
        byte flag = (byte) ((flags >> 8 - ((index + 1) * 2)) & 0xFF);
        ArgumentType type = ArgumentType.index(flag);

        if (type != ArgumentType.REGISTER) {
            throw new NonMaskableInterrupt("invalid argument type");
        }

        return register(Register.index(memory[ip++]));
    }

    private int register(Register reg) {
        switch (reg) {
            case EAX:
                return eax;
            case EBX:
                return ebx;
            case ECX:
                return ecx;
            case EDX:
                return edx;
            case IP:
                return ip;
            case SP:
                return sp;
            default:
                throw new NonMaskableInterrupt("invalid register id");
        }
    }

    private void register(Register reg, int value) {
        switch (reg) {
            case EAX:
                eax = value;
                break;
            case EBX:
                ebx = value;
                break;
            case ECX:
                ecx = value;
                break;
            case EDX:
                edx = value;
                break;
            case IP:
                ip = value;
                break;
            case SP:
                sp = value;
                break;
            default:
                throw new NonMaskableInterrupt("invalid register id");
        }
    }

    private void push(Register reg) {
        // sp[0] holds the stack size
        // sp[4] is the first value

        // Get the current stack size
        int stackSize = ((memory[sp] << 24) + (memory[sp + 1] << 16) + (memory[sp + 2] << 8) + (memory[sp + 3]));

        // Shift the stack
        System.arraycopy(memory, sp + 4, memory, sp + 8, stackSize * 4);

        // Set the new stack size
        stackSize++;
        memory[sp] = (byte) (stackSize >> 24);
        memory[sp + 1] = (byte) (stackSize >> 16);
        memory[sp + 2] = (byte) (stackSize >> 8);
        memory[sp + 3] = (byte) (stackSize);

        // Push the value on top of the stack
        int val = register(reg);
        memory[sp + 4] = (byte) (val >> 24);
        memory[sp + 5] = (byte) (val >> 16);
        memory[sp + 6] = (byte) (val >> 8);
        memory[sp + 7] = (byte) (val);
    }

    private void pop(Register reg) {
        // sp[0] holds the stack size
        // sp[4] is the first value

        // Get the current stack size
        int stackSize = ((memory[sp] << 24) + (memory[sp + 1] << 16) + (memory[sp + 2] << 8) + (memory[sp + 3]));

        if (stackSize < 1) {
            throw new NonMaskableInterrupt("empty stack");
        }

        // Get the top of the stack
        int stackTop = ((memory[sp + 4] << 24) + (memory[sp + 5] << 16) + (memory[sp + 6] << 8) + (memory[sp + 7]));

        // Resize the stack
        System.arraycopy(memory, sp + 8, memory, sp + 4, (stackSize - 1) * 4);

        // Set the new stack size
        stackSize--;
        memory[sp] = (byte) (stackSize >> 24);
        memory[sp + 1] = (byte) (stackSize >> 16);
        memory[sp + 2] = (byte) (stackSize >> 8);
        memory[sp + 3] = (byte) (stackSize);

        // Set the register to the popped value
        register(reg, stackTop);
    }

    public String dumpRegisters() {
        StringBuilder sb = new StringBuilder();

        sb.append("ip=").append(String.format("0x%08X", ip)).append(' ');
        sb.append("sp=").append(String.format("0x%08X", sp)).append(' ');
        sb.append("eax=").append(String.format("0x%08X", eax)).append(' ');
        sb.append("ebx=").append(String.format("0x%08X", ebx)).append(' ');
        sb.append("ecx=").append(String.format("0x%08X", ecx)).append(' ');
        sb.append("edx=").append(String.format("0x%08X", edx)).append(' ');

        return sb.toString();
    }

    public String dumpMemory() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < memory.length; i++) {
            if (i % 16 == 0) {
                if (i != 0) {
                    sb.append("\n");
                }

                sb.append(String.format("%08X", i)).append("    ");
            }

            sb.append(String.format("%02X", memory[i])).append(" ");
        }

        return sb.toString();
    }

    public void halt() {
        halted = true;
        System.out.println("CPU halted.");
        System.out.println();
        System.out.println("Register dump:");
        System.out.println(dumpRegisters());
        System.out.println();
        System.out.println("Memory dump:");
        System.out.println(dumpMemory());
    }
}
