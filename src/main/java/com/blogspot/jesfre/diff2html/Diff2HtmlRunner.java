package com.blogspot.jesfre.diff2html;

import static com.blogspot.jesfre.diff2html.DiffConstants.CODE_DIFF_FOLDER;
import static com.blogspot.jesfre.diff2html.DiffConstants.SLASH;
import static com.blogspot.jesfre.misc.PathUtils.formatPath;
import static com.blogspot.jesfre.svn.utils.SvnLogExtractor.CommandExecutionMode.COMMAND_FILE;
import static com.blogspot.jesfre.svn.utils.SvnLogExtractor.CommandExecutionMode.DIRECT_COMMAND;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.blogspot.jesfre.commandline.CommandLineRunner;
import com.blogspot.jesfre.svn.utils.SvnDiff;
import com.blogspot.jesfre.svn.utils.SvnExport;
import com.blogspot.jesfre.svn.utils.SvnLog;
import com.blogspot.jesfre.svn.utils.SvnLogExtractor;

public class Diff2HtmlRunner {

	// ----------- SET -----------------------
	private static final String SETUP_FILE = "C:/xutilities/src/com/blogspot/jesfre/diff2html/resources/Diff2HtmlRunner_setup.txt";
	private static final String BAT_FILENAME_TEMPLATE = "svn_diff_commands_%s.bat";
	private static final String SVN_BASE_URL = "http://ussltccsw2601.solutions.glbsnet.com/svn/IES_WP/branches/IES_BATCH/Code/";
	private static final String DEFAULT_HTML_NAME_PREFIX = "";
	private static final String DEFAULT_HTML_NAME_SUFFIX = "_Code_Diff";
	private static boolean cmdFileBasedExecution = false;

	private String workingDir = "";

	public static void main(String[] args) throws Exception {
		// Load configuration and generates .bat file for svn commands
		System.out.println("Loading Diff2Html Runner settings...");
		Diff2HtmlRunner diff2HtmlRunner = new Diff2HtmlRunner();
		CodeDiffGeneratorSettings runnerSettings = diff2HtmlRunner.loadSettingsFile(SETUP_FILE);

		// Run svn command to get list-of-revisions or latest two commit revisions
		System.out.println("Getting files revisions...");
		Map<String, List<SvnLog>> fileSvnLogMap = new HashMap<String, List<SvnLog>>();
		SvnLogExtractor versionExtractor = new SvnLogExtractor("TBD", runnerSettings.getReportOutputLocation());
		for (String javaFile : runnerSettings.getAnalyzingFileDiffFileMap().keySet()) {
			List<SvnLog> logList = versionExtractor
					.withLimit(2)
					.withExecutionMode(cmdFileBasedExecution ? COMMAND_FILE : DIRECT_COMMAND)
					.analyze(javaFile).extract();
			fileSvnLogMap.put(javaFile, logList);
		}
		
		// Execute svn commands to get .java and .diff files using latest two revisions
		System.out.println("Generating SVN diff commands...");
		if (cmdFileBasedExecution) {
			diff2HtmlRunner.generateDiffFilesCommandFileDriven(runnerSettings, fileSvnLogMap);
		} else {
			diff2HtmlRunner.generateDiffFiles(runnerSettings, fileSvnLogMap);
		}

		// Generate HTML files
		System.out.println("Generating HTML diff files...");
		for (Entry<String, String> e : runnerSettings.getAnalyzingFileDiffFileMap().entrySet()) {
			new Diff2Html(DEFAULT_HTML_NAME_PREFIX, DEFAULT_HTML_NAME_SUFFIX + "-v" + runnerSettings.getVersion())
					.processDiff(diff2HtmlRunner.workingDir, e.getKey(), e.getValue());
		}
		System.out.println("Done.");
	}

