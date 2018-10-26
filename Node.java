
/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {

  public static int count = 0; // maintain unique id for each node
  private double rv; // to store return value of evaluate for if-else
  private int id;
  public static int argCount = 0, paramCount = 0; //Used to compare number of arguments to number of parameters

  private static Node funcRoot;
  private boolean returnBool; //true if return statement has been executed

  private String kind; // non-terminal or terminal category for the node
  private String info; // extra information about the node such as
                       // the actual identifier for an I

  // references to children in the parse tree
  private Node first, second, third;

  // memory tables shared by all nodes but private to a function
  private static ArrayList<MemTable> tables;

  private static Scanner keys = new Scanner(System.in);

  // construct a common node with no info specified
  public Node(String k, Node one, Node two, Node three) {
    kind = k;
    info = "";
    first = one;
    second = two;
    third = three;
    id = count;
    count++;
    System.out.println(this);
  }

  // construct a node with specified info
  public Node(String k, String inf, Node one, Node two, Node three) {
    kind = k;
    info = inf;
    first = one;
    second = two;
    third = three;
    id = count;
    count++;
    System.out.println(this);
  }

  // construct a node that is essentially a token
  public Node(Token token) {
    kind = token.getKind();
    info = token.getDetails();
    first = null;
    second = null;
    third = null;
    id = count;
    count++;
    System.out.println(this);
  }

  public String toString() {
    return "#" + id + "[" + kind + "," + info + "]<" + nice(first) + " " + nice(second) + ">";
  }

  public String nice(Node node) {
    if (node == null) {
      return "";
    } else {
      return "" + node.id;
    }
  }

  // produce array with the non-null children
  // in order
  private Node[] getChildren() {
    int count = 0;
    if (first != null)
      count++;
    if (second != null)
      count++;
    if (third != null)
      count++;
    Node[] children = new Node[count];
    int k = 0;
    if (first != null) {
      children[k] = first;
      k++;
    }
    if (second != null) {
      children[k] = second;
      k++;
    }
    if (third != null) {
      children[k] = third;
      k++;
    }

    return children;
  }

  // ******************************************************
  // graphical display of this node and its subtree
  // in given camera, with specified location (x,y) of this
  // node, and specified distances horizontally and vertically
  // to children
  public void draw(Camera cam, double x, double y, double h, double v) {

    System.out.println("draw node " + id);

    // set drawing color
    cam.setColor(Color.black);

    String text = kind;
    if (!info.equals(""))
      text += "(" + info + ")";
    cam.drawHorizCenteredText(text, x, y);

    // positioning of children depends on how many
    // in a nice, uniform manner
    Node[] children = getChildren();
    int number = children.length;
    System.out.println("has " + number + " children");

    double top = y - 0.75 * v;

    if (number == 0) {
      return;
    } else if (number == 1) {
      children[0].draw(cam, x, y - v, h / 2, v);
      cam.drawLine(x, y, x, top);
    } else if (number == 2) {
      children[0].draw(cam, x - h / 2, y - v, h / 2, v);
      cam.drawLine(x, y, x - h / 2, top);
      children[1].draw(cam, x + h / 2, y - v, h / 2, v);
      cam.drawLine(x, y, x + h / 2, top);
    } else if (number == 3) {
      children[0].draw(cam, x - h, y - v, h / 2, v);
      cam.drawLine(x, y, x - h, top);
      children[1].draw(cam, x, y - v, h / 2, v);
      cam.drawLine(x, y, x, top);
      children[2].draw(cam, x + h, y - v, h / 2, v);
      cam.drawLine(x, y, x + h, top);
    } else {
      System.out.println("no Node kind has more than 3 children???");
      System.exit(1);
    }

  }// draw

  public static void error(String message) {
    System.out.println(message);
    System.exit(1);
  }

  // ask this node to execute itself
  // (for nodes that don't return a value)
  public void execute() {

    if (kind.equals("program")) {
      tables = new ArrayList<MemTable>();
      if (second != null)
        funcRoot = second;
      else
        error("Program does have any function deffinitions");
      if (first != null)
        first.evaluate();
      else
        error("Program does have an initial function call");
    }

    else if (kind.equals("funcDef")) {
        if(first != null) {
            paramCount = 0;
            first.execute();
            if (paramCount != argCount)
                error("Function " + info + " has " + paramCount + " parameters and was passed " + argCount + "arguments");
        }
        if(second != null)
            second.execute();
    }

    else if (kind.equals("params")) {
        MemTable tempTable = tables.get(tables.size()-1);
        tempTable.changeName(paramCount, info);
        tables.set(tables.size()-1, tempTable);
        paramCount++;
        if (first != null) {
            first.execute();
        }
    }

    else if (kind.equals("args")) {
        MemTable tempTable = tables.get(tables.size()-1);
        tempTable.store((Integer.toString(argCount)), first.evaluate());
        tables.set(tables.size()-1, tempTable);
        argCount++;
        if (second != null) {
          second.execute();
        }
    }

    else if (kind.equals("stmts")) {
      if (first != null) {
        first.execute();
      }
      if (second != null && !returnBool) {
          second.execute();
      }
    }

    else if (kind.equals("if")) {
      if(first.evaluate() != 0){
        if(second != null){
          second.execute();
        }
      }
      else{
        if(third != null){
          third.execute();
        }
      }
    }

    else if (kind.equals("return")) {
      rv = first.evaluate();
      returnBool = true;
    }

    else if (kind.equals("print")) {
      System.out.print(info);
    }

    else if (kind.equals("prtexp")) {
      double value = first.evaluate();
      if (value % 1 == 0) {
        System.out.print((int) value);
      } else {
        System.out.print(value);
      }
    }

    else if (kind.equals("nl")) {
      System.out.print("\n");
    }

    else if (kind.equals("sto")) {
      double value = first.evaluate();
      MemTable table = tables.get(tables.size()-1);
      table.store(info, value);
      tables.set(tables.size()-1, table);
    }

    else {
      error("Unknown kind of node [" + kind + "]");
    }

  }// execute

  // compute and return value produced by this node
  public double evaluate() {

    if (kind.equals("funcCall")) {
      boolean found = false, eof = false;
      argCount = 0;
      if(first != null){
          tables.add(new MemTable());
          first.execute();
      }
      Node node = funcRoot;
      while (!found && !eof) {
        if (info == node.first.info) {
          found = true;
          node.first.execute();
        }
        else {
          if (node.second != null) {
            node = node.second;
          }
          else {
            eof = true;
            error("Function " + info + " was not found.");
          }
        }
      }
      tables.remove(tables.size()-1);
      returnBool = false;
      return rv;
    }

    else if (kind.equals("lt")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a < b) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("le")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a < b || a == b) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("eq")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a == b) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("ne")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a != b) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("or")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a != 0 || b != 0) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("and")) {
      double a = Double.parseDouble(first.info);
      double b = Double.parseDouble(second.info);

      if (a != 0 && b != 0) {
        return 1;
      } else {
        return 0;
      }
    } 
    else if (kind.equals("not")) {
      double a = Double.parseDouble(first.info);

      if (a == 0) {
        return 1;
      } else {
        return 0;
      }
    } 

    else if (kind.equals("num")) {
      return Double.parseDouble(info);
    }

    else if (kind.equals("var")) {
      MemTable table = tables.get(tables.size()-1);
      return table.retrieve(info);
    }

    else if (kind.equals("+") || kind.equals("-")) {
      double value1 = first.evaluate();
      double value2 = second.evaluate();
      if (kind.equals("+"))
        return value1 + value2;
      else
        return value1 - value2;
    }

    else if (kind.equals("*") || kind.equals("/")) {
      double value1 = first.evaluate();
      double value2 = second.evaluate();
      if (kind.equals("*"))
        return value1 * value2;
      else
        return value1 / value2;
    }

    else if (kind.equals("input")) {
      return keys.nextDouble();
    }

    else if (kind.equals("sqrt") || kind.equals("cos") || kind.equals("sin") || kind.equals("atan")) {
      double value = first.evaluate();

      if (kind.equals("sqrt"))
        return Math.sqrt(value);
      else if (kind.equals("cos"))
        return Math.cos(Math.toRadians(value));
      else if (kind.equals("sin"))
        return Math.sin(Math.toRadians(value));
      else if (kind.equals("atan"))
        return Math.toDegrees(Math.atan(value));
      else {
        error("unknown function name [" + kind + "]");
        return 0;
      }

    }

    else if (kind.equals("pow")) {
      double value1 = first.evaluate();
      double value2 = second.evaluate();
      return Math.pow(value1, value2);
    }

    else if (kind.equals("opp")) {
      double value = first.evaluate();
      return -value;
    }

    else if (kind.equals("round")) {
      return (int) Math.round(first.evaluate());
    }

    else if (kind.equals("trunc")) {
      return (double) Math.floor(first.evaluate());
    }

    else {
      error("Unknown node kind [" + kind + "]");
      return 0;
    }

  }// evaluate

}// Node
