package org.opencds.cqf.tooling.exception;

public class InvalidOperationInitialization extends RuntimeException {
   static final long serialVersionUID = 1L;

   public InvalidOperationInitialization() {
      super();
   }
   public InvalidOperationInitialization(String message) {
      super(message);
   }
   public InvalidOperationInitialization(String message, Throwable cause) {
      super(message, cause);
   }
}
