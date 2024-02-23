package com.blogspot.jesfre.diff2html;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 *         Feb 10, 2024
 */
public class CodeDiffGeneratorSettings {

	private String project;
	private String jiraTicket;
	private String version;
	private String workingDirPath;
	private String commandFile;
	private String reportOutputLocation;
	private Map<String, String> analyzingFileDiffFileMap = new HashMap<String, String>();

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getJiraTicket() {
		return jiraTicket;
	}

	public void setJiraTicket(String jiraTicket) {
		this.jiraTicket = jiraTicket;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getWorkingDirPath() {
		return workingDirPath;
	}

	public void setWorkingDirPath(String workingDirPath) {
		this.workingDirPath = workingDirPath;
	}

	public String getCommandFile() {
		return commandFile;
	}

	public void setCommandFile(String commandFile) {
		this.commandFile = commandFile;
	}

	public String getReportOutputLocation() {
		return reportOutputLocation;
	}

	public void setReportOutputLocation(String reportOutputLocation) {
		this.reportOutputLocation = reportOutputLocation;
	}

	public Map<String, String> getAnalyzingFileDiffFileMap() {
		return analyzingFileDiffFileMap;
	}

	public String getDiffFile(String analyzingFile) {
		return analyzingFileDiffFileMap.get(analyzingFile);
	}

	public void putAnalyzingFile(String analyzingFile) {
		this.analyzingFileDiffFileMap.put(analyzingFile, "");
	}

	public void putAnalyzingFile(String analyzingFile, String diffFile) {
		this.analyzingFileDiffFileMap.put(analyzingFile, diffFile);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CodeDiffGeneratorSettings [project=");
		builder.append(project);
		builder.append(", jiraTicket=");
		builder.append(jiraTicket);
		builder.append(", version=");
		builder.append(version);
		builder.append(", workingDirPath=");
		builder.append(workingDirPath);
		builder.append(", commandFile=");
		builder.append(commandFile);
		builder.append(", reportOutputLocation=");
		builder.append(reportOutputLocation);
		builder.append(", analyzingFileDiffFileMap=");
		builder.append(analyzingFileDiffFileMap);
		builder.append("]");
		return builder.toString();
	}

}
