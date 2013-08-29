package com.lazerycode.jmeter.testrunner;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kg.apc.jmeter.PluginsCMDWorker;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.jmeter.NewDriver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.DirectoryScanner;

import com.lazerycode.jmeter.JMeterMojo;
import com.lazerycode.jmeter.UtilityFunctions;
import com.lazerycode.jmeter.configuration.JMeterArgumentsArray;
import com.lazerycode.jmeter.configuration.RemoteConfiguration;
import com.lazerycode.jmeter.threadhandling.ExitException;


/**
 * TestManager encapsulates functions that gather JMeter Test files and execute the tests
 */
public class TestManager extends JMeterMojo {

	private final JMeterArgumentsArray baseTestArgs;
	private final File logsDirectory;
	private final File testFilesDirectory;
	private final List<String> testFilesIncluded;
	private final List<String> testFilesExcluded;
	private final boolean suppressJMeterOutput;
	private final RemoteConfiguration remoteServerConfiguration;
	private String resultName;
	private final File resultDirectory;
	private final File baseDirectory;
	private List<String> pluginTypes;
	private static Map<String, String> resultnames = new HashMap<String, String>();
	private String jtlresultName;
	private List<String> resultFilesName;


	public static Map<String, String> getResultnames() {
		return resultnames;
	}

	public TestManager(JMeterArgumentsArray baseTestArgs, File logsDirectory, File testFilesDirectory, List<String> testFilesIncluded, List<String> testFilesExcluded, RemoteConfiguration remoteServerConfiguration, boolean suppressJMeterOutput,File resultDir,File baseDir,List<String> plugintypes,List<String> resultFilesName) {
		this.baseTestArgs = baseTestArgs;
		this.logsDirectory = logsDirectory;
		this.testFilesDirectory = testFilesDirectory;
		this.testFilesIncluded = testFilesIncluded;
		this.testFilesExcluded = testFilesExcluded;
		this.remoteServerConfiguration = remoteServerConfiguration;
		this.suppressJMeterOutput = suppressJMeterOutput;
		this.resultDirectory = resultDir;
		this.baseDirectory = baseDir;
		this.pluginTypes = plugintypes;
		this.resultFilesName = resultFilesName;
	}

	/**
	 * It map the given jmx name with result file name
	 * 
	 * @throws MojoExecutionException
	 * 			it throw the exception jmx and resultfilename count are not equal
	 */
	public void mapResultNames() throws MojoExecutionException {
		if (this.testFilesIncluded.size() == this.resultFilesName.size()) {
			for(int i=0; i< this.testFilesIncluded.size(); i++){ 
				String testFileIncluded = this.testFilesIncluded.get(i);
				String resultName = this.resultFilesName.get(i);
				resultnames.put(testFileIncluded, resultName);
			}
		} else {
			throw new MojoExecutionException("Number of jmx files count is ::" + this.testFilesIncluded.size() + " and the result files name count is ::" + this.resultFilesName.size() + " not equal. Please enter the same numer of jmx files and resultfile names ");
		}
	}

	/**
	 * Executes all tests and returns the resultFile names
	 *
	 * @return the list of resultFile names
	 * @throws MojoExecutionException
	 */
	public List<String> executeTests() throws MojoExecutionException {
		JMeterArgumentsArray thisTestArgs = baseTestArgs;
		List<String> tests = generateTestList();
		List<String> results = new ArrayList<String>();
		for (String file : tests) {
			// get the value for given key
			jtlresultName = resultnames.get(file);
			//set the result name
			thisTestArgs.setResultFilesName(jtlresultName);

			if ((remoteServerConfiguration.isStartServersBeforeTests() && tests.get(0).equals(file)) || remoteServerConfiguration.isStartAndStopServersForEachTest()) {
				thisTestArgs.setRemoteStart();
				thisTestArgs.setRemoteStartServerList(remoteServerConfiguration.getServerList());
			}
			if ((remoteServerConfiguration.isStopServersAfterTests() && tests.get(tests.size() - 1).equals(file)) || remoteServerConfiguration.isStartAndStopServersForEachTest()) {
				thisTestArgs.setRemoteStop();
			}
			results.add(executeSingleTest(new File(testFilesDirectory, file), thisTestArgs));
			generateTestResultGraphs(thisTestArgs);

		}


		return results;
	}

	//=============================================================================================

