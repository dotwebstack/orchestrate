package nl.geostandaarden.imx.orchestrate.model;

public final class ModelException extends RuntimeException {

  public ModelException(String message) {
    super(message);
  }

  public ModelException(String message, Throwable cause) {
    super(message, cause);
  }
}
