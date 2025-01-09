package MiniVHDL;

import MiniVHDL.Circuit.Circuit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * The main entry point for the MiniVHDL to FIRRTL Compiler.
 */
public class Main {

    /**
     * The main method for executing the MiniVHDL compiler.
     *
     * <p>
     * The program reads a VHDL file, parses its contents, and generates an equivalent FIRRTL
     * representation of the circuit described in the file. If parsing succeeds without errors,
     * the generated FIRRTL program is written to an output file in the same directory as the input file.
     * </p>
     *
     * @param args the command-line arguments. The program expects two arguments:
     *             <ul>
     *                 <li><code>args[0]</code>: The name of the input VHDL file to be compiled.</li>
     *                 <li><code>args[1]</code>: The name of the top-level module for the FIRRTL file.</li>
     *             </ul>
     */
    public static void main(String[] args) {
        System.out.println("MiniVHDL to FIRRTL Compiler");

        if (args.length < 2) {
            System.err.println("Error: Missing required arguments. Usage: java Main <filename> <topLevelModule>");
            System.exit(1);
        }

        String filename = args[0];
        String topLevelModule = args[1];
        Scanner scanner = new Scanner(filename);
        Parser parser = new Parser(scanner);
        parser.Parse();
        Circuit circuit = parser.circuit;

        System.out.printf("%d %s detected%n", parser.errors.count, parser.errors.count == 1 ? " error" : " errors");

        if (parser.errors.count == 0) {
            Generator generator = new Generator();
            String firrtlProgram = generator.generate(topLevelModule, circuit);
            try (PrintWriter pw = new PrintWriter(getOutputFile(filename, topLevelModule))) {
                pw.println(firrtlProgram);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getOutputFile(String inputFile, String topLevelModule) {
        File input = new File(inputFile);
        String parentDir = input.getParent();
        return (parentDir != null ? parentDir + File.separator : "") + topLevelModule + ".fir";
    }
}