package io.ap4k.annotation;

public @interface GitRepoVolume {

    /**
     * The volumeName name.
     * @return  The volumeName name.
     */
    String volumeName();

    /**
     * Git repoistory URL.
     * @return  The url of the repository.
     */
    String repository();

    /**
     * The directory of the repository to mount.
     * @return  The relative path to the directory.
     */
    String directory() default "";


    /**
     * The commit hash to use.
     * @return  The hash, or empty if the head of the repo (default).
     */
    String revision() default "";
}
