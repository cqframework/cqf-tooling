package org.opencds.cqf.tooling.operation.ig;

import org.hl7.fhir.r5.context.ILoggingService;
import org.slf4j.Logger;

public class IGLoggingService implements ILoggingService {

   private final Logger logger;

   public IGLoggingService(Logger logger) {
      this.logger = logger;
   }

   @Override
   public void logMessage(String s) {
      logger.info(s);
   }

   @Override
   public void logDebugMessage(LogCategory logCategory, String message) {
      String category = logCategory.name();
      logger.debug("Category: {} Message: {}", category, message);
   }

   @Override
   public boolean isDebugLogging() {
      return false;
   }
}
