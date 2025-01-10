package org.opencds.cqf.tooling.terminology.compatators;

import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Period;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/*
An abastract class that holds comparisons of common parts of ValueSets and CodeSystems with utilities to help with that.
 */
abstract class Comparator {
    protected static final String newLine = System.getProperty("line.separator");

    protected void compareContacts(Set<Map<String, String>> fieldsWithErrors, List<ContactDetail> termContacts, List<ContactDetail> truthContacts) {
        Map<String, String> contactFailure = new HashMap<>();
        Map<String, Map<String, ContactPoint>> termContactMap = createContactMap(termContacts);
        Map<String, Map<String, ContactPoint>> truthContactMap = createContactMap(truthContacts);
        if (termContactMap != null && truthContactMap != null) {
            if (termContactMap.size() == truthContactMap.size()) {
                termContactMap.forEach((termContactName, termContactPoints) -> {
                    Map<String, ContactPoint> truthContactPoints = truthContactMap.get(termContactName);
                    if (termContactPoints != null && truthContactPoints != null) {
                        Map<String, String> contactPointFailure = new HashMap<>();
                        if (!compareContactPoints(termContactPoints, truthContactPoints, contactPointFailure)) {
                            fieldsWithErrors.add(contactPointFailure);
                        }
                    } else {
                        if (termContactPoints != null) {
                            contactFailure.put("Contact", "This server's contact point has values and the matching IG contact point does not." + newLine);
                            fieldsWithErrors.add(contactFailure);
                        } else {
                            contactFailure.put("Contact", "This server's contact point does not have values and the matching IG contact point does." + newLine);
                            fieldsWithErrors.add(contactFailure);
                        }
                    }
                });
            } else {
                contactFailure.put("Contact", "This server's number of contacts does not match the IG's." + newLine);
                fieldsWithErrors.add(contactFailure);
            }
        } else {
            if (termContacts != null) {
                contactFailure.put("Contact", "This server has contacts and the IG oes not." + newLine);
                fieldsWithErrors.add(contactFailure);
            } else {
                contactFailure.put("Contact", "This server does not have contacts and the IG does." + newLine);
                fieldsWithErrors.add(contactFailure);
            }
        }
    }

    private Map<String, Map<String, ContactPoint>> createContactMap(List<ContactDetail> contacts) {
        // 0..* contacts
        //      0..1 name
        //      0..* telecom
        Map<String, Map<String, ContactPoint>> contactMap = new HashMap<>();
        contacts.forEach(contact -> {
            String contactName = contact.getName();
            if (contactName != null && !contactName.isEmpty()) {  // if no name, skip the contact
                Map<String, ContactPoint> contactPoints = new HashMap<>();
                contact.getTelecom().forEach(telcom -> {
                    contactPoints.put(telcom.getValue(), telcom);
                });
                contactMap.put(contactName, contactPoints);
            }
        });
        return contactMap;
    }

    private boolean compareContactPoints(Map<String, ContactPoint> termContactPoints, Map<String, ContactPoint> truthContactPoints, Map<String, String> contactPointFailure) {
        AtomicBoolean contactPointsMatch = new AtomicBoolean(true);
        termContactPoints.forEach((termCPValue, termCP) -> {
            ContactPoint truthCP = truthContactPoints.get(termCPValue);
            if (truthCP != null) {
                if (termCP.getSystem() != null && !termCP.getSystem().equals(truthCP.getSystem())) {
                    contactPointsMatch.set(false);
                    contactPointFailure.put("ContactPoint", "The server's contact point system with the value of \"" + termCP.getSystem() + "\" does not match the IG's contact point system of \"" + truthCP.getSystem() + "\"." + newLine);
                } else if (truthCP.getSystem() != null && termCP.getSystem() == null) {
                    contactPointsMatch.set(false);
                    contactPointFailure.put("ContactPoint", "The IG's contact point system with the value of \"" + truthCP.getSystem() + "\" does not match the server's null value." + newLine);
                }
                if (termCP.getUse() != null && !termCP.getUse().equals(truthCP.getUse())) {
                    contactPointsMatch.set(false);
                    contactPointFailure.put("ContactPoint", "The server's contact point use with the value of \"" + termCP.getUse() + "\" does not match the IG's contact point system of \"" + truthCP.getUse() + "\"." + newLine);
                } else if (truthCP.getUse() != null && termCP.getUse() == null) {
                    contactPointsMatch.set(false);
                    contactPointFailure.put("ContactPoint", "The IG's contact point system with the value of \"" + truthCP.getUse() + "\" does not match the server's null value." + newLine);
                }
                if (termCP.getRank() != truthCP.getRank()) {
                    contactPointsMatch.set(false);
                    contactPointFailure.put("ContactPoint", "The server's contact point rank with the value of \"" + termCP.getRank() + "\" does not match the IG's contact point system of \"" + truthCP.getRank() + "\"." + newLine);
                }
                comparePeriods(termCP.getPeriod(), truthCP.getPeriod(), contactPointFailure);
            } else {
                contactPointsMatch.set(false);
                contactPointFailure.put("ContactPoint", "The server's contact point with the value of \"" + termCPValue + "\" does not exist in the IG's contact points." + newLine);
            }
        });
        return contactPointsMatch.get();
    }