	public CodeDiffGeneratorSettings loadSettingsFile(String setupFileLocation) throws Exception {
		// TODO buffer the file reading to avoid out-of-memory errors
		List<String> lines = FileUtils.readLines(new File(setupFileLocation));
		String project = null;
		String ticket = null;
		String version = null;
		int lnNum = 0;
		if (lines.size() > 0) {
			project = StringUtils.remove(lines.get(lnNum++), "PROJECT:");
			version = StringUtils.remove(lines.get(lnNum++), "JIRA_TICKET:");
			version = StringUtils.remove(lines.get(lnNum++), "VERSION:");
			workingDir = StringUtils.remove(lines.get(lnNum++), "WORKING_DIRECTORY:");
		} else {
			throw new Exception("Setup file is empty.");
		}

		String outputFolderPath = workingDir + SLASH + CODE_DIFF_FOLDER;
		CodeDiffGeneratorSettings settings = new CodeDiffGeneratorSettings();
		settings.setProject(project);
		settings.setJiraTicket(ticket);
		settings.setVersion(version);
		settings.setWorkingDirPath(workingDir);
		// settings.setCommandFile(newFileName); // No command to be generated at this point
		settings.setReportOutputLocation(outputFolderPath);

		// Create content for .bat file
		System.out.println("Reading files...");
		int filesFound = 0;
		lnNum++; // To skip FILE_LIST_BELOW
		for (; lnNum < lines.size(); lnNum++) {
			String f = lines.get(lnNum);
			if (f.startsWith("#") || f.startsWith("//") || StringUtils.isBlank(f)) {
				continue;
			}
			String file = f.contains("|") ? f.substring(0, f.indexOf("|")) : f;
			// TODO handle revision
			String revision = f.contains("|") ? f.substring(f.indexOf("|") + 1) : "";
			settings.putAnalyzingFile(file);
			filesFound++;
		}
		System.out.println("Files found: " + filesFound);

		return settings;
	}

	private void generateDiffFiles(CodeDiffGeneratorSettings settings, Map<String, List<SvnLog>> fileRevisionListMap) throws Exception {
		String codeDiffOutputFolder = settings.getWorkingDirPath() + SLASH + CODE_DIFF_FOLDER;
		Set<String> fileList = settings.getAnalyzingFileDiffFileMap().keySet();
		for (String rawLine : fileList) {
			String file = rawLine.contains("|") ? rawLine.substring(0, rawLine.indexOf("|")) : rawLine;
			String originalFileName = FilenameUtils.getName(rawLine);
			String originalFileType = FilenameUtils.getExtension(rawLine);
			String cName = FilenameUtils.getBaseName(rawLine);
			String exportedFileFromSvn = cName + "_HEAD." + originalFileType;

			String fileExportedFromRepo = codeDiffOutputFolder + "/" + exportedFileFromSvn;
			new SvnExport().export(file, formatPath(fileExportedFromRepo));

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
			}
			String outDiffFile = codeDiffOutputFolder + SLASH + originalFileName + "_r" + headRev + "-r" + prevRev + ".diff";
			new SvnDiff().exportDiff(formatPath(file), formatPath(outDiffFile), headRev, prevRev);

			settings.getAnalyzingFileDiffFileMap().put(file, outDiffFile);
		}
	}

	private void generateDiffFilesCommandFileDriven(CodeDiffGeneratorSettings settings, Map<String, List<SvnLog>> fileRevisionListMap) throws Exception {
		String codeDiffOutputFolder = settings.getWorkingDirPath() + SLASH + CODE_DIFF_FOLDER;
		Set<String> fileList = settings.getAnalyzingFileDiffFileMap().keySet();
		List<String> resultContent = new ArrayList<String>();
		for (String rawLine : fileList) {
			String file = rawLine.contains("|") ? rawLine.substring(0, rawLine.indexOf("|")) : rawLine;
			String revision = rawLine.contains("|") ? rawLine.substring(rawLine.indexOf("|") + 1) : "";
			String originalFileName = FilenameUtils.getName(rawLine);
			String originalFileType = FilenameUtils.getExtension(rawLine);
			String cName = FilenameUtils.getBaseName(rawLine);
			String exportedFileFromSvn = cName + "_HEAD." + originalFileType;
			String urlFile = SVN_BASE_URL + file;

			String fileExportedFromRepo = codeDiffOutputFolder + "/" + exportedFileFromSvn;
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
			}
			if (logList.size() > 1) {
				prevRev = logList.get(1).getRevision();
			}
			String outDiffFile = codeDiffOutputFolder + SLASH + originalFileName + "_r" + headRev + "-r" + prevRev + ".diff";
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