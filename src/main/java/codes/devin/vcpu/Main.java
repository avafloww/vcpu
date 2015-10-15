package codes.devin.vcpu;

import codes.devin.vcpu.asm.Assembler;
import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2
                || !(args[0].equals("--assemble") || args[0].equals("--run"))
                || (args[0].equals("--assemble") && args.length < 3)) {
            System.err.println("Valid usage arguments:");
            System.err.println("\t--assemble in.asm out.bin\t\tAssembles the specified assembly file to the specified output file.");
            System.err.println("\t--run program.bin\t\t\tRuns the specified program.");
            System.exit(1);
        }
        
        File inFile = new File(args[1]);
        if (!inFile.exists()) {
            System.err.println("Specified input file does not exist.");
            System.exit(2);
        }
        
        if (args[0].equals("--assemble")) {
            assemble(inFile, args[2]);
        } else if (args[0].equals("--run")) {
            run(inFile);
        }
    }
    
    private static void assemble(File inFile, String outFileName) throws Exception {
        File outFile = new File(outFileName);
        List<String> code = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                code.add(line + "\n");
            }
        }
        
        Assembler asm = new Assembler(code.toArray(new String[0]));
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(asm.assemble());
        }
    }
    
    private static void run(File inFile) throws Exception {
        byte[] program;
        try (FileInputStream fis = new FileInputStream(inFile)) {
            program = ByteStreams.toByteArray(fis);
        }
        
        new CPU(program).run();
    }
}