	/**
	 * Executes a single JMeter test by building up a list of command line
	 * parameters to pass to JMeter.start().
	 *
	 * @param test JMeter test XML
	 * @return the report file names.
	 * @throws org.apache.maven.plugin.MojoExecutionException
	 *          Exception
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private String executeSingleTest(File test, JMeterArgumentsArray testArgs) throws MojoExecutionException {
		getLog().info(" ");
		testArgs.setTestFile(test);
		//Delete results file if it already exists
		new File(testArgs.getResultsLogFileName()).delete();
		getLog().debug("JMeter is called with the following command line arguments: " + UtilityFunctions.humanReadableCommandLineOutput(testArgs.buildArgumentsArray()));
		SecurityManager originalSecurityManager = overrideSecurityManager();
		Thread.UncaughtExceptionHandler originalExceptionHandler = overrideUncaughtExceptionHandler();
		PrintStream originalOut = System.out;
		setJMeterLogFile(test.getName() + ".log");
		getLog().info("Executing test: " + test.getName());
		try {
			//Suppress JMeter's annoying System.out messages.
			if (suppressJMeterOutput) System.setOut(new PrintStream(new NullOutputStream()));
			//Start the test.
			NewDriver.main(testArgs.buildArgumentsArray());
			waitForTestToFinish(UtilityFunctions.getThreadNames(false));
		} catch (ExitException e) {
			if (e.getCode() != 0) {
				throw new MojoExecutionException("Test failed", e);
			}
		} catch (InterruptedException ex) {
			getLog().info(" ");
			getLog().info("System Exit Detected!  Stopping Test...");
			getLog().info(" ");
		} finally {
			//TODO wait for child thread shutdown here?
			//TODO kill child threads if waited too long?
			//Reset everything back to normal
			System.setSecurityManager(originalSecurityManager);
			Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler);
			System.setOut(originalOut);
			getLog().info("Completed Test: " + test.getName());
		}
		return testArgs.getResultsLogFileName();
	}

	/**
	 * Create the jmeter.log file and set the log_file system property for JMeter to pick up
	 *
	 * @param value String
	 */
	private void setJMeterLogFile(String value) {
		System.setProperty("log_file", new File(this.logsDirectory + File.separator + value).getAbsolutePath());
	}

	/**
	 * Scan Project directories for JMeter Test Files according to includes and excludes
	 *
	 * @return found JMeter tests
	 */
	private List<String> generateTestList() {
		List<String> jmeterTestFiles = new ArrayList<String>();
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(this.testFilesDirectory);
		scanner.setIncludes(this.testFilesIncluded == null ? new String[]{"**/*.jmx"} : this.testFilesIncluded.toArray(new String[jmeterTestFiles.size()]));
		if (this.testFilesExcluded != null) {
			scanner.setExcludes(this.testFilesExcluded.toArray(new String[testFilesExcluded.size()]));
		}
		scanner.scan();
		final List<String> includedFiles = Arrays.asList(scanner.getIncludedFiles());
		jmeterTestFiles.addAll(includedFiles);
		return jmeterTestFiles;
	}
	/**
	 * It generate the graph in image for mat the given plugin types
	 * 
	 * @param testArgs
	 *    It is an object for JMeterArgumentsArray 
	 */
	public void generateTestResultGraphs(JMeterArgumentsArray testArgs)  {
		try {
			getLog().info("  ");
			getLog().info("  ");
			getLog().info("------------------------------------------------------------");
			getLog().info(" C R E A T I N G - G R A P H S  -  A S  -  I M A G E  -  F I L E S  ");
			getLog().info("------------------------------------------------------------");
			getLog().info("  ");
			getLog().info("  ");

			/*ArrayList<String> imageTypes = new ArrayList<String>();
			imageTypes.add("ThreadsStateOverTime");
			imageTypes.add("BytesThroughputOverTime");
			imageTypes.add("HitsPerSecond");
			imageTypes.add("LatenciesOverTime");
			imageTypes.add("ResponseCodesPerSecond");
			imageTypes.add("ResponseTimesDistribution");
			imageTypes.add("ResponseTimesOverTime");
			imageTypes.add("ResponseTimesPercentiles");
			imageTypes.add("ThroughputOverTime");
			imageTypes.add("ThroughputVsThreads");
			imageTypes.add("TransactionsPerSecond");*/

			if(this.pluginTypes != null ){
				File file = new File(this.baseDirectory + "/target/jmeter/bin/lib");
				if(!file.exists()){
					file.mkdirs();
				}
				resultName = testArgs.getTestResultsName();
				for(String plugintypes:this.pluginTypes){
					String resultfilename = resultName.substring(0, resultName.lastIndexOf("."));
					String imageName = this.resultDirectory + "/graphs/" + resultfilename + "-" + plugintypes + ".png";
					removeFiles(imageName);
					PluginsCMDWorker worker = new PluginsCMDWorker();
					worker.addExportMode(PluginsCMDWorker.EXPORT_PNG);
					worker.setOutputPNGFile(imageName);
					worker.setInputFile(this.resultDirectory + "/" + resultName);
					worker.setPluginType(plugintypes);
					worker.doJob();
					getLog().info("  ");
					getLog().info("  ");
					getLog().info(" Creating  " + plugintypes + "  graph as image file ");
				}
				getLog().info("  ");
				getLog().info("  ");
				getLog().info("-----------------------------------------------------------");
				getLog().info(" C R E A T I N G  -  I M A G E  -  F I L E S  - C O M P L E T E D");
				getLog().info("-----------------------------------------------------------");
				getLog().info(" ");
				getLog().info(" ");
			} else {
				throw new Exception("Enter the List of pluginTypes name in pom inside this tag <pluginTypes> to generate the graphs in image file");

			}
		} catch (Exception e) {
			getLog().info(" Exception " + e.getMessage());
		} 
	}
	public void removeFiles(String fileName){
		File file = new File(fileName);
		if(file.exists()){
			file.delete();
		}
	}
}