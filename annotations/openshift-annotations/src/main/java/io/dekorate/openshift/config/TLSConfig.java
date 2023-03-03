package io.dekorate.openshift.config;

public class TLSConfig {
  private String caCertificate;
  private String certificate;
  private String destinationCACertificate;
  private String insecureEdgeTerminationPolicy;
  private String key;
  private String termination;

  public TLSConfig() {

  }

  public TLSConfig(String caCertificate, String certificate, String destinationCACertificate,
      String insecureEdgeTerminationPolicy, String key, String termination) {
    this.caCertificate = caCertificate;
    this.certificate = certificate;
    this.destinationCACertificate = destinationCACertificate;
    this.insecureEdgeTerminationPolicy = insecureEdgeTerminationPolicy;
    this.key = key;
    this.termination = termination;
  }

  /**
   * @return the cert authority certificate contents.
   */
  public String getCaCertificate() {
    return this.caCertificate;
  }

  public void setCaCertificate(String caCertificate) {
    this.caCertificate = caCertificate;
  }

  /**
   * @return the certificate contents.
   */
  public String getCertificate() {
    return this.certificate;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  /**
   * @return the contents of the ca certificate of the final destination.
   */
  public String getDestinationCACertificate() {
    return this.destinationCACertificate;
  }

  public void setDestinationCACertificate(String destinationCACertificate) {
    this.destinationCACertificate = destinationCACertificate;
  }

  /**
   * @return the desired behavior for insecure connections to a route. Options are: `allow`, `disable`, and `redirect`.
   */
  public String getInsecureEdgeTerminationPolicy() {
    return this.insecureEdgeTerminationPolicy;
  }

  public void setInsecureEdgeTerminationPolicy(String insecureEdgeTerminationPolicy) {
    this.insecureEdgeTerminationPolicy = insecureEdgeTerminationPolicy;
  }

  /**
   * @return the key file contents.
   */
  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the termination type.
   */
  public String getTermination() {
    return this.termination;
  }

  public void setTermination(String termination) {
    this.termination = termination;
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TLSConfig that = (TLSConfig) o;
    if (caCertificate != null ? !caCertificate.equals(that.caCertificate) : that.caCertificate != null)
      return false;
    if (certificate != null ? !certificate.equals(that.certificate) : that.certificate != null)
      return false;
    if (destinationCACertificate != null ? !destinationCACertificate.equals(that.destinationCACertificate)
        : that.destinationCACertificate != null)
      return false;
    if (insecureEdgeTerminationPolicy != null ? !insecureEdgeTerminationPolicy.equals(that.insecureEdgeTerminationPolicy)
        : that.insecureEdgeTerminationPolicy != null)
      return false;
    if (key != null ? !key.equals(that.key) : that.key != null)
      return false;
    if (termination != null ? !termination.equals(that.termination) : that.termination != null)
      return false;
    return true;
  }

  public int hashCode() {
    return java.util.Objects.hash(caCertificate, certificate, destinationCACertificate, insecureEdgeTerminationPolicy, key,
        termination, super.hashCode());
  }

}
