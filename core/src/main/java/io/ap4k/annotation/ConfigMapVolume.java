package io.ap4k.annotation;

public @interface ConfigMapVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * The name of the config map to mount.
     * @return  The name.
     */
    String configMapName();

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
