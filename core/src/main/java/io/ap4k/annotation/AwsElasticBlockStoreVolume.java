package io.ap4k.annotation;

public @interface AwsElasticBlockStoreVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * The name of the disk to mount.
     * @return  The name.
     */
    String volumeId();

    String partition();

    String fsType() default "ext4";

    /**
     * Wether the volumeName is read only or not.
     * @return  True if read only, False otherwise.
     */
    boolean readOnly() default false;

}
