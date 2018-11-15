package io.ap4k.annotation;

public @interface PersistentVolumeClaimVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * The persistent volumeName claim name.
     * @return  The name of the pvcVolume.
     */
    String claimName();

    /**
     * Wether the volumeName is read only or not.
     * @return  True if read only, False otherwise.
     */
    boolean readOnly() default false;
}
