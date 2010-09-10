//
// Generated by JTB 1.3.2
//

package org.nabucco.framework.generator.parser.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;EXTENDS&gt;
 * nodeChoice -> ( &lt;UNQUALIFIED_TYPE_NAME&gt; | &lt;QUALIFIED_TYPE_NAME&gt; )
 * </PRE>
 */
public class ExtensionDeclaration implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice;

   public ExtensionDeclaration(NodeToken n0, NodeChoice n1) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public ExtensionDeclaration(NodeChoice n0) {
      nodeToken = new NodeToken("extends");
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public void accept(org.nabucco.framework.generator.parser.visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(org.nabucco.framework.generator.parser.visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(org.nabucco.framework.generator.parser.visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(org.nabucco.framework.generator.parser.visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}
