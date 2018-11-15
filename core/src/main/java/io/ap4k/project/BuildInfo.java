/**
 * Copyright (C) 2018 Ioannis Canellos 
 *     
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.project;

public interface BuildInfo {

    String DEFAULT_PACKAGING = "jar";
    String OUTPUTFILE_FORMAT = "%s-%s.%s";

    /**
     * Get the project name.
     * @return The project name.
     */
    String getName();

    /**
     * Get the project version.
     * @return The project version.
     */
    String getVersion();


    /**
     * Get the project packaging.
     * @return The project packaging.
     */
    default String getPackaging() {
        return DEFAULT_PACKAGING;
    }
    /**
     * Get the output file name.
     * @return  The output file name.
     */
    default String getOutputFileName() {
        return String.format(OUTPUTFILE_FORMAT, getName(), getVersion(), getPackaging());
    }
}
