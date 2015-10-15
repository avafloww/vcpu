package codes.devin.vcpu.asm;

import codes.devin.vcpu.ArgumentType;
import codes.devin.vcpu.Opcode;
import codes.devin.vcpu.Register;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Assembler {

    private static final Pattern MEMORY_VALUE_PATTERN = Pattern.compile("\\[[A-Za-z0-9_]+\\]");

    private final String[] code;
    private final Set<String> labelSet = new HashSet<>();
    private final Map<String, Integer> labelMap = new HashMap<>(); // label name > line number
    private ByteArrayOutputStream out;

    public Assembler(String code) {
        this.code = code.split("\n");
    }

    public Assembler(String... code) {
        this.code = code;
    }

    public byte[] assemble() throws AssemblyException {
        if (out != null && out.size() > 0) {
            return out.toByteArray();
        }

        try {
            // Pre-scan labels
            for (int i = 0; i < code.length; i++) {
                int lineNumber = i + 1;
                String line = code[i];

                preScanLabels(lineNumber, line);
            }

            // Two passes, to allow for jumps preceding labels
            for (int pass = 1; pass <= 2; pass++) {
                out = new ByteArrayOutputStream();
                for (int i = 0; i < code.length; i++) {
                    int lineNumber = i + 1;
                    String line = code[i];

                    assembleInstruction(pass, lineNumber, line);
                }
            }

            return out.toByteArray();
        } catch (IOException ex) {
            throw new AssemblyException(ex);
        }
    }

    private void preScanLabels(int lineNumber, String line) throws AssemblyException {
        line = line.trim();

        if (line.startsWith(":")) {
            String labelName = line.substring(1);
            if (labelSet.contains(labelName)) {
                throw new AssemblyException(lineNumber, line, "duplicate label: " + labelName);
            }

            labelSet.add(labelName);
        }
    }

    private void assembleInstruction(int pass, int lineNumber, String line) throws AssemblyException, IOException {
        line = line.trim();

        // Is this a label?
        if (line.startsWith(":")) {
            if (pass == 1) {
                // This _is_ a label
                String labelName = line.substring(1);
                if (labelMap.containsKey(labelName)) {
                    throw new AssemblyException(lineNumber, line, "duplicate label: " + labelName);
                }

                labelMap.put(labelName, out.size());
            }

            return;
        }

        // Get rid of commas; they're nice, but unnecessary
        line = line.replace(",", "");

        // Split up the line into components
        String[] components = line.split(" ");

        Opcode opcode;
        try {
            opcode = Opcode.valueOf(components[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AssemblyException(lineNumber, line, "invalid opcode: " + components[0].toUpperCase());
        }

        if (components.length - 1 < opcode.argumentCount) {
            throw new AssemblyException(lineNumber, line, "invalid number of arguments for " + opcode + ": expected " + opcode.argumentCount + ", got " + (components.length - 1));
        }

        out.write(opcode.opcode);
        if (opcode.argumentCount > 0) {
            byte flags = 0;
            byte[] args = null;

            try (ByteArrayOutputStream argsOut = new ByteArrayOutputStream()) {
                try (DataOutputStream argsDos = new DataOutputStream(argsOut)) {
                    for (int j = 0; j < opcode.argumentCount; j++) {
                        int cmptIndex = j + 1;
                        String argString = components[cmptIndex];
                        ArgumentType type = getArgumentType(lineNumber, line, argString);

                        byte typeFlag = (byte) (type.index << (8 - ((j + 1) * 2)));
                        flags |= typeFlag;

                        switch (type) {
                            case CONSTANT:
                            case MEMORY_VALUE:
                                String sanitized = argString.replace("[", "").replace("]", "");
                                try {
                                    argsDos.writeInt(Integer.decode(sanitized));
                                } catch (NumberFormatException ex) {
                                    if (labelMap.containsKey(sanitized)) {
                                        argsDos.writeInt(labelMap.get(sanitized));
                                    } else {
                                        if (pass == 1) {
                                            // First pass, no big deal
                                            argsDos.writeInt(0);
                                        } else {
                                            // Now it is a big deal, because we should know where the labels are
                                            throw new AssemblyException(lineNumber, line, "undefined label: " + sanitized);
                                        }
                                    }
                                }
                                break;
                            case REGISTER:
                                argsDos.write(Register.valueOf(argString.toUpperCase()).index);
                                break;
                        }
                    }
                }

                args = argsOut.toByteArray();
            }

            out.write(flags);
            if (args != null) {
                out.write(args);
            }
        }
    }

    private ArgumentType getArgumentType(int lineNumber, String line, String arg) throws AssemblyException {
        try {
            Register.valueOf(arg.toUpperCase());
            return ArgumentType.REGISTER;
        } catch (IllegalArgumentException ex) {
            // Ignore it
        }

        if (MEMORY_VALUE_PATTERN.matcher(arg).matches()) {
            return ArgumentType.MEMORY_VALUE;
        }

        if (labelSet.contains(arg)) {
            return ArgumentType.CONSTANT;
        }

        try {
            Long.decode(arg);
            return ArgumentType.CONSTANT;
        } catch (IllegalArgumentException ex) {
            throw new AssemblyException(lineNumber, line, "unknown argument type");
        }
    }
}
