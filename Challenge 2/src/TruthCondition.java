import java.util.Hashtable;

/**
 * Essentially, denotes a specific true/false condition.
 */
public class TruthCondition {
  private final TruthTypes ttyp;
  private String op1 = null;
  private String op2 = null;

  private int op1Lit = -1;
  private int op2Lit = -1;
  private boolean first = true;

  public TruthCondition(TruthTypes s_ttyp, String s_op1, String s_op2) {
    if (AstCompiler.isNumeric(s_op1)) {
      op1Lit = Integer.parseInt(s_op1);
    } else {
      op1 = s_op1;
    }

    if (AstCompiler.isNumeric(s_op2)) {
      op2Lit = Integer.parseInt(s_op2);
    } else {
      op2 = s_op2;
    }

    ttyp = s_ttyp;
  }

  // Constant
  public TruthCondition(TruthTypes s_ttyp, int l_op1, int l_op2) {
    op1 = null;
    op2 = null;
    op1Lit = l_op1;
    op2Lit = l_op2;
    ttyp = s_ttyp;
  }

  /**
   * Tests this statement based on typ, operator, operand
   */
  public boolean testTrue(Hashtable<String, Integer> vals) {
    // Get from vals if a variable else use literal value
    // If the condition is ONCE, then it doesn't need either, so don't fetch

    int op1Got = -1;
    int op2Got = -1;
    if (ttyp != TruthTypes.ONCE) {
      op1Got = op1 == null ? op1Lit : vals.get(op1);
      op2Got = op2 == null ? op2Lit : vals.get(op2);
    }

    switch (ttyp) {
      case ONCE -> {
        boolean firstMade = first;
        first = false;
        return firstMade;
      }
      case IS -> {
        return op1Got == op2Got;
      }
      case NOT -> {
        return op1Got != op2Got;
      }
      case GT -> {
        return op1Got > op2Got;
      }
      case LT -> {
        return op1Got < op2Got;
      }
    }

    return false;
  }
}