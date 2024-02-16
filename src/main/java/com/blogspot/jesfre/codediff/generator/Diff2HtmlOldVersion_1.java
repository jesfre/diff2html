package com.blogspot.jesfre.codediff.generator;

import static com.blogspot.jesfre.codediff.generator.BlockType.BLOCK_END;
import static com.blogspot.jesfre.codediff.generator.BlockType.BLOCK_INIT;
import static com.blogspot.jesfre.codediff.generator.BlockType.NO_BLOCK;
import static com.blogspot.jesfre.codediff.generator.BlockType.SINGLE_LINE;
import static com.blogspot.jesfre.codediff.generator.DiffConstants.BLANK_SP;
import static com.blogspot.jesfre.codediff.generator.DiffConstants.TAB_SPS;
import static com.blogspot.jesfre.codediff.generator.DiffType.BOTH;
import static com.blogspot.jesfre.codediff.generator.DiffType.LEFT;
import static com.blogspot.jesfre.codediff.generator.DiffType.RIGHT;

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
public class Diff2HtmlOldVersion_1 {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy h:m:s a");

	// public static void main(String[] args) {
	// String workingDir = "/path/do/any/code-diff-generator/workingdirectory";
	// String file = new Diff2HtmlOldVersion_1().processDiff(workingDir,
	// workingDir + "Sample1.java",
	// workingDir + "Sample1.diff");
	// System.out.println("Generated HTML: " + file);
	// }
	//
	// public String processDiff(String workingDirPath, String javaFileLocation, String diffPath) {
	// File afterChangesFile = new File(javaFileLocation);
	// File diffFile = new File(diffPath);
	// String fileName = FilenameUtils.getBaseName(diffPath);
	// File htmlFile = new File(workingDirPath + fileName + ".html");
	// String htmlContent = null;
	// try {
	// htmlContent = new Diff2HtmlOldVersion_1().toHtml(afterChangesFile, diffFile, javaFileLocation);
	// } catch (IOException e1) {
	// System.out.println("Cannot process file " + diffFile.getName());
	// e1.printStackTrace();
	// }
	// if (StringUtils.isBlank(htmlContent)) {
	// return "NO HTML CONTENT";
	// }
	// try {
	// FileUtils.writeStringToFile(htmlFile, htmlContent);
	// } catch (IOException e) {
	// System.err.println("Cannot write HTML file " + htmlFile.getName());
	// e.printStackTrace();
	// }
	// return htmlFile.getAbsolutePath();
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
	// for (String codeLine : codeLines) {
	// String spaces = formatSpaces(codeLine);
	// String escapedLine = escapeText(codeLine);
	// difference.addLine(DiffType.SAME, spaces, escapedLine, escapedLine);
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
	// int startIndexLastDiff = -1;
	// DiffType lastDiffTypeProcessed = DiffType.SAME;
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
	//
	// // Delete the block (all lines) using the values for left side (which is the file we are working on)
	// // System.out.println("Removing block");
	// // for (int j = nextIndexPosition; j < leftNumberOfLines; j++) {
	// // DifferenceLine removed = difference.getLines().remove(j);
	// // System.out.println(removed.getLeft());
	// // }
	//
	// blockNum++;
	// continue;
	// }
	//
	// String noDiffSymbolLine = line;
	// if (line.length() > 0) {
	// noDiffSymbolLine = line.substring(1);
	// }
	//
	// String spaces = formatSpaces(noDiffSymbolLine);
	// String escapedLine = escapeText(noDiffSymbolLine);
	// if (line.startsWith(DiffConstants.RIGHT) || line.startsWith(DiffConstants.LEFT)) {
	//
	// if (line.startsWith(DiffConstants.LEFT)) {
	//
	// if (startIndexLastDiff == -1) {
	// startIndexLastDiff = nextIndexPosition;
	// }
	//
	// if (lastDiffTypeProcessed != DiffType.LEFT) {
	// lastDiffTypeProcessed = DiffType.LEFT;
	// nextIndexPosition = startIndexLastDiff;
	// }
	//
	// DifferenceLine existingDiffLine = difference.getLines().get(nextIndexPosition);
	// if (existingDiffLine.getDiffType() == DiffType.RIGHT) {
	// existingDiffLine.setLeftText(escapedLine);
	// existingDiffLine.setDiffType(DiffType.BOTH);
	// } else {
	// difference.getLines().add(nextIndexPosition, new DifferenceLine(NO_BLOCK, DiffType.LEFT, spaces, escapedLine, BLANK_SP));
	// }
	// } else {
	//
	// if (startIndexLastDiff == -1) {
	// startIndexLastDiff = nextIndexPosition;
	// }
	//
	// if (lastDiffTypeProcessed != DiffType.RIGHT) {
	// lastDiffTypeProcessed = DiffType.RIGHT;
	// nextIndexPosition = startIndexLastDiff;
	// }
	//
	// DifferenceLine existingDiffLine = difference.getLines().get(nextIndexPosition);
	// if (existingDiffLine.getDiffType() == DiffType.LEFT) {
	// existingDiffLine.setRightText(escapedLine);
	// existingDiffLine.setDiffType(DiffType.BOTH);
	// } else {
	// difference.getLines().add(nextIndexPosition, new DifferenceLine(NO_BLOCK, DiffType.RIGHT, spaces, BLANK_SP, escapedLine));
	// }
	// }
	//
	// } else {
	// // No difference
	// lastDiffTypeProcessed = DiffType.SAME;
	// BlockType newBlockType = NO_BLOCK;
	// if (startIndexLastDiff > -1) {
	// startIndexLastDiff = -1;
	//
	// DifferenceLine prevLine = difference.getLines().get(nextIndexPosition - 1);
	// prevLine.setBlockType(BLOCK_END);
	// newBlockType = BLOCK_INIT;
	// }
	// difference.getLines().add(nextIndexPosition, new DifferenceLine(newBlockType, DiffType.SAME, spaces, escapedLine, escapedLine));
	// }
	//
	// nextIndexPosition++;
	// }
	//
	// updateBlockTypes(difference);
	// return difference;
	// }
	//
	// private void updateBlockTypes(DifferenceContent difference) {
	// // Set first line of the file as BLOCK_INIT
	// DifferenceLine firstLine = difference.getLines().get(0);
	// if (firstLine.getBlockType() == NO_BLOCK) {
	// firstLine.setBlockType(BLOCK_INIT);
	// }
	//
	// DifferenceLine lastLine = difference.getLines().get(difference.getLines().size() - 1);
	// if (lastLine.getBlockType() == NO_BLOCK) {
	// lastLine.setBlockType(BLOCK_END);
	// }
	//
	// // Update the type of block for all blocks of differences
	// DiffType tempCurrentType = DiffType.SAME;
	// DiffType tempPrevType = DiffType.SAME;
	// for (int i = 0; i < difference.getLines().size(); i++) {
	// DifferenceLine prevDiff = null;
	// if (i > 0) {
	// prevDiff = difference.getLines().get(i - 1);
	// tempPrevType = prevDiff.getDiffType();
	// if (tempPrevType == LEFT || tempPrevType == RIGHT || tempPrevType == BOTH) {
	// tempPrevType = DiffType.BOTH;
	// }
	// }
	//
	// DifferenceLine diff = difference.getLines().get(i);
	// tempCurrentType = diff.getDiffType();
	// if (tempCurrentType == LEFT || tempCurrentType == RIGHT || tempCurrentType == BOTH) {
	// tempCurrentType = DiffType.BOTH;
	// }
	//
	// if (tempCurrentType != tempPrevType) {
	// diff.setBlockType(BLOCK_INIT);
	//
	// if (prevDiff != null) {
	// if (prevDiff.getBlockType() == BLOCK_INIT) {
	// prevDiff.setBlockType(SINGLE_LINE);
	// } else {
	// prevDiff.setBlockType(BLOCK_END);
	// }
	// }
	// }
	// }
	// }
	//
	// private String formatSpaces(String text) {
	// if (StringUtils.isBlank(text)) {
	// return BLANK_SP;
	// }
	// // Replace spaces
	// StringBuilder spaces = new StringBuilder();
	// for (int i = 0; i < text.length(); i++) {
	// char c = text.charAt(i);
	// if (c == ' ') {
	// spaces.append(BLANK_SP);
	// } else if (c == '\t') {
	// spaces.append(TAB_SPS);
	// } else {
	// break;
	// }
	// }
	// return spaces.toString();
	// }
	//
	// private String escapeText(String text) {
	// if (StringUtils.isEmpty(text)) {
	// return BLANK_SP;
	// }
	// return StringEscapeUtils.escapeHtml(text).trim();
	// }
	//
	// private String getHtmlContent(String velocityTemplate, Map<String, Object> contextParams) {
	// Properties props = new Properties();
	// props.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, DiffConstants.TEMPLATE_FOLDER);
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
