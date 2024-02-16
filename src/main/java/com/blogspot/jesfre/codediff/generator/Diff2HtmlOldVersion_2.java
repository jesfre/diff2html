package com.blogspot.jesfre.codediff.generator;

import java.text.SimpleDateFormat;

/**
 * @author <a href="mailto:jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Feb 5, 2024
 */
public class Diff2HtmlOldVersion_2 {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy h:m:s a");

	// public static void main(String[] args) {
	// String workingDir = "/path/do/any/code-diff-generator/workingdirectory";
	// String file = new Diff2HtmlOldVersion_2().processDiff(workingDir,
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
	// htmlContent = new Diff2HtmlOldVersion_2().toHtml(afterChangesFile, diffFile, javaFileLocation);
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
	// VelocityTemplateProcessorUtil templateProcessor = getProcessor(DiffConstants.TEMPLATE_FOLDER);
	// String htmlContent = templateProcessor.process("diff-template.html", context);
	// return htmlContent;
	// }
	//
	// private DifferenceContent getDifferences(File javaFile, File diffFile) throws IOException {
	// DifferenceContent difference = new DifferenceContent();
	// List<String> originalCodeLines = (List<String>) FileUtils.readLines(javaFile);
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
	// int maxLeftIndexProcessedInPrevBlock = 0;
	// int currentIndex = 0;
	// int startIndexLastDiff = -1;
	// DiffType lastDiffTypeProcessed = DiffType.SAME;
	//
	// while (iterator.hasNext()) {
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
	// // Add unchanged block from original code
	// for (int i = maxLeftIndexProcessedInPrevBlock; i < leftLinePosition - 1; i++) {
	// String codeLine = originalCodeLines.get(i);
	// String spaces = formatIndentation(codeLine);
	// String escapedLine = escapeText(codeLine);
	// difference.addLine(DiffType.SAME, spaces, escapedLine, escapedLine);
	// }
	//
	// // Set new positions
	// currentIndex = leftLinePosition - 1;
	// maxLeftIndexProcessedInPrevBlock = currentIndex + leftNumberOfLines;
	//
	// blockNum++;
	// continue;
	// }
	//
	// if (line.contains("boolean isResintatement = false;")) {
	// System.out.println("");
	// }
	//
	// String noDiffSymbolLine = line;
	// if (line.length() > 0) {
	// noDiffSymbolLine = line.substring(1);
	// }
	//
	// String indentation = formatIndentation(noDiffSymbolLine);
	// String escapedLine = escapeText(noDiffSymbolLine);
	// if (line.startsWith(DiffConstants.RIGHT) || line.startsWith(DiffConstants.LEFT)) {
	//
	// if (startIndexLastDiff == -1) {
	// startIndexLastDiff = currentIndex;
	// }
	//
	// if (line.startsWith(DiffConstants.LEFT)) {
	//
	// if (lastDiffTypeProcessed == DiffType.RIGHT) {
	// currentIndex = startIndexLastDiff;
	// }
	// lastDiffTypeProcessed = DiffType.LEFT;
	//
	// if (difference.getLines().size() - 1 > currentIndex) {
	// DifferenceLine existingDiffLine = difference.getLines().get(currentIndex);
	// if (existingDiffLine.getDiffType() == DiffType.RIGHT) {
	// existingDiffLine.setDiffType(DiffType.BOTH);
	// existingDiffLine.setLeftText(escapedLine);
	// }
	// } else {
	// difference.addLine(DiffType.LEFT, indentation, escapedLine, BLANK_SP);
	// }
	// } else {
	//
	// if (lastDiffTypeProcessed == DiffType.LEFT) {
	// currentIndex = startIndexLastDiff;
	// }
	// lastDiffTypeProcessed = DiffType.RIGHT;
	//
	// // TODO probably needs to add new counter to hold the right index position
	// if (difference.getLines().size() - 1 > currentIndex) {
	// DifferenceLine existingDiffLine = difference.getLines().get(currentIndex);
	// if (existingDiffLine.getDiffType() == DiffType.LEFT) {
	// existingDiffLine.setDiffType(DiffType.BOTH);
	// existingDiffLine.setRightText(escapedLine);
	// }
	// } else {
	// difference.addLine(DiffType.RIGHT, indentation, BLANK_SP, escapedLine);
	// }
	// }
	// currentIndex++;
	//
	// } else {
	// // No difference
	// lastDiffTypeProcessed = DiffType.SAME;
	// if (startIndexLastDiff > -1) {
	// startIndexLastDiff = -1;
	// }
	// difference.addLine(DiffType.SAME, indentation, escapedLine, escapedLine);
	// currentIndex = difference.getLines().size() - 1;
	// }
	//
	//
	// }
	//
	// // Add unchanged last block from original code
	// for (int j = maxLeftIndexProcessedInPrevBlock; j < originalCodeLines.size(); j++) {
	// String codeLine = originalCodeLines.get(j);
	// String spaces = formatIndentation(codeLine);
	// String escapedLine = escapeText(codeLine);
	// difference.addLine(DiffType.SAME, spaces, escapedLine, escapedLine);
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
	// private String formatIndentation(String text) {
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

}
