package edu.byu.cs329.constantfolding;

import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

public class BlockFolding implements Folding {
  
  class Visitor extends ASTVisitor {
    boolean didFold = false;
    
    @Override
    public void endVisit(Block node) {
      List<Statement> statements = getStatementList(node.statements());
      AST ast = node.getAST();
      Block block  = ast.newBlock();
      List<Statement> newStatements = getStatementList(block.statements());
      for (Statement statement : statements) {
        addStatements(statement, newStatements);
      }
      Utils.replaceChildInParent(node, block);
    }

    private void addStatements(Statement statement, List<Statement> newStatements) {
      AST ast = statement.getAST();
      if (!(statement instanceof Block)) {
        Statement newStatement = (Statement)(ASTNode.copySubtree(ast, statement));
        newStatements.add(newStatement);
        return;
      }
      didFold = true;
      Block block = (Block)statement;
      List<Statement> statements = getStatementList(block.statements());
      for (Statement statementInBlock : statements) {
        Statement newStatement = (Statement)(ASTNode.copySubtree(ast, statementInBlock));
        newStatements.add(newStatement);
      }
    }

    private List<Statement> getStatementList(Object list) {
      @SuppressWarnings("unchecked")
      List<Statement> statementList = (List<Statement>)(list);
      return statementList;
    }
  }

  /**
   * Folds block when parent is a Block
   * 
   * <p>Visits the root and any reachable nodes from the root to replace
   * any immediate child in a Block that is itself a Block with the statements 
   * in that child block. In other words, it removes nested blocks
   * that are not part of any complex statement (e.g., if, while, etc.).
   * 
   * <p>top(root) := all nodes reachable from root such that each node 
   *                 is a statement in a block that is a block itself.
   * 
   * <p>statements(n) := all statements in a block n.
   * 
   * <p>topParents(root) := all nodes such that each one is the parent
   *                        of some node in top(root)
   * 
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   * 
   * @ensures fold(root) = !(old(top(root)) == \emptyset)
   * @ensures nodes(root) = 
   *     (old(nodes(root)) \setminus old(top(root)))
   * @ensures \forall n \in old(top(root)), 
   *        parent(statments(n)) = old(parent(n)))
   *     /\ children(old(parent(n))) = 
   *          (old(children(parent(n))) \setminus {n}) \cup statements(n)
   * @ensures \forall n \in old(topParents(root)), parent(n) = old(parent(n))
   * @ensures \forall n \in (old(nodes(root)) \setminus 
   *         (old(top(root)) \cup old(topParents(root))),
   *        parents(n) = old(parents(n))
   *     /\ children(n) = old(children(n))
   * @ensures top(root) = \emptyset 
   *     /\ topParents(root) = \emptyset
   *   
   * @param root the root of the tree to traverse
   * @return true if a block is reduced otherwise false
   */
  @Override
  public boolean fold(ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    Utils.requiresNonNull(root, "Null root passed to InfixPlusFolding.fold");

    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      Utils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to InfixPlusFolding.fold");
    }
  }
}
