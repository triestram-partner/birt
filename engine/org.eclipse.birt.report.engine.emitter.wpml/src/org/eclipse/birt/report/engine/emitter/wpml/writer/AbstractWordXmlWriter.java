/*******************************************************************************
 * Copyright (c) 2008,2009 Actuate Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.wpml.writer;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.engine.StyleConstants;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.eclipse.birt.report.engine.emitter.XMLWriter;
import org.eclipse.birt.report.engine.emitter.wpml.AbstractEmitterImpl;
import org.eclipse.birt.report.engine.emitter.wpml.AbstractEmitterImpl.TextFlag;
import org.eclipse.birt.report.engine.emitter.wpml.DiagonalLineInfo;
import org.eclipse.birt.report.engine.emitter.wpml.DiagonalLineInfo.Line;
import org.eclipse.birt.report.engine.emitter.wpml.HyperlinkInfo;
import org.eclipse.birt.report.engine.emitter.wpml.SpanInfo;
import org.eclipse.birt.report.engine.emitter.wpml.WordUtil;
import org.eclipse.birt.report.engine.layout.pdf.util.PropertyUtil;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractWordXmlWriter {

	protected XMLWriter writer;

	protected final String RIGHT = "right"; //$NON-NLS-1$

	protected final String LEFT = "left"; //$NON-NLS-1$

	protected final String TOP = "top"; //$NON-NLS-1$

	protected final String BOTTOM = "bottom"; //$NON-NLS-1$

	public static final char SPACE = ' ';

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static final int INDEX_NOTFOUND = -1;

	protected int imageId = 75;

	protected int bookmarkId = 0;

	private int lineId = 0;

	// Holds the global layout orientation.
	protected boolean rtl = false;

	protected abstract void writeTableLayout();

	protected abstract void writeFontSize(IStyle style);

	protected abstract void writeFont(String fontFamily);

	protected abstract void writeFontStyle(IStyle style);

	protected abstract void writeFontWeight(IStyle style);

	protected abstract void openHyperlink(HyperlinkInfo info);

	protected abstract void closeHyperlink(HyperlinkInfo info);

	protected abstract void writeVmerge(SpanInfo spanInfo);

	protected abstract void writeIndent(int indent);

	protected abstract void writeIndent(int leftMargin, int rightMargin, int textIndent);

	public void startSectionInParagraph() {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		startSection();
	}

	public void endSectionInParagraph() {
		endSection();
		writer.closeTag("w:pPr"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	public void startSection() {
		writer.openTag("w:sectPr"); //$NON-NLS-1$
	}

	public void endSection() {
		writer.closeTag("w:sectPr"); //$NON-NLS-1$
	}

	protected void drawImageShapeType(int imageId) {
		writer.openTag("v:shapetype"); //$NON-NLS-1$
		writer.attribute("id", "_x0000_t" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("coordsize", "21600,21600"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("o:spt", "75"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("o:preferrelative", "t"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("path", "m@4@5l@4@11@9@11@9@5xe"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("filled", "f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("stroked", "f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("v:stroke"); //$NON-NLS-1$
		writer.attribute("imagealignshape", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("joinstyle", "miter"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("v:stroke"); //$NON-NLS-1$
		writer.openTag("v:formulas"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "if lineDrawn pixelLineWidth 0"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "sum @0 1 0"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "sum 0 0 @1"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @2 1 2"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @3 21600 pixelWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @3 21600 pixelHeight"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "sum @0 0 1"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @6 1 2"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @7 21600 pixelWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "sum @8 21600 0 "); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "prod @7 21600 pixelHeight"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.openTag("v:f"); //$NON-NLS-1$
		writer.attribute("eqn", "sum @10 21600 0"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:f"); //$NON-NLS-1$
		writer.closeTag("v:formulas"); //$NON-NLS-1$
		writer.openTag("v:path"); //$NON-NLS-1$
		writer.attribute("o:extrusionok", "f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("gradientshapeok", "t"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("o:connecttype", "rect"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("v:path"); //$NON-NLS-1$
		writer.openTag("o:lock"); //$NON-NLS-1$
		writer.attribute("v:ext", "edit"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("aspectratio", "t"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("o:lock"); //$NON-NLS-1$
		writer.closeTag("v:shapetype"); //$NON-NLS-1$
	}

	protected void drawImageBordersStyle(IStyle style) {
		drawImageBorderStyle(BOTTOM, style.getBorderBottomStyle(),
				style.getProperty(StyleConstants.STYLE_BORDER_BOTTOM_WIDTH));
		drawImageBorderStyle(TOP, style.getBorderTopStyle(), style.getProperty(StyleConstants.STYLE_BORDER_TOP_WIDTH));
		drawImageBorderStyle(LEFT, style.getBorderLeftStyle(),
				style.getProperty(StyleConstants.STYLE_BORDER_LEFT_WIDTH));
		drawImageBorderStyle(RIGHT, style.getBorderRightStyle(),
				style.getProperty(StyleConstants.STYLE_BORDER_RIGHT_WIDTH));
	}

	private void drawImageBorderStyle(String pos, String style, CSSValue width) {
		if (PropertyUtil.getDimensionValue(width) != 0) {
			String direct = "w10:border" + pos; //$NON-NLS-1$
			writer.openTag(direct);
			writer.attribute("type", WordUtil.parseImageBorderStyle(style)); //$NON-NLS-1$
			writer.attribute("width", WordUtil.parseBorderSize(PropertyUtil.getDimensionValue(width))); //$NON-NLS-1$
			writer.closeTag(direct);
		}
	}

	protected void drawImageBordersColor(IStyle style) {
		drawImageBorderColor(BOTTOM, style.getBorderBottomColor());
		drawImageBorderColor(TOP, style.getBorderTopColor());
		drawImageBorderColor(LEFT, style.getBorderLeftColor());
		drawImageBorderColor(RIGHT, style.getBorderRightColor());
	}

	private void drawImageBorderColor(String pos, String color) {
		String borderColor = "#" + WordUtil.parseColor(color); //$NON-NLS-1$
		String direct = "o:border" + pos + "color"; //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute(direct, borderColor);
	}

	public void writePageProperties(int pageHeight, int pageWidth, int headerHeight, int footerHeight, int topMargin,
			int bottomMargin, int leftMargin, int rightMargin, String orient) {
		writer.openTag("w:pgSz"); //$NON-NLS-1$
		writer.attribute("w:w", pageWidth); //$NON-NLS-1$
		writer.attribute("w:h", pageHeight); //$NON-NLS-1$
		writer.attribute("w:orient", orient); //$NON-NLS-1$
		writer.closeTag("w:pgSz"); //$NON-NLS-1$

		writer.openTag("w:pgMar"); //$NON-NLS-1$
		writer.attribute("w:top", topMargin); //$NON-NLS-1$
		writer.attribute("w:bottom", bottomMargin); //$NON-NLS-1$
		writer.attribute("w:left", leftMargin); //$NON-NLS-1$
		writer.attribute("w:right", rightMargin); //$NON-NLS-1$
		writer.attribute("w:header", topMargin); //$NON-NLS-1$
		writer.attribute("w:footer", bottomMargin); //$NON-NLS-1$
		writer.closeTag("w:pgMar"); //$NON-NLS-1$
	}

	// write the table properties to the output stream
	public void startTable(IStyle style, int tablewidth) {
		startTable(style, tablewidth, false);
	}

	// write the table properties to the output stream
	public void startTable(IStyle style, int tablewidth, boolean inForeign) {
		writer.openTag("w:tbl"); //$NON-NLS-1$
		writer.openTag("w:tblPr"); //$NON-NLS-1$
		writeTableIndent(style);
		writeAttrTag("w:tblStyle", "TableGrid"); //$NON-NLS-1$ //$NON-NLS-2$
		writeAttrTag("w:tblOverlap", "Never"); //$NON-NLS-1$ //$NON-NLS-2$
		writeBidiTable();
		writeTableWidth(style, tablewidth);
		writeAttrTag("w:tblLook", "01E0"); //$NON-NLS-1$ //$NON-NLS-2$
		writeTableLayout();
		writeTableBorders(style);
		writeBackgroundColor(style.getBackgroundColor());

		// "justify" is not an option for table alignment in word
		if ("justify".equalsIgnoreCase(style.getTextAlign())) { //$NON-NLS-1$
			writeAlign("left", style.getDirection()); //$NON-NLS-1$
		} else {
			writeAlign(style.getTextAlign(), style.getDirection());
		}
		if (inForeign) {
			writer.openTag("w:tblCellMar"); //$NON-NLS-1$
			writer.openTag("w:top"); //$NON-NLS-1$
			writer.attribute("w:w", 0); //$NON-NLS-1$
			writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.closeTag("w:top"); //$NON-NLS-1$
			writer.openTag("w:left"); //$NON-NLS-1$
			writer.attribute("w:w", 0); //$NON-NLS-1$
			writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.closeTag("w:left"); //$NON-NLS-1$
			writer.openTag("w:bottom"); //$NON-NLS-1$
			writer.attribute("w:w", 0); //$NON-NLS-1$
			writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.closeTag("w:bottom"); //$NON-NLS-1$
			writer.openTag("w:right"); //$NON-NLS-1$
			writer.attribute("w:w", 0); //$NON-NLS-1$
			writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.closeTag("w:right"); //$NON-NLS-1$
			writer.closeTag("w:tblCellMar"); //$NON-NLS-1$
		}
		writer.closeTag("w:tblPr"); //$NON-NLS-1$
	}

	private void writeTableBorders(IStyle style) {
		writer.openTag("w:tblBorders"); //$NON-NLS-1$
		writeBorders(style, 0, 0, 0, 0);
		writer.closeTag("w:tblBorders"); //$NON-NLS-1$
	}

	public void endTable() {
		writer.closeTag("w:tbl"); //$NON-NLS-1$
	}

	private void writeTableWidth(int tablewidth) {
		writer.openTag("w:tblW"); //$NON-NLS-1$
		writer.attribute("w:w", tablewidth); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tblW"); //$NON-NLS-1$
	}

	private void writeTableIndent(IStyle style) {
		writer.openTag("w:tblInd"); //$NON-NLS-1$
		writer.attribute("w:w", WordUtil //$NON-NLS-1$
				.milliPt2Twips(PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_LEFT))));
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tblInd"); //$NON-NLS-1$
	}

	private void writeTableWidth(IStyle style, int tablewidth) {
		int leftSpace = WordUtil
				.milliPt2Twips(PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_LEFT)));
		int rightSpace = WordUtil
				.milliPt2Twips(PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_RIGHT)));
		writeTableWidth(tablewidth - leftSpace - rightSpace);
	}

	protected void writeBorders(IStyle style, int bottomMargin, int topMargin, int leftMargin, int rightMargin) {
		String borderStyle = style.getBorderBottomStyle();
		if (hasBorder(borderStyle)) {
			writeSingleBorder(BOTTOM, borderStyle, style.getBorderBottomColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_BOTTOM_WIDTH), bottomMargin);
		}

		borderStyle = style.getBorderTopStyle();
		if (hasBorder(borderStyle)) {
			writeSingleBorder(TOP, borderStyle, style.getBorderTopColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_TOP_WIDTH), topMargin);
		}

		borderStyle = style.getBorderLeftStyle();
		if (hasBorder(borderStyle)) {
			writeSingleBorder(LEFT, borderStyle, style.getBorderLeftColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_LEFT_WIDTH), leftMargin);
		}

		borderStyle = style.getBorderRightStyle();
		if (hasBorder(borderStyle)) {
			writeSingleBorder(RIGHT, borderStyle, style.getBorderRightColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_RIGHT_WIDTH), rightMargin);
		}
	}

	private void writeSingleBorder(String type, String borderStyle, String color, CSSValue width, int margin) {
		writer.openTag("w:" + type); //$NON-NLS-1$
		writeBorderProperty(borderStyle, color, width, margin);
		writer.closeTag("w:" + type); //$NON-NLS-1$
	}

	private void writeBorderProperty(String style, String color, CSSValue width, int margin) {
		writer.attribute("w:val", WordUtil.parseBorderStyle(style)); //$NON-NLS-1$
		int borderSize = WordUtil.parseBorderSize(PropertyUtil.getDimensionValue(width));
		writer.attribute("w:sz", "double".equals(style) ? borderSize / 3 : borderSize); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:space", validateBorderSpace(margin)); //$NON-NLS-1$
		writer.attribute("w:color", WordUtil.parseColor(color)); //$NON-NLS-1$
	}

	private int validateBorderSpace(int margin) {
		// word only accept 0-31 pt
		int space = (int) WordUtil.twipToPt(margin);
		if (space > 31) {
			space = 31;
		}
		return space;
	}

	protected void writeAlign(String align, String direction) {
		if (null == align) {
			return;
		}
		String textAlign = align;
		if ("justify".equalsIgnoreCase(align)) { //$NON-NLS-1$
			textAlign = "both"; //$NON-NLS-1$
		}

		// Need to swap 'left' and 'right' when orientation is RTL.
		if (CSSConstants.CSS_RTL_VALUE.equalsIgnoreCase(direction)) {
			if (IStyle.CSS_RIGHT_VALUE.equals(textAlign)) {
				writeAttrTag("w:jc", IStyle.CSS_LEFT_VALUE); //$NON-NLS-1$
			} else if (IStyle.CSS_LEFT_VALUE.equals(textAlign)) {
				writeAttrTag("w:jc", IStyle.CSS_RIGHT_VALUE); //$NON-NLS-1$
			} else {
				writeAttrTag("w:jc", textAlign); //$NON-NLS-1$
			}
		} else {
			writeAttrTag("w:jc", textAlign); //$NON-NLS-1$
		}
	}

	protected void writeBackgroundColor(String color) {
		String cssColor = WordUtil.parseColor(color);
		if (cssColor == null) {
			return;
		}
		writer.openTag("w:shd"); //$NON-NLS-1$
		writer.attribute("w:val", "clear"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:color", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:fill", cssColor); //$NON-NLS-1$
		writer.closeTag("w:shd"); //$NON-NLS-1$
	}

	/**
	 * @param direction
	 *
	 * @author bidi_hcg
	 */
	private void writeBidiTable() {
		if (this.rtl) {
			writer.openTag("w:bidiVisual"); //$NON-NLS-1$
			writer.closeTag("w:bidiVisual"); //$NON-NLS-1$
		}
	}

	protected void writeRunBorders(IStyle style) {
		String borderStyle = style.getBorderTopStyle();
		if (hasBorder(borderStyle)) {
			writeRunBorder(borderStyle, style.getBorderTopColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_TOP_WIDTH));
			return;
		}

		borderStyle = style.getBorderBottomStyle();
		if (hasBorder(borderStyle)) {
			writeRunBorder(borderStyle, style.getBorderBottomColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_BOTTOM_WIDTH));
			return;
		}

		borderStyle = style.getBorderLeftStyle();
		if (hasBorder(borderStyle)) {
			writeRunBorder(borderStyle, style.getBorderLeftColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_LEFT_WIDTH));
			return;
		}

		borderStyle = style.getBorderRightStyle();
		if (hasBorder(borderStyle)) {
			writeRunBorder(borderStyle, style.getBorderRightColor(),
					style.getProperty(StyleConstants.STYLE_BORDER_RIGHT_WIDTH));
		}
	}

	private boolean hasBorder(String borderStyle) {
		return !(borderStyle == null || "none".equalsIgnoreCase(borderStyle)); //$NON-NLS-1$
	}

	private void writeRunBorder(String borderStyle, String color, CSSValue borderWidth) {
		writer.openTag("w:bdr"); //$NON-NLS-1$
		writeBorderProperty(borderStyle, color, borderWidth, 0);
		writer.closeTag("w:bdr"); //$NON-NLS-1$
	}

	private boolean needNewParagraph(String txt) {
		return ("\n".equals(txt) || "\r".equalsIgnoreCase(txt) || "\r\n".equals(txt)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void startParagraph(IStyle style, boolean isInline, int paragraphWidth) {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writeSpacing((style.getProperty(StyleConstants.STYLE_MARGIN_TOP)),
				(style.getProperty(StyleConstants.STYLE_MARGIN_BOTTOM)));
		writeAlign(style.getTextAlign(), style.getDirection());
		int indent = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_TEXT_INDENT), paragraphWidth)
				/ 1000 * 20;

		int leftMargin = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_LEFT),
				paragraphWidth) / 1000 * 20;

		int rightMargin = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_RIGHT),
				paragraphWidth) / 1000 * 20;
		writeIndent(leftMargin, rightMargin, indent);

		if (!isInline) {
			writeBackgroundColor(style.getBackgroundColor());
			writeParagraphBorders(style);
		}
		writeBidi(CSSConstants.CSS_RTL_VALUE.equals(style.getDirection()));
		writer.closeTag("w:pPr"); //$NON-NLS-1$
	}

	/**
	 * Used only in inline text .The text align style of inline text is ignored,but
	 * its parent text align should be applied.
	 *
	 * @param style
	 * @param isInline
	 * @param paragraphWidth
	 * @param textAlign      parent text align of inline text
	 */
	public void startParagraph(IStyle style, boolean isInline, int paragraphWidth, String textAlign) {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writeSpacing((style.getProperty(StyleConstants.STYLE_MARGIN_TOP)),
				(style.getProperty(StyleConstants.STYLE_MARGIN_BOTTOM)));
		writeAlign(textAlign, style.getDirection());
		int indent = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_TEXT_INDENT), paragraphWidth)
				/ 1000 * 20;

		int leftMargin = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_LEFT),
				paragraphWidth) / 1000 * 20;

		int rightMargin = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_MARGIN_RIGHT),
				paragraphWidth) / 1000 * 20;
		writeIndent(leftMargin, rightMargin, indent);

		if (!isInline) {
			writeBackgroundColor(style.getBackgroundColor());
			writeParagraphBorders(style);
		}
		writeBidi(CSSConstants.CSS_RTL_VALUE.equals(style.getDirection()));
		writer.closeTag("w:pPr"); //$NON-NLS-1$
	}

	private void writeSpacing(CSSValue height) {
		// unit: twentieths of a point(twips)
		float spacingValue = PropertyUtil.getDimensionValue(height);
		int spacing = WordUtil.milliPt2Twips(spacingValue);
		writer.openTag("w:spacing"); //$NON-NLS-1$
		writer.attribute("w:lineRule", "exact"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:line", spacing); //$NON-NLS-1$
		writer.closeTag("w:spacing"); //$NON-NLS-1$
	}

	private void writeSpacing(CSSValue top, CSSValue bottom) {
		float topSpacingValue = PropertyUtil.getDimensionValue(top);
		float bottomSpacingValue = PropertyUtil.getDimensionValue(bottom);
		writeSpacing(WordUtil.milliPt2Twips(topSpacingValue) / 2, WordUtil.milliPt2Twips(bottomSpacingValue) / 2);
	}

	private void writeSpacing(int beforeValue, int afterValue) {
		writer.openTag("w:spacing"); //$NON-NLS-1$
		writer.attribute("w:before", beforeValue); //$NON-NLS-1$
		writer.attribute("w:after", afterValue); //$NON-NLS-1$
		writer.closeTag("w:spacing"); //$NON-NLS-1$
	}

	protected void writeAutoText(int type) {
		writer.openTag("w:instrText"); //$NON-NLS-1$
		if (type == IAutoTextContent.PAGE_NUMBER) {
			writer.text("PAGE"); //$NON-NLS-1$
		} else if (type == IAutoTextContent.TOTAL_PAGE) {
			writer.text("NUMPAGES"); //$NON-NLS-1$
		}
		writer.closeTag("w:instrText"); //$NON-NLS-1$
	}

	protected void writeSimpleField( String fieldFunction) {
		if (fieldFunction != null){
			writer.openTag("w:instrText"); //$NON-NLS-1$
			writer.text(fieldFunction); // $NON-NLS-1$
			writer.closeTag("w:instrText"); //$NON-NLS-1$
		}
	}

	private void writeString(String txt, IStyle style) {
		if (txt == null) {
			return;
		}
		if (style != null) {
			String textTransform = style.getTextTransform();
			if (CSSConstants.CSS_CAPITALIZE_VALUE.equalsIgnoreCase(textTransform)) {
				txt = WordUtil.capitalize(txt);
			} else if (CSSConstants.CSS_UPPERCASE_VALUE.equalsIgnoreCase(textTransform)) {
				txt = txt.toUpperCase();
			} else if (CSSConstants.CSS_LOWERCASE_VALUE.equalsIgnoreCase(textTransform)) {
				txt = txt.toLowerCase();
			}
		}

		writer.openTag("w:t"); //$NON-NLS-1$
		writer.attribute("xml:space", "preserve"); //$NON-NLS-1$ //$NON-NLS-2$
		int length = txt.length();
		int start = 0;
		int end = 0;
		while (end < length) {
			char ch = txt.charAt(end);
			if (ch == '\r' || ch == '\n') {
				// output previous text
				writeText(txt.substring(start, end));
				writer.cdata("<w:br/>"); //$NON-NLS-1$ ;
				start = end + 1;
				if (ch == '\r' && start < length && txt.charAt(start) == '\n') {
					start++;
				}
				end = start + 1;
				continue;
			}
			end++;
		}
		writeText(txt.substring(start));

		writer.closeTag("w:t"); //$NON-NLS-1$
	}

	/**
	 * Word have extra limitation on text in run: a. it must following xml format.
	 * b. no ]]> so , we need replace all &, <,> in the text
	 *
	 * @param text
	 */
	private void writeText(String text) {
		int length = text.length();
		StringBuilder sb = new StringBuilder(length * 2);
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
			case '&':
				sb.append("&amp;"); //$NON-NLS-1$
				break;
			case '>':
				sb.append("&gt;"); //$NON-NLS-1$
				break;
			case '<':
				sb.append("&lt;"); //$NON-NLS-1$
				break;
			default:
				sb.append(ch);
			}
		}
		writer.cdata(sb.toString());
	}

	private void writeLetterSpacing(IStyle style) {
		int letterSpacing = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_LETTER_SPACING));
		writeAttrTag("w:spacing", WordUtil.milliPt2Twips(letterSpacing)); //$NON-NLS-1$
	}

	private void writeHyperlinkStyle(HyperlinkInfo info, IStyle style) {
		// deal with hyperlink
		if (info != null) {
			String color = info.getColor();
			if (color != null) {
				writeAttrTag("w:color", color); //$NON-NLS-1$
			}
			writeAttrTag("w:rStyle", "Hyperlink"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			writeTextUnderline(style);
			writeTextColor(style);
		}
	}

	protected void writeTocText(String tocText, int level) {
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:instrText"); //$NON-NLS-1$
		writer.text(" TC \"" + tocText + "\"" + " \\f C \\l \"" + String.valueOf(level) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		writer.closeTag("w:instrText"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
	}

	/**
	 * @param direction
	 *
	 * @author bidi_hcg
	 */
	protected void writeBidi(boolean rtl) {
		writeAttrTag("w:bidi", rtl ? "" : "off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void writeField(boolean isStart) {
		String fldCharType = isStart ? "begin" : "end"; //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:fldChar"); //$NON-NLS-1$
		writer.attribute("w:fldCharType", fldCharType); //$NON-NLS-1$
		writer.closeTag("w:fldChar"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
	}

	protected void writeField(boolean isStart, IStyle style, String fontName) {
		String fldCharType = isStart ? "begin" : "end"; //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("w:r"); //$NON-NLS-1$
		writeFieldRunProperties(style, fontName);
		writer.openTag("w:fldChar"); //$NON-NLS-1$
		writer.attribute("w:fldCharType", fldCharType); //$NON-NLS-1$
		writer.closeTag("w:fldChar"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
	}

	protected void writeFieldSeparator( IStyle style, String fontName) {
		writer.openTag("w:r"); //$NON-NLS-1$
		writeFieldRunProperties( style, fontName);
		writer.openTag("w:fldChar"); //$NON-NLS-1$
		writer.attribute("w:fldCharType", "separate"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:fldChar"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
	}

	public void writeColumn(int[] cols) {
		// unit: twips
		writer.openTag("w:tblGrid"); //$NON-NLS-1$

		for (int i = 0; i < cols.length; i++) {
			writeAttrTag("w:gridCol", cols[i]); //$NON-NLS-1$
		}
		writer.closeTag("w:tblGrid"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param style  style of the row
	 * @param height height of current row, if heigh equals 1 then ignore height
	 * @param type   header or normal
	 */

	public void startTableRow(double height, boolean isHeader, boolean repeatHeader, boolean fixedLayout,
			boolean cantSplit) {
		writer.openTag("w:tr"); //$NON-NLS-1$

		// write the row height, unit: twips
		writer.openTag("w:trPr"); //$NON-NLS-1$

		if (height != -1) {
			writer.openTag("w:trHeight"); //$NON-NLS-1$
			if (fixedLayout) {
				writer.attribute("w:h-rule", "exact"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.attribute("w:val", height); //$NON-NLS-1$
			writer.closeTag("w:trHeight"); //$NON-NLS-1$
		}

		// if value is "off",the header will be not repeated
		if (isHeader) {
			String headerOnOff = repeatHeader ? "on" : "off"; //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:tblHeader", headerOnOff); //$NON-NLS-1$
		}

		if (cantSplit) {
			writer.openTag("w:cantSplit"); //$NON-NLS-1$
			writer.closeTag("w:cantSplit"); //$NON-NLS-1$
		}
		writer.closeTag("w:trPr"); //$NON-NLS-1$
	}

	public void endTableRow() {
		writer.closeTag("w:tr"); //$NON-NLS-1$
	}

	public void startTableCell(int width, IStyle style, SpanInfo spanInfo) {
		writer.openTag("w:tc"); //$NON-NLS-1$
		writer.openTag("w:tcPr"); //$NON-NLS-1$
		writeCellWidth(width);
		if (spanInfo != null) {
			writeGridSpan(spanInfo);
			writeVmerge(spanInfo);
		}
		writeCellProperties(style);
		writer.closeTag("w:tcPr"); //$NON-NLS-1$

		String align = style.getTextAlign();
		if (align == null) {
			return;
		}
		String direction = style.getDirection(); // bidi_hcg
		if (CSSConstants.CSS_LEFT_VALUE.equals(align)) {
			if (!CSSConstants.CSS_RTL_VALUE.equals(direction)) {
				return;
			}
		}
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writeAlign(align, direction);
		writer.closeTag("w:pPr"); //$NON-NLS-1$
	}

	private void writeCellWidth(int width) {
		writer.openTag("w:tcW"); //$NON-NLS-1$
		writer.attribute("w:w", width); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tcW"); //$NON-NLS-1$
	}

	private void writeGridSpan(SpanInfo spanInfo) {
		int columnSpan = spanInfo.getColumnSpan();
		if (columnSpan > 1) {
			writeAttrTag("w:gridSpan", columnSpan); //$NON-NLS-1$
		}
	}

	public void writeSpanCell(SpanInfo info) {
		writer.openTag("w:tc"); //$NON-NLS-1$
		writer.openTag("w:tcPr"); //$NON-NLS-1$
		writeCellWidth(info.getCellWidth());
		writeGridSpan(info);
		writeVmerge(info);
		writeCellProperties(info.getStyle());
		writer.closeTag("w:tcPr"); //$NON-NLS-1$
		insertEmptyParagraph();
		writer.closeTag("w:tc"); //$NON-NLS-1$
	}

	public void endTableCell(boolean empty)	{
		// The resulting doc looks more sane than with the original code
		// endTableCell( empty, false );
		// because an empty table cell now contains the "cell marker" (a bit like a small o).
		// and enables the user to tab into the cell and enter text directly;
		// whereas with the old code, the cell was completely empty and the text caret
		// could only be placed into the cell using a mouse double-click.
		endTableCell(empty, true);
	}

	public void endTableCell(boolean empty, boolean inForeign) {

		if (empty) {
			if (inForeign) {
				insertEmptyParagraphInForeign();
			} else {
				insertEmptyParagraph();
			}
		}
		writer.closeTag("w:tc"); //$NON-NLS-1$
	}

	public void writeEmptyCell() {
		writer.openTag("w:tc"); //$NON-NLS-1$
		writer.openTag("w:tcPr"); //$NON-NLS-1$
		writer.openTag("w:tcW"); //$NON-NLS-1$
		writer.attribute("w:w", 0); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tcW"); //$NON-NLS-1$
		writer.closeTag("w:tcPr"); //$NON-NLS-1$
		insertEmptyParagraph();
		writer.closeTag("w:tc"); //$NON-NLS-1$
	}

	public void insertEmptyParagraph() {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writer.openTag("w:spacing"); //$NON-NLS-1$
		writer.attribute("w:line", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:lineRule", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:spacing"); //$NON-NLS-1$
		writer.closeTag("w:pPr"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	public void insertEmptyParagraphInForeign() {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	public void insertHiddenParagraph() {
		writer.openTag("w:p"); //$NON-NLS-1$
		writeHiddenProperty();
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	public void writeHiddenProperty() {
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeAttrTag("w:vanish", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:rPr"); //$NON-NLS-1$
	}

	public void endParagraph() {
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	public void writeCaption(String txt) {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writeAlign("center", null); //$NON-NLS-1$
		writer.closeTag("w:pPr"); //$NON-NLS-1$
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeString(txt, null);
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	/**
	 * If the cell properties is not set, then check the row properties and write
	 * those properties.
	 *
	 * @param style this cell style
	 */
	@SuppressWarnings("nls")
	private void writeCellProperties(IStyle style) {
		// A cell background color may inherit from row background,
		// so we should get the row background color here,
		// if the cell background is transparent
		if (style == null) {
			return;
		}
		writeBackgroundColor(style.getBackgroundColor());
		writeCellBorders(style);
		writeCellPadding(style);
		String verticalAlign = style.getVerticalAlign();
		if (verticalAlign != null) {
			writeAttrTag("w:vAlign", WordUtil.parseVerticalAlign(verticalAlign));
		}
		String noWrap = CSSConstants.CSS_NOWRAP_VALUE.equalsIgnoreCase(style.getWhiteSpace()) ? "on" : "off";
		writeAttrTag("w:noWrap", noWrap);
	}

	@SuppressWarnings("nls")
	private void writeCellBorders(IStyle style) {
		writer.openTag("w:tcBorders");
		writeBorders(style, 0, 0, 0, 0);
		writer.closeTag("w:tcBorders");
	}

	private void writeCellPadding(IStyle style) {
		int bottomPadding = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_PADDING_BOTTOM));
		int leftPadding = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_PADDING_LEFT));
		int topPadding = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_PADDING_TOP));
		int rightPadding = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_PADDING_RIGHT));

		// the cell padding in DOC is tcMar
		writer.openTag("w:tcMar"); //$NON-NLS-1$
		writeCellPadding(bottomPadding, BOTTOM);
		writeCellPadding(leftPadding, LEFT);
		writeCellPadding(topPadding, TOP);
		writeCellPadding(rightPadding, RIGHT);
		writer.closeTag("w:tcMar"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param padding  milliPoint
	 * @param position top/right/bottom/left
	 */
	private void writeCellPadding(int padding, String position) {
		writer.openTag("w:" + position); //$NON-NLS-1$
		writer.attribute("w:w", WordUtil.milliPt2Twips(padding)); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:" + position); //$NON-NLS-1$
	}

	protected void writeAttrTag(String name, String val) {
		writer.openTag(name);
		writer.attribute("w:val", val); //$NON-NLS-1$
		writer.closeTag(name);
	}

	protected void writeAttrTag(String name, int val) {
		writer.openTag(name);
		writer.attribute("w:val", val); //$NON-NLS-1$
		writer.closeTag(name);
	}

	protected void writeAttrTag(String name, double val) {
		writer.openTag(name);
		writer.attribute("w:val", val); //$NON-NLS-1$
		writer.closeTag(name);
	}

	protected int nextImageID() {
		return imageId++;
	}

	private void writeTextInParagraph(int type, String txt, IStyle style, String fontFamily, HyperlinkInfo info,
			int paragraphWidth, boolean runIsRtl) {
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$

		CSSValue lineHeight = style.getProperty(StyleConstants.STYLE_LINE_HEIGHT);
		if (!"normal".equalsIgnoreCase(lineHeight.getCssText())) { //$NON-NLS-1$
			writeSpacing(lineHeight);
		}

		writeAlign(style.getTextAlign(), style.getDirection());
		writeBackgroundColor(style.getBackgroundColor());
		writeParagraphBorders(style);
		int indent = PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_TEXT_INDENT),
				paragraphWidth * 1000) / 1000 * 20;
		if (indent != 0) {
			writeIndent(indent);
		}
		writeBidi(CSSConstants.CSS_RTL_VALUE.equals(style.getDirection())); // bidi_hcg
		// We need to apply the text font style to the paragraph. It is useful
		// if the end user want to paste some text into this paragraph and
		// changes the text to the paragraph's font style.
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeRunProperties(style, fontFamily, info);
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.closeTag("w:pPr"); //$NON-NLS-1$
		writeTextInRun(type, txt, style, fontFamily, info, false, paragraphWidth, runIsRtl);
	}

	private void writeParagraphBorders(IStyle style) {
		writer.openTag("w:pBdr"); //$NON-NLS-1$
		writeBorders(style, 0, 0, 0, 0);
		writer.closeTag("w:pBdr"); //$NON-NLS-1$
	}

	public void writeText(int type, String txt, IStyle style, String fontFamily, HyperlinkInfo info, TextFlag flag,
			int paragraphWidth, boolean runIsRtl) {
		if (flag == TextFlag.START) {
			writeTextInParagraph(type, txt, style, fontFamily, info, paragraphWidth, runIsRtl);
		} else if (flag == TextFlag.END) {
			writer.closeTag("w:p"); //$NON-NLS-1$
		} else if (flag == TextFlag.MIDDLE) {
			writeTextInRun(type, txt, style, fontFamily, info, false, paragraphWidth, runIsRtl);
		} else {
			writeTextInParagraph(type, txt, style, fontFamily, info, paragraphWidth, runIsRtl);
			writer.closeTag("w:p"); //$NON-NLS-1$
		}
	}

	public void writeTextInRun(int type, String txt, IStyle style, String fontFamily, HyperlinkInfo info,
			boolean isInline, int paragraphWidth, boolean runIsRtl) {
		if ("".equals(txt)) { //$NON-NLS-1$
			return;
		}
		if (needNewParagraph(txt)) {
			writer.closeTag("w:p"); //$NON-NLS-1$
			startParagraph(style, isInline, paragraphWidth);
			return;
		}

		openHyperlink(info);
		boolean isField = WordUtil.isField(type);
		String direction = style.getDirection();

		if (isField) {
			writeField(true, style, fontFamily);
		}
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeRunProperties(style, fontFamily, info);
		if (isInline) {
			writeAlign(style.getTextAlign(), direction);
			writeBackgroundColor(style.getBackgroundColor());
			writePosition(style.getVerticalAlign(), style.getProperty(StyleConstants.STYLE_FONT_SIZE));
			writeRunBorders(style);
		}
		if (!isField && runIsRtl) {
			writer.openTag("w:rtl"); //$NON-NLS-1$
			writer.closeTag("w:rtl"); //$NON-NLS-1$
		}
		writer.closeTag("w:rPr"); //$NON-NLS-1$

		if (isField) {
			if (!(type == AbstractEmitterImpl.CUSTOM_FIELD)) {
				writeAutoText(type);
			} else {
				writeSimpleField(txt);
				writer.closeTag("w:r"); //$NON-NLS-1$
				writeFieldSeparator(style, fontFamily);
				writer.openTag("w:r"); //$NON-NLS-1$
				writer.openTag("w:rPr"); //$NON-NLS-1$
				writeRunProperties(style, fontFamily, info);
				if (isInline) {
					writeAlign(style.getTextAlign(), direction);
					writeBackgroundColor(style.getBackgroundColor());
					writePosition( style.getVerticalAlign(), style.getProperty(StyleConstants.STYLE_FONT_SIZE));
					writeRunBorders(style);
				}
				if (!isField && runIsRtl) {
					writer.openTag("w:rtl"); //$NON-NLS-1$
					writer.closeTag("w:rtl"); //$NON-NLS-1$
				}
				writer.closeTag("w:rPr"); //$NON-NLS-1$
				writeString( txt, style);
			}
		} else {
			// get text attribute overflow hidden
			// and run the function to emulate if true
			if (CSSConstants.CSS_OVERFLOW_HIDDEN_VALUE.equals(style.getOverflow())) {
				txt = cropOverflowString(txt, style, fontFamily, paragraphWidth);
			}
			writeString(txt, style);
		}
		writer.closeTag("w:r"); //$NON-NLS-1$
		if (isField) {
			writeField(false, style, fontFamily);
		}
		closeHyperlink(info);
	}

	/**
	 * function emulate the overflow hidden behavior on table cell
	 *
	 * @param text       String to check
	 * @param style      style of the text
	 * @param fontFamily fond of the text
	 * @param cellWidth  the width of the container in points
	 * @return String with truncated words that surpasses the cell width
	 */
	public String cropOverflowString(String text, IStyle style, String fontFamily, int cellWidth) {// TODO: retrieve
																									// font type and
																									// replace plain
																									// with
																									// corresponding
		Font font = new Font(fontFamily, Font.PLAIN, WordUtil
				.parseFontSize(PropertyUtil.getDimensionValue(style.getProperty(StyleConstants.STYLE_FONT_SIZE))));
		Canvas c = new Canvas();
		FontMetrics fm = c.getFontMetrics(font);
		// conversion from point to advancement point from sample linear
		// regression:
		int cellWidthInPointAdv = (cellWidth * (int) WordUtil.PT_TWIPS - 27) / 11;
		StringBuilder sb = new StringBuilder(text.length() + 1);
		int wordEnd = INDEX_NOTFOUND;
		do {
			wordEnd = text.indexOf(SPACE);
			if (wordEnd != INDEX_NOTFOUND) // space found
			{
				String word = text.substring(0, wordEnd);
				word = cropOverflowWord(word, fm, cellWidthInPointAdv);
				sb.append(word);
				sb.append(SPACE);
				text = text.substring(wordEnd + 1);
			}
		} while (wordEnd != INDEX_NOTFOUND && !EMPTY_STRING.equals(text));
		sb.append(cropOverflowWord(text, fm, cellWidthInPointAdv));
		return sb.toString();
	}

	/**
	 * crop words according to the given container point advance
	 *
	 * @param text                   it is a given word
	 * @param fm                     the Font metrics
	 * @param containerPointAdvWidth
	 * @return the word is cropped if longer than container width
	 */
	private String cropOverflowWord(String word, FontMetrics fm, int containerPointAdvWidth) {
		int wordlength = fm.stringWidth(word);
		if (wordlength > containerPointAdvWidth) {
			int cropEnd = (containerPointAdvWidth * word.length()) / wordlength;
			if (cropEnd == 0) {
				return ""; //$NON-NLS-1$
			}
			return word.substring(0, cropEnd);
		}
		return word;
	}

	public void writeTextInRun(int type, String txt, IStyle style, String fontFamily, HyperlinkInfo info,
			boolean isInline, int paragraphWidth, boolean runIsRtl, String textAlign) {
		if ("".equals(txt)) { //$NON-NLS-1$
			return;
		}
		if (needNewParagraph(txt)) {
			writer.closeTag("w:p"); //$NON-NLS-1$
			startParagraph(style, isInline, paragraphWidth, textAlign);
			return;
		}

		openHyperlink(info);
		boolean isField = WordUtil.isField(type);
		String direction = style.getDirection();

		if (isField) {
			writeField(true, style, fontFamily);
		}
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeRunProperties(style, fontFamily, info);
		if (isInline) {
			writeAlign(textAlign, direction);
			writeBackgroundColor(style.getBackgroundColor());
			writePosition(style.getVerticalAlign(), style.getProperty(StyleConstants.STYLE_FONT_SIZE));
			writeRunBorders(style);
		}
		if (!isField && runIsRtl) {
			writer.openTag("w:rtl"); //$NON-NLS-1$
			writer.closeTag("w:rtl"); //$NON-NLS-1$
		}
		writer.closeTag("w:rPr"); //$NON-NLS-1$

		if (isField) {
			if (!(type == AbstractEmitterImpl.CUSTOM_FIELD)) {
				writeAutoText(type);
			} else {
				writeSimpleField(txt);
				writer.closeTag("w:r"); //$NON-NLS-1$
				writeFieldSeparator(style, fontFamily);
				writer.openTag("w:r"); //$NON-NLS-1$
				writer.openTag("w:rPr"); //$NON-NLS-1$
				writeRunProperties(style, fontFamily, info);
				if (isInline) {
					writeAlign(textAlign, direction);
					writeBackgroundColor(style.getBackgroundColor());
					writePosition(style.getVerticalAlign(), style.getProperty(StyleConstants.STYLE_FONT_SIZE));
					writeRunBorders(style);
				}
				if (!isField && runIsRtl) {
					writer.openTag("w:rtl"); //$NON-NLS-1$
					writer.closeTag("w:rtl"); //$NON-NLS-1$
				}
				writer.closeTag("w:rPr"); //$NON-NLS-1$
				writeString(txt, style);
			}
		} else {
			writeString(txt, style);
		}
		writer.closeTag("w:r"); //$NON-NLS-1$
		if (isField) {
			writeField(false, style, fontFamily);
		}
		closeHyperlink(info);
	}

	private void writePosition(String verticalAlign, CSSValue fontSize) {
		int size = WordUtil.parseFontSize(PropertyUtil.getDimensionValue(fontSize));
		if ("top".equalsIgnoreCase(verticalAlign)) { //$NON-NLS-1$
			writeAttrTag("w:position", (int) (size * 1 / 3)); //$NON-NLS-1$
		} else if ("bottom".equalsIgnoreCase(verticalAlign)) { //$NON-NLS-1$
			writeAttrTag("w:position", (int) (-size * 1 / 3)); //$NON-NLS-1$
		}
	}

	protected void writeRunProperties(IStyle style, String fontFamily, HyperlinkInfo info) {
		writeHyperlinkStyle(info, style);
		writeFont(fontFamily);
		writeFontSize(style);
		writeLetterSpacing(style);
		writeTextLineThrough(style);
		writeFontStyle(style);
		writeFontWeight(style);
	}

	protected void writeFieldRunProperties(IStyle style, String fontFamily) {
		writeFont(fontFamily);
		writeFontSize(style);
		writeLetterSpacing(style);
		writeTextLineThrough(style);
		writeFontStyle(style);
		writeFontWeight(style);
	}

	private void writeTextColor(IStyle style) {
		String val = WordUtil.parseColor(style.getColor());
		if (val != null) {
			writeAttrTag("w:color", val); //$NON-NLS-1$
		}
	}

	private void writeTextUnderline(IStyle style) {
		String val = WordUtil.removeQuote(style.getTextUnderline());
		if (!"none".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:u", "single"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void writeTextLineThrough(IStyle style) {
		String val = WordUtil.removeQuote(style.getTextLineThrough());
		if (!"none".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:strike", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void startHeaderFooterContainer(int headerHeight, int headerWidth) {
		startHeaderFooterContainer(headerHeight, headerWidth, false);
	}

	protected void startHeaderFooterContainer(int headerHeight, int headerWidth, boolean writeColumns) {
		// the tableGrid in DOC has a 0.19cm cell margin by default on left and right.
		// so the header or footer width should be 2*0.19cm larger. that is 215.433
		// twips.
		headerWidth += 215;
		writer.openTag("w:tbl"); //$NON-NLS-1$
		writer.openTag("w:tblPr"); //$NON-NLS-1$
		writeTableWidth(headerWidth);
		writeAttrTag("w:tblLook", "01E0"); //$NON-NLS-1$ //$NON-NLS-2$
		writeTableLayout();
		writer.closeTag("w:tblPr"); //$NON-NLS-1$
		if (writeColumns) {
			writeColumn(new int[] { headerWidth });
		}
		writer.openTag("w:tr"); //$NON-NLS-1$
		// write the row height, unit: twips
		writer.openTag("w:trPr"); //$NON-NLS-1$
		writeAttrTag("w:trHeight", headerHeight); //$NON-NLS-1$
		writer.closeTag("w:trPr"); //$NON-NLS-1$
		writer.openTag("w:tc"); //$NON-NLS-1$
		writer.openTag("w:tcPr"); //$NON-NLS-1$
		writeCellWidth(headerWidth);
		writer.closeTag("w:tcPr"); //$NON-NLS-1$
	}

	protected void endHeaderFooterContainer() {
		insertEmptyParagraph();
		writer.closeTag("w:tc"); //$NON-NLS-1$
		writer.closeTag("w:tr"); //$NON-NLS-1$
		writer.closeTag("w:tbl"); //$NON-NLS-1$
	}

	public void drawDiagonalLine(DiagonalLineInfo diagonalLineInfo) {
		if (diagonalLineInfo.getDiagonalNumber() <= 0 && diagonalLineInfo.getAntiDiagonalNumber() <= 0) {
			return;
		}
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:pict"); //$NON-NLS-1$
		double diagonalLineWidth = diagonalLineInfo.getDiagonalLineWidth();
		String diagonalLineStyle = diagonalLineInfo.getDiagonalStyle();
		double antidiagonalLineWidth = diagonalLineInfo.getAntiDiagonalLineWidth();
		String antidiagonalLineStyle = diagonalLineInfo.getAntiDiagonalStyle();
		String lineColor = diagonalLineInfo.getColor();
		for (Line line : diagonalLineInfo.getDiagonalLine()) {
			drawLine(diagonalLineWidth, diagonalLineStyle, lineColor, line);
		}
		for (Line antiLine : diagonalLineInfo.getAntidiagonalLine()) {
			drawLine(antidiagonalLineWidth, antidiagonalLineStyle, lineColor, antiLine);
		}
		writer.closeTag("w:pict"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
	}

	private void drawLine(double width, String style, String color, Line line) {
		writer.openTag("v:line"); //$NON-NLS-1$
		writer.attribute("id", "Line" + getLineId()); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("style", "position:absolute;left:0;text-align:left;z-index:1"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("from", line.getXCoordinateFrom() + "pt," + line.getYCoordinateFrom() + "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.attribute("to", line.getXCoordinateTo() + "pt," + line.getYCoordinateTo() + "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.attribute("strokeweight", width + "pt"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("strokecolor", "#" + color); //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("v:stroke"); //$NON-NLS-1$
		writer.attribute("dashstyle", WordUtil.parseLineStyle(style)); //$NON-NLS-1$
		writer.closeTag("v:stroke"); //$NON-NLS-1$
		writer.closeTag("v:line"); //$NON-NLS-1$
	}

	private int getLineId() {
		return lineId++;
	}

	public void writeEmptyElement(String tag) {
		writer.openTag(tag);
		writer.closeTag(tag);
	}
}
