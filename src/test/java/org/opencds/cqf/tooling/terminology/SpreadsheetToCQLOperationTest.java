package org.opencds.cqf.tooling.terminology;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;

public class SpreadsheetToCQLOperationTest {

	String input_xlsx = "../cqf-tooling/src/test/resources/org/opencds/cqf/tooling/testfiles/SpreadsheetToCQLOperation/CQLv151ChangesApplied.xlsx";
	String output_cql_dir = "../cqf-tooling/src/test/resources/org/opencds/cqf/tooling/testfiles/SpreadsheetToCQLOperation/generated/";
	String expected_output = "../cqf-tooling/src/test/resources/org/opencds/cqf/tooling/testfiles/SpreadsheetToCQLOperation/CQLv151ChangesApplied.cql";

	@Test
	public void generate_CQL_test() throws IOException {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();

		String[] arguments = new String[3];
		arguments[0] = "-SpreadsheetToCQL";
		arguments[1] = "-pts=" + input_xlsx;
		arguments[2] = "-op=" + output_cql_dir;

		try {
			tester.execute(arguments);
		} catch (Exception e) {
			fail("Should not be throwing exceptions");
		} finally {
			assertTrue(true);
		}

	}
	
	
	@Test
	//checking if it works with header argument as well
	public void generate_CQL_test2()  {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();

		String[] arguments = new String[4];
		arguments[0] = "-SpreadsheetToCQL";
		arguments[1] = "-pts=" + input_xlsx;
		arguments[2] = "-op=" + output_cql_dir;
		arguments[3] = "-hh=" + true;
		tester.execute(arguments);
		assert(true);
		
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	//If the first argument is not SpreadsheetToCQL, then it will cause an illegal argument exception
	public void first_arg_test()  {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();

		String[] arguments = new String[3];
		arguments[0] = "-Spreasheet";
		arguments[1] = "-pts=" + input_xlsx;
		arguments[2] = "-op=" + output_cql_dir;

		tester.execute(arguments);
		
		
		
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	//If there is less than two arguments passed then an illegal argument exception will be thrown
	public void arguments_test()  {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();

		String[] arguments = new String[2];
		arguments[0] = "SpreadsheetToCQL";
		tester.execute(arguments);
		
		
		
	}
	
	
	
	@Test(expected = IllegalArgumentException.class)
	//Checking if proper flag in wrong place throws exception
	public void unknown_flag_test()  {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();

		String[] arguments = new String[3];
		arguments[0] = "-SpreadsheetToCQL";
		arguments[1] = "-hh=" + input_xlsx;
		arguments[2] = "-op=" + output_cql_dir;

		tester.execute(arguments);
		
		
		
	}
	@Test(expected = IllegalArgumentException.class)
	//If there is no path to the spreadsheet then illegal argument exception is thrown
	public void pathToSpreadsheet_test()  {
		SpreadsheetToCQLOperation tester = new SpreadsheetToCQLOperation();
		String[] arguments = new String[3];
		arguments[0] = "-SpreadsheetToCQL";
		arguments[1] = "-pathtospreadsheet=" + " ";
		arguments[2] = "-op=" + output_cql_dir;
		
		

		tester.execute(arguments);
		
		
		
	}
	

	@Test
	public void compare_output_test() throws IOException {

		BufferedReader reader1 = new BufferedReader(new FileReader(output_cql_dir + "CQLv151ChangesApplied.cql"));
		BufferedReader reader2 = new BufferedReader(new FileReader(expected_output));

		// skip past generated time so we only compare everything in CQL format
		for (int i = 0; i <= 5; i++) {
			reader1.readLine();
			reader2.readLine();
		}

		String line1 = reader1.readLine();
		String line2 = reader2.readLine();

		boolean isEqual = true;

		int curr_line = 1;

		while (line1 != null || line2 != null) {
			if (line1 == null || line2 == null) {
				isEqual = false;

				break;
			} else if (!line1.equals(line2)) {
				isEqual = false;

				break;
			}

			line1 = reader1.readLine();

			line2 = reader2.readLine();

			curr_line++;
		}

		if (isEqual) {
			System.out.println("Both files are the same.");
		} else {
			System.out.println("Both files have different text on line " + curr_line);

			System.out.println("File1 has " + line1 + " and File2 has " + line2 + " at line " + curr_line);
		}

		reader1.close();

		reader2.close();

		assertTrue(isEqual);
	}

}
