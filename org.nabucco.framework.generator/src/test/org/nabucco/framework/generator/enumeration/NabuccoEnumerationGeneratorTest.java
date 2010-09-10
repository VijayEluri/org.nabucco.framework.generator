/*
* Copyright 2010 PRODYNA AG
*
* Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.opensource.org/licenses/eclipse-1.0.php or
* http://www.nabucco-source.org/nabucco-license.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.nabucco.framework.generator.enumeration;

import java.io.File;

import org.junit.Test;
import org.nabucco.framework.generator.AbstractNabuccoGeneratorTest;


/**
 * NabuccoEnumerationGeneratorTest
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
public class NabuccoEnumerationGeneratorTest extends AbstractNabuccoGeneratorTest {

    private static final File ENUM_DIR = new File(
            "../org.nabucco.framework.base/src/nbc/org/nabucco/framework/base/facade/datatype/user/UserType.nbc");

    @Test
    public void testEnumerationGeneration() throws Exception {
        this.generateFile(ENUM_DIR);
    }

}