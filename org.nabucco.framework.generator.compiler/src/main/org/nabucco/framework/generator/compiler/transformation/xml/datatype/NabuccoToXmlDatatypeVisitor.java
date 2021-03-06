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

import java.util.List;

import org.nabucco.framework.generator.compiler.NabuccoCompilerSupport;
import org.nabucco.framework.generator.compiler.constants.NabuccoXmlTemplateConstants;
import org.nabucco.framework.generator.compiler.transformation.NabuccoTransformationException;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.NabuccoAnnotation;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.NabuccoAnnotationMapper;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.NabuccoAnnotationType;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.association.AssociationStrategyType;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.association.FetchStrategyType;
import org.nabucco.framework.generator.compiler.transformation.common.annotation.association.OrderStrategyType;
import org.nabucco.framework.generator.compiler.transformation.java.common.basetype.BasetypeMapping;
import org.nabucco.framework.generator.compiler.transformation.java.constants.CollectionConstants;
import org.nabucco.framework.generator.compiler.transformation.util.NabuccoTransformationUtility;
import org.nabucco.framework.generator.compiler.transformation.util.dependency.NabuccoDependencyResolver;
import org.nabucco.framework.generator.compiler.transformation.xml.basetype.NabuccoToXmlBasetypeFacade;
import org.nabucco.framework.generator.compiler.transformation.xml.constants.PersistenceConstants;
import org.nabucco.framework.generator.compiler.transformation.xml.visitor.NabuccoToXmlTraversingVisitor;
import org.nabucco.framework.generator.compiler.transformation.xml.visitor.NabuccoToXmlVisitorContext;
import org.nabucco.framework.generator.compiler.transformation.xml.visitor.NabuccoToXmlVisitorSupport;
import org.nabucco.framework.generator.compiler.visitor.NabuccoVisitorContext;
import org.nabucco.framework.generator.compiler.visitor.NabuccoVisitorException;
import org.nabucco.framework.generator.parser.model.NabuccoModel;
import org.nabucco.framework.generator.parser.model.NabuccoModelResourceType;
import org.nabucco.framework.generator.parser.model.multiplicity.NabuccoMultiplicityType;
import org.nabucco.framework.generator.parser.model.multiplicity.NabuccoMultiplicityTypeMapper;
import org.nabucco.framework.generator.parser.syntaxtree.BasetypeDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.ComponentDatatypeDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.ComponentStatement;
import org.nabucco.framework.generator.parser.syntaxtree.DatatypeDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.EnumerationDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.ExtensionDeclaration;
import org.nabucco.framework.generator.parser.syntaxtree.NodeToken;
import org.nabucco.framework.mda.logger.MdaLogger;
import org.nabucco.framework.mda.logger.MdaLoggingFactory;
import org.nabucco.framework.mda.model.MdaModel;
import org.nabucco.framework.mda.model.xml.XmlModel;
import org.nabucco.framework.mda.template.xml.XmlTemplate;
import org.nabucco.framework.mda.template.xml.XmlTemplateException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * NabuccoToXmlDatatypeVisitor
 * <p/>
 * Visitor for persistent datatypes. Do not use this class directly, use
 * {@link NabuccoToXmlDatatypeFacade} instead.
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
class NabuccoToXmlDatatypeVisitor extends NabuccoToXmlVisitorSupport implements PersistenceConstants,
        CollectionConstants {

    /** Name of the primary generated component. */
    private String componentName;

    /** Package of the primary generated component. */
    private String rootPackage;

    /** Prefix of the current component @ComponentId */
    private String componentId;

    /** ORM dependencies must be collected seperately */
    NabuccoToXmlDatatypeCollector collector;

    /** The logger */
    private static MdaLogger logger = MdaLoggingFactory.getInstance().getLogger(NabuccoToXmlDatatypeVisitor.class);

    /**
     * Creates a new {@link NabuccoToXmlDatatypeVisitor} instance.
     * 
     * @param visitorContext
     *            the visitor context
     * @param collector
     *            the visitor collector (must be closed after visitation)
     * @param rootPackage
     *            the root package of the XML transformation (the focused component)
     */
    public NabuccoToXmlDatatypeVisitor(NabuccoToXmlVisitorContext visitorContext,
            NabuccoToXmlDatatypeCollector collector, String rootPackage) {
        super(visitorContext);
        if (collector == null) {
            throw new IllegalArgumentException("Collector must be defined.");
        }
        this.collector = collector;
        this.rootPackage = rootPackage;
    }

    @Override
    public void visit(ComponentStatement nabuccoComponent, MdaModel<XmlModel> target) {
        if (this.rootPackage == null) {
            this.rootPackage = super.getVisitorContext().getPackage();
        }

        this.componentId = this.createComponentId(nabuccoComponent).toLowerCase();

        super.visit(nabuccoComponent, target);
    }

    /**
     * Extracts the component id from the ComponentId annotation.
     * 
     * @param nabuccoComponent
     *            the component
     * 
     * @return the component id
     */
    private String createComponentId(ComponentStatement nabuccoComponent) {

        NabuccoAnnotation annotation = NabuccoAnnotationMapper.getInstance().mapToAnnotation(
                nabuccoComponent.annotationDeclaration, NabuccoAnnotationType.COMPONENT_PREFIX);

        if (annotation == null) {
            String componentName = nabuccoComponent.nodeToken2.tokenImage;

            if (componentName.length() < 4) {
                return COMPONENT.substring(0, componentName.length()) + componentName;
            }

            return componentName.substring(0, 4);
        }

        if (annotation.getValue() == null || annotation.getValue().length() != 4) {
            String componentName = nabuccoComponent.nodeToken2.tokenImage;

            if (componentName.length() < 4) {
                return COMPONENT.toLowerCase().substring(0, componentName.length()) + componentName;
            }

            logger.warning("ComponentId ", annotation.getValue(), " is not valid using name '",
                    componentName.substring(0, 4), "'. Must be a 4 character string.");

            return componentName.substring(0, 4);
        }

        return annotation.getValue();
    }

    @Override
    public void visit(ExtensionDeclaration nabuccoExtension, MdaModel<XmlModel> target) {

        // Components do not have an extension declaration!

        super.visit(nabuccoExtension, target); // Extract super type

        String superType = this.getVisitorContext().getNabuccoExtension();
        String superImport = super.resolveImport(superType);

        if (superImport == null) {
            logger.warning("Cannot resolve depdendency for Datatype '", superType, "'.");
            return;
        }

        if (this.collector.contains(superImport)) {
            logger.debug("Depdendency already visited '" + superImport + "'.");
            return;
        }

        this.createMappedSuperclass(superType, superImport, target);
    }

    @Override
    public void visit(ComponentDatatypeDeclaration nabuccoDatatype, MdaModel<XmlModel> target) {

        String type = ((NodeToken) nabuccoDatatype.nodeChoice1.choice).tokenImage;
        boolean isPersistent = nabuccoDatatype.nodeOptional.present();

        String datatypeImport = super.resolveImport(type);

        try {

            if (isPersistent) {

                if (datatypeImport == null) {
                    logger.warning("Cannot resolve depdendency for Datatype '", type, "'.");
                    return;
                }

                this.createEntity(type, datatypeImport, target);

                if (NabuccoAnnotationMapper.getInstance().hasAnnotation(nabuccoDatatype.annotationDeclaration,
                        NabuccoAnnotationType.REFERENCEABLE)) {

                    this.createComponentRelationEntity(datatypeImport, target);
                }
            }

        } catch (NabuccoTransformationException e) {
            throw new NabuccoVisitorException("Error resolving XML dependency for Datatype '" + type + "'.", e);
        }
    }

    /**
     * Creates an entity fragment for the particular type.
     * 
     * @param type
     *            the type
     * @param datatypeImport
     *            the import to resolve
     * @param target
     *            the target model
     * 
     * @throws NabuccoTransformationException
     */
    private void createEntity(String type, String datatypeImport, MdaModel<XmlModel> target)
            throws NabuccoTransformationException {

        if (this.collector.isEntity(datatypeImport)) {
            logger.debug("Entity already visited '" + datatypeImport + "'.");
            return;
        }
        MdaModel<NabuccoModel> model = this.resolveDependency(datatypeImport);

        // Do not create XML for archived entities (only mapped-superclasses).
        if (model.getModel().getResourceType() == NabuccoModelResourceType.ARCHIVE) {
            return;
        }

        NabuccoToXmlVisitorContext context = this.copyContext();

        NabuccoToXmlDatatypeEntityVisitor visitor = new NabuccoToXmlDatatypeEntityVisitor(context, this.collector,
                this.rootPackage, this.componentId);

        model.getModel().getUnit().accept(visitor, target);

        // Create basetype embeddables
        NabuccoToXmlBasetypeFacade.getInstance().createOrmBasetypeFragments(model, target, super.getVisitorContext(),
                this.componentName);
    }

    /**
     * Create an entity fragment for a component relation of a particular type.
     * 
     * @param datatypeImport
     *            the datatype import
     * @param target
     *            the target XML model
     * 
     * @throws NabuccoTransformationException
     */
    private void createComponentRelationEntity(String datatypeImport, MdaModel<XmlModel> target)
            throws NabuccoTransformationException {

        NabuccoToXmlVisitorContext context = this.copyContext();
        MdaModel<NabuccoModel> model = this.resolveDependency(datatypeImport);

        NabuccoToXmlComponentRelationEntityVisitor visitor = new NabuccoToXmlComponentRelationEntityVisitor(
                new NabuccoToXmlVisitorContext(context), this.componentId);

        model.getModel().getUnit().accept(visitor, target);
    }

    /**
     * Creates an mapped superclass fragment for the particular type.
     * 
     * @param type
     *            the type
     * @param superclassImport
     *            the import to resolve
     * @param target
     *            the target model
     */
    private void createMappedSuperclass(String type, String superclassImport, MdaModel<XmlModel> target) {

        if (this.collector.contains(superclassImport)) {
            logger.debug("MappedSuperclass already visited '" + superclassImport + "'.");
            return;
        }

        try {
            MdaModel<NabuccoModel> model = this.resolveDependency(superclassImport);

            NabuccoToXmlVisitorContext context = this.copyContext();

            NabuccoToXmlDatatypeSuperclassVisitor visitor = new NabuccoToXmlDatatypeSuperclassVisitor(context,
                    this.componentName, this.collector, this.rootPackage, this.componentId);

            model.getModel().getUnit().accept(visitor, target);

            // Create basetype embeddables
            NabuccoToXmlBasetypeFacade.getInstance().createOrmBasetypeFragments(model, target,
                    new NabuccoToXmlVisitorContext(super.getVisitorContext()), this.componentName);

        } catch (NabuccoTransformationException e) {
            throw new NabuccoVisitorException("Error resolving XML dependency for Datatype '" + type + "'.", e);
        }
    }

    /**
     * Creates the entity/mapped-superclass relation to another entity.
     * 
     * @param nabuccoDatatype
     *            the datatype
     * @param elementList
     *            the list to add the mapping
     * @param name
     *            name of the root entity/mapped-superclass
     * @param componentPrefix
     *            the component prefix for join tables
     */
    void createEntityRelation(DatatypeDeclaration nabuccoDatatype, List<Node> elementList, String name,
            String componentPrefix) {

        String type = ((NodeToken) nabuccoDatatype.nodeChoice1.choice).tokenImage;
        String refName = nabuccoDatatype.nodeToken2.tokenImage;
        String pkg = super.getVisitorContext().getPackage();
        String typeImport = super.resolveImport(type);

        try {

            NabuccoMultiplicityType multiplicity = NabuccoMultiplicityTypeMapper.getInstance().mapToMultiplicity(
                    nabuccoDatatype.nodeToken1.tokenImage);

            XmlTemplate ormTemplate = this.getVisitorContext().getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

            // Transient Field
            if (nabuccoDatatype.nodeOptional.present()) {
                this.createTransientField(refName, elementList);
                return;
            }

            // NType cannot be mapped
            if (BasetypeMapping.N_TYPE.getName().equals(type)) {
                this.createTransientField(type, elementList);
                return;
            }

            if (NabuccoCompilerSupport.isOtherComponent(pkg, typeImport)) {

                if (multiplicity.isMultiple()) {

                    // TODO: Validate in verifier before!

                    StringBuilder msg = new StringBuilder();
                    msg.append("Relations between datatypes of different components and a multiplicity larger 1 are not valid (");
                    msg.append(refName).append(" in ").append(name).append(".");
                    throw new NabuccoVisitorException(msg.toString());
                }

                this.createRefId(refName, multiplicity, elementList);

            } else {

                AssociationStrategyType association = this.extractAssociationStrategy(nabuccoDatatype);

                FetchStrategyType fetch = this.extractFetchStrategy(nabuccoDatatype);

                Element relation = NabuccoToXmlDatatypeVisitorSupport.resolveMapping(multiplicity, association,
                        ormTemplate);

                // JPA specific property name.
                if (multiplicity.isMultiple()) {
                    this.createTransientField(refName, elementList);

                    // Persistence of maps is currently not supported!
                    if (this.extractOrderStrategy(nabuccoDatatype) == OrderStrategyType.MAPPED) {
                        return;
                    }

                    relation.setAttribute(NAME, refName + SUFFIX_JPA);
                } else {
                    relation.setAttribute(NAME, refName);
                }

                relation.setAttribute(TARGET_ENTITY, typeImport);
                relation.setAttribute(FETCH, fetch.getId());

                String foreignKey;
                if (association == AssociationStrategyType.COMPOSITION && multiplicity.isMultiple()) {
                    foreignKey = NabuccoTransformationUtility.toTableName(name)
                            + TABLE_SEPARATOR + NabuccoTransformationUtility.toTableName(refName) + TABLE_SEPARATOR
                            + ID;
                } else {
                    foreignKey = NabuccoTransformationUtility.toTableName(refName) + TABLE_SEPARATOR + ID;
                }

                // 1:1, 1:N, N:1, N:N Mappings
                if (association == AssociationStrategyType.AGGREGATION && multiplicity.isMultiple()) {
                    this.createJoinTable(relation, name, type, componentPrefix);
                } else {
                    this.createJoinColumn(relation, foreignKey);
                }

                elementList.add(relation);
            }

            // Transient field for code path
            NabuccoAnnotation codePath = NabuccoAnnotationMapper.getInstance().mapToAnnotation(
                    nabuccoDatatype.annotationDeclaration, NabuccoAnnotationType.CODE_PATH);

            if (codePath != null && codePath.getValue() != null) {
                this.createTransientField(refName + NabuccoAnnotationType.CODE_PATH.getName(), elementList);
            }

        } catch (XmlTemplateException te) {
            throw new NabuccoVisitorException("Cannot extract XML template '"
                    + NabuccoXmlTemplateConstants.ORM_TEMPLATE + "'.", te);
        }
    }

    /**
     * Checks whether a type is of another componentName and creates a reference ID.
     * 
     * @param name
     *            the element name
     * @param multiplicity
     *            the multiplicity
     * 
     * @throws XmlTemplateException
     */
    private void createRefId(String name, NabuccoMultiplicityType multiplicity, List<Node> elementList)
            throws XmlTemplateException {

        this.createTransientField(name, elementList);

        XmlTemplate ormTemplate = this.getVisitorContext().getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

        List<Node> nodes = ormTemplate.copyNodesByXPath(XPATH_BASIC);

        if (nodes.size() == 1 && nodes.get(0) instanceof Element) {
            Element basic = (Element) nodes.get(0);
            basic.setAttribute(NAME, name + REF_ID);

            NodeList childNodes = basic.getChildNodes();
            if (childNodes.getLength() > 1 && childNodes.item(1) instanceof Element) {
                Element column = (Element) childNodes.item(1);

                column.setAttribute(NAME, NabuccoTransformationUtility.toTableName(name)
                        + TABLE_SEPARATOR + REF_ID_COLUMN);

                if (!multiplicity.isOptional()) {
                    column.setAttribute(NULLABLE, Boolean.FALSE.toString());
                }
            }

            elementList.add(basic);
        }
    }

    /**
     * Creates a join-column mapping for 1:1, 1:N, N:1 relationships.
     * 
     * @param mapping
     *            the xml mapping tag
     * @param name
     *            the foreign key name
     */
    private void createJoinColumn(Element mapping, String name) {
        Element joinColumn = (Element) mapping.getElementsByTagName(JOIN_COLUMN).item(0);
        joinColumn.setAttribute(NAME, name);
    }

    /**
     * Creates a join-table mapping for M:N relationships.
     * 
     * @param mapping
     *            the xml mapping tag
     * @param firstType
     *            the first type name
     * @param secondType
     *            the second type name
     * @param componentPrefix
     *            the table name prefix
     */
    private void createJoinTable(Element mapping, String firstType, String secondType, String componentPrefix) {

        Element joinTable = (Element) mapping.getElementsByTagName(JOIN_TABLE).item(0);

        String first = NabuccoTransformationUtility.toTableName(firstType);
        String second = NabuccoTransformationUtility.toTableName(secondType);

        if (first.equalsIgnoreCase(second)) {
            first = first + TABLE_SEPARATOR + 1;
            second = second + TABLE_SEPARATOR + 2;
        }

        String tableName = componentPrefix + TABLE_SEPARATOR + first + TABLE_SEPARATOR + second;
        String joinColumnName = first + TABLE_SEPARATOR + ID;
        String inverseColumnName = second + TABLE_SEPARATOR + ID;

        joinTable.setAttribute(NAME, tableName);

        Element joinColumn = (Element) joinTable.getElementsByTagName(JOIN_COLUMN).item(0);
        Element inverseJoinColumn = (Element) joinTable.getElementsByTagName(INVERSE_JOIN_COLUMN).item(0);

        joinColumn.setAttribute(NAME, joinColumnName);
        inverseJoinColumn.setAttribute(NAME, inverseColumnName);
    }

    /**
     * Extracts the association strategy of a datatype declaration
     * 
     * @param nabuccoDatatype
     *            the datatype holding the annotations
     * 
     * @return the association strategy type
     */
    private AssociationStrategyType extractAssociationStrategy(DatatypeDeclaration nabuccoDatatype) {
        NabuccoAnnotation annotation = NabuccoAnnotationMapper.getInstance().mapToAnnotation(
                nabuccoDatatype.annotationDeclaration, NabuccoAnnotationType.ASSOCIATION_STRATEGY);

        if (annotation != null) {
            return AssociationStrategyType.getType(annotation.getValue());
        }
        return AssociationStrategyType.COMPOSITION;
    }

    /**
     * Extracts the fetch strategy of a datatype declaration
     * 
     * @param nabuccoDatatype
     *            the datatype holding the annotations
     * 
     * @return the fetch strategy type
     */
    private FetchStrategyType extractFetchStrategy(DatatypeDeclaration nabuccoDatatype) {
        NabuccoAnnotation annotation = NabuccoAnnotationMapper.getInstance().mapToAnnotation(
                nabuccoDatatype.annotationDeclaration, NabuccoAnnotationType.FETCH_STRATEGY);

        if (annotation != null) {
            return FetchStrategyType.getType(annotation.getValue());
        }
        return FetchStrategyType.LAZY;
    }

    /**
     * Extracts the order strategy of a datatype declaration
     * 
     * @param nabuccoDatatype
     *            the datatype holding the annotations
     * 
     * @return the collection oder strategy type
     */
    private OrderStrategyType extractOrderStrategy(DatatypeDeclaration nabuccoDatatype) {
        NabuccoAnnotation annotation = NabuccoAnnotationMapper.getInstance().mapToAnnotation(
                nabuccoDatatype.annotationDeclaration, NabuccoAnnotationType.ORDER_STRATEGY);

        if (annotation != null) {
            return OrderStrategyType.getType(annotation.getValue());
        }
        return OrderStrategyType.ORDERED;
    }

    /**
     * Creates the entity/mapped-superclass relation to a basic type.
     * 
     * @param nabuccoBasetype
     *            the basetype
     * @param elementList
     *            the list to add the mapping
     */
    void createBasetypeRelation(BasetypeDeclaration nabuccoBasetype, List<Node> elementList) {

        String type = nabuccoBasetype.nodeToken1.tokenImage;
        String name = nabuccoBasetype.nodeToken3.tokenImage;

        NabuccoMultiplicityType multiplicity = NabuccoMultiplicityTypeMapper.getInstance().mapToMultiplicity(
                nabuccoBasetype.nodeToken2.tokenImage);

        if (multiplicity == NabuccoMultiplicityType.ONE_TO_MANY || multiplicity == NabuccoMultiplicityType.ZERO_TO_MANY) {
            throw new NabuccoVisitorException("Multiplicity '"
                    + multiplicity + "' is not supported for basetype '" + name + "'.");
        }

        String columnLength = NabuccoToXmlDatatypeVisitorSupport.extractColumnLength(nabuccoBasetype,
                super.getVisitorContext(), this.rootPackage, super.resolveImport(type));

        try {
            XmlTemplate ormTemplate = super.getVisitorContext().getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

            // Transient Field
            if (nabuccoBasetype.nodeOptional.present()) {
                this.createTransientField(name, elementList);
            } else {
                boolean suffix = evaluateSuffix(nabuccoBasetype);
                Element element = NabuccoToXmlDatatypeVisitorSupport.resolveElementType(
                        nabuccoBasetype.annotationDeclaration, ormTemplate, name, columnLength,
                        multiplicity.isOptional(), suffix);

                elementList.add(element);
            }

        } catch (XmlTemplateException te) {
            throw new NabuccoVisitorException("Error creating XML basetype '" + name + "'.", te);
        }
    }

    /**
     * Checks for Types that need an additional JPA suffix.
     * 
     * @param nabuccoBasetype
     *            Basetype usage within a Datatype.
     * @return <code>true</code> if suffixed value field is needed.
     */
    private boolean evaluateSuffix(BasetypeDeclaration nabuccoBasetype) {
        NabuccoToXmlTraversingVisitor checkVisitor = new NabuccoToXmlTraversingVisitor(new NabuccoVisitorContext(
                super.getVisitorContext()));
        nabuccoBasetype.accept(checkVisitor, this);
        return checkVisitor.isJpaSuffix();
    }

    /**
     * Creates the entity/mapped-superclass relation to an enumeration.
     * 
     * @param nabuccoEnum
     *            the enumeration
     * @param elementList
     *            the list to add the mapping
     */
    void createEnumRelation(EnumerationDeclaration nabuccoEnum, List<Node> elementList) {

        String name = nabuccoEnum.nodeToken2.tokenImage;

        NabuccoMultiplicityType multiplicity = NabuccoMultiplicityTypeMapper.getInstance().mapToMultiplicity(
                nabuccoEnum.nodeToken1.tokenImage);

        if (multiplicity == NabuccoMultiplicityType.ONE_TO_MANY || multiplicity == NabuccoMultiplicityType.ZERO_TO_MANY) {
            throw new NabuccoVisitorException("Multiplicity '"
                    + multiplicity + "' is not supported for enumeration '" + name + "'.");
        }

        try {
            // Transient Field
            if (nabuccoEnum.nodeOptional.present()) {
                this.createTransientField(name, elementList);
            } else {

                XmlTemplate ormTemplate = this.getVisitorContext()
                        .getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

                List<Node> basicNodes = ormTemplate.copyNodesByXPath(XPATH_BASIC);

                if (basicNodes.size() == 1 && basicNodes.get(0) instanceof Element) {
                    Element basic = (Element) basicNodes.get(0);
                    basic.setAttribute(NAME, name);


                    // <column>
                    NodeList childNodes = basic.getChildNodes();
                    if (childNodes.getLength() > 1 && childNodes.item(1) instanceof Element) {
                        Element column = (Element) childNodes.item(1);
                        column.setAttribute(NAME, NabuccoTransformationUtility.toTableName(name));
                        column.setAttribute(NULLABLE, String.valueOf(multiplicity.isOptional()));
                    }

                    // <enumeration>
                    List<Node> enumNodes = ormTemplate.copyNodesByXPath(XPATH_ENUM);
                    if (enumNodes.size() == 1 && enumNodes.get(0) instanceof Element) {
                        Element enumeration = (Element) enumNodes.get(0);
                        basic.appendChild(enumeration);
                    }
                    elementList.add(basic);
                }
            }

        } catch (XmlTemplateException te) {
            throw new NabuccoVisitorException("Error creating XML enum '" + name + "'.", te);
        }
    }

    /**
     * Getter for the componentName.
     * 
     * @return Returns the componentName.
     */
    public String getComponentName() {
        return this.componentName;
    }

    /**
     * Setter for the componentName.
     * 
     * @param componentName
     *            the componentName to set.
     */
    void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Creates a transient field for the given name.
     * 
     * @param name
     *            name of the field
     * @param elementList
     *            the list of elements
     * 
     * @throws XmlTemplateException
     *             if the template is not valid
     */
    void createTransientField(String name, List<Node> elementList) throws XmlTemplateException {

        XmlTemplate ormTemplate = this.getVisitorContext().getTemplate(NabuccoXmlTemplateConstants.ORM_TEMPLATE);

        List<Node> nodes = ormTemplate.copyNodesByXPath(XPATH_TRANSIENT);

        if (nodes.size() == 1 && nodes.get(0) instanceof Element) {
            Element reference = (Element) nodes.get(0);
            reference.setAttribute(NAME, name);
            elementList.add(reference);
        }
    }

    /**
     * Creates a copy of the current visitor context. Templates and root dir is copied, nothing
     * else.
     * 
     * @return the context copy
     */
    private NabuccoToXmlVisitorContext copyContext() {
        NabuccoToXmlVisitorContext context = new NabuccoToXmlVisitorContext();
        super.getVisitorContext().copyTemplates(context);
        context.setRootDir(super.getVisitorContext().getRootDir());
        context.setOutDir(super.getVisitorContext().getOutDir());
        return context;
    }

    /**
     * Resolves a dependency for a full quallified name.
     * 
     * @param name
     *            the full qualified name name
     * 
     * @return the resolved NABUCCO model
     * 
     * @throws NabuccoTransformationException
     */
    private MdaModel<NabuccoModel> resolveDependency(String name) throws NabuccoTransformationException {

        MdaModel<NabuccoModel> model = NabuccoDependencyResolver.getInstance().resolveDependency(
                super.getVisitorContext(), this.rootPackage, name);

        if (model.getModel() == null) {
            throw new IllegalStateException("Cannot resolve dependency " + name + ". NabuccoModel is corrupt.");
        }

        return model;
    }

}
