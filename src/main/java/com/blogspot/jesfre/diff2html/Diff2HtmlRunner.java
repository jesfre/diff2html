package com.blogspot.jesfre.diff2html;

import static com.blogspot.jesfre.diff2html.DiffConstants.CODE_DIFF_FOLDER;
import static com.blogspot.jesfre.diff2html.DiffConstants.SLASH;
import static com.blogspot.jesfre.diff2html.DiffConstants.SOURCE_CODE_FOLDER;
import static com.blogspot.jesfre.misc.PathUtils.formatPath;
import static com.blogspot.jesfre.svn.utils.SvnLogExtractor.CommandExecutionMode.COMMAND_FILE;
import static com.blogspot.jesfre.svn.utils.SvnLogExtractor.CommandExecutionMode.DIRECT_COMMAND;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.blogspot.jesfre.commandline.CommandLineRunner;
import com.blogspot.jesfre.svn.ModifiedFile;
import com.blogspot.jesfre.svn.OperationType;
import com.blogspot.jesfre.svn.utils.SvnDiff;
import com.blogspot.jesfre.svn.utils.SvnExport;
import com.blogspot.jesfre.svn.utils.SvnLog;
import com.blogspot.jesfre.svn.utils.SvnLogExtractor;

public class Diff2HtmlRunner {

	// ----------- SET -----------------------
	private static final String BAT_FILENAME_TEMPLATE = "svn_diff_commands_%s.bat";
	private static final String SVN_BASE_URL = "some SVN URL";
	private static final String DEFAULT_HTML_NAME_PREFIX = "";
	private static final String DEFAULT_HTML_NAME_SUFFIX = "_Code_Diff";
	private static final OperationType[] OPERATIONS_TO_REVIEW = {OperationType.ADDED, OperationType.MERGED, OperationType.MODIFIED, OperationType.UPDATED};
	// TODO add valid file types to the configuration file
	private static final String[] VALID_FILE_TYPES = { "java", "properties", "txt", "xml", "jsp", "js", "html", "css" };

