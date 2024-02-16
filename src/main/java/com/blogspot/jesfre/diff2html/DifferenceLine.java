package com.blogspot.jesfre.diff2html;

public class DifferenceLine {
	private BlockType blockType = BlockType.NO_BLOCK;
	private DiffType diffType = DiffType.SAME;
	private String leftIndentation;
	private String rightIndentation;
	private String leftText;
	private String rightText;

	DifferenceLine(BlockType blockType, DiffType diffType, String leftIndentation, String leftText, String rightIndentation, String rightText) {
		this.blockType = blockType;
		this.diffType = diffType;
		this.leftIndentation = leftIndentation;
		this.leftText = leftText;
		this.rightIndentation = rightIndentation;
		this.rightText = rightText;
	}

	public BlockType getBlockType() {
		return blockType;
	}

	public void setBlockType(BlockType blockType) {
		this.blockType = blockType;
	}

	public DiffType getDiffType() {
		return diffType;
	}

	public void setDiffType(DiffType type) {
		this.diffType = type;
	}

	public String getLeftIndentation() {
		return leftIndentation;
	}

	public void setLeftIndentation(String spaces) {
		this.leftIndentation = spaces;
	}

	public String getRightIndentation() {
		return rightIndentation;
	}

	public void setRightIndentation(String rightIndentation) {
		this.rightIndentation = rightIndentation;
	}

	public String getLeftText() {
		return leftText;
	}

	public void setLeftText(String left) {
		this.leftText = left;
	}

	public String getRightText() {
		return rightText;
	}

	public void setRightText(String right) {
		this.rightText = right;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DifferenceLine [blockType=");
		builder.append(blockType);
		builder.append(", diffType=");
		builder.append(diffType);
		builder.append(", leftIndentation=");
		builder.append(leftIndentation);
		builder.append(", rightIndentation=");
		builder.append(rightIndentation);
		builder.append(", leftText=");
		builder.append(leftText);
		builder.append(", rightText=");
		builder.append(rightText);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockType == null) ? 0 : blockType.hashCode());
		result = prime * result + ((diffType == null) ? 0 : diffType.hashCode());
		result = prime * result + ((leftIndentation == null) ? 0 : leftIndentation.hashCode());
		result = prime * result + ((leftText == null) ? 0 : leftText.hashCode());
		result = prime * result + ((rightIndentation == null) ? 0 : rightIndentation.hashCode());
		result = prime * result + ((rightText == null) ? 0 : rightText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DifferenceLine other = (DifferenceLine) obj;
		if (blockType != other.blockType)
			return false;
		if (diffType != other.diffType)
			return false;
		if (leftIndentation == null) {
			if (other.leftIndentation != null)
				return false;
		} else if (!leftIndentation.equals(other.leftIndentation))
			return false;
		if (leftText == null) {
			if (other.leftText != null)
				return false;
		} else if (!leftText.equals(other.leftText))
			return false;
		if (rightIndentation == null) {
			if (other.rightIndentation != null)
				return false;
		} else if (!rightIndentation.equals(other.rightIndentation))
			return false;
		if (rightText == null) {
			if (other.rightText != null)
				return false;
		} else if (!rightText.equals(other.rightText))
			return false;
		return true;
	}

}
