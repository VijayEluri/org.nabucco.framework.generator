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
package org.nabucco.framework.generator.compiler.transformation.xml.constants;

/**
 * EjbJarConstants
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
public interface EjbJarConstants extends XmlConstants {

    final String EJB_JAR = "ejb-jar";

    final String EJB_NAME = "ejb-name";

    final String EJB_CLASS = "ejb-class";

    final String EJB_REMOTE = "business-remote";

    final String EJB_LOCAL = "business-local";

    final String EJB_REF_NAME = "ejb-ref-name";

    final String EJB_REF_REMOTE = "remote";

    final String EJB_REF_LOCAL = "local";

    final String REMOTE = "Remote";

    final String LOCAL = "Local";

    final String SESSION = "session";

    final String ENTERPRISE_BEANS = "enterprise-beans";

    final String ASSEMBlY_DESCRIPTOR = "assembly-descriptor";

    final String CONTAINER_TRANSACTION = "container-transaction";

    final String APPLICATION_EXCEPTION = "application-exception";

    final String PERSISTENCE_REF_NAME = "persistence-context-ref-name";

    final String RESOURCE_REF_NAME = "res-ref-name";

    final String RESOURCE_TYPE = "res-type";

    final String RESOURCE_MAPPED_NAME = "mapped-name";

    final String PERSISTENCE_UNIT_NAME = "persistence-unit-name";

    final String INJECTION_TARGET = "injection-target";

    final String INJECTION_TARGET_NAME = "injection-target-name";

    final String INJECTION_TARGET_CLASS = "injection-target-class";

    final String INTERCEPTOR_BINDING = "interceptor-binding";

    final String JNDI_SESSION_CONTEXT = "java:comp/EJBContext";
    
    final String IMPORT_SESSION_CONTEXT = "javax.ejb.SessionContext";
    
    final String XPATH_FRAGMENT = "/fragment";
    
    final String XPATH_FRAGMENT_SESSION = "/fragment/session";
    
    final String XPATH_FRAGMENT_EJB_NAME = "/fragment/container-transaction/method/ejb-name";
    
    final String XPATH_FRAGMENT_TRANSACTION_ATTRIBUTE = "/fragment/container-transaction/trans-attribute";

    final String XPATH_FRAGMENT_EXCEPTION_CLASS = "/fragment/application-exception/exception-class";

    final String XPATH_FRAGMENT_EXCEPTION_ROLLBACK = "/fragment/application-exception/rollback";

    final String XPATH_POST_CONSTRUCT = "/ejb-jar/enterprise-beans/session/post-construct";

    final String XPATH_PRE_DESTROY = "/ejb-jar/enterprise-beans/session/pre-destroy";

    final String XPATH_PERSISTENCE_REF = "/ejb-jar/enterprise-beans/session/persistence-context-ref";

    final String XPATH_RESOURCE_REF = "/ejb-jar/enterprise-beans/session/resource-ref";

    final String XPATH_EJB_REF = "/ejb-jar/enterprise-beans/session/ejb-ref";

    final String XPATH_EJB_LOCAL_REF = "/ejb-jar/enterprise-beans/session/ejb-local-ref";
    
    final String XPATH_CONTAINER_TRANSACTION = "/ejb-jar/assembly-descriptor/container-transaction";

}
