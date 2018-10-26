   /*
    This class provides a recursive descent parser 
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      System.out.println("-----> parsing <program>:");
      first = parseFuncCall();

      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "program", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseFuncDefs();
         return new Node( "program", first, second, null );
      }
   }

   private Node parseFuncCall() {
      System.out.println("-----> parsing <funcCall>:");
      Token token = lex.getNextToken();
      funcName = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();
      if(token.getDetails() == ")"){
         return new Node("funcCall", funcName, null, null, null);
      }
      else{
         lex.putBackToken(token);
         Node first = parseArgs();
         return new Node("funcCall", funcName, first, null, null);
      }
   }

   private Node parseFuncDefs() {
      System.out.println("-----> parsing <funcDefs>:");

      Node first = parseFuncDef();

      // look ahead to see if there are more funcDefs
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "funcDefs", first, null, null );
      }
      else {
         lex.putBackToken(token);
         Node second = parseFuncDef();
         return new Node("funcDefs", first, second, null);
      }
   }

   private Node parseFuncDef() {
      System.out.println("-----> parsing <funcDef>:");
      Token token = lex.getNextToken();
      errorCheck( token, "var", "def" );
      token = lex.getNextToken();
      funcName = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();
      if(token.getDetails() == ")"){//No <params>
         token = lex.getNextToken();
         if(token.getDetails() == "end"){//No <stmts>
               return new Node("funcDef", funcName, null, null, null);
         }
         else{//Has <stmts>
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("funcDef", funcName, null, second, null);
         }
      }
      else{//Has <params>
         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();
         if(token.getDetails() == "end"){//No <stmts>
            return new Node("funcDef", funcName, first, null, null);
         }
         else{//Has <stmts>
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("funcDef", funcName, first, second, null);
         }
      }
   }

   private Node parseArgs(){
      System.out.println("-----> parsing <args>:");
      Node first = parseExpr();
      Token token = lex.getNextToken();
      if(token.getDetails() == ")"){
         return new Node("args", first, null, null);
      }
      else {
         lex.putBackToken(token);
         Node second = parseArgs();
         return new Node("args", first, second, null);
      }
   }

   private Node parseParams(){
      System.out.println("-----> parsing <params>:");
      Node first = parseFactor();
      Token token = lex.getNextToken();
      if(token.getDetails() == ")"){
         return new Node("params", first, null, null);
      }
      else {
         lex.putBackToken(token);
         Node second = parseParams();
         return new Node("params", first, second, null);
      }
   }

   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
 
      Node first = parseStatement();
 
      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();
 
      if ( token.isKind("eof") || token.getDetails() == "else" || token.getDetails() == "end") {
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
   }// <statements>

   private Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
 
      Token token = lex.getNextToken();
      if ( token.isKind("string") ){
         return new Node( "print", token.getDetails(), null, null, null )
      }
      else if ( token.isKind("var" && token.getDetails() != "if" )) {
            // --------------->>>   <var> = <expr>
            String varName = token.getDetails();
            token = lex.getNextToken();
            errorCheck( token, "single", "=" );
            Node first = parseExpr();
            return new Node( "sto", varName, first, null, null );
      } 
      // --> if-else
      else if ( token.isKind("var") && token.getDetails() == "if" ) {
         Node first = parseExpr(); //return value from parse Expr will 
         token = lex.getNextToken();
         if ( token.isKind("var") && token.getDetails() == "else") {
            token = lex.getNextToken();
            // --> if <expr> else end
            if ( token.isKind("var") && token.getDetails() == "end" ) {
               return new Node( "stmt", "ifelse1" , first, null, null)
            }
            // --> if <expr> else <statements> end
            else {
               lex.putBackToken( token );
               Node second = parseStatements();
               token = lex.getNextToken();
               // break if end not found for if-else
               errorCheck( token, "var", "end" )
               return new Node("stmt", "ifelse2", first, second, null);
            }
         }
         // --> if <expr> <statements>
         else {
            Node second = parseStatements();
            token = lex.getNextToken();
            if ( token.isKind("var") && token.getDetails() == "else") {
               token = lex.getNextToken();
               // --> if <expr> <statements> else end
               if ( token.isKind("var") && token.getDetails() == "end" ) {
                  return new Node( "stmt", "ifelse_2" , first, second, null);
               }
               // --> if <expr> <statements> else <statements> end
               else {
                  lex.putBackToken( token );
                  Node third = parseStatements();
                  token = lex.getNextToken();
                  errorCheck( token, "var", "end" );
                  return new Node("stmt", "ifelse3", first, second, third);
               }
            }
         } 
      }
      // --> return <exp>
      // TODO decide if this is the right way to handle returning
      else if ( token.isKind("var") && token.getDetails() == "return" ){
         Node first = parseExp();
         return new Node("stmt", "return", first);
      }
      else {
      	Node first = parseFuncCall();
      	return first;
      }
 
   }// <statement>

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "+") ||
           token.matches("single", "-") 
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one term
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "*") ||
           token.matches("single", "/") 
         ) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one factor
         lex.putBackToken( token );
         return first;
      }
      
   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
         return new Node("var", token.getDetails(), null, null, null );
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.isKind("bif0") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, null, null, null );
      }
      else if ( token.isKind("bif1") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first, null, null );
      }
      else if ( token.isKind("bif2") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", "," );
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first, second, null );
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         System.out.println("Can't have factor starting with " + token );
         System.exit(1);
         return null;
      }
      
   }// <factor>

  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token + 
                          " to be kind=" + kind + 
                          " and details=" + details );
      System.exit(1);
    }
  }

}
