package io.ap4k.component.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "annotationCmds",
  "repo",
  "tag",
  "dockerImage"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Image {

  private String name;
  private boolean annotationCmds;
  private String repo;
  private String tag;
  private String dockerImage;

  public Image(String name, boolean annotationCmds, String repo, String tag, String dockerImage) {
    this.name = name;
    this.annotationCmds = annotationCmds;
    this.repo = repo;
    this.tag = tag;
    this.dockerImage = dockerImage;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAnnotationCmds() {
    return annotationCmds;
  }

  public void setAnnotationCmds(boolean annotationCmds) {
    this.annotationCmds = annotationCmds;
  }

  public String getRepo() {
    return repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getDockerImage() {
    return dockerImage;
  }

  public void setDockerImage(String dockerImage) {
    this.dockerImage = dockerImage;
  }
}
