package com.blogspot.jesfre.codediff.generator;

import static com.blogspot.jesfre.codediff.generator.BlockType.BLOCK_END;
import static com.blogspot.jesfre.codediff.generator.BlockType.BLOCK_INIT;
import static com.blogspot.jesfre.codediff.generator.BlockType.NO_BLOCK;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author <a href="mailto:jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Feb 5, 2024
 */
public class Diff2HtmlIncompleteOldVersion_ {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy h:m:s a");
	private static final String TEMPLATE_FOLDER = "C:/xutilities/src/com/blogspot/jesfre/batchjob/setup/codediff/resources/";
	private static final String workingDir = "/path/do/any/code-diff-generator/workingdirectory";
	private static final String javaFileLocation = workingDir + "Sample1.java";
	private static final String diffPath = workingDir + "Sample1.diff";
	

	// public static void main(String[] args) {
	// File afterChangesFile = new File(javaFileLocation);
	// File diffFile = new File(diffPath);
	// String fileName = FilenameUtils.getBaseName(diffPath);
	// File htmlFile = new File(workingDir + fileName + ".html");
	// String htmlContent = null;
	// try {
	// htmlContent = new Diff2HtmlIncompleteOldVersion_().toHtml(afterChangesFile, diffFile, javaFileLocation);
	// } catch (IOException e1) {
	// System.out.println("Cannot process file " + diffFile.getName());
	// e1.printStackTrace();
	// }
	// if (StringUtils.isBlank(htmlContent)) {
	// return;
	// }
	// try {
	// FileUtils.writeStringToFile(htmlFile, htmlContent);
	// } catch (IOException e) {
	// System.err.println("Cannot write HTML file " + htmlFile.getName());
	// e.printStackTrace();
	// }
	// }
	//
	// private String toHtml(File javaFile, File diffFile, String workspaceFileLocation) throws IOException {
	// DifferenceContent difference = getDifferences(javaFile, diffFile);
	// String producedDate = DATE_FORMAT.format(new Date());
	// String leftRev = workspaceFileLocation.replace(".java", "_" + difference.getLeftRevision() + ".java");
	// String rightRev = workspaceFileLocation.replace(".java", "_" + difference.getRightRevision() + ".java");
	//
	// Map<String, Object> context = new HashMap<String, Object>();
	// context.put("javaFileName", workspaceFileLocation);
	// context.put("produced", producedDate);
	// context.put("leftFile", leftRev);
	// context.put("rightFile", rightRev);
	// context.put("difference", difference);
	//
	// String htmlContent = getHtmlContent("diff-template.html", context);
	// return htmlContent;
	// }
	//
	// private DifferenceContent getDifferences(File javaFile, File diffFile) throws IOException {
	// DifferenceContent difference = new DifferenceContent();
	// List<String> codeLines = (List<String>) FileUtils.readLines(javaFile);
	// for (String line : codeLines) {
	// if (StringUtils.isEmpty(line)) {
	// difference.addLine(DiffType.SAME, "", "&nbsp;", "&nbsp;");
	// continue;
	// }
	// StringBuilder spaces = new StringBuilder();
	// for (int i = 0; i < line.length(); i++) {
	// char c = line.charAt(i);
	// if (c == ' ') {
	// spaces.append("&nbsp;");
	// } else if (c == '\t') {
	// spaces.append("&nbsp;&nbsp;&nbsp;&nbsp;");
	// } else {
	// break;
	// }
	// }
	// String escapedLine = StringEscapeUtils.escapeHtml(line).trim();
	// difference.addLine(DiffType.SAME, spaces.toString(), escapedLine, escapedLine);
	// }
	//
	// LineIterator iterator = IOUtils.lineIterator(new FileReader(diffFile));
	// if (!iterator.hasNext()) {
	// System.out.println("No differences found!");
	// return difference;
	// }
	// String fname = iterator.nextLine();
	// fname = fname.replace("Index: ", "");
	// difference.setFileName(fname);
	//
	// iterator.nextLine(); // Separator =====
	// String leftRev = iterator.nextLine();
	// leftRev = leftRev.substring(leftRev.indexOf("(revision ") + 10, leftRev.indexOf(")"));
	// difference.setLeftRevision(Long.valueOf(leftRev));
	//
	// String rightRev = iterator.nextLine();
	// rightRev = rightRev.substring(rightRev.indexOf("(revision ") + 10, rightRev.indexOf(")"));
	// difference.setRightRevision(Long.valueOf(rightRev));
	//
	// int blockNum = 0;
	// int leftLinePosition = 0;
	// int leftNumberOfLines = 0;
	// int rightLinePosition = 0;
	// int rightNumberOfLines = 0;
	// int maxLinesChangedInBlock = 0;
	// int nextIndexPosition = -1;
	// boolean blockStarted = false;
	// int lastDifferenceFoundAtIndex = -1;
	//
	// for (int i = 0; iterator.hasNext(); i++) {
	// String line = iterator.nextLine();
	//
	// if (line.startsWith(DiffConstants.BLOCK)) {
	// // Example where line = "@@ -1923,32 +1919,38 @@"
	// String startLineLeft = line.substring(line.indexOf('-') + 1, line.indexOf(','));
	// leftLinePosition = Integer.valueOf(startLineLeft.trim());
	//
	// String leftNumberOfLinesStr = line.substring(line.indexOf(',') + 1, line.indexOf('+'));
	// leftNumberOfLines = Integer.valueOf(leftNumberOfLinesStr.trim());
	//
	// String startLineRight = line.substring(line.indexOf('+') + 1, line.lastIndexOf(','));
	// rightLinePosition = Integer.valueOf(startLineRight.trim());
	//
	// String rightNumberOfLinesStr = line.substring(line.lastIndexOf(',') + 1, line.lastIndexOf("@@"));
	// rightNumberOfLines = Integer.valueOf(rightNumberOfLinesStr.trim());
	//
	// maxLinesChangedInBlock = leftNumberOfLines > rightNumberOfLines ? leftNumberOfLines : rightNumberOfLines;
	// nextIndexPosition = leftLinePosition - 1;
	// if (blockNum > 0 && nextIndexPosition > lastDifferenceFoundAtIndex) {
	// DifferenceLine lastDiffLine = difference.getLines().get(lastDifferenceFoundAtIndex);
	// lastDiffLine.setBlockType(BLOCK_END);
	// DifferenceLine lastInitBlock = difference.getLines().get(lastDifferenceFoundAtIndex + 1);
	// lastInitBlock.setBlockType(BLOCK_INIT);
	// lastDifferenceFoundAtIndex = nextIndexPosition;
	// }
	// if (blockNum == 0) {
	// DifferenceLine firstLine = difference.getLines().get(0);
	// if (firstLine.getBlockType() == NO_BLOCK) {
	// firstLine.setBlockType(BLOCK_INIT);
	// }
	// }
	// blockStarted = false;
	// blockNum++;
	// continue;
	// } else if (line.startsWith(DiffConstants.RIGHT) || line.startsWith(DiffConstants.LEFT)) {
	// DifferenceLine currentDiffLine = difference.getLines().get(nextIndexPosition);
	//
	// if (line.startsWith(DiffConstants.RIGHT)) {
	// currentDiffLine.setDiffType(DiffType.RIGHT);
	// currentDiffLine.setLeftText("&nbsp;");
	// } else {
	// currentDiffLine.setDiffType(DiffType.LEFT);
	// currentDiffLine.setRightText("&nbsp;");
	// }
	//
	// lastDifferenceFoundAtIndex = nextIndexPosition;
	// if (!blockStarted) {
	// blockStarted = true;
	// setBlockInitAndEnd(difference, currentDiffLine, nextIndexPosition);
	// }
	// } else {
	// // No difference
	// }
	//
	// nextIndexPosition++;
	// }
	// if (nextIndexPosition > lastDifferenceFoundAtIndex) {
	// DifferenceLine lastDiffLine = difference.getLines().get(lastDifferenceFoundAtIndex);
	// lastDiffLine.setBlockType(BLOCK_END);
	// DifferenceLine lastInitBlock = difference.getLines().get(lastDifferenceFoundAtIndex + 1);
	// lastInitBlock.setBlockType(BLOCK_INIT);
	// }
	// DifferenceLine lastLine = difference.getLines().get(difference.getLines().size() - 1);
	// if (lastLine.getBlockType() == NO_BLOCK) {
	// lastLine.setBlockType(BLOCK_END);
	// }
	//
	// return difference;
	// }
	//
	// private void setBlockInitAndEnd(DifferenceContent difference, DifferenceLine currentDiffLine, int nextIndexPosition) {
	// currentDiffLine.setBlockType(BLOCK_INIT);
	// if (nextIndexPosition >= 1) {
	// DifferenceLine prevDiffLine = difference.getLines().get(nextIndexPosition - 1);
	// prevDiffLine.setBlockType(BLOCK_END);
	// }
	// }
	//
	//
	// private String getHtmlContent(String velocityTemplate, Map<String, Object> contextParams) {
	// Properties props = new Properties();
	// props.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, TEMPLATE_FOLDER);
	//
	// VelocityEngine ve = new VelocityEngine();
	// ve.init(props);
	//
	// Template vtemplate = ve.getTemplate(velocityTemplate);
	// VelocityContext context = new VelocityContext();
	// for (Entry<String, Object> param : contextParams.entrySet()) {
	// context.put(param.getKey(), param.getValue());
	// }
	//
	// StringWriter writer = new StringWriter();
	// vtemplate.merge(context, writer);
	// return writer.toString();
	// }
}
