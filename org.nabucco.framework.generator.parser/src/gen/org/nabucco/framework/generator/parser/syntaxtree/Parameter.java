//
// Generated by JTB 1.3.2
//

package org.nabucco.framework.generator.parser.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeOptional -> [ &lt;COMMA_CHAR&gt; ]
 * nodeToken -> &lt;UNQUALIFIED_TYPE_NAME&gt;
 * nodeToken1 -> &lt;NAME_IDENTIFIER&gt;
 * </PRE>
 */
public class Parameter implements Node {
   private Node parent;
   public NodeOptional nodeOptional;
   public NodeToken nodeToken;
   public NodeToken nodeToken1;

   public Parameter(NodeOptional n0, NodeToken n1, NodeToken n2) {
      nodeOptional = n0;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeToken1 = n2;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
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

