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
package org.nabucco.framework.generator.parser.file;

import java.io.File;
import java.io.FileFilter;



/**
 * NabuccoFileFilter
 * 
 * @author Nicolas Moser, PRODYNA AG
 */
public class NabuccoFileFilter implements FileFilter, NabuccoFileConstants {

    /**
     * Singleton instance.
     */
    private static NabuccoFileFilter instance = new NabuccoFileFilter();

    /**
     * Private constructor.
     */
    private NabuccoFileFilter() {
    }

    /**
     * Singleton access.
     * 
     * @return the NabuccoFileFilter instance.
     */
    public static NabuccoFileFilter getInstance() {
        return instance;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String name = file.getName();
        return name.endsWith(NABUCCO_SUFFIX) || name.toLowerCase().endsWith(NABUCCO_SUFFIX);
    }

}