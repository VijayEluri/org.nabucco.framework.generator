/*
 * Copyright 2012 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nabucco.framework.generator.parser.syntaxtree;

import org.nabucco.framework.generator.parser.visitor.GJNoArguVisitor;
import org.nabucco.framework.generator.parser.visitor.GJVisitor;
import org.nabucco.framework.generator.parser.visitor.GJVoidVisitor;
import org.nabucco.framework.generator.parser.visitor.Visitor;

/**
 * This class contains manually extracted method body content using the JAVACODE keyword.
 * <p/>
 * Attention, this class is not generated by JavaCC/JTB and must created manually to fix the JTB bug
 * about javacode in the JTB grammar file.
 * 
 * @see http://compilers.cs.ucla.edu/jtb/jtb-2003/releasenotes.html
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
public class Block implements Node {

    private static final long serialVersionUID = 1L;

    private Node parent;

    private String content;

    /**
     * Creates a new {@link Block} instance.
     * 
     * @param content
     *            the extracted content
     */
    public Block(String content) {
        this.content = content;
    }

    @Override
    public void accept(Visitor v) {
    }

    @Override
    public <R, A> R accept(GJVisitor<R, A> v, A argu) {
        return null;
    }

    @Override
    public <R> R accept(GJNoArguVisitor<R> v) {
        return null;
    }

    @Override
    public <A> void accept(GJVoidVisitor<A> v, A argu) {
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public Node getParent() {
        return this.parent;
    }

    /**
     * Getter for the content.
     * 
     * @return Returns the content.
     */
    public String getContent() {
        return this.content;
    }

}
