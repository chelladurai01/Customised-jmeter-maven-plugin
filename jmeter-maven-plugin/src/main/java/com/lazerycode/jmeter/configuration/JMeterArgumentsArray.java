package com.lazerycode.jmeter.configuration;

import static com.lazerycode.jmeter.UtilityFunctions.isNotSet;
import static com.lazerycode.jmeter.UtilityFunctions.isSet;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.JMETER_HOME_OPT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.LOGFILE_OPT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.LOGLEVEL;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.NONGUI_OPT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.NONPROXY_HOSTS;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.PROPFILE2_OPT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.PROXY_HOST;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.PROXY_PASSWORD;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.PROXY_PORT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.PROXY_USERNAME;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.REMOTE_OPT;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.REMOTE_OPT_PARAM;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.REMOTE_STOP;
import static com.lazerycode.jmeter.configuration.JMeterCommandLineArguments.TESTFILE_OPT;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Creates an arguments array to pass to the JMeter object to run tests.
 *
 * @author Mark Collin
 */
public class JMeterArgumentsArray {

	private final String jMeterHome;
	private final boolean disableTests;
	private final TreeSet<JMeterCommandLineArguments> argumentList = new TreeSet<JMeterCommandLineArguments>();
	private DateTimeFormatter dateFormat = ISODateTimeFormat.basicDate();
	private ProxyConfiguration proxyConfiguration;
	private boolean timestampResults = false;
	private boolean appendTimestamp = false;
	private String resultFileExtension = ".jtl";
	private String remoteStartServerList;
	private String customPropertiesFile;
	private String testFile;
	private String resultsLogFileName;
	private String resultsDirectory;
	private LogLevel overrideRootLogLevel;
	private String resultFilesName;
	private String resultsName;

	/**
	 * Create an instance of JMeterArgumentsArray
	 *
	 * @param disableGUI          If GUI should be disabled or not
	 * @param jMeterHomeDirectory The JMETER_HOME directory, what JMeter bases its classpath on
	 * @throws MojoExecutionException
	 */
	public JMeterArgumentsArray(boolean disableGUI, String jMeterHomeDirectory) throws MojoExecutionException {
		if (isNotSet(jMeterHomeDirectory)) throw new MojoExecutionException("Unable to set JMeter Home Directory...");
		jMeterHome = jMeterHomeDirectory;
		argumentList.add(JMETER_HOME_OPT);
		if (disableGUI) {
			argumentList.add(NONGUI_OPT);
			disableTests = false;
		} else {
			disableTests = true;
		}
	}

	public void setRemoteStop() {
		argumentList.add(REMOTE_STOP);
	}

	public void setRemoteStart() {
		argumentList.add(REMOTE_OPT);
	}

	public void setRemoteStartServerList(String serverList) {
		if (isNotSet(serverList)) return;
		remoteStartServerList = serverList;
		argumentList.add(REMOTE_OPT_PARAM);
	}

	public void setProxyConfig(ProxyConfiguration configuration) {
		this.proxyConfiguration = configuration;
		if (isSet(proxyConfiguration.getHost())) {
			argumentList.add(PROXY_HOST);
			argumentList.add(PROXY_PORT);
		}
		if (isSet(proxyConfiguration.getUsername())) {
			argumentList.add(PROXY_USERNAME);
		}
		if (isSet(proxyConfiguration.getPassword())) {
			argumentList.add(PROXY_PASSWORD);
		}
		if (isSet(proxyConfiguration.getHostExclusions())) {
			argumentList.add(NONPROXY_HOSTS);
		}
	}

	public void setACustomPropertiesFile(File customProperties) {
		if (isNotSet(customProperties)) return;
		customPropertiesFile = customProperties.getAbsolutePath();
		argumentList.add(PROPFILE2_OPT);
	}

