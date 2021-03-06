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
package org.nabucco.framework.generator.compiler.transformation.xml.datatype;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nabucco.framework.generator.compiler.NabuccoCompilerSupport;
import org.nabucco.framework.generator.compiler.constants.NabuccoXmlTemplateConstants;
import org.nabucco.framework.generator.compiler.transformation.xml.constants.PersistenceConstants;
import org.nabucco.framework.generator.compiler.transformation.xml.visitor.NabuccoToXmlVisitorContext;
import org.nabucco.framework.generator.compiler.visitor.NabuccoVisitorException;
import org.nabucco.framework.generator.parser.model.NabuccoModel;
import org.nabucco.framework.generator.parser.syntaxtree.BasetypeDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.DatatypeDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.DatatypeStatement;
import org.nabucco.framework.generator.parser.syntaxtree.EnumerationDeclaration;
import org.nabucco.framework.mda.model.MdaModel;
import org.nabucco.framework.mda.model.xml.XmlDocument;
import org.nabucco.framework.mda.model.xml.XmlModel;
import org.nabucco.framework.mda.template.xml.XmlTemplate;
import org.nabucco.framework.mda.template.xml.XmlTemplateException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * NabuccoToXmlDatatypeSuperclassVisitor
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
class NabuccoToXmlDatatypeSuperclassVisitor extends NabuccoToXmlDatatypeVisitor implements PersistenceConstants {

    private String superClassName;

    private String componentPrefix;

    private List<Node> elementList = new ArrayList<Node>();

    /**
     * Creates a new {@link NabuccoToXmlDatatypeSuperclassVisitor} instance.
     * 
     * @param visitorContext
     *            the visitor context
     * @param mainVisitor
     *            the main ORM visitor
     * @param rootPackage
     *            the root package of the XML transformation (the starting element)
     */
    public NabuccoToXmlDatatypeSuperclassVisitor(NabuccoToXmlVisitorContext visitorContext, String componentName,
            NabuccoToXmlDatatypeCollector collector, String rootPackage, String componentPrefix) {
        super(visitorContext, collector, rootPackage);

        super.setComponentName(componentName);
        this.componentPrefix = componentPrefix;
    }

    @Override
    public void visit(DatatypeStatement nabuccoDatatype, MdaModel<XmlModel> target) {

        this.superClassName = nabuccoDatatype.nodeToken2.tokenImage;

        // Visit sub-nodes first!
        super.visit(nabuccoDatatype, target);

        String componentName = super.getComponentName();
        if (componentName == null) {
            componentName = super.getProjectName(null, null);
        }

        try {
            XmlDocument document = super.extractDocument(NabuccoXmlTemplateConstants.ORM_FRAGMENT_TEMPLATE);

            document.getDocument().getDocumentElement().setAttribute(NAME, this.superClassName);
            document.getDocument().getDocumentElement().setAttribute(ORDER, FRAGMENT_ORDER_SUPERCLASS);

            // Ref IDs
            this.createParentRefIds(target);

            Element entityElement = this.createMappedSuperclass(this.superClassName);

            document.getDocument().getDocumentElement()
                    .appendChild(document.getDocument().importNode(entityElement, true));

            // File creation
            document.setProjectName(componentName);
            document.setConfFolder(super.getConfFolder() + FRAGMENT + File.separator);

            String superclassImport = super.getVisitorContext().getPackage() + PKG_SEPARATOR + this.superClassName;

            super.collector.addMappedSuperclass(superclassImport, document);

        } catch (XmlTemplateException te) {
            throw new NabuccoVisitorException("Error during XML template datatype processing.", te);
        }
    }

    /**
     * Create the reference IDs for the datatypes parent datatypes.
     * 
     * @param target
     *            the java target
     */
    private void createParentRefIds(MdaModel<XmlModel> target) {
        NabuccoModel parent = super.getParent();

        if (parent == null) {
            return;
        }

        String pkg = super.getVisitorContext().getPackage();
        if (!NabuccoCompilerSupport.isOtherComponent(pkg, parent.getPackage())) {
            return;
        }

        NabuccoToXmlDatatypeRefIdVisitor visitor = new NabuccoToXmlDatatypeRefIdVisitor(super.getVisitorContext());
        parent.getUnit().accept(visitor, target);

        this.elementList.addAll(visitor.getElementList());
    }

    /**
     * Create an mapped-superclass XML tag for the current class.
     * 
     * @param name
     *            name of the super-class
     * 
     * @return the XML element
     * 
     * @throws XmlTemplateException
     */
    private Element createMappedSuperclass(String name) throws XmlTemplateException {

        XmlTemplate ormTemplate = this.getVisitorContext().getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

        String pkg = this.getVisitorContext().getPackage();

        Element entityElement = (Element) ormTemplate.copyNodesByXPath(XPATH_SUPERCLASS).get(0);
        entityElement.setAttribute(CLASS, pkg + PKG_SEPARATOR + name);

        Element attributes = (Element) entityElement.getElementsByTagName(ATTRIBUTES).item(0);

        NabuccoToXmlDatatypeVisitorSupport.mergeAttributeNodes(attributes, this.elementList);

        return entityElement;
    }

    @Override
    public void visit(DatatypeDeclaration nabuccoDatatype, MdaModel<XmlModel> target) {
        String componentName = super.getComponentName();
        String pkg = super.getVisitorContext().getPackage();

        boolean isTransient = nabuccoDatatype.nodeOptional.present();
        boolean isSameComponent = !NabuccoCompilerSupport.isOtherComponent(componentName, pkg);

        // Only generate Datatype reference when in the same component (ref ID) or transient!
        if (isSameComponent || isTransient) {
            super.createEntityRelation(nabuccoDatatype, this.elementList, this.superClassName, this.componentPrefix);
        } else {
            String refName = nabuccoDatatype.nodeToken2.tokenImage;
            try {
                super.createTransientField(refName, this.elementList);
            } catch (XmlTemplateException te) {
                throw new NabuccoVisitorException("Error creating transient XML tag for '" + refName + "'.", te);
            }
        }
    }

    @Override
    public void visit(BasetypeDeclaration nabuccoBasetype, MdaModel<XmlModel> target) {
        super.createBasetypeRelation(nabuccoBasetype, this.elementList);
    }

    @Override
    public void visit(EnumerationDeclaration nabuccoEnum, MdaModel<XmlModel> target) {
        super.createEnumRelation(nabuccoEnum, this.elementList);
    }

}
