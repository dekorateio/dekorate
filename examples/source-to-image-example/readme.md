# Source To Image Example 

A very simple example that demonstrates the use of `@OpenshiftApplication` in its simplest form.

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/ap4k/openshfit.yml`.

On top of the `@OpenshiftApplication` the presence of `@SourceToImage` annotation will trigger the creation of a `BuildConfig` along with all the required `ImageStreams`.
