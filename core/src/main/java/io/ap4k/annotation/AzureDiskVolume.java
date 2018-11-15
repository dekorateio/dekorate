package io.ap4k.annotation;

public @interface AzureDiskVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * The name of the disk to mount.
     * @return  The name.
     */
    String diskName();

    String diskURI();

    String kind() default "Managed";

    String cachingMode() default "ReadWrite";

    String fsType() default "ext4";

    /**
     * Wether the volumeName is read only or not.
     * @return  True if read only, False otherwise.
     */
    boolean readOnly() default false;

}
