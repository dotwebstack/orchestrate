package org.dotwebstack.orchestrate.model.transforms;

public final class TransformException extends RuntimeException {

  public TransformException(String message) {
    super(message);
  }

  public TransformException(String message, Throwable cause) {
    super(message, cause);
  }
}
