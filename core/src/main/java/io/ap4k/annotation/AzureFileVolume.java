package io.ap4k.annotation;

public @interface AzureFileVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    String shareName();

    String secretName();

    /**
     * Wether the volumeName is read only or not.
     * @return  True if read only, False otherwise.
     */
    boolean readOnly() default false;
}
