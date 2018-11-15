package io.ap4k.annotation;

public @interface SecretVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * The name of the secret to mount.
     * @return  The name.
     */
    String secretName();

    /**
     * Default mode.
     * @return  The default mode.
     */
    int defaultMode() default 600;

    /**
     * Optional
     * @return True if optional, False otherwise.
     */
    boolean optional() default false;


}
