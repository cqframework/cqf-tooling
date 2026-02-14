package org.opencds.cqf.tooling.exception;

public class InvalidOperationArgs extends RuntimeException {
   static final long serialVersionUID = 1L;

   public InvalidOperationArgs() {
      super();
   }
   public InvalidOperationArgs(String message) {
      super(message);
   }
}
