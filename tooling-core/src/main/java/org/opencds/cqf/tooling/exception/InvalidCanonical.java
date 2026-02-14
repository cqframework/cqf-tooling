package org.opencds.cqf.tooling.exception;

public class InvalidCanonical extends RuntimeException {
   static final long serialVersionUID = 1L;

   public InvalidCanonical() {
      super();
   }
   public InvalidCanonical(String message) {
      super(message);
   }
}
