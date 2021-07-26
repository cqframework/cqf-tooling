package org.opencds.cqf.tooling.terminology;

import java.io.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseMockup extends Operation {

    FhirContext fContext;
    File outputDir;
//    File inputPath;
    Workbook workbook;
    Sheet sheet;
    
    public TestCaseMockup() {
        this.fContext = FhirContext.forR4();
        // TODO: Operation parameters.
        this.outputDir = this.makeDirIfAbsent("C:\\Users\\DadeMurphy\\Desktop\\TestCaseMockup");
        this.workbook = SpreadsheetHelper.getWorkbook("C:\\Users\\DadeMurphy\\Desktop\\TestCaseMockup\\test.xlsx");
    }

    @Override
    public void execute(String[] args) {
        this.sheet = this.workbook.getSheet("Respostas ao formulário 1");
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

        Patient patient = new Patient();

        Encounter reasonForComing = new Encounter();
        List<CodeableConcept> rfcList = new ArrayList<>();

        Observation healthConcerns = new Observation().setValue(new CodeableConcept());
        Observation dangerSigns = new Observation().setValue(new CodeableConcept());

        // 1 observation for each sheet, or one resource per defined structure..?
        Observation quickCheck = new Observation();
        //  - - - - - - -

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

            // TODO: Remove this.
            if (cellStr == "Cherry" || cellStr == "Lily") {
                continue;
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
                    for (String s : cellStr.split(","))
                    {
                        // TODO: Probably not the correct system link.
                        healthConcerns.getValueCodeableConcept()
                                .addCoding(new Coding()
                                .setVersion("4.0.1")
                                .setDisplay(s.replace(",", ""))
                                .setSystem("http://fhir.org/guides/who/anc-cds/CodeSystem/anc-custom"));
                    }
                    break;
                case "Any danger sign? {danger_signs}":
                    for (String s : cellStr.split(";,"))
                    {
                        s = s.replace(";", "");
                        dangerSigns.getValueCodeableConcept().addCoding(
                                new Coding()
                                        .setVersion("4.0.1")
                                        .setDisplay(s.replace(",", ""))
                                        .setSystem("http://fhir.org/guides/who/anc-cds/CodeSystem/anc-custom"));
                    }
                    break;
                case "Highest level of school {educ_level}":
                    patient.addExtension()
                            .setValue(new StringType().setValue(cellStr))
                            .setUrl(CodeSystemLookupDictionary.getUrlFromName("EducationLevel"));
                    break;
                case "Marital status {marital_status}":
                    patient.setMaritalStatus(
                            new CodeableConcept().setText(cellStr).addCoding(
                                    new Coding().setCode(
                                            CodeSystemLookupDictionary.getUrlFromName("MaritalStatus"))));
                    break;
                case "Occupation {occupation}":
                    for (String s : cellStr.split(";"))
                    {
                        patient.addExtension(new Extension().setValue(new Coding().setDisplay(s)));
                    }
                    break;
                case "Last Menstrual Period {lmp}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    break;
                case "GA from LMP {lmp_gest_age}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    break;
                case "Ultrasound done? {ultrasound_done}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    break;
                case "Ultrasound date {ultrasound_date}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    // TODO: REMOVE ADDED ULTRASOUND_DATE FROM ROW 59
                    break;
                case "GA from ultrasound - weeks {ultrasound_gest_age_wks}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
//                  // TODO: REMOVE ADDED ULTRASOUND_GEST_AGE_WKS & ULTRASOUND_GEST_AGE_DAYS FROM ROW 59
                    break;
                case "GA from SFH - weeks {sfh_gest_age}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    break;
                case "Select preferred gestational age {select_gest_age_edd}":
                    // TODO: Gestational age is not clear in data dictionary. Ask about it.
                    break;
                case "Obstetric History [No. of pregnancies {gravida}]":
                    // TODO: There's no way this is correct, ask Rob or Bryn.
                    if (cellStr.contains("+5")) {
                        cellStr = "5";
                    }
                    quickCheck.getValueIntegerType().setValue(Integer.parseInt(cellStr));
                    break;
                case "Obstetric History [No. of pregnancies lost/ended\t{miscarriages_abortions}]":
                    // TODO: New observation for each field?
                    break;
                case "Obstetric History [No. of live births\t{live_births}]":
                    // TODO: New observation for each field?
                    break;
                case "Obstetric History [No. of stillbirths\t{stillbirths}]":
                    // TODO: New observation for each field?
                    break;
                case "Obstetric History [No. of C-sections\t{c_sections}]":
                    // TODO: New observation for each field?
//                    caesarian.getValueIntegerType().setValue(Integer.parseInt(cellStr));
                    break;
                case "Any past pregnancy problems?\t{prev_preg_comps}":
//                    ccSplitAndAdd(pastPregnancyComps, cellStr);
                    break;
//                case "If alcohol or illicit substance use, indicate what type of drug {substances_used}":
//                    ccSplitAndAdd(substUseComps, cellStr);
//                    break;
//                case "Any allergies? {allergies}":
//                    ccSplitAndAdd(allergies, cellStr);
//                    break;
//                case "Any surgeries? {surgeries}":
//                    ccSplitAndAdd(pastSurgeries, cellStr);
//                    break;
//                case "Any chronic or past health conditions? {health_conditions} ":
//                    ccSplitAndAdd(healthConditions, cellStr);
//                    break;
//                case "TT immunisation status {tt_immun_status}":
//                    ccSplitAndAdd(ttcvStatus, cellStr);
//                    break;
//                case "Flu immunisation status {flu_immun_status}":
//                    ccSplitAndAdd(fluStatus, cellStr);
//                    break;
//                case "Any current medications?\t{medications}":
//                    ccSplitAndAdd(currentMedications, cellStr);
//                    break;
//                case "Daily caffeine intake {caffeine_intake}":
//                    ccSplitAndAdd(caffeineIntake, cellStr);
//                    break;
//                case "Uses tobacco products?\t{tobacco_user}":
//                    tobaccoUse = setObsBool(tobaccoUse, cellStr);
//                    break;
//                case "Anyone in the household smokes tobacco products? {shs_exposure}":
//                    tobaccoExposure = setObsBool(tobaccoExposure, cellStr);
//                    break;
//                case "Uses condoms during sex? {condom_use}":
//                    partnerHIVStatus
//                    break;
                case "Clinical enquiry for alcohol and other substance use done? {alcohol_substance_enquiry}":
                    break;
                case "Uses alcohol and/or other substances?	{alcohol_substance_use}":
                    break;
                case "Partner HIV status {partner_hiv_status}":
                    break;
                case "Any physiological symptoms?	{phys_symptoms}":
                    break;
                case "If the woman has varicose veins or oedema, check any of the following symptoms {other_sym_vvo}":
                    break;
                case "Any other symptoms? 	{other_phys_symptoms}":
                    break;
                case "Has the woman felt the baby move? 	{mat_percept_fetal_move}":
                    break;
                case "Pre-gestational weight (kg)	{pregest_weight}":
                    break;
                case "Height (m) {height}":
                    break;
                case "Current weight (kg) {current_weight}":
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
        reasonForComing.setType(rfcList);
        output(healthConcerns, "json", "healthConcerns", row.getRowNum());
        output(reasonForComing, "json", "reasonForComing", row.getRowNum());
        output(dangerSigns, "json", "dangerSigns", row.getRowNum());
        output(patient, "json", "patient", row.getRowNum());
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

    private Observation setObsBool(Observation obs, String input) {
        if (input.toLowerCase().contains("yes")) {
            obs.getValueBooleanType().setValue(true);
        } else if (input.toLowerCase().contains("no")) {
            obs.getValueBooleanType().setValue(false);
        }
        return obs;
    }

    private void output(IBaseResource resource, String encoding, String fileName, int indice) {
        try (FileOutputStream writer = new FileOutputStream(
                "C:\\Users\\DadeMurphy\\Desktop\\TestCaseMockup\\" + indice + "\\" + fileName + ".json"))
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
