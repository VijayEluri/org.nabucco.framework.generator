//
// Generated by JTB 1.3.2
//

package org.nabucco.framework.generator.parser.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * block -> Block()
 * </PRE>
 */
public class MethodBody implements Node {
   private Node parent;
   public Block block;

   public MethodBody(Block n0) {
      block = n0;
      if ( block != null ) block.setParent(this);
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

