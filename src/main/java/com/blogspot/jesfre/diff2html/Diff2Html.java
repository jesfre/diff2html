package com.blogspot.jesfre.diff2html;

import static com.blogspot.jesfre.diff2html.DiffConstants.SLASH;
import static com.blogspot.jesfre.velocity.utils.VelocityTemplateProcessor.getProcessor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.blogspot.jesfre.svn.utils.diff.DifferenceAnalyzer;
import com.blogspot.jesfre.svn.utils.diff.DifferenceContent;
import com.blogspot.jesfre.velocity.utils.VelocityTemplateProcessor;

/**
 * @author <a href="mailto:jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Feb 5, 2024
 */
public class Diff2Html {

	public static void main(String[] args) {
		String workingDir = "/path/do/any/code-diff-generator/workingdirectory";
		String file = new Diff2Html().processDiff(workingDir,
				workingDir + "Sample.java",
				workingDir + "Sample.diff");
		System.out.println("Generated HTML: " + file);
	}

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy h:m:s a");
	private CodeDiffGeneratorSettings settings = null; 
	private String htmlFilenamePrefix = "";
	private String htmlFilenameSuffix = "";

	public Diff2Html() {
	}

	public Diff2Html(CodeDiffGeneratorSettings settings, String htmlFilenamePrefix, String htmlFilenameSuffix) {
		this.settings = settings;
		this.htmlFilenamePrefix = htmlFilenamePrefix;
		this.htmlFilenameSuffix = htmlFilenameSuffix;
	}

	public String processDiff(String workingDirPath, String svnManagedFilePath, String diffPath) {
		File afterChangesFile = new File(svnManagedFilePath);
		File diffFile = new File(diffPath);
		DifferenceContent differenceContent = null;
		try {
			differenceContent = DifferenceAnalyzer.getDifferenceContent(afterChangesFile, diffFile);
		} catch (Exception e1) {
			System.out.println("Cannot collfrom ect differences content for file " + diffFile.getName() + " and "
					+ afterChangesFile.getName());
			e1.printStackTrace();
		}

		String fileName = differenceContent.getFileName();
		String htmlFilePath = workingDirPath + SLASH + htmlFilenamePrefix + fileName + htmlFilenameSuffix + ".html";
		File htmlFile = new File(htmlFilePath);
		String htmlContent = null;
		try {
			htmlContent = this.toHtml(afterChangesFile, differenceContent, svnManagedFilePath);
		} catch (Exception e1) {
			System.out.println("Cannot generate HTML diff content for file " + diffFile.getName() + " and " + afterChangesFile.getName());
			e1.printStackTrace();
		}
		if (StringUtils.isBlank(htmlContent)) {
			return "NO HTML CONTENT";
		}
		try {
			FileUtils.writeStringToFile(htmlFile, htmlContent);
		} catch (Exception e) {
			System.err.println("Cannot write HTML file " + htmlFile.getName());
			e.printStackTrace();
		}
		return htmlFile.getAbsolutePath();
	}

	private String toHtml(File javaFile, DifferenceContent difference, String workspaceFileLocation) throws IOException {
		String producedDate = DATE_FORMAT.format(new Date());

		String fullPath = FilenameUtils.getFullPath(workspaceFileLocation);
		String baseName = FilenameUtils.getBaseName(difference.getFileName());
		String extension = FilenameUtils.getExtension(difference.getFileName());
		String leftFileName = baseName + "_" + difference.getLeftRevision() + "." + extension;
		String rightFileName = baseName + "_" + difference.getRightRevision() + "." + extension;

		String leftRevFullPath = FilenameUtils.concat(fullPath, leftFileName);
		String rightRevFullPath = FilenameUtils.concat(fullPath, rightFileName);

		String path;
		String templateFilename;
		if("DEFAULT".equals(settings.getHtmlTemplate())) {
			// TODO set default
			path = DiffConstants.TEMPLATE_FOLDER;
			templateFilename = "diff-template.html";
			// TODO Load template from JAR. Add this option in the VelocityTemplateProcessor.
		} else {
			path = FilenameUtils.getFullPath(settings.getHtmlTemplate());
			templateFilename = FilenameUtils.getName(settings.getHtmlTemplate());
			if(StringUtils.isBlank(path)) {
				// Will try to use a template file located in the same directory as the configuration file  
				path = FilenameUtils.getPath(settings.getConfigFile());
			}
		}

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("javaFileName", workspaceFileLocation);
		context.put("produced", producedDate);
		context.put("leftFile", leftRevFullPath);
		context.put("rightFile", rightRevFullPath);
		context.put("difference", difference);

		VelocityTemplateProcessor templateProcessor = getProcessor(path);
		String htmlContent = templateProcessor.process(templateFilename, context);
		return htmlContent;
	}

}