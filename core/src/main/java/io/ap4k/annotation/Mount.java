package io.ap4k.annotation;

public @interface Mount {

    /**
     * The name of the volumeName to mount.
     * @return  The name.
     */
    String name();

    /**
     * The path to mount.
     * @return  The path.
     */
    String path();

    /**
     * Path within the volumeName from which the container's volumeName should be mounted.
     * @return  The subPath.
     */
    String subPath() default "";

    /**
     * ReadOnly
     * @return  True if mount is readonly, False otherwise.
     */
    boolean readOnly() default false;
}
