package com.blogspot.jesfre.diff2html;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 *         Feb 10, 2024
 */
public class CodeDiffGeneratorSettings {

	private String configFile;
	private String repositoryBaseUrl;
	private String repositoryWorkingBranch;
	private String htmlTemplate;
	private boolean verbose;
	private boolean overwriteFiles;
	private String project;
	private String jiraTicket;
	private String version;
	private String workingDirPath;
	private String commandFile;
	private String reportOutputLocation;
	private int searchRangeDays;
	private Map<String, String> analyzingFileDiffFileMap = new HashMap<String, String>();

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getRepositoryBaseUrl() {
		return repositoryBaseUrl;
	}

	public void setRepositoryBaseUrl(String repositoryUrl) {
		this.repositoryBaseUrl = repositoryUrl;
	}

	public String getRepositoryWorkingBranch() {
		return repositoryWorkingBranch;
	}

	public void setRepositoryWorkingBranch(String repositoryWorkingBranch) {
		this.repositoryWorkingBranch = repositoryWorkingBranch;
	}

	public String getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isOverwriteFiles() {
		return overwriteFiles;
	}

	public void setOverwriteFiles(boolean overwriteFiles) {
		this.overwriteFiles = overwriteFiles;
	}

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

	public int getSearchRangeDays() {
		return searchRangeDays;
	}

	public void setSearchRangeDays(int rangeDays) {
		this.searchRangeDays = rangeDays;
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
		builder.append("CodeDiffGeneratorSettings [configFile=");
		builder.append(configFile);
		builder.append(", repositoryBaseUrl=");
		builder.append(repositoryBaseUrl);
		builder.append(", repositoryWorkingBranch=");
		builder.append(repositoryWorkingBranch);
		builder.append(", htmlTemplate=");
		builder.append(htmlTemplate);
		builder.append(", verbose=");
		builder.append(verbose);
		builder.append(", overwriteFiles=");
		builder.append(overwriteFiles);
		builder.append(", project=");
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
		builder.append(", searchRangeDays=");
		builder.append(searchRangeDays);
		builder.append(", analyzingFileDiffFileMap=");
		builder.append(analyzingFileDiffFileMap);
		builder.append("]");
		return builder.toString();
	}

}