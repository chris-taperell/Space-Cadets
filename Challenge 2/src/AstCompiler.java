import java.util.*;

/**
 * Takes in a text file's content and compiles the AST for it, which can then be interpreted.
 * Since this language is relatively simple in syntax, lines are delimited with ;
 * and the only control statement is WHILE, this isn't too complex to manage.
 */
public class AstCompiler {
  public static boolean isNumeric(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private final String[] lines;

  public AstCompiler(String content) {
    lines = content.split(";");
  }

  public AstNode compile() throws SyntaxError {
    // The root node is a node with a single condition - ONCE;
    // so it does nothing but contain the entire rest of the tree.
    AstNode rootNode = new AstNode(0, new TruthCondition(TruthTypes.ONCE, 0, 0));
    makeAst(rootNode, lines, 0);

    return rootNode;
  }

  public String[] getLines() {
    return lines;
  }

  /**
   * Recursive function to create the AST from a specific line number.
   * Returns when it encounters "end;".
   * Since it modifies the AST in-place, there is no need to return the AST.
   * It instead returns the number of lines parsed, to allow for easy handling of moving on the pointer.
   */
  private int makeAst(AstNode root, String[] lines, int position) throws SyntaxError {
    int offset = position;

    // We only need to initialise this once.
    // List<TokenTypes[]> grammarList = Arrays.asList(CodeGrammars.acceptedGrammars);
    // Maybe not even once.

    for (int i = 0; i + offset < lines.length; i++) {
      // Essentially; parse every line in turn, adding it to the root.
      // If the line matches the signature of a WHILE
      // (or any other control statements that may or may not be added lol haha ;) )
      // then add the condition node to the tree and recurse into that node starting at line i,
      // then add the number of lines handled inside the recursed function to offset.
      //
      int location = i + offset;
      String line = lines[location];

      // Split by every space to get each token, add to the token list
      String[] tokens = line.split(" ");
      List<TokenTypes> tokenTypes = new ArrayList<>(); // Used for syntax checking

      // For all tokens in the list,
      for (String token : tokens) {
        // Try to select an operator or control statement first
        boolean gotToken = false;
        switch (token) {
          case "clear", "incr", "decr" -> {
            tokenTypes.add(TokenTypes.UOPERATOR);
            gotToken = true;
          }
          case "while" -> {
            tokenTypes.add(TokenTypes.CONTROL);
            gotToken = true;
          }
          case "not" -> {
            tokenTypes.add(TokenTypes.NOT);
            gotToken = true;
          }
          case "do" -> {
            tokenTypes.add(TokenTypes.DO);
            gotToken = true;
          }
          case "end" -> {
            tokenTypes.add(TokenTypes.END);
            gotToken = true;
          }
        }

        // If none selected, it has to be either a variable or a literal
        // We can tell by testing if the token is parseable as an int
        if (!gotToken) {
          if (isNumeric(token)) {
            tokenTypes.add(TokenTypes.LITERAL);
          } else {
            tokenTypes.add(TokenTypes.VARIABLE);
          }
        }
      }

      if (tokens.length > 0) {
        // Now that we have a token list, check on grammars to see if the list exists.
        // If it doesn't, throw a SyntaxError.
        // int gramIndex = grammarList.indexOf(tokenTypes.toArray(new TokenTypes[0]));
        // Doesn't work because ??????? so I'm using a custom function that should do it for me

        int gramIndex = findGrammarIndex(CodeGrammars.acceptedGrammars, tokenTypes);

        if (gramIndex == -1) {
          throw new SyntaxError(
              String.format("SyntaxError in line %d: '%s;'", location + 1, line)
          );
        }

        TokenTypes[] grammarArr = CodeGrammars.acceptedGrammars[gramIndex];

        // OK FINALLY we get to take the tokens and build an actual statement.
        // Every statement will contain one operator / condition - UOPERATOR, [BOPERATOR], CONTROL or END.
        // Get it, then fill up the rest of the slots with the given grammar. Remember, we know exactly
        // how the code will look, since we are known to conform to a specific grammar at this point.
        TokenTypes mainToken = grammarArr[CodeGrammars.mainLocations[gramIndex]];
        switch (mainToken) {
          case UOPERATOR -> {
            // Make the new node then continue.
            // Unary operator so construct with only one value.
            AstNode newNode = new AstNode(
                location + 1, new Operator(CodeGrammars.operKeywords.get(tokens[0]), tokens[1])
            );

            root.addNode(newNode);
            // System.out.println("Added UOPERATOR node at " + location + " - " + CodeGrammars.operKeywords.get(tokens[0]));
          }
          case CONTROL -> {
            // Make the new node, then call makeAst() with that node as root and current position as location.
            // Then, add the value returned from makeAst() to offset in this function.
            AstNode newNode = new AstNode(
                location + 1, new TruthCondition(
                CodeGrammars.mainTruthTypes[gramIndex], tokens[1], tokens[3]
            )
            );

            root.addNode(newNode);
            // System.out.println("Added CONTROL node at " + location + " - " + CodeGrammars.mainTruthTypes[gramIndex]);

            offset += makeAst(newNode, lines, location + 1) + 1;
          }
          case END -> {
            // END is a little different; it means we immediately jump out of the current function
            // since the control block is finished.
            // We also return the current value of i (the number of lines traversed)
            // which the caller will add to its offset.
            // System.out.println("Jumped out through END node at " + location);
            return i;
          }
        }
      }
    }

    // After it's done, we don't need to return anything.
    return -1;
  }

  private int findGrammarIndex(TokenTypes[][] arr, List<TokenTypes> gram) {
    for (int i = 0; i < arr.length; i++) {
      if (Arrays.equals(arr[i], gram.toArray())) {
        return i;
      }
    }

    return -1;
  }
}

class SyntaxError extends Exception {
  public SyntaxError(String errorMessage) {
    super(errorMessage);
  }
}

class CodeGrammars {
  /**
   * UOPERATOR VARIABLE                  incr x
   * CONTROL VARIABLE NOT LITERAL DO     while x not 0 do
   * END                                 end
   */
  static TokenTypes[][] acceptedGrammars = {
      {TokenTypes.UOPERATOR, TokenTypes.VARIABLE},
      {TokenTypes.CONTROL, TokenTypes.VARIABLE, TokenTypes.NOT, TokenTypes.LITERAL, TokenTypes.DO},
      {TokenTypes.END}
  };

  // Shows where the main operator/control token will exist in each grammar.
  static int[] mainLocations = {
      0, 0, 0
  };

  // Shows which truth type is associated with each (control) grammar.
  static TruthTypes[] mainTruthTypes = {
      null, TruthTypes.NOT, null
  };

  static HashMap<String, OperatorTypes> operKeywords = new HashMap<String, OperatorTypes>() {
    {
      put("incr", OperatorTypes.INCR);
      put("decr", OperatorTypes.DECR);
      put("clear", OperatorTypes.CLEAR);
    }
  };
}

enum TokenTypes {
  // UOPERATOR - Unary operator
  // BOPERATOR - Binary operator
  VARIABLE, LITERAL, CONTROL, UOPERATOR, BOPERATOR, NOT, DO, END
}