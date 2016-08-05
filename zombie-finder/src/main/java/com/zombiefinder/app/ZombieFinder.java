package com.zombiefinder.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;

import com.gigaspaces.grid.gsa.AgentProcessDetails;
import com.zombiefinder.model.XapProcess;

public class ZombieFinder {
	private Properties properties = new Properties();

	public ZombieFinder() {
		// default
	}

	private void loadProperties(String[] args) {
		System.out.println("Begin loading properties file.");

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			properties.load(
					new FileInputStream(classLoader.getResource("zombie-finder-" + args[0] + ".properties").getFile()));
		} catch (Exception exception) {
			System.out.println("EXCEPTION: " + exception.getMessage());
			System.exit(1);
		}

		System.out.println("Completed loading properties file.");
	}

	private String getLookupLocator() {
		return properties.getProperty("lookup-locator", "");
	}

	private Set<String> getConfiguredHosts() {
		System.out.println("Begin getting hosts from configuration.");

		Set<String> hostSet = new HashSet<String>();
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith("host")) {
				String[] tokens = propertyName.trim().split("\\|");
				hostSet.add(tokens[1]);
			}
		}

		System.out.println("Completed getting hosts from configuration.");
		return hostSet;
	}

	private int getTotalGSAs() {
		int count = 0;
		for (String host : getConfiguredHosts()) {
			count += Integer.valueOf(properties.getProperty("host|" + host + "|total-gsa", "0"));
		}
		return count;
	}
	
	private int getTotalGSCsPerHost(String host) {
		return Integer.valueOf(properties.getProperty("host|" + host + "|total-gsc", "0"));
	}

	private int getTotalGSCs() {
		int count = 0;
		for (String host : getConfiguredHosts()) {
			count += getTotalGSCsPerHost(host);
		}
		return count;
	}

	private long getTimeout() {
		return Long.valueOf(properties.getProperty("timeout", "20000"));
	}
	
	private String getUser() {
		return properties.getProperty("user", "");
	}

	private XapProcess buildXapProcess(String host, String processId, String serviceType) {
		System.out.println("Begin building XapProcess object.");

		XapProcess xapProcess = new XapProcess();
		xapProcess.setHost(host);
		xapProcess.setProcessId(processId);
		xapProcess.setServiceType(serviceType);

		System.out.println("Completed building XapProcess object.");
		return xapProcess;
	}

	private List<XapProcess> getHostProcesses() {
		System.out.println("Begin getting host processes.");

		List<XapProcess> hostProcessList = new ArrayList<XapProcess>();
		for (String host : getConfiguredHosts()) {
			BufferedReader reader = null;
			try {
				ProcessBuilder processBuilder = new ProcessBuilder();
//				processBuilder.command("/bin/bash", "-c", "ps -ef | grep 'com.gigaspaces.start.SystemBoot'");
				processBuilder.command("/bin/bash", "-c", "ssh " + getUser() + "@" + host + " ps -ef | grep 'com.gigaspaces.start.SystemBoot'");
				Process process = processBuilder.start();
				if (process.waitFor() != 0) {
					System.out.println("EXCEPTION: There was a problem running the remote server process.");
					System.exit(1);
				}
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String output = "";
				while ((output = reader.readLine()) != null) {
					if (output.contains("services=GSC")) {
						String[] tokens = output.trim().split(" +");
						hostProcessList.add(buildXapProcess(host, tokens[1], "gsc"));
					}
				}
			} catch (Exception exception) {
				System.out.println("EXCEPTION: " + exception.getMessage());
				System.exit(1);
			} finally {
				try {
					reader.close();
				} catch (Exception exception) {
					System.out.println("EXCEPTION: " + exception.getMessage());
					System.exit(1);
				}
			}
		}

		System.out.println("Completed getting host processes.");
		return hostProcessList;
	}
	
	private List<XapProcess> getAdminProcesses() {
		System.out.println("Begin getting XAP Admin processes.");

		Admin admin = new AdminFactory().addLocator(getLookupLocator()).createAdmin();
		admin.getGridServiceAgents().waitFor(getTotalGSAs(), getTimeout(), TimeUnit.MILLISECONDS);
		if (admin.getGridServiceAgents().getSize() != getTotalGSAs()) {
			System.out.println("EXCEPTION: Was not able to locate configured number of GSAs.");
			System.exit(1);
		}
		List<XapProcess> adminProcessList = new ArrayList<XapProcess>();
		for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
			for (AgentProcessDetails agentProcessDetails : gsa.getProcessesDetails()) {
				if ("gsc".equals(agentProcessDetails.getServiceType())) {
					adminProcessList.add(buildXapProcess(gsa.getMachine().getHostAddress(),
							((Long) agentProcessDetails.getProcessId()).toString(),
							agentProcessDetails.getServiceType()));
				}
			}
		}

		System.out.println("Completed getting XAP Admin processes.");
		return adminProcessList;
	}

	private void validateConfiguredGSCs(List<XapProcess> hostProcessList) {
		System.out.println("Begin validating configured GSCs.");
		
		if (hostProcessList.size() < getTotalGSCs()) {
			System.out.println("IMPORTANT: Located fewer total GSC processes than what is configured for this XAP service grid environment.");
		}
		if (hostProcessList.size() > getTotalGSCs()) {
			System.out.println("IMPORTANT: Located more total GSC processes than what is configured for this XAP service grid environment.");
		}
		for (String host : getConfiguredHosts()) {
			int gscCount = 0;
			for (XapProcess xapProcess : hostProcessList) {
				if (host.equals(xapProcess.getHost())) {
					gscCount += 1;
				}
			}
			if (gscCount < getTotalGSCsPerHost(host)) {
				System.out.println("IMPORTANT: Located fewer GSC processes than what is configured for host [" + host + "].");
			}
			if (gscCount > getTotalGSCsPerHost(host)) {
				System.out.println("IMPORTANT: Located more GSC processes than what is configured for host [" + host + "].");
			}			
		}		
		
		System.out.println("Completed validating configured GSCs.");
	}
	
	private void checkForZombies(List<XapProcess> hostProcessList, List<XapProcess> adminProcessList) {
		System.out.println("Begin checking for zombie XAP processes.");

		int totalZombies = 0;
		for (XapProcess xapProcess : hostProcessList) {
			if (!adminProcessList.contains(xapProcess)) {
				System.out.println("IMPORTANT: Found a zombie XapProcess: [Host: " + xapProcess.getHost() + ", ProcessId: "
						+ xapProcess.getProcessId() + "]");
				totalZombies++;
			}
		}
		System.out.println("IMPORTANT: Found total of " + totalZombies + " zombie process(es).");

		System.out.println("Completed checking for zombie XAP processes.");
	}

	public static void main(String[] args) {
		System.out.println("ZombieFinder started.");

		ZombieFinder zf = new ZombieFinder();
		zf.loadProperties(args);
		List<XapProcess> hostProcessList = zf.getHostProcesses();
		List<XapProcess> adminProcessList = zf.getAdminProcesses();
		zf.validateConfiguredGSCs(hostProcessList);
		zf.checkForZombies(hostProcessList, adminProcessList);

		System.out.println("ZombieFinder completed.");
		System.exit(0);
	}
}
