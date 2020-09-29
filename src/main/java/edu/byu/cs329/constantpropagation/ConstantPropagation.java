package edu.byu.cs329.constantpropagation;

import edu.byu.cs329.constantfolding.Utils;
import java.io.File;
import java.io.PrintWriter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constant Propagation.
 * 
 * @author Eric Mercer
 */
public class ConstantPropagation {

  static final Logger log = LoggerFactory.getLogger(ConstantPropagation.class);

  /**
   * Performs constant propagation.
   * 
   * @param node the root node for constant propagation.
   */
  public static void propagate(ASTNode node) {
  }

  /**
   * Performs constant folding an a Java file.
   * 
   * @param args args[0] is the file to fold and args[1] is where to write the
   *             output
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("Missing Java input file or output file on command line");
      System.out.println("usage: java DomViewer <java file to parse> <html file to write>");
      System.exit(1);
    }

    File inputFile = new File(args[0]);
    // String inputFileAsString = readFile(inputFile.toURI());
    ASTNode node = Utils.getCompilationUnit(inputFile.toURI());//parse(inputFileAsString);
    ConstantPropagation.propagate(node);

    try {
      PrintWriter writer = new PrintWriter(args[1], "UTF-8");
      writer.print(node.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
