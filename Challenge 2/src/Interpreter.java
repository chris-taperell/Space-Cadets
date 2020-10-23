import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;

public class Interpreter {
  // Compile the tree from the file being read,
  // then run a pre-order traversal on it.
  // When branching down into a condition, run the child nodes until the condition is no longer true.
  public static String[] lines; // Not technically needed but used for debugging purposes
  public static Integer lineCur; // Same here

  public static void main(String[] args) {
    Hashtable<String, Integer> runVals = new Hashtable<>();
    StringBuilder fileContentBuild = new StringBuilder();

    System.out.print("Enter the path to a .bb file >> ");
    Scanner scanner = new Scanner(System.in);
    String filePath = scanner.nextLine();

    System.out.print("Verbose mode? [y/N] ");
    String verboseOpt = scanner.nextLine();
    boolean verbose = verboseOpt.equals("y");

    try {
      File fileObj = new File(filePath);
      Scanner fileReadScanner = new Scanner(fileObj);
      while (fileReadScanner.hasNextLine()) {
        fileContentBuild.append(fileReadScanner.nextLine().replace("    ", ""));
      }
    } catch (FileNotFoundException e) {
      System.out.println("Invalid file path :(");
      return;
    }

    String fileContent = fileContentBuild.toString();

    try {
      AstCompiler compiler = new AstCompiler(fileContent);
      AstNode root = compiler.compile();
      lines = compiler.getLines();
      runNode(root, runVals, verbose);
    } catch (SyntaxError e) {
      System.out.println("Failed to compile - " + e.getMessage());
      return;
    }
  }

  public static void printInfo(Hashtable<String, Integer> vals, boolean inCondition, boolean vrb) {
    // lineCur = 0 means that the code has ended
    if (lineCur == 0) {
      if (!inCondition) {
        System.out.println("\nCode finished. Have a great day!");

        if (!vrb) {
          for (String key : vals.keySet()) {
            System.out.println(key + ": " + vals.get(key).toString());
          }
        }
      }
    } else if (vrb) {
      if (inCondition) {
        System.out.println("\nLine " + (lineCur - 1) + " - " + lines[lineCur - 1] + ";  " +
            "Control statement passed, so looping inside...\n");
      } else {
        System.out.println("\nLine " + (lineCur - 1) + " - " + lines[lineCur - 1] + ";\n");
      }

      for (String key : vals.keySet()) {
        System.out.println(key + ": " + vals.get(key).toString());
      }
    }
  }

  public static void runNode(AstNode node, Hashtable<String, Integer> vals, boolean verbose) {
    // Run every node in this node's children. After this, run the node's operator, if it has one.
    // Thanks to my "bulletproof" """code""" and """""futureproofing""""", this is very short.
    // Test if condition is true before moving to children
    // If no condition (-1), default to false
    AstNode[] children = node.getChildren();
    // While the condition is true, keep running children
    while (node.testCondition(vals) == 1) {
      lineCur = node.line;
      printInfo(vals, true, verbose);
      for (AstNode child : children) {
        runNode(child, vals, verbose);
      }
    }

    // Run operator
    node.runOperator(vals);
    lineCur = node.line;
    printInfo(vals, false, verbose);
  }
}
