/**
 * Copyright 2015 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 **/

package io.ap4k.utils;

import java.util.regex.Pattern;


public class Images {

    private static final String SLASH = "/";
    private static final String COLN = ":";

   
    /**
     * Return the registry part of the docker image.
     * @param image  The actual docker image.
     * @return       The registry or null, if not registry was found.
     */
    public static String getRegistry(String image) {
        String[] parts = image.split(SLASH);
        if (parts.length <= 2) {
            return null;
        } else {
            return parts[0];
        }
    }

    /**
     * Return the docker image repository.
     * @param image The docker image. 
     * @return The image repository. 
     */
    public static String getRepository(String image) {
        String[] parts = image.split(SLASH);
        String tagged = image;
        if (parts.length <= 2) {
            tagged = image;
        } else if (parts.length == 3) {
            tagged = parts[1] + SLASH + parts[2];
        }

        if (tagged.contains(COLN)) {
            return tagged.substring(0, tagged.indexOf(COLN));
        }
        return tagged;
    }

    /**
     * Return the tag of the image.
     * @param image The docker image.
     * @return The tag if present or null otherwise.
     */
    public static String getTag(String image) {
        if (image.contains(COLN)) {
            return image.substring(image.indexOf(COLN) + 1);
        }
        return image;
    }
}
