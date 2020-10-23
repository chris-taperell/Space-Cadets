import java.util.Hashtable;

/**
 * Applies a specific function to op1 (with op2 included if necessary)
 */
public class Operator {
  private final OperatorTypes typ;
  private final String op1;
  private final String op2;
  private final int op2Lit;

  public Operator(OperatorTypes s_typ, String s_op1) {
    typ = s_typ;
    op1 = s_op1;
    op2 = null;
    op2Lit = -1;
  }

  public Operator(OperatorTypes s_typ, String s_op1, String s_op2) {
    typ = s_typ;
    op1 = s_op1;
    op2 = s_op2;
    op2Lit = -1;
  }

  public Operator(OperatorTypes s_typ, String s_op1, int l_op2) {
    typ = s_typ;
    op1 = s_op1;
    op2Lit = l_op2;
    op2 = null;
  }

  /**
   * Always modifies op1 in-place.
   */
  public void runOperator(Hashtable<String, Integer> vals) {
    // Get from vals if a variable else use literal value (only for op2 as op1 must be a variable)
    // op2 may never be used, but setting it to -1 means no conditional required

    int op1Got = -1;
    int op2Got = -1;

    // If CLEAR, op1, op2 may not exist - so don't try to fetch it
    if (typ != OperatorTypes.CLEAR) {
      op1Got = vals.get(op1);
      op2Got = op2 == null ? op2Lit : vals.get(op2);
    }

    switch (typ) {
      case CLEAR -> {
        // Make a new entry with value 0
        vals.put(op1, 0);
      }
      case INCR -> {
        // Add 1
        vals.put(op1, vals.get(op1) + 1);
      }
      case DECR -> {
        // Take away 1
        vals.put(op1, vals.get(op1) - 1);
      }
      case ADD -> {
        // TODO write inbuilt function that implements ADD using a looped INCR
      }
      case SUB -> {
        // TODO write inbuilt function that implements SUB using a looped DECR
      }
      case MUL -> {
        // TODO write inbuilt function that implements MUL using a looped ADD
      }
      case DIV -> {
        // TODO write inbuilt function that implements DIV... somehow
      }
    }
  }
}
