/*******************************************************************************
 * Copyright (c) 2006 Inetsoft Technology Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Inetsoft Technology Corp  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.wpml.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.birt.report.engine.content.IForeignContent;
import org.eclipse.birt.report.engine.content.IImageContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.engine.StyleConstants;
import org.eclipse.birt.report.engine.emitter.EmitterUtil;
import org.eclipse.birt.report.engine.emitter.XMLWriter;
import org.eclipse.birt.report.engine.emitter.wpml.AbstractEmitterImpl.InlineFlag;
import org.eclipse.birt.report.engine.emitter.wpml.AbstractEmitterImpl.TextFlag;
import org.eclipse.birt.report.engine.emitter.wpml.HyperlinkInfo;
import org.eclipse.birt.report.engine.emitter.wpml.IWordWriter;
import org.eclipse.birt.report.engine.emitter.wpml.SpanInfo;
import org.eclipse.birt.report.engine.emitter.wpml.WordUtil;
import org.eclipse.birt.report.engine.layout.emitter.Image;
import org.eclipse.birt.report.engine.layout.pdf.util.PropertyUtil;
import org.w3c.dom.css.CSSValue;

public class DocWriter extends AbstractWordXmlWriter implements IWordWriter {

	protected static Logger logger = Logger.getLogger(DocWriter.class.getName());

	public DocWriter(OutputStream out) {
		this(out, "UTF-8"); //$NON-NLS-1$
	}

	public DocWriter(OutputStream out, String encoding) {
		writer = new XMLWriter();
		writer.open(out, encoding);
	}

	public void start(boolean rtl, String creator, String title, String description, String subject) {
		this.rtl = rtl;
		writer.startWriter();
		writer.literal("\n"); //$NON-NLS-1$
		writer.literal("<?mso-application progid=\"Word.Document\"?>"); //$NON-NLS-1$
		writer.literal("\n"); //$NON-NLS-1$
		writer.openTag("w:wordDocument"); //$NON-NLS-1$
		writer.attribute("xmlns:w", "http://schemas.microsoft.com/office/word/2003/wordml"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("xmlns:v", "urn:schemas-microsoft-com:vml"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("xmlns:w10", "urn:schemas-microsoft-com:office:word"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("xmlns:o", "urn:schemas-microsoft-com:office:office"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("xmlns:dt", "uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("xmlns:wx", "http://schemas.microsoft.com/office/word/2003/auxHint"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("xmlns:aml", "http://schemas.microsoft.com/aml/2001/core"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("xml:space", "preserve"); //$NON-NLS-1$ //$NON-NLS-2$
		writeCoreProperties(creator, title, description, subject);

		// style for outline
		writer.openTag("w:styles"); //$NON-NLS-1$
		writer.openTag("w:style"); //$NON-NLS-1$
		writer.attribute("w:type", "paragraph"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute(" w:styleId", 4); //$NON-NLS-1$
		writer.openTag("w:name"); //$NON-NLS-1$
		writer.attribute("w:val", "toc 4"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:name"); //$NON-NLS-1$
		writer.openTag("wx:uiName"); //$NON-NLS-1$
		writer.attribute("wx:val", "catalog 4"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("wx:uiName"); //$NON-NLS-1$
		writer.openTag("w:autoRedefine"); //$NON-NLS-1$
		writer.closeTag("w:autoRedefine"); //$NON-NLS-1$
		writer.openTag("w:semiHidden"); //$NON-NLS-1$
		writer.closeTag("w:semiHidden"); //$NON-NLS-1$
		writer.openTag("w:rsid"); //$NON-NLS-1$
		writer.attribute("w:val", "009B3C8F"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:rsid"); //$NON-NLS-1$
		writer.openTag("w:pPr"); //$NON-NLS-1$
		writer.openTag("w:pStyle"); //$NON-NLS-1$
		writer.attribute("w:val", 4); //$NON-NLS-1$
		writer.closeTag("w:pStyle"); //$NON-NLS-1$
		writeBidi(rtl); // bidi_hcg
		writer.closeTag("w:pPr"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writer.openTag("wx:font"); //$NON-NLS-1$
		writer.attribute("wx:val", "Times New Roman"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("wx:font"); //$NON-NLS-1$
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.closeTag("w:style"); //$NON-NLS-1$

		writer.openTag("w:style"); //$NON-NLS-1$
		writer.attribute("w:type", "character"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:styleId", "Hyperlink"); //$NON-NLS-1$ //$NON-NLS-2$
		writeAttrTag("w:name", "Hyperlink"); //$NON-NLS-1$//$NON-NLS-2$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writeAttrTag("w:u", "single"); //$NON-NLS-1$ //$NON-NLS-2$
		writeAttrTag("w:color", "0000ff"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.closeTag("w:style"); //$NON-NLS-1$

		writer.openTag("w:style"); //$NON-NLS-1$
		writer.attribute("w:type", "table"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:default", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("styleId", "TableNormal"); //$NON-NLS-1$//$NON-NLS-2$
		writeAttrTag("w:name", "Normal Table"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("wx:uiName"); //$NON-NLS-1$
		writer.attribute("wx:val", "Table Normal"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("wx:uiName"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writer.openTag("wx:font"); //$NON-NLS-1$
		writer.attribute("wx:val", "Calibri"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("wx:font"); //$NON-NLS-1$
		writer.openTag("w:lang"); //$NON-NLS-1$
		writer.attribute("w:val", "EN-US"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:fareast", "ZH-CN"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:bidi", "AR-SA"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("w:lang"); //$NON-NLS-1$
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.openTag("w:tblPr"); //$NON-NLS-1$
		writer.openTag("w:tblInd"); //$NON-NLS-1$
		writer.attribute("w:w", 0); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tblInd"); //$NON-NLS-1$
		writer.openTag("w:tblCellMar"); //$NON-NLS-1$
		writer.openTag("w:top"); //$NON-NLS-1$
		writer.attribute("w:w", 0); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:top"); //$NON-NLS-1$
		writer.openTag("w:left"); //$NON-NLS-1$
		writer.attribute("w:w", 108); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:left"); //$NON-NLS-1$
		writer.openTag("w:bottom"); //$NON-NLS-1$
		writer.attribute("w:w", 0); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:bottom"); //$NON-NLS-1$
		writer.openTag("w:right"); //$NON-NLS-1$
		writer.attribute("w:w", 108); //$NON-NLS-1$
		writer.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:right"); //$NON-NLS-1$
		writer.closeTag("w:tblCellMar"); //$NON-NLS-1$
		writer.closeTag("w:tblPr"); //$NON-NLS-1$
		writer.closeTag("w:style"); //$NON-NLS-1$

		writer.closeTag("w:styles"); //$NON-NLS-1$

		// For show background
		writer.openTag("w:displayBackgroundShape"); //$NON-NLS-1$
		writer.closeTag("w:displayBackgroundShape"); //$NON-NLS-1$

		writer.openTag("w:docPr"); //$NON-NLS-1$
		writer.openTag("w:view"); //$NON-NLS-1$
		writer.attribute("w:val", "print"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:view"); //$NON-NLS-1$
		writer.openTag("w:zoom"); //$NON-NLS-1$
		writer.attribute("w:percent", "100"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:zoom"); //$NON-NLS-1$
		writer.closeTag("w:docPr"); //$NON-NLS-1$
		writer.openTag("w:body"); //$NON-NLS-1$
	}

	private void writeCoreProperties(String creator, String title, String description, String subject) {
		writer.openTag("o:DocumentProperties"); //$NON-NLS-1$
		writer.openTag("o:Author"); //$NON-NLS-1$
		writer.text(creator);
		writer.closeTag("o:Author"); //$NON-NLS-1$
		writer.openTag("o:Title"); //$NON-NLS-1$
		writer.text(title);
		writer.closeTag("o:Title"); //$NON-NLS-1$
		writer.openTag("o:Description"); //$NON-NLS-1$
		writer.text(description);
		writer.closeTag("o:Description"); //$NON-NLS-1$
		writer.openTag("o:Subject"); //$NON-NLS-1$
		writer.text(subject);
		writer.closeTag("o:Subject"); //$NON-NLS-1$
		writer.closeTag("o:DocumentProperties"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param data   image data
	 * @param height image height, unit = pt
	 * @param width  image width, unit = pt
	 */
	public void drawImage(byte[] data, double height, double width, HyperlinkInfo hyper, IStyle style,
			InlineFlag inlineFlag, String altText, String imageUrl) {
		if (inlineFlag == InlineFlag.BLOCK || inlineFlag == InlineFlag.FIRST_INLINE) {
			writer.openTag("w:p"); //$NON-NLS-1$
		}
		int imageId = nextImageID();
		openHyperlink(hyper);
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:pict"); //$NON-NLS-1$
		drawImageShapeType(imageId);
		drawImageData(data, imageId);
		drawImageShape(height, width, style, altText, imageId);
		writer.closeTag("w:pict"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
		closeHyperlink(hyper);
		if (inlineFlag == InlineFlag.BLOCK) {
			writer.closeTag("w:p"); //$NON-NLS-1$
		}
	}

	private void drawImageData(byte[] data, int imageId) {
		String pic2Text = null;
		if (data != null && data.length != 0) {
			pic2Text = new String(Base64.encodeBase64(data, false));
		}
		if (pic2Text != null) {
			writer.openTag("w:binData"); //$NON-NLS-1$
			writer.attribute("w:name", "wordml://" + imageId + ".png"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writer.text(pic2Text);
			writer.closeTag("w:binData"); //$NON-NLS-1$
		}
	}

	private void drawImageShape(double height, double width, IStyle style, String altText, int imageId) {
		writer.openTag("v:shape"); //$NON-NLS-1$
		writer.attribute("id", "_x0000_i10" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("type", "#_x0000_t" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("alt", altText); //$NON-NLS-1$
		writer.attribute("style", "width:" + width + "pt;height:" + height + "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		drawImageBordersColor(style);
		writer.openTag("v:imagedata"); //$NON-NLS-1$
		writer.attribute("src", "wordml://" + imageId + ".png"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.attribute("otitle", ""); //$NON-NLS-1$
		writer.closeTag("v:imagedata"); //$NON-NLS-1$
		drawImageBordersStyle(style);
		writer.closeTag("v:shape"); //$NON-NLS-1$
	}

	public void writeContent(int type, String txt, IStyle style, IStyle inlineStyle, String fontFamily,
			HyperlinkInfo info, InlineFlag inlineFlag, TextFlag flag, int paragraphWidth, boolean runIsRtl,
			String textAlign) {
		if (inlineFlag == InlineFlag.BLOCK) {
			writeText(type, txt, style, fontFamily, info, flag, paragraphWidth, runIsRtl);
		} else {
			boolean isInline = true;
			if (inlineFlag == InlineFlag.FIRST_INLINE && flag == TextFlag.START) {
				startParagraph(style, isInline, paragraphWidth, textAlign);
			}
			if (inlineStyle != null)
				writeTextInRun(type, txt, inlineStyle, fontFamily, info, isInline, paragraphWidth, runIsRtl, textAlign);
			else
				writeTextInRun(type, txt, style, fontFamily, info, isInline, paragraphWidth, runIsRtl, textAlign);
		}
	}

	protected void openHyperlink(HyperlinkInfo info) {
		if (info == null) {
			return;
		}
		writer.openTag("w:hlink"); //$NON-NLS-1$
		if (HyperlinkInfo.BOOKMARK == info.getType()) {
			writer.attribute("w:bookmark", info.getUrl()); //$NON-NLS-1$
		} else if (HyperlinkInfo.HYPERLINK == info.getType()) {
			writer.attribute("w:dest", info.getUrl()); //$NON-NLS-1$
			if (info.getBookmark() != null) {
				writer.attribute("w:bookmark", info.getBookmark()); //$NON-NLS-1$
			}
		}
		if (info.getTooltip() != null) {
			writer.attribute("w:screenTip", info.getTooltip()); //$NON-NLS-1$
		}
	}

	protected void closeHyperlink(HyperlinkInfo info) {
		if ((info == null) || (info.getType() == HyperlinkInfo.DRILL)) {
			return;
		}
		writer.closeTag("w:hlink"); //$NON-NLS-1$
	}

	public void writeBookmark(String bm) {
		bm = WordUtil.validBookmarkName(bm);

		writer.openTag("aml:annotation"); //$NON-NLS-1$
		writer.attribute("aml:id", bookmarkId); //$NON-NLS-1$
		writer.attribute("w:type", "Word.Bookmark.Start"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:name", bm); //$NON-NLS-1$
		writer.closeTag("aml:annotation"); //$NON-NLS-1$

		writer.openTag("aml:annotation"); //$NON-NLS-1$
		writer.attribute("aml:id", bookmarkId); //$NON-NLS-1$
		writer.attribute("w:type", "Word.Bookmark.End"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("aml:annotation"); //$NON-NLS-1$

		bookmarkId++;
	}

	public void close() {
		writer.close();
	}

	protected void writeTableLayout() {
		writer.openTag("w:tblLayout"); //$NON-NLS-1$
		writer.attribute("w:type", "Fixed"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tblLayout"); //$NON-NLS-1$
	}

	protected void writeFontSize(IStyle style) {
		CSSValue fontSize = style.getProperty(StyleConstants.STYLE_FONT_SIZE);
		int size = WordUtil.parseFontSize(PropertyUtil.getDimensionValue(fontSize));
		writeAttrTag("w:sz", size); //$NON-NLS-1$
		writeAttrTag("w:sz-cs", size); //$NON-NLS-1$
	}

	protected void writeFont(String fontFamily) {
		writer.openTag("w:rFonts"); //$NON-NLS-1$
		writer.attribute("w:ascii", fontFamily); //$NON-NLS-1$
		writer.attribute("w:fareast", fontFamily); //$NON-NLS-1$
		writer.attribute("w:h-ansi", fontFamily); //$NON-NLS-1$
		writer.attribute("w:cs", fontFamily); //$NON-NLS-1$
		writer.closeTag("w:rFonts"); //$NON-NLS-1$
	}

	protected void writeFontStyle(IStyle style) {
		String val = WordUtil.removeQuote(style.getFontStyle());
		if (!"normal".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:i", "on"); //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:i-cs", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void writeFontWeight(IStyle style) {
		String val = WordUtil.removeQuote(style.getFontWeight());
		if (!"normal".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:b", "on"); //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:b-cs", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void drawDocumentBackground(String bgcolor, String backgroundImageUrl, String backgroundHeight,
			String backgroundWidth) {
		// Image priority is higher than color.
		writer.openTag("w:bgPict"); //$NON-NLS-1$
		if (backgroundImageUrl != null && backgroundHeight == null && backgroundWidth == null) {
			try {
				byte[] backgroundImageData = EmitterUtil.getImageData(backgroundImageUrl);
				drawDocumentBackgroundImage(backgroundImageData);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getLocalizedMessage());
			}
		} else
			drawDocumentBackgroundColor(bgcolor);
		writer.closeTag("w:bgPict"); //$NON-NLS-1$
	}

	public void drawDocumentBackgroundImage(String backgroundImageUrl, String height, String width, double topMargin,
			double leftMargin, double pageHeight, double pageWidth) {
		if (backgroundImageUrl != null) {
			try {
				Image imageInfo = EmitterUtil.parseImage(null, IImageContent.IMAGE_URL, backgroundImageUrl, null, null);
				int imageWidth = imageInfo.getWidth();
				int imageHeight = imageInfo.getHeight();
				String[] realSize = WordUtil.parseBackgroundSize(height, width, imageWidth, imageHeight, pageWidth,
						pageHeight);
				byte[] backgroundImageData = EmitterUtil.getImageData(backgroundImageUrl);
				int imageId = nextImageID();
				writer.openTag("w:p"); //$NON-NLS-1$
				writeHiddenProperty();
				writer.openTag("w:r"); //$NON-NLS-1$
				writer.openTag("w:pict"); //$NON-NLS-1$
				drawImageShapeType(imageId);
				drawImageData(backgroundImageData, imageId);
				drawBackgroundImageShape(realSize, topMargin, leftMargin, imageId);
				writer.closeTag("w:pict"); //$NON-NLS-1$
				writer.closeTag("w:r"); //$NON-NLS-1$
				writer.closeTag("w:p"); //$NON-NLS-1$
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
	}

	private void drawBackgroundImageShape(String[] size, double topMargin, double leftMargin, int imageId) {
		writer.openTag("v:shape"); //$NON-NLS-1$
		writer.attribute("id", "_x0000_i10" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("type", "#_x0000_t" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("style", "position:absolute;left:0;text-align:left;margin-left:-" + leftMargin + "pt" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ";margin-top:-" + topMargin + "pt" + ";width:" + size[1] + ";height:" + size[0] + ";z-index:-1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		writer.openTag("v:imagedata"); //$NON-NLS-1$
		writer.attribute("src", "wordml://" + imageId + ".png"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.attribute("otitle", ""); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:imagedata"); //$NON-NLS-1$
		writer.closeTag("v:shape"); //$NON-NLS-1$
	}

	private void drawDocumentBackgroundImage(byte[] data) {
		int imgId = nextImageID();
		drawImageData(data, imgId);
		writer.openTag("w:background"); //$NON-NLS-1$
		writer.attribute("w:bgcolor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:background", "wordml://" + imgId + ".png"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.closeTag("w:background"); //$NON-NLS-1$
	}

	private void drawDocumentBackgroundColor(String data) {
		String color = WordUtil.parseColor(data);
		if (color != null) {
			writer.openTag("w:background"); //$NON-NLS-1$
			writer.attribute("w:bgcolor", color); //$NON-NLS-1$
			writer.closeTag("w:background"); //$NON-NLS-1$
		}
	}

	public void startTableRow(double height) {
		startTableRow(height, false, false, false, false);
	}

	public void startPage() {
		writer.openTag("wx:sect"); //$NON-NLS-1$
	}

	public void endPage() {
		writer.closeTag("wx:sect"); //$NON-NLS-1$
	}

	public void end() {
		writer.closeTag("w:body"); //$NON-NLS-1$
		writer.closeTag("w:wordDocument"); //$NON-NLS-1$
		writer.close();
	}

	public void startHeader(boolean showHeaderOnFirst, int headerHeight, int headerWidth) {
		if (!showHeaderOnFirst) {
			writer.openTag("w:hdr"); //$NON-NLS-1$
			writer.attribute("w:type", "first"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.openTag("w:p"); //$NON-NLS-1$
			writer.openTag("w:r"); //$NON-NLS-1$
			writer.closeTag("w:r"); //$NON-NLS-1$
			writer.closeTag("w:p"); //$NON-NLS-1$
			writer.closeTag("w:hdr"); //$NON-NLS-1$
		}
		writer.openTag("w:hdr"); //$NON-NLS-1$
		writer.attribute("w:type", "odd"); //$NON-NLS-1$ //$NON-NLS-2$
		startHeaderFooterContainer(headerHeight, headerWidth);
	}

	public void endHeader() {
		endHeaderFooterContainer();
		writer.closeTag("w:hdr"); //$NON-NLS-1$
	}

	public boolean mustCloneFooter() {
		return true;
	}

	public void startFooter(boolean isFirstPage, int footerHeight, int footerWidth ) {
		writer.openTag("w:ftr"); //$NON-NLS-1$
		writer.attribute("w:type", (isFirstPage ? "first" : "odd")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		startHeaderFooterContainer(footerHeight, footerWidth);
	}

	public void endFooter() {
		endHeaderFooterContainer();
		writer.closeTag("w:ftr"); //$NON-NLS-1$
	}

	public void writeTOC(String tocText, int level) {
		writeTOC(tocText, null, level, false);
	}

	public void writeTOC(String tocText, String color, int level, boolean middleInline) {
		if (!middleInline) {
			writer.openTag("w:p"); //$NON-NLS-1$
		}

		if (color != null) {
			writer.openTag("w:pPr"); //$NON-NLS-1$
			writer.openTag("w:shd"); //$NON-NLS-1$
			writer.attribute("w:val", "clear"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.attribute("w:color", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.attribute("w:fill", color); //$NON-NLS-1$
			writer.closeTag("w:shd"); //$NON-NLS-1$
			writer.openTag("w:rPr"); //$NON-NLS-1$
			writer.openTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:rPr"); //$NON-NLS-1$
			writer.closeTag("w:pPr"); //$NON-NLS-1$
		} else {
			writer.openTag("w:pPr"); //$NON-NLS-1$
			writer.openTag("w:rPr"); //$NON-NLS-1$
			writer.openTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:rPr"); //$NON-NLS-1$
			writer.closeTag("w:pPr"); //$NON-NLS-1$
		}

		writer.openTag("aml:annotation"); //$NON-NLS-1$
		writer.attribute("aml:id", bookmarkId); //$NON-NLS-1$
		writer.attribute("w:type", "Word.Bookmark.Start"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:name", "_Toc" + tocText); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("aml:annotation"); //$NON-NLS-1$

		writer.openTag("aml:annotation"); //$NON-NLS-1$
		writer.attribute("aml:id", bookmarkId++); //$NON-NLS-1$
		writer.attribute("w:type", "Word.Bookmark.End"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("aml:annotation"); //$NON-NLS-1$

		writeField(true);
		writeTocText(tocText, level);
		writeField(false);
		if (!middleInline) {
			writer.closeTag("w:p"); //$NON-NLS-1$
		}
	}

	protected void writeVmerge(SpanInfo spanInfo) {
		if (spanInfo.isStart()) {
			writeAttrTag("w:vmerge", "restart"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			writer.openTag("w:vmerge"); //$NON-NLS-1$
			writer.closeTag("w:vmerge"); //$NON-NLS-1$
		}
	}

	public void writeForeign(IForeignContent foreignContent) {
	}

	public void writePageBorders(IStyle style, int topMargin, int bottomMargin, int leftMargin, int rightMargin) {
		writer.openTag("w:pgBorders"); //$NON-NLS-1$
		writer.attribute("w:offset-from", "page"); //$NON-NLS-1$ //$NON-NLS-2$
		writeBorders(style, topMargin, bottomMargin, leftMargin, rightMargin);
		writer.closeTag("w:pgBorders"); //$NON-NLS-1$

	}

	protected void writeIndent(int textIndent) {
		writer.openTag("w:ind"); //$NON-NLS-1$
		writer.attribute("w:first-line", textIndent); //$NON-NLS-1$
		writer.closeTag("w:ind"); //$NON-NLS-1$
	}

	protected void writeIndent(int leftMargin, int rightMargin, int textIndent) {
		if (leftMargin == 0 && rightMargin == 0 && textIndent == 0) {
			return;
		}
		writer.openTag("w:ind"); //$NON-NLS-1$
		if (leftMargin != 0) {
			writer.attribute("w:left", leftMargin); //$NON-NLS-1$
		}

		if (rightMargin != 0) {
			writer.attribute("w:right", rightMargin); //$NON-NLS-1$
		}

		if (textIndent != 0) {
			writer.attribute("w:first-line", textIndent); //$NON-NLS-1$
		}
		writer.closeTag("w:ind"); //$NON-NLS-1$
	}
}