    private boolean comparePeriods(Period termPeriod, Period truthPeriod, Map<String, String> contactPointFailure) {
        boolean periodsMatch = true;
        if (termPeriod.getStart() != null && truthPeriod.getStart() != null) {
            if (!termPeriod.getStart().equals(truthPeriod.getStart())) {
                contactPointFailure.put("ContactPointPeriod", "The server's contact point period with the start value of \"" + termPeriod.getStart() + "\" does not match in the IG's contact point period start of \"" + truthPeriod.getStart() + "\"." + newLine);
                periodsMatch = false;
            }
        } else if (termPeriod.getStart() != null) {
            contactPointFailure.put("ContactPointPeriod", "The server's contact point period start value of \"" + termPeriod.getStart() + "\" does not match the IG's contact point period start of \"null\"." + newLine);
            periodsMatch = false;
        } else {
            contactPointFailure.put("ContactPointPeriod", "The IG's contact point period start value of \"" + truthPeriod.getStart() + "\" does not match the server's contact point period start of \"null\"." + newLine);
            periodsMatch = false;
        }
        if (termPeriod.getEnd() != null && truthPeriod.getEnd() != null) {
            if (!termPeriod.getEnd().equals(truthPeriod.getEnd())) {
                contactPointFailure.put("ContactPointPeriod", "The server's contact point period with the end value of \"" + termPeriod.getEnd() + "\" does not match in the IG's contact point period end of \"" + truthPeriod.getEnd() + "\"." + newLine);
                periodsMatch = false;
            }
        } else if (termPeriod.getEnd() != null) {
            contactPointFailure.put("ContactPointPeriod", "The server's contact point period end value of \"" + termPeriod.getEnd() + "\" does not match the IG's contact point period end of \"null\"." + newLine);
            periodsMatch = false;
        } else {
            contactPointFailure.put("ContactPointPeriod", "The IG's contact point period end value of \"" + truthPeriod.getEnd() + "\" does not match the server's contact point period end of \"null\"." + newLine);
            periodsMatch = false;
        }
        if (termPeriod.getId() != null && truthPeriod.getId() != null) {
            if (!termPeriod.getId().equals(truthPeriod.getId())) {
                contactPointFailure.put("ContactPointPeriod", "The server's contact point period with the id value of \"" + termPeriod.getId() + "\" does not match in the IG's contact point period id of \"" + truthPeriod.getId() + "\"." + newLine);
                periodsMatch = false;
            }
        } else if (termPeriod.getId() != null) {
            contactPointFailure.put("ContactPointPeriod", "The server's contact point period id value of \"" + termPeriod.getId() + "\" does not match the IG's contact point period id of \"null\"." + newLine);
            periodsMatch = false;
        } else {
            contactPointFailure.put("ContactPointPeriod", "The IG's contact point period id value of \"" + truthPeriod.getId() + "\" does not match the server's contact point period id of \"null\"." + newLine);
            periodsMatch = false;
        }
        return periodsMatch;
    }

}
