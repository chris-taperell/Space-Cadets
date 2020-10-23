import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Forms a single node of an AST. Can have arbitrarily many nodes, for which the interpreter
 * will recursively iterate through for as long as node.condition remains true.
 */
public class AstNode {
  // All nodes must contain either a condition or an operator.
  // The constructor is overloaded to reflect this.
  private TruthCondition condition = null;
  private Operator operator = null;
  public int line;

  private List<AstNode> children = new ArrayList<>();

  public AstNode(int line, TruthCondition sCond) {
    this.line = line;
    condition = sCond;
  }

  public AstNode(int line, Operator sOper) {
    this.line = line;
    operator = sOper;
  }

  // Returns an array of the node's children.
  public AstNode[] getChildren() {
    return children.toArray(new AstNode[0]);
  }

  public void addNode(AstNode node) {
    this.children.add(node);
  }

  public void runOperator(Hashtable<String, Integer> vals) {
    // Does not run operator if operator is null.
    if (operator != null) {
      operator.runOperator(vals);
    }
  }

  public int testCondition(Hashtable<String, Integer> vals) {
    // Returns -1 (no condition) if condition is null.
    if (condition == null) {
      return -1;
    } else {
      // Convert to int using simple ternary expression
      return condition.testTrue(vals) ? 1 : 0;
    }
  }
}
