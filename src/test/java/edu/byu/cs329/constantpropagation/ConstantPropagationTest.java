package edu.byu.cs329.constantpropagation;

import java.net.URI;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.byu.cs329.constantfolding.Utils;

public class ConstantPropagationTest {

  /**
   * Get the ASTNode from compiling the named file.
   * 
   * @param t object to use to get the class loader.
   * @param name file to be opened.
   * @return ASTNode for parsed file.
   */
  public static ASTNode getASTNodeFor(final Object t, String name){
    URI uri = Utils.getUri(t, name);
    Objects.requireNonNull(uri);
    ASTNode root = Utils.getCompilationUnit(uri);
    return root;
  }

}