	public void setLogRootOverride(String requestedLogLevel) {
		if (isNotSet(requestedLogLevel)) return;
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.toString().equals(requestedLogLevel.toUpperCase())) {
				overrideRootLogLevel = logLevel;
				argumentList.add(LOGLEVEL);
			}
		}
	}

	public void setResultsDirectory(String resultsDirectory) {
		this.resultsDirectory = resultsDirectory;
	}

	public void setResultsTimestamp(boolean addTimestamp) {
		timestampResults = addTimestamp;
	}

	public void setResultsFileNameDateFormat(DateTimeFormatter dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void appendTimestamp(boolean append) {
		appendTimestamp = append;
	}

	public String getResultsLogFileName() {
		return resultsLogFileName;
	}

	public void setResultFileOutputFormatIsCSV(boolean isCSVFormat) {
		if (isCSVFormat) {
			resultFileExtension = ".csv";
		} else {
			resultFileExtension = ".jtl";
		}
	}

	/**
	 *  It set the result file name to save the xml/csv report
	 *  
	 * @param resultFilesName
	 * 			getting the result name as parameter
	 */
	public void setResultFilesName(String resultFilesName) {
		this.resultFilesName = resultFilesName;
	}

	public void setTestFile(File value) {
		if (isNotSet(value) || disableTests) return;
		testFile = value.getAbsolutePath();	
		if (timestampResults) {
			//TODO investigate when timestamp is generated.
			if (appendTimestamp) {
				if(StringUtils.isEmpty(this.resultFilesName)) {
					String resultFileName = value.getName().substring(0, value.getName().lastIndexOf(".")) + "-" + dateFormat.print(new LocalDateTime()) + resultFileExtension;
					resultsLogFileName = resultsDirectory + File.separator + resultFileName;
					resultsName = resultFileName;
				} else {
					String resultFileName = this.resultFilesName + "-" + dateFormat.print(new LocalDateTime()) + resultFileExtension;
					resultsLogFileName = resultsDirectory + File.separator + resultFileName;
					resultsName =  resultFileName;
				}
			} else {
				if(StringUtils.isEmpty(this.resultFilesName)){
					String resultFileName = dateFormat.print(new LocalDateTime()) + "-" +value.getName().substring(0, value.getName().lastIndexOf(".")) + resultFileExtension;
					resultsLogFileName = resultsDirectory + File.separator + resultFileName;
					resultsName = resultFileName;
				}
				else {
					String resultFileName = dateFormat.print(new LocalDateTime()) + "-" + this.resultFilesName + resultFileExtension;
					resultsLogFileName = resultsDirectory + File.separator + resultFileName;
					resultsName = resultFileName;
				}
			}
		} else {
			if(StringUtils.isEmpty(this.resultFilesName)){
				String resultFileName = value.getName().substring(0, value.getName().lastIndexOf(".")) + resultFileExtension;
				resultsLogFileName = resultsDirectory + File.separator + resultFileName;
				resultsName = resultFileName;
			}
			else {
				String resultFileName = this.resultFilesName + resultFileExtension;
				resultsLogFileName = resultsDirectory + File.separator + resultFileName;
				resultsName = resultFileName;
			}
		}
		argumentList.add(TESTFILE_OPT);
		argumentList.add(LOGFILE_OPT);
	}

	/**
	 * 
	 * @return
	 *      Return the test result name to generate the graph in image format
	 */
	public String getTestResultsName() {
		return resultsName;
	}


	/**
	 * Generate an arguments array representing the command line options you want to send to JMeter.
	 * The order of the array is determined by the order the values in JMeterCommandLineArguments are defined.
	 *
	 * @return An array representing the command line sent to JMeter
	 * @throws MojoExecutionException
	 */
	public String[] buildArgumentsArray() throws MojoExecutionException {
		if (!argumentList.contains(TESTFILE_OPT) && !disableTests) throw new MojoExecutionException("No test(s) specified!");
		ArrayList<String> argumentsArray = new ArrayList<String>();
		for (JMeterCommandLineArguments argument : argumentList) {
			switch (argument) {
			case NONGUI_OPT:
				argumentsArray.add(NONGUI_OPT.getCommandLineArgument());
				break;
			case TESTFILE_OPT:
				argumentsArray.add(TESTFILE_OPT.getCommandLineArgument());
				argumentsArray.add(testFile);
				break;
			case LOGFILE_OPT:
				argumentsArray.add(LOGFILE_OPT.getCommandLineArgument());
				argumentsArray.add(resultsLogFileName);
				break;
			case JMETER_HOME_OPT:
				argumentsArray.add(JMETER_HOME_OPT.getCommandLineArgument());
				argumentsArray.add(jMeterHome);
				break;
			case LOGLEVEL:
				argumentsArray.add(LOGLEVEL.getCommandLineArgument());
				argumentsArray.add(overrideRootLogLevel.toString());
				break;
			case PROPFILE2_OPT:
				argumentsArray.add(PROPFILE2_OPT.getCommandLineArgument());
				argumentsArray.add(customPropertiesFile);
				break;
			case REMOTE_OPT:
				argumentsArray.add(REMOTE_OPT.getCommandLineArgument());
				break;
			case PROXY_HOST:
				argumentsArray.add(PROXY_HOST.getCommandLineArgument());
				argumentsArray.add(proxyConfiguration.getHost());
				break;
			case PROXY_PORT:
				argumentsArray.add(PROXY_PORT.getCommandLineArgument());
				argumentsArray.add(proxyConfiguration.getPort());
				break;
			case PROXY_USERNAME:
				argumentsArray.add(PROXY_USERNAME.getCommandLineArgument());
				argumentsArray.add(proxyConfiguration.getUsername());
				break;
			case PROXY_PASSWORD:
				argumentsArray.add(PROXY_PASSWORD.getCommandLineArgument());
				argumentsArray.add(proxyConfiguration.getPassword());
				break;
			case NONPROXY_HOSTS:
				argumentsArray.add(NONPROXY_HOSTS.getCommandLineArgument());
				argumentsArray.add(proxyConfiguration.getHostExclusions());
				break;
			case REMOTE_STOP:
				argumentsArray.add(REMOTE_STOP.getCommandLineArgument());
				break;
			case REMOTE_OPT_PARAM:
				argumentsArray.add(REMOTE_OPT_PARAM.getCommandLineArgument());
				argumentsArray.add(remoteStartServerList);
				break;
			}
		}
		return argumentsArray.toArray(new String[argumentsArray.size()]);
	}
}