	private static boolean cmdFileBasedExecution = false;
	private static boolean usingRepoUrl = false;

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			throw new IllegalArgumentException("Setup file was not provided.");
		}

		Diff2HtmlRunner diff2HtmlRunner = new Diff2HtmlRunner();
		CodeDiffGeneratorSettings runnerSettings = diff2HtmlRunner.loadSettingsProperties(args);

		// Run svn command to get list-of-revisions or latest two commit revisions
		System.out.println("Getting files revisions...");
		File codeDiffFolder = new File(runnerSettings.getReportOutputLocation());
		codeDiffFolder.mkdirs();

		Map<String, List<SvnLog>> fileSvnLogMap = new HashMap<String, List<SvnLog>>();
		SvnLogExtractor versionExtractor = new SvnLogExtractor("TBD", runnerSettings.getReportOutputLocation());

		if(runnerSettings.getAnalyzingFileDiffFileMap().size() == 0 && StringUtils.isBlank(runnerSettings.getRepositoryBaseUrl())) {
			throw new IllegalStateException("No files to be analyzed.");
		}

		if(runnerSettings.getAnalyzingFileDiffFileMap().size() == 0) {
			usingRepoUrl = true;

			String repoUrlString = runnerSettings.getRepositoryBaseUrl() + "/" + runnerSettings.getRepositoryWorkingBranch();
			URL repoUrl = new URL(repoUrlString).toURI().normalize().toURL();

			// Will discover the modified files from the repository
			List<SvnLog> logList = versionExtractor
					.withComment(runnerSettings.getJiraTicket())
					.withExecutionMode(cmdFileBasedExecution ? COMMAND_FILE : DIRECT_COMMAND)
					.verbose(runnerSettings.isVerbose())
					.lookDaysBack(runnerSettings.getSearchRangeDays())
					.clearTempFiles(true)
					.exportLog(false)
					.listModifiedFiles(true)
					.analyzeUrl(repoUrl).extract();

			if (logList.isEmpty()) {
				System.err.println("Cannot find any files to analyze.");
				System.exit(-1);
			}

			for(SvnLog log : logList) {
				for(ModifiedFile mf : log.getModifiedFiles()) {
					if(ArrayUtils.contains(OPERATIONS_TO_REVIEW, mf.getOperation())) {
						if (!ArrayUtils.contains(VALID_FILE_TYPES, FilenameUtils.getExtension(mf.getFile()))) {
							// TODO check if this one works
							// http://www.java2s.com/example/java/file-path-io/checks-whether-or-not-a-file-is-a-text-file-or-a-binary-one.html
							// Instead of checking for valid file extensions
							System.out.println("Invalid file type skipped: " + FilenameUtils.getName(mf.getFile()));
							continue;
						}

						String fileUrlString = runnerSettings.getRepositoryBaseUrl() + "/" + mf.getFile();
						URL fileUrl = new URL(fileUrlString).toURI().normalize().toURL();
						runnerSettings.putAnalyzingFile(fileUrl.toString());
					}
				}
			}

			if(runnerSettings.isVerbose() && !runnerSettings.getAnalyzingFileDiffFileMap().isEmpty()) {
				System.out.println("Files committed with " + runnerSettings.getJiraTicket());
				for(String fileFound : runnerSettings.getAnalyzingFileDiffFileMap().keySet()) {
					System.out.println("- " + FilenameUtils.getName(fileFound));
				}
			}
		}

		// Using files from the configuration file
		for (String analyzedFile : runnerSettings.getAnalyzingFileDiffFileMap().keySet()) {
			List<SvnLog> logList = new ArrayList<SvnLog>(); 
			versionExtractor
			// .withLimit(2)
			.withComment(runnerSettings.getJiraTicket())
			.withExecutionMode(cmdFileBasedExecution ? COMMAND_FILE : DIRECT_COMMAND)
			.verbose(runnerSettings.isVerbose())
			.lookDaysBack(runnerSettings.getSearchRangeDays())
			.clearTempFiles(true)
			.exportLog(false);
			if(usingRepoUrl) {
				logList = versionExtractor.analyzeUrl(new URL(analyzedFile)).extract();
			} else {
				logList = versionExtractor.analyze(analyzedFile).extract();
			}

			if(logList.size() == 0) {
				System.err.println("No log found for " + analyzedFile + " using comment " + runnerSettings.getJiraTicket());
			}
			fileSvnLogMap.put(analyzedFile, logList);
		}

		// Execute svn commands to get .java and .diff files using latest two revisions
		System.out.println("Generating SVN diff files...");
		if (cmdFileBasedExecution) {
			diff2HtmlRunner.generateDiffFilesCommandFileDriven(runnerSettings, fileSvnLogMap);
		} else {
			diff2HtmlRunner.generateDiffFiles(runnerSettings, fileSvnLogMap, true);
		}

		// Generate HTML files
		System.out.println("Generating HTML diff files...");
		for (Entry<String, String> e : runnerSettings.getAnalyzingFileDiffFileMap().entrySet()) {
			String htmlPath = new Diff2Html(runnerSettings, DEFAULT_HTML_NAME_PREFIX, DEFAULT_HTML_NAME_SUFFIX + "-v" + runnerSettings.getVersion())
					.processDiff(runnerSettings.getReportOutputLocation(), e.getKey(), e.getValue());
			new File(e.getValue()).delete();
			System.out.println("Generated " + htmlPath);
		}
		System.out.println("Done.");
	}

	@SuppressWarnings("unchecked")
	public CodeDiffGeneratorSettings loadSettingsProperties(String[] setupFileLocations) throws FileNotFoundException, IOException, ConfigurationException {
		CodeDiffGeneratorSettings settings = new CodeDiffGeneratorSettings();

		for(int i=0; i<setupFileLocations.length; i++) {
			String setupFilePath = setupFileLocations[i];
			System.out.println("Loading Diff2Html settings from " + setupFilePath);

			PropertiesConfiguration config = new PropertiesConfiguration();
			config.setListDelimiter('|');
			config.load(setupFilePath);

			settings.setConfigFile(setupFilePath);

			if(config.containsKey("repository.baseUrl")) {
				settings.setRepositoryBaseUrl(config.getString("repository.baseUrl", ""));
			}
			if(config.containsKey("repository.workingBranch")) {
				settings.setRepositoryWorkingBranch(config.getString("repository.workingBranch", ""));
			}
			if(config.containsKey("project")) {
				settings.setProject(config.getString("project", "no_project"));
			}
			if(config.containsKey("jira.ticket")) {
				settings.setJiraTicket(config.getString("jira.ticket", ""));
			}
			if(config.containsKey("review.version")) {
				settings.setVersion(config.getString("review.version", "1"));
			}
			if(config.containsKey("resource.htmlTemplate")) {
				settings.setHtmlTemplate(config.getString("resource.htmlTemplate", ""));
			}
			if(config.containsKey("global.verbose")) {
				settings.setVerbose(config.getBoolean("global.verbose", false));
			}
			if(config.containsKey("global.overwriteFiles")) {
				settings.setOverwriteFiles(config.getBoolean("global.overwriteFiles", false));
			}
			if (config.containsKey("repository.search.range.days")) {
				settings.setSearchRangeDays(config.getInt("repository.search.range.days", 1));
			}

			if(config.containsKey("workingDirectory")) {
				String workingDir = config.getString("workingDirectory");
				settings.setWorkingDirPath(workingDir);

				String outputFolderPath = workingDir + SLASH + CODE_DIFF_FOLDER;
				settings.setReportOutputLocation(outputFolderPath);
			}

			if(config.containsKey("file")) {
				List<String> fileList = config.getList("file");
				for (String file : fileList) {
					settings.putAnalyzingFile(file);
				}
			}
			if(config.containsKey("f")) {
				List<String> fList = config.getList("f");
				for (String file : fList) {
					settings.putAnalyzingFile(file);
				}
			}
		}

		if(StringUtils.isBlank(settings.getHtmlTemplate())) {
			System.out.println("Not HTML template file was provided. Using a default template.");
			settings.setHtmlTemplate("DEFAULT");
		}
		return settings;
	}

	private void generateDiffFiles(CodeDiffGeneratorSettings settings, Map<String, List<SvnLog>> fileRevisionListMap,
			boolean exportFileFromRepo) throws Exception {
		Set<String> fileList = new LinkedHashSet<String>(settings.getAnalyzingFileDiffFileMap().keySet());
		for (String file : fileList) {
			String originalFileName = FilenameUtils.getName(file);
			String originalFileType = FilenameUtils.getExtension(file);
			String cName = FilenameUtils.getBaseName(file);

			// TODO to validate with new files where there is one single revision
			if (!fileRevisionListMap.containsKey(file)) {
				continue;
			}
			long headRev = 0;
			long prevRev = 0;
			List<SvnLog> logList = fileRevisionListMap.get(file);
			if (logList.size() > 0) {
				headRev = logList.get(0).getRevision();
				prevRev = logList.get(logList.size() - 1).getRevision();
				if (prevRev > 1) {
					// Compare with the revision before the list last entry's revision number
					prevRev = prevRev - 1;
				}
			}

			String sourFolderPath = settings.getWorkingDirPath() + SLASH + SOURCE_CODE_FOLDER;
			String outDiffFile = sourFolderPath + SLASH + originalFileName + "_r" + headRev + "-r" + prevRev + ".diff";

			File sourceFolder = new File(sourFolderPath);
			sourceFolder.mkdirs();

			new SvnDiff().exportDiff(formatPath(file), formatPath(outDiffFile), headRev, prevRev);

			String exportedFilePath = null;
			if (exportFileFromRepo) {
				String exportedFileName = cName + "_" + (headRev > 0 ? headRev : "HEAD") + "." + originalFileType;
				exportedFilePath = sourFolderPath + SLASH + exportedFileName;
				if(headRev > 0 ) {
					new SvnExport()
					.verbose(settings.isVerbose())
					.overwriteFile(settings.isOverwriteFiles())
					.export(headRev, file, formatPath(exportedFilePath));
				} else {
					new SvnExport()
					.verbose(settings.isVerbose())
					.overwriteFile(settings.isOverwriteFiles())
					.exportHead(file, formatPath(exportedFilePath));
				}

			}
			if(exportedFilePath != null) {
				// Replace the configured file path with the exported file location
				settings.getAnalyzingFileDiffFileMap().remove(file);
				settings.getAnalyzingFileDiffFileMap().put(exportedFilePath, outDiffFile);
			} else {
				settings.getAnalyzingFileDiffFileMap().put(file, outDiffFile);
			}
		}
	}

	private void generateDiffFilesCommandFileDriven(CodeDiffGeneratorSettings settings, Map<String, List<SvnLog>> fileRevisionListMap) throws Exception {
		Set<String> fileList = settings.getAnalyzingFileDiffFileMap().keySet();
		List<String> resultContent = new ArrayList<String>();
		for (String rawLine : fileList) {
			String file = rawLine.contains("|") ? rawLine.substring(0, rawLine.indexOf("|")) : rawLine;
			String originalFileName = FilenameUtils.getName(rawLine);
			String originalFileType = FilenameUtils.getExtension(rawLine);
			String cName = FilenameUtils.getBaseName(rawLine);
			String exportedFileFromSvn = cName + "_HEAD." + originalFileType;

			String fileExportedFromRepo = settings.getReportOutputLocation() + "/" + exportedFileFromSvn;
			String cmdExportCommand = new SvnExport().getCommand(file, formatPath(fileExportedFromRepo));
			resultContent.add(cmdExportCommand);
			resultContent.add("echo");

			// TODO to validate with new files where there is one single revision
			if (!fileRevisionListMap.containsKey(file)) {
				continue;
			}
			long headRev = 0;
			long prevRev = 0;
			List<SvnLog> logList = fileRevisionListMap.get(file);
			if (logList.size() > 0) {
				headRev = logList.get(0).getRevision();
				prevRev = logList.get(logList.size() - 1).getRevision();
				if (prevRev > 1) {
					// Compare with the revision before the list last entry's revision number
					prevRev = prevRev - 1;
				}
			}
			String outDiffFile = settings.getReportOutputLocation() + SLASH + originalFileName + "_r" + headRev + "-r" + prevRev + ".diff";
			SvnDiff svnDiff = new SvnDiff();
			String svnDiffCommand = svnDiff.getCommand(formatPath(file), formatPath(outDiffFile), headRev, prevRev);
			resultContent.add(svnDiffCommand);
			resultContent.add("echo");

			settings.getAnalyzingFileDiffFileMap().put(file, outDiffFile);
		}
		resultContent.add("exit 0");

		// New batch file-name definition
		Integer fileCount = 1;
		// Create the code diff output folder
		String workingDir = settings.getWorkingDirPath();
		String outputFolderPath = workingDir + SLASH + CODE_DIFF_FOLDER;
		File outputFolder = new File(outputFolderPath);
		int folderCount = 0;
		while (outputFolder.exists()) {
			outputFolderPath = outputFolderPath + "-" + (++folderCount);
			outputFolder = new File(workingDir + SLASH + outputFolderPath);
		}
		outputFolder.mkdirs();

		String newFileName = workingDir + SLASH + String.format(BAT_FILENAME_TEMPLATE, fileCount);
		File newFile = new File(newFileName);
		if (StringUtils.isBlank(settings.getVersion())) {
			while (newFile.exists()) {
				fileCount++;
				newFileName = workingDir + SLASH + String.format(BAT_FILENAME_TEMPLATE, fileCount);
				newFile = new File(newFileName);
			}
		} else {
			fileCount = Integer.valueOf(settings.getVersion());
			newFileName = workingDir + SLASH + String.format(BAT_FILENAME_TEMPLATE, fileCount);
			newFile = new File(newFileName);
		}
		// resultContent.add("pause");
		if (resultContent.size() > 1) {
			if (newFile.exists()) {
				newFile.delete();
				newFile.createNewFile();
			}
			FileUtils.writeLines(newFile, resultContent);
			System.out.println("New cmd file generated: " + newFileName);
		}

		CommandLineRunner cmdRunner = new CommandLineRunner();
		cmdRunner.run(newFileName);
	}
}