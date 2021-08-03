package org.opencds.cqf.tooling.terminology;

import java.io.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseMockup extends Operation {

    FhirContext fContext;
    String outputDir;
    Workbook workbook;
    Sheet sheet;
    
    public TestCaseMockup() {
        this.fContext = FhirContext.forR4();
        // TODO: Operation parameters.
    }

    @Override
    public void execute(String[] args) {
        // =======================================================================
        for (String arg : args) {
            if (arg.equals("-TestCaseMockup")) continue;

            String[] flagVal = arg.split("=");
            if (flagVal.length < 2) {
                throw new IllegalArgumentException("Invalid flag \"" + arg + "\"");
            }

            String flag  = flagVal[0];
            String value = flagVal[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "output":
                    this.outputDir = value;
                    break;
                case "input":
                    this.workbook = SpreadsheetHelper.getWorkbook(value);
                    break;
            }
        }
        // TODO: When finished add input parameter for target sheet.
        this.sheet = this.workbook.getSheet("Respostas ao formulário 1");
        // =======================================================================
        Iterator<Row> rIterator = sheet.rowIterator();

        while (rIterator.hasNext()) {
            // Get current row
            Row currentRow = rIterator.next();

            // Check if the current row is the header row,
            // if so, don't parse each cell for values.
            // Also not going over 57 for testing purposes (entries > 57 are pretty empty.)
            if (currentRow.getRowNum() == 0 || currentRow.getRowNum() > 57) {
                continue;
            }

            if (isRowEmpty(currentRow))
                continue;

            checkCellString(currentRow);
        }
    }

    private void checkCellString(Row row) {
        // Make directory for the current row's output.
        this.makeDirIfAbsent(this.outputDir + "\\" + row.getRowNum());
        // =======================================================================
        // Setting up objects that will be added to throughout the switch statement.
        // =======================================================================
        Patient patient = new Patient();
        Encounter reasonForComing = new Encounter();
        Observation healthConcerns = new Observation().setValue(new CodeableConcept());
        Observation dangerSigns = new Observation().setValue(new CodeableConcept());
        Observation gestationalAge = new Observation();
        Observation pastPregComps = new Observation();
        Observation numPrevPreg = new Observation();
        Observation profile = new Observation();

        List<CodeableConcept> rfcList = new ArrayList<>();
        Iterator<Cell> cIterator = row.cellIterator();
        Row headerRow = this.sheet.getRow(0);

        while (cIterator.hasNext()) {
            Cell currentCell = cIterator.next();

            if (currentCell == null || currentCell.getCellType() == CellType.BLANK) {
                continue;
            }

            int cellPosition = currentCell.getColumnIndex();
            Cell respectiveHeaderCell = headerRow.getCell(cellPosition);

            String headerCellStr;
            headerCellStr = respectiveHeaderCell.getStringCellValue();

            String cellStr;
            if (currentCell.getCellType() == CellType.NUMERIC) {
                cellStr = Double.toString(currentCell.getNumericCellValue());
            } else {
                cellStr = currentCell.getStringCellValue();
            }
            if (cellStr.isEmpty())
                continue;
            switch (headerCellStr) {
                case "First Name":
                    patient.addName().addGiven(cellStr);
                    break;
                case "Date of birth {dob_entered}":
                    patient.setBirthDate(currentCell.getDateCellValue());
                    break;
                case "Age {age_entered}":
                    // "Observable Entity" - Briefly checked for specification on this and came up nil.
                    break;
                case "Reason for coming to facility? {contact_reason}":
                    // TODO: This isn't entirely correct. Needs "code" property set for "Reason for coming to facility".
                    // 'display' should be set to the value.
                    for (String s : cellStr.split(","))
                    {
                        rfcList.add(new CodeableConcept(new Coding()
                                .setVersion("4.0.1")
                                .setDisplay(s.replace(",", ""))
                                .setSystem("http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-encounter")));
                    }
                    break;
                case "If specific complaint, please say why: {specific_complaint}":
                    outputVs(
                            cellStr.split(","),
                            "Specific health concerns",
                            "specific-health-concerns",
                            "If the woman came to the facility with a specific health concern, select the health concern(s) from the list",
                           "http://fhir.org/guides/who/anc-cds/ValueSet/specific-health-concerns",
                            row.getRowNum()
                    );
                    break;
                case "Any danger sign? {danger_signs}":
                    outputVs(
                            cellStr.split(","),
                            "Danger signs",
                            "danger-signs",
                            "Before each contact, the health worker should check whether the woman has any of the danger signs listed here – if yes, she should refer to the hospital urgently; if no, she should continue to the normal contact",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/danger-signs",
                            row.getRowNum()
                    );
                    break;
                case "Highest level of school {educ_level}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "Highest level of education achieved",
                            "highest-level-of-education-achieved",
                            "The highest level of schooling the woman has reached",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/highest-level-of-education-achieved",
                            row.getRowNum()
                    );
                    break;
                case "Marital status {marital_status}":
                    patient.setMaritalStatus(
                            new CodeableConcept().setText(cellStr).addCoding(
                                    new Coding().setCode(
                                            CodeSystemLookupDictionary.getUrlFromName("MaritalStatus"))));
                    break;
                case "Occupation {occupation}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "Occupation",
                            "occupation",
                            "The woman's occupation",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/occupation",
                            row.getRowNum()
                    );
                    break;
                case "Last Menstrual Period {lmp}":
//                    gestationalAge.getValueDateTimeType().setValue(new Date(cellStr));
                    break;
                case "GA from LMP {lmp_gest_age}":
                    gestationalAge.getValueQuantity().setValue(Double.parseDouble(cellStr));
                    break;
//                case "Ultrasound done? {ultrasound_done}":
//                    BooleanType btus = new BooleanType();
//                    if (cellStr.toLowerCase().contains("yes")) {
//                        btus.setValue(true);
////                        gestationalAge.getValueBooleanType().setValue((true);
//                    } else {
//                        btus.setValue(false);
////                        gestationalAge.getValueBooleanType().setValue(false);
//                    }
//                    gestationalAge.getValueBooleanType().setValue(btus);
//                    break;
                case "Ultrasound date {ultrasound_date}":
                    // Resource map - Procedure.performed[x], ask about this.
                    break;
//                case "GA from ultrasound - weeks {ultrasound_gest_age_wks}":
//                    break;
//                case "GA from SFH - weeks {sfh_gest_age}":
//                    break;
//                case "Select preferred gestational age {select_gest_age_edd}":
//                    break;
                case "Obstetric History [No. of pregnancies {gravida}]":
//                    if (cellStr.contains("+5")) {
//                        cellStr = "5";
//                    }
//                    numPrevPreg.getValueIntegerType().setValue(Integer.parseInt(cellStr));
                    break;
                case "Obstetric History [No. of pregnancies lost/ended\t{miscarriages_abortions}]":
                    break;
                case "Obstetric History [No. of live births\t{live_births}]":
                    break;
                case "Obstetric History [No. of stillbirths\t{stillbirths}]":
                    break;
                case "Obstetric History [No. of C-sections\t{c_sections}]":
//                    caesarian.getValueIntegerType().setValue(Integer.parseInt(cellStr));
                    break;
                case "Any past pregnancy problems?\t{prev_preg_comps}":
//                    ccSplitAndAdd(pastPregnancyComps, cellStr);
                    break;
                case "If alcohol or illicit substance use, indicate what type of drug {substances_used}":
                    cellStr = cellStr.replace(";", "");
                    for (String s : cellStr.split(",")) {
                        pastPregComps.getValueCodeableConcept()
                                .getCoding()
                                .add(new Coding()
                                        .setSystem("http://fhir.org/guides/who/anc-cds/ValueSet/past-pregnancy-complications")
                                        .setCode("Past pregnancy complications")
                                        .setDisplay(cellStr));
                    }
//                    profile.addContained(cdSubstances);
                    break;
                case "Any allergies? {allergies}":
                    cellStr = cellStr.replace(";", "");
                    break;
                case "Any surgeries? {surgeries}":
                    cellStr = cellStr.replace(";", "");
                    outputVs(
                            cellStr.split(","),
                            "Allergies",
                            "allergies",
                            "Does the woman have any allergies?",
                            "past-surgeries-choices",
                            row.getRowNum()
                    );
                    break;
                case "Any chronic or past health conditions? {health_conditions} ":
                    cellStr = cellStr.replace(";", "");
                    outputVs(
                            cellStr.split(","),
                            "existing-chronic-health-conditions",
                            "existing-chronic-health-conditions",
                            "Does the woman have any current chronic health conditions or problems?",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/existing-chronic-health-conditions",
                            row.getRowNum()
                    );
                    break;
                case "TT immunisation status {tt_immun_status}":
                    cellStr = cellStr.replace(";", "");
                    outputVs(
                            cellStr.split(","),
                            "tetanus-toxoid-containing-vaccine-ttcv-immunization-history",
                            "tetanus-toxoid-containing-vaccine-ttcv-immunization-history",
                            "The woman's history of receiving tetanus toxoid-containing vaccine (TTCV)",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/tetanus-toxoid-containing-vaccine-ttcv-immunization-history",
                            row.getRowNum()
                    );
                    break;
                case "Flu immunisation status {flu_immun_status}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "flu-immunization-provided",
                            "flu-immunization-provided",
                            "Whether or not this year's seasonal flu vaccine has been provided",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/flu-immunization-provided",
                            row.getRowNum()
                    );
                    break;
                case "Any current medications?\t{medications}":
                    outputVs(
                            cellStr.split(","),
                            "current-medications-choices",
                            "current-medications-choices",
                            "Select all of the medications the woman is currently taking",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/current-medications-choices",
                            row.getRowNum()
                    );
                    break;
                case "Daily caffeine intake {caffeine_intake}":
                    outputVs(
                            cellStr.split(","),
                            "daily-caffeine-intake",
                            "daily-caffeine-intake",
                            "Assesses whether the woman consumes more than 300 mg of caffeine per day",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/daily-caffeine-intake",
                            row.getRowNum()
                    );
                    break;
                case "Uses tobacco products?\t{tobacco_user}":
//                    outputVs(
//                            cellStr.split(","),
//                            "Whether the woman uses tobacco products",
//                            "tobacco-use",
//                            row.getRowNum()
//                    );
                    break;
                case "Anyone in the household smokes tobacco products? {shs_exposure}":
                    if (cellStr.toLowerCase().contains("recently quit")) {
                        outputVs(
                                "Recently quit",
                                "persistent-behaviours-recently-quit-tobacco-products-choices",
                                "persistent-behaviours-recently-quit-tobacco-products-choices",
                                "Whether the woman has recently quit using any tobacco products",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/persistent-behaviours-recently-quit-tobacco-products-choices",
                                row.getRowNum()
                        );
                    }
                    break;
                case "Uses condoms during sex? {condom_use}":
                    outputVs(
                            cellStr.split(","),
                            "contraceptive-use-of-female-condoms",
                            "contraceptive-use-of-female-condoms",
                            "Whether or not the woman (and her partner) use female condoms during sex",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/contraceptive-use-of-female-condoms",
                            row.getRowNum()
                    );
                    break;
                case "Clinical enquiry for alcohol and other substance use done? {alcohol_substance_enquiry}":
                    outputVs(
                            cellStr.split(","),
                            "contraceptive-use-of-male-condoms",
                            "contraceptive-use-of-male-condoms",
                            "Whether or not the woman (and her partner) use male condoms during sex",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/contraceptive-use-of-male-condoms",
                            row.getRowNum()
                    );
                    break;
                case "Uses alcohol and/or other substances?	{alcohol_substance_use}":
                    // TODO; Need to account for each possible drug manually?
//                    outputVs(
//                            cellStr.replace(";", ""),
//                            "current-alcohol-and-or-other-substance-use-alcohol-choices",
//                            "current-alcohol-and-or-other-substance-use-alcohol-choices",
//                            "Whether or not the woman currently consumes any alcohol or substances",
//                            "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-alcohol-choices",
//                            row.getRowNum()
//                    );
                    // Maybe something like this?
                    String substanceTmp = cellStr.toLowerCase();
                    if (substanceTmp.contains("marijuana")) {
                        outputVs(
                                "Marijuana",
                                "current-alcohol-and-or-other-substance-use-marijuana",
                                "current-alcohol-and-or-other-substance-use-marijuana",
                                "Woman currently uses marijuana",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-marijuana",
                                row.getRowNum()
                        );
                    }
                    if (substanceTmp.contains("cocaine")) {
                        outputVs(
                                "Cocaine",
                                "current-alcohol-and-or-other-substance-use-cocaine",
                                "current-alcohol-and-or-other-substance-use-cocaine",
                                "Woman currently uses cocaine",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-cocaine",
                                row.getRowNum()
                        );
                    }
                    if (substanceTmp.contains("injectable")) {
                        outputVs(
                                "Injectable drugs",
                                "current-alcohol-and-or-other-substance-use-injectable-drugs",
                                "current-alcohol-and-or-other-substance-use-injectable-drugs",
                                "Woman currently uses injectable drugs",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-injectable-drugs",
                                row.getRowNum()
                        );
                    }
                    if (substanceTmp.contains("alcohol")) {
                        outputVs(
                                "Alcohol",
                                "current-alcohol-and-or-other-substance-use-alcohol",
                                "current-alcohol-and-or-other-substance-use-alcohol",
                                "Woman currently uses alcohol",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-alcohol",
                                row.getRowNum()
                        );
                    }
                    if (substanceTmp.contains("other")) {
                        outputVs(
                                "Other Substances",
                                "current-alcohol-and-or-other-substance-use-other-substances",
                                "current-alcohol-and-or-other-substance-use-other-substances",
                                "Woman currently uses other substances not listed above",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/current-alcohol-and-or-other-substance-use-other-substances",
                                row.getRowNum()
                        );
                    }
                    break;
                case "Partner HIV status {partner_hiv_status}":
                    // This could be the proper way of handling multiple choice inputs - I believe it is,
                    // but make sure to check with Bryn.
                    // To add: May need to add a function similar to outputVs overloads for expansions.
                    String partnerHIVTmp = cellStr.toLowerCase();
                    if (partnerHIVTmp.contains("don't know")) {
                        outputVs(
                                "Don't know",
                                "Partner HIV status (reported)",
                                "Partner HIV status (reported)",
                                "Don't know HIV status – woman does not know partner's HIV status",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/partner-hiv-status-reported",
                                row.getRowNum()
                        );
                    }
                    if (partnerHIVTmp.contains("negative")) {
                        outputVs(
                                "Negative",
                                "Partner HIV status (reported)",
                                "Partner HIV status (reported)",
                                "Woman's partner is HIV negative",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/partner-hiv-status-reported",
                                row.getRowNum()
                        );
                    }
                    if (partnerHIVTmp.contains("positive")) {
                        outputVs(
                                "Positive",
                                "Partner HIV status (reported)",
                                "Partner HIV status (reported)",
                                "Woman's partner is HIV positive",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/partner-hiv-status-reported",
                                row.getRowNum()
                        );
                    }
                    break;
                case "Any physiological symptoms?	{phys_symptoms}":
                    for (String physSmp : cellStr.split(",")) {
                        physSmp = physSmp.replace(";", "");
                        String physName = String.format(
                                "persistent-physiological-symptoms-%s",
                                physSmp.toLowerCase()
                        );
                        String physDesc = String.format(
                                "Woman reported %s during a previous contact and is still experiencing %s",
                                physSmp,
                                physSmp
                        );
                        outputVs(
                                physSmp,
                                "persistent-physiological-symptoms",
                                "persistent-physiological-symptoms",
                                physDesc,
                                "http://fhir.org/guides/who/anc-cds/ValueSet/persistent-physiological-symptoms",
                                row.getRowNum()
                        );
                    }
                    break;
                case "If the woman has varicose veins or oedema, check any of the following symptoms {other_sym_vvo}":
                    if (cellStr.toLowerCase().contains("varicose")) {
                        outputVs(
                                "Varicose veins",
                                "persistent-physiological-symptoms-varicose-vein",
                                "persistent-physiological-symptoms-varicose-vein",
                                "Woman reported varicose veins during a previous contact and is still experiencing varicose veins",
                                "http://fhir.org/guides/who/anc-cds/ValueSet/persistent-physiological-symptoms-varicose-veins",
                                row.getRowNum()
                        );
                    }
                    break;
                case "Any other symptoms? 	{other_phys_symptoms}":
                    break;
                case "Has the woman felt the baby move? 	{mat_percept_fetal_move}":
                    break;
                case "Pre-gestational weight (kg)	{pregest_weight}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "Pre-gestational weight",
                            "pre-gestational-weight",
                            "The woman's pre-gestational weight in kilograms",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/pre-gestational-weight",
                            row.getRowNum()
                    );
                    break;
                case "Height (m) {height}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "Height",
                            "height",
                            "The woman's current height in centimetres",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/height",
                            row.getRowNum()
                    );
                    break;
                case "Current weight (kg) {current_weight}":
                    outputVs(
                            cellStr.replace(";", ""),
                            "Current weight",
                            "current-weight",
                            "The woman's current weight in kilograms",
                            "http://fhir.org/guides/who/anc-cds/ValueSet/current-weight",
                            row.getRowNum()
                    );
                    break;
                case "Systolic blood pressure (SBP) {bp_systolic}":
                    break;
                case "Diastolic blood pressure (DBP) {bp_diastolic}":
                    break;
                case "Temperature (ºC) {body_temp}":
                    break;
                case "Pulse rate (bpm) {pulse_rate}":
                    break;
                case "Pallor present? {pallor}":
                    break;
                case "Respiratory exam	{repiratory_exam}":
                    break;
                case "Oximetry (%)	{oximetry}":
                    break;
                case "Cardiac exam {cardiac_exam}":
                    break;
                case "Breast exam	{breast_exam}":
                    break;
                case "Abdominal exam	{abdominal_exam}":
                    break;
                case "Pelvic exam (visual)	{pelvic_exam}":
                    break;
                case "Cervical exam done?	{cervical_exam}":
                    break;
                case "If done, how many cm dilated? 	{dilation_cm}":
                    break;
                case "Oedema present?	{oedema}":
                    break;
                case "If yes, oedema type {oedema_type}":
                    break;
                case "Oedema severity	{oedema_severity}":
                    break;
                case "SFH (cm) {sfh}":
                    break;
                case "Fetal movement felt?	{fetal_movement}":
                    break;
                case "Fetal heart beat present? {fetal_heartbeat}":
                    break;
                case "Fetal heart rate (bpm)	{fetal_heart_rate}":
                    break;
                case "No. of fetuses	{no_of_fetuses}":
                    break;
                case "Fetal presentation {fetal_presentation}":
                    break;
                case "2nd SBP after 10-15 min rest	{bp_systolic_repeat}":
                    break;
                case "2nd DBP after 10-15 min rest	{bp_diastolic_repeat}":
                    break;
                case "Any symptoms of severe pre-eclampsia?	{symp_sev_preeclampsia}":
                    break;
                case "Urine dipstick result - protein	{urine_protein}":
                    break;
                case "Ultrasound test {ultrasound}":
                    break;
                case "Ultrasound Date {ultrasound_date}":
                    break;
                case "GA from ultrasound - days	{ultrasound_gest_age_days}":
                    break;
                case "No. of fetuses {us_no_of_fetuses}":
                    break;
                case "Fetal presentation {us_fetal_presentation}":
                    break;
                case "Amniotic fluid {us_amniotic_fluid}":
                    break;
                case "Placenta location	{us_placenta_location}":
                    break;
                case "Blood type test {blood_type_test_status}":
                    break;
                case "Blood type	{blood_type}":
                    break;
                case "Rh factor {rh_factor}":
                    break;
                case "HIV test	{hiv_test_status}":
                    break;
                case "HIV result {hiv_test_result}":
                    break;
                case "Partner HIV test {hiv_test_partner_status}":
                    break;
                case "Partner HIV result {hiv_test_partner_result}":
                    break;
                case "Hepatitis B test {hepb_test_status}":
                    break;
                case "Hep B result [HBsAg laboratory-based imunoassay (recommended) {hbsag_lab_ima}]":
                    break;
                case "Hep B result [HBsAg rapid diagnostic test (RDT) {hbsag_rdt}]":
                    break;
                case "Hep B result [Dried Blood Spot (DBS) HBsAg test {hbsag_dbs}]":
                    break;
                case "Hepatitis C test {hepc_test_status}":
                    break;
                case "Hep C result [Anti- HCV laboratory-based imunoassay (recommended) {hcv_lab_ima}]":
                    break;
                case "Hep C result [Anti- HCV rapid diagnostic test (RDT) {hcv_rdt}]":
                    break;
                case "Hep C result [Dried Blood Spot (DBS) Anti-HCV test {hcv_dbs}]":
                    break;
                case "Syphilis test	{syph_test_status}":
                    break;
                case "Syphilis Result [Rapid syphilis test (RST) {rapid_syphilis_test}]":
                    break;
                case "Syphilis Result [Rapid plasma reagin (RPR) test {rpr_syphilis_test}]":
                    break;
                case "Syphilis Result [Off-site lab test for syphilis {lab_syphilis_test}]":
                    break;
                case "Urine test {urine_test_status}":
                    break;
                case "Urine result [Midstream urine culture (recommended)  {urine_culture}]":
                    break;
                case "Urine result [Midstream urine Gram-staining {urine_gram_stain}]":
                    break;
                case "Urine result - dipstick [Urine dipstick result - nitrites {urine_nitrites}]":
                    break;
                case "Urine result - dipstick [Urine dipstick result - leukocytes {urine_leukocytes}]":
                    break;
                case "Urine result - dipstick [Urine dipstick result - protein {urine_protein}]":
                    break;
                case "Urine result - dipstick [Urine dipstick result - glucose {urine_glucose}]":
                    break;
                case "Blood glucose test {glucose_test_status}":
                    break;
                case "Blood Glucose Result [Fasting plasma glucose results (mg/dl) {fasting_plasma_gluc}]":
                    break;
                case "Blood Glucose Result [75g OGTT - fasting glucose results (mg/dl) {ogtt_fasting}]":
                    break;
                case "Blood Glucose Result [75g OGTT - 1 hr results (mg/dl) {ogtt_1}]":
                    break;
                case "Blood Glucose Result [75g OGTT - 2 hrs results (mg/dl) {ogtt_2}]":
                    break;
                case "Blood Glucose Result [Random plasma glucose results (mg/dl) {random_plasma}]":
                    break;
                case "Blood haemoglobin test {hb_test_status}":
                    break;
                case "Hb result [Complete blood count test result (g/dl) (recommended) {cbc}]":
                    break;
                case "Hb result [Hb test result - haemoglobinometer (g/dl) {hb_gmeter}]":
                    break;
                case "Hb result [Hb test result - haemoglobin colour scale (g/dl) {hb_colour}]":
                    break;
                case "TB screening	 {tb_screening_status}":
                    break;
                case "Tb result {tb_screening_result}":
                    break;
            }
        }
        output(pastPregComps, "json", "valueset-past-pregnancy-complications", row.getRowNum());
        output(profile, "json", "profile", row.getRowNum());
        output(gestationalAge, "json", "valueset-gestational-age", row.getRowNum());
    }

    private void mkRespectiveDirs(Sheet sheet) {
        for (Row currentRow : sheet) {
            if (!isRowEmpty(currentRow)) {
                this.makeDirIfAbsent(
                        "C:\\Users\\DadeMurphy\\Desktop\\TestCaseMockup\\" + currentRow.getRowNum());
            }
        }
    }

    private File makeDirIfAbsent(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (file.mkdirs())
                System.out.println("CREATE DIRECTORY " + filePath + " - SUCCESS");
            else
                System.out.println("CREATE DIRECTORY " + filePath + " - FAILED");
        }
        return file;
    }

    private void outputVs(
            String[] values,
            String name,
            String id,
            String description,
            String url,
            int rowNum
    ) {
        // Transform input id
        // Get -> "highest-level-education-achieved"
        // Out -> "valueset-highest-level-education-achieved"
        id = "valueset-" + id;

        ValueSet vs = new ValueSet();
        vs.setDescription(description);
        ValueSet.ValueSetComposeComponent vsCompose = new ValueSet.ValueSetComposeComponent();
        ValueSet.ConceptSetComponent vsConcept = new ValueSet.ConceptSetComponent();
        vsConcept.setSystem(url);

        vsConcept.setSystem("http://fhir.org/guides/who/anc-cds/CodeSystem/anc-custom");
        for (String value : values) {
            // Spreadsheet contains semicolons inconsistently. Just in case :^)
            value = value.replace(";", "");
            vsConcept.addConcept().setCode(name).setDisplay(value);
        }

        vsCompose.addInclude(vsConcept);
        vs.setCompose(vsCompose);

        output(vs, "json", id, rowNum);
    }

    private void outputVs(
            String value,
            String name,
            String id,
            String description,
            String url,
            int rowNum
    ) {
        // Transform input id
        // Get -> "highest-level-education-achieved"
        // Out -> "valueset-highest-level-education-achieved"
        id = "valueset-" + id;

        ValueSet vs = new ValueSet();
        vs.setDescription(description);
        ValueSet.ValueSetComposeComponent vsCompose = new ValueSet.ValueSetComposeComponent();
        ValueSet.ConceptSetComponent vsConcept = new ValueSet.ConceptSetComponent();
        vsConcept.setSystem(url);

        vsConcept.setSystem("http://fhir.org/guides/who/anc-cds/CodeSystem/anc-custom");
        vsConcept.addConcept().setCode(name).setDisplay(value);

        vsCompose.addInclude(vsConcept);
        vs.setCompose(vsCompose);

        output(vs, "json", id, rowNum);
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    private CodeableConcept ccSplitAndAdd(CodeableConcept cc, String input) {
        if (input.toLowerCase() == "none") {
            cc.addCoding().setCode("None");
            return cc;
        }

        for (String value : input.split(",")) {
            cc.addCoding().setCode(value.replace(",", ""));
        }
        return cc;
    }

    private void output(IBaseResource resource, String encoding, String fileName, int indice) {
        try (FileOutputStream writer = new FileOutputStream(
                this.outputDir + "\\" + indice + "\\" + fileName + ".json"))
        {
            writer.write(
                    encoding.equals("json")
                            ? fContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                            : fContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs switch statement based on spreadsheet header values.
     * @param row Header row.
     * @param outputPath Path to the file in which the output will be written.
     */
    private void generateSwitchStatement(Row row, String outputPath) {
        // This is either only useful to me in this instance, or actually pretty useful.
        // If it is useful, it should probably be moved to a helper class of some kind.
        Iterator<Cell> cIterator = row.cellIterator();
        String statement = "switch (yourCondition) {\n";
        ArrayList<String> alreadyAdded = new ArrayList<String>();

        while (cIterator.hasNext()) {
            Cell cell = cIterator.next();
            if (cell== null || cell.getCellType() == CellType.BLANK) {
                continue;
            }

            String cellStr = cell.getStringCellValue();
            if (!cellStr.contains("{") && !cellStr.contains("}") || alreadyAdded.contains(cellStr)) {
                continue;
            }

            statement += String.format(
                    "\tcase \"%s\":\n\t\tbreak;\n",
                    cellStr
            );

            alreadyAdded.add(cellStr);
        }

        statement += "}";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputPath));
            out.write(statement);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
