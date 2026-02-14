package org.opencds.cqf.tooling.exception;

public class OperationNotFound extends RuntimeException {
   static final long serialVersionUID = 1L;

   public OperationNotFound() {
      super();
   }
   public OperationNotFound(String message) {
      super(message);
   }
}
