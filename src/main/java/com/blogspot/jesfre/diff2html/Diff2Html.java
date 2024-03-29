package com.blogspot.jesfre.diff2html;

import static com.blogspot.jesfre.diff2html.BlockType.BLOCK_END;
import static com.blogspot.jesfre.diff2html.BlockType.BLOCK_INIT;
import static com.blogspot.jesfre.diff2html.BlockType.NO_BLOCK;
import static com.blogspot.jesfre.diff2html.BlockType.SINGLE_LINE;
import static com.blogspot.jesfre.diff2html.DiffConstants.BLANK_SP;
import static com.blogspot.jesfre.diff2html.DiffConstants.SLASH;
import static com.blogspot.jesfre.diff2html.DiffConstants.TAB_SPS;
import static com.blogspot.jesfre.diff2html.DiffType.BOTH;
import static com.blogspot.jesfre.diff2html.DiffType.LEFT;
import static com.blogspot.jesfre.diff2html.DiffType.RIGHT;
import static com.blogspot.jesfre.velocity.utils.VelocityTemplateProcessor.getProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

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
		String fileName = FilenameUtils.getName(svnManagedFilePath);
		// TODO generate new filename, if Java, remove extension, otherwise don't
		String htmlFilePath = workingDirPath + SLASH + htmlFilenamePrefix + fileName + htmlFilenameSuffix + ".html";
		File htmlFile = new File(htmlFilePath);
		String htmlContent = null;
		try {
			htmlContent = this.toHtml(afterChangesFile, diffFile, svnManagedFilePath);
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

	private String toHtml(File javaFile, File diffFile, String workspaceFileLocation) throws IOException {
		DifferenceContent difference = populateDifferences(javaFile, diffFile);
		String producedDate = DATE_FORMAT.format(new Date());
		// TODO change the ".java". Use FileNameUtils.getExtension() and other logic instead of lokking specifically for Java files
		String leftRev = workspaceFileLocation.replace(".java", "_" + difference.getLeftRevision() + ".java");
		String rightRev = workspaceFileLocation.replace(".java", "_" + difference.getRightRevision() + ".java");

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
		context.put("leftFile", leftRev);
		context.put("rightFile", rightRev);
		context.put("difference", difference);

		VelocityTemplateProcessor templateProcessor = getProcessor(path);
		String htmlContent = templateProcessor.process(templateFilename, context);
		return htmlContent;
	}

	private DifferenceContent populateDifferences(File javaFile, File diffFile) throws IOException {
		DifferenceContent difference = new DifferenceContent();
		List<String> originalCodeLines = (List<String>) FileUtils.readLines(javaFile);

		LineIterator iterator = IOUtils.lineIterator(new FileReader(diffFile));
		if (!iterator.hasNext()) {
			System.out.println("No differences found in file " + diffFile.getName());
			return difference;
		}
		String fname = iterator.nextLine();
		fname = fname.replace("Index: ", "");
		difference.setFileName(fname);

		iterator.nextLine(); // Separator =====
		String leftRev = iterator.nextLine();
		leftRev = leftRev.substring(leftRev.indexOf("(revision ") + 10, leftRev.indexOf(")"));
		difference.setLeftRevision(Long.valueOf(leftRev));

		String rightRev = iterator.nextLine();
		if(rightRev.trim().endsWith("(nonexistent)")) {
			// Is first revision of the file
			difference.setRightRevision(Long.valueOf(leftRev));
		} else {
			rightRev = rightRev.substring(rightRev.indexOf("(revision ") + 10, rightRev.indexOf(")"));
			difference.setRightRevision(Long.valueOf(rightRev));
		}


		int blockNum = 0;
		int leftLinePosition = 0;
		int leftNumberOfLines = 0;
		int rightLinePosition = 0;
		int rightNumberOfLines = 0;
		int maxLeftIndexProcessedInPrevBlock = 0;

		List<String> leftStringList = new ArrayList<String>();
		List<String> rightStringList = new ArrayList<String>();
		while (iterator.hasNext()) {
			String line = iterator.nextLine();

			if (line.startsWith(DiffConstants.BLOCK)) {
				// Example where line = "@@ -1923,32 +1919,38 @@"
				String startLineLeft = line.substring(line.indexOf('-') + 1, line.indexOf(','));
				leftLinePosition = Integer.valueOf(startLineLeft.trim());

				String leftNumberOfLinesStr = line.substring(line.indexOf(',') + 1, line.indexOf('+'));
				leftNumberOfLines = Integer.valueOf(leftNumberOfLinesStr.trim());

				String startLineRight = line.substring(line.indexOf('+') + 1, line.lastIndexOf(','));
				rightLinePosition = Integer.valueOf(startLineRight.trim());

				String rightNumberOfLinesStr = line.substring(line.lastIndexOf(',') + 1, line.lastIndexOf("@@"));
				rightNumberOfLines = Integer.valueOf(rightNumberOfLinesStr.trim());

				populateDifferenceContentBlock(difference, leftStringList, rightStringList);
				leftStringList.clear();
				rightStringList.clear();

				// Add unchanged block from original code
				for (int i = maxLeftIndexProcessedInPrevBlock; i < leftLinePosition - 1; i++) {
					String codeLine = originalCodeLines.get(i);
					String spaces = formatIndentation(codeLine);
					String escapedLine = escapeText(codeLine);
					difference.addLine(DiffType.SAME, spaces, escapedLine, spaces, escapedLine);
				}

				maxLeftIndexProcessedInPrevBlock = leftLinePosition - 1 + leftNumberOfLines;
				blockNum++;
				continue;
			}

			if (line.startsWith(DiffConstants.LEFT)) {
				leftStringList.add(line);
				rightStringList.add("");
			} else if (line.startsWith(DiffConstants.RIGHT)) {
				leftStringList.add("");
				rightStringList.add(line);
			} else {
				// No difference
				leftStringList.add(line);
				rightStringList.add(line);
			}
		}

		// Remaining strings
		populateDifferenceContentBlock(difference, leftStringList, rightStringList);
		leftStringList.clear();
		rightStringList.clear();

		// Add unchanged last block from original code
		for (int j = maxLeftIndexProcessedInPrevBlock; j < originalCodeLines.size(); j++) {
			String codeLine = originalCodeLines.get(j);
			String spaces = formatIndentation(codeLine);
			String escapedLine = escapeText(codeLine);
			difference.addLine(DiffType.SAME, spaces, escapedLine, spaces, escapedLine);
		}

		updateBlockTypes(difference);
		return difference;
	}

	private void populateDifferenceContentBlock(DifferenceContent difference, List<String> leftStringList, List<String> rightStringList) {
		int lastDiffIndex = -1;
		int maxBlockSize = leftStringList.size() > rightStringList.size() ? leftStringList.size() : rightStringList.size();
		for (int i = 0; i < maxBlockSize; i++) {
			String leftString = BLANK_SP;
			String rightString = BLANK_SP;
			String leftIndent = BLANK_SP;
			String rightIndent = BLANK_SP;
			String originalLeftText = "";
			String originalRightText = "";

			if (i < leftStringList.size()) {
				originalLeftText = leftStringList.get(i);
				String noDiffSymbolLine = originalLeftText;
				if (noDiffSymbolLine.length() > 0) {
					noDiffSymbolLine = noDiffSymbolLine.substring(1);
				}
				leftString = escapeText(noDiffSymbolLine);
				leftIndent = formatIndentation(noDiffSymbolLine);
			}

			if (i < rightStringList.size()) {
				originalRightText = rightStringList.get(i);
				String noDiffSymbolLine = originalRightText;
				if (noDiffSymbolLine.length() > 0) {
					noDiffSymbolLine = noDiffSymbolLine.substring(1);
				}
				rightString = escapeText(noDiffSymbolLine);
				rightIndent = formatIndentation(noDiffSymbolLine);
			}

			DiffType diffType = DiffType.SAME;
			if (StringUtils.isNotBlank(originalLeftText)
					&& StringUtils.isNotBlank(originalRightText)) {
				if (!originalLeftText.equals(originalRightText)) {
					diffType = BOTH;
				}
			} else if (StringUtils.isNotBlank(originalLeftText)) {
				diffType = LEFT;
				lastDiffIndex = i;
			} else if (StringUtils.isNotBlank(originalRightText)) {
				diffType = RIGHT;
			} else {
				lastDiffIndex = -1;
			}

			if (diffType == RIGHT && lastDiffIndex >= 0) {
				// This should be BOTH. Move this RIGHT lines above.
				DifferenceLine prevLine = null;
				for (int j = difference.getLines().size() - 1; j >= lastDiffIndex; j--) {
					DifferenceLine tempPrevious = difference.getLines().get(j);
					if (tempPrevious.getDiffType() == BOTH) {
						diffType = BOTH;
						break;
					} else if (tempPrevious.getDiffType() != LEFT) {
						break;
					}
					prevLine = tempPrevious;
					// prevLine.setDiffType(BOTH);
				}
				if (prevLine != null) {
					prevLine.setDiffType(BOTH);
					prevLine.setRightIndentation(rightIndent);
					prevLine.setRightText(rightString);
				} else {
					difference.addLine(diffType, leftIndent, leftString, rightIndent, rightString);
				}
			} else {
				difference.addLine(diffType, leftIndent, leftString, rightIndent, rightString);
			}
		}
	}

	private void updateBlockTypes(DifferenceContent difference) {
		// Set first line of the file as BLOCK_INIT
		DifferenceLine firstLine = difference.getLines().get(0);
		if (firstLine.getBlockType() == NO_BLOCK) {
			firstLine.setBlockType(BLOCK_INIT);
		}

		DifferenceLine lastLine = difference.getLines().get(difference.getLines().size() - 1);
		if (lastLine.getBlockType() == NO_BLOCK) {
			lastLine.setBlockType(BLOCK_END);
		}

		// Update the type of block for all blocks of differences
		DiffType tempCurrentType = DiffType.SAME;
		DiffType tempPrevType = DiffType.SAME;
		for (int i = 0; i < difference.getLines().size(); i++) {
			DifferenceLine prevDiff = null;
			if (i > 0) {
				prevDiff = difference.getLines().get(i - 1);
				tempPrevType = prevDiff.getDiffType();
				if (tempPrevType == LEFT || tempPrevType == RIGHT || tempPrevType == BOTH) {
					tempPrevType = DiffType.BOTH;
				}
			}

			DifferenceLine diff = difference.getLines().get(i);
			tempCurrentType = diff.getDiffType();
			if (tempCurrentType == LEFT || tempCurrentType == RIGHT || tempCurrentType == BOTH) {
				tempCurrentType = DiffType.BOTH;
			}

			if (tempCurrentType != tempPrevType) {
				diff.setBlockType(BLOCK_INIT);

				if (prevDiff != null) {
					if (prevDiff.getBlockType() == BLOCK_INIT) {
						prevDiff.setBlockType(SINGLE_LINE);
					} else {
						prevDiff.setBlockType(BLOCK_END);
					}
				}
			}
		}
	}

	private String formatIndentation(String text) {
		if (StringUtils.isBlank(text)) {
			return BLANK_SP;
		}
		// Replace spaces
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				spaces.append(BLANK_SP);
			} else if (c == '\t') {
				spaces.append(TAB_SPS);
			} else {
				break;
			}
		}
		return spaces.toString();
	}

	private String escapeText(String text) {
		if (StringUtils.isEmpty(text)) {
			return BLANK_SP;
		}
		return StringEscapeUtils.escapeHtml(text).trim();
	}

}