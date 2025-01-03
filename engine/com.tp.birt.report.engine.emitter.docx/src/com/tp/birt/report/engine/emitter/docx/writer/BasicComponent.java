/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
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

package com.tp.birt.report.engine.emitter.docx.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IForeignContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.engine.StyleConstants;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.eclipse.birt.report.engine.emitter.EmitterUtil;
import org.eclipse.birt.report.engine.emitter.HTMLTags;
import org.eclipse.birt.report.engine.emitter.HTMLWriter;
import org.eclipse.birt.report.engine.executor.css.HTMLProcessor;
import org.eclipse.birt.report.engine.ir.DimensionType;
import org.eclipse.birt.report.engine.ir.EngineIRConstants;
import org.eclipse.birt.report.engine.layout.pdf.util.PropertyUtil;
import org.eclipse.birt.report.engine.ooxml.IPart;
import org.eclipse.birt.report.engine.ooxml.ImageManager;
import org.eclipse.birt.report.engine.ooxml.ImageManager.ImagePart;
import org.eclipse.birt.report.engine.ooxml.MimeType;
import org.eclipse.birt.report.engine.ooxml.constants.NameSpaces;
import org.eclipse.birt.report.engine.ooxml.constants.RelationshipTypes;
import org.eclipse.birt.report.engine.ooxml.writer.OOXmlWriter;
import org.eclipse.birt.report.engine.parser.TextParser;
import org.eclipse.birt.report.engine.util.FileUtil;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSValue;

import com.tp.birt.report.engine.emitter.wpml.AbstractEmitterImpl.InlineFlag;
import com.tp.birt.report.engine.emitter.wpml.HyperlinkInfo;
import com.tp.birt.report.engine.emitter.wpml.SpanInfo;
import com.tp.birt.report.engine.emitter.wpml.WordUtil;
import com.tp.birt.report.engine.emitter.wpml.writer.AbstractWordXmlWriter;

public abstract class BasicComponent extends AbstractWordXmlWriter {

	private static Logger logger = Logger.getLogger(BasicComponent.class.getName());

	protected ImageManager imageManager;

	protected IPart part;

	private final String validHtml = "^\\s*(.*)<html(.*?)>(.*?)</html>\\s*$"; //$NON-NLS-1$

	private final int DISPLAY_BLOCK = 1;

	private final int DISPLAY_FLAG_ALL = 0xffff;

	private final int DISPLAY_INLINE = 2;

	private final int DISPLAY_INLINE_BLOCK = 4;

	private final int DISPLAY_NONE = 8;

	private OOXmlWriter ooxmlWriter;

	private OOXmlWriter mhtPartWriter;

	private final String BOUNDARY = "___Actuate_Content_Boundary___"; //$NON-NLS-1$

	private List<String> imageSrc = new ArrayList<String>();

	private ReportDesignHandle handle;

	protected BasicComponent(IPart part) throws IOException {
		this.part = part;
		this.imageManager = (ImageManager) part.getPackage().getExtensionData();
		ooxmlWriter = part.getCacheWriter();
		writer = ooxmlWriter;
	}

	protected void writeXmlns() {
		ooxmlWriter.nameSpace("ve", NameSpaces.VE); //$NON-NLS-1$
		ooxmlWriter.nameSpace("o", NameSpaces.OFFICE); //$NON-NLS-1$
		ooxmlWriter.nameSpace("r", NameSpaces.RELATIONSHIPS); //$NON-NLS-1$
		ooxmlWriter.nameSpace("m", NameSpaces.MATH); //$NON-NLS-1$
		ooxmlWriter.nameSpace("v", NameSpaces.VML); //$NON-NLS-1$
		ooxmlWriter.nameSpace("wp", NameSpaces.WORD_DRAWING); //$NON-NLS-1$
		ooxmlWriter.nameSpace("w10", NameSpaces.WORD); //$NON-NLS-1$
		ooxmlWriter.nameSpace("w", NameSpaces.WORD_PROCESSINGML); //$NON-NLS-1$
		ooxmlWriter.nameSpace("wne", NameSpaces.WORDML); //$NON-NLS-1$
	}

	protected void drawImage(byte[] data, double height, double width, HyperlinkInfo hyper, IStyle style,
			InlineFlag inlineFlag, String altText, String uri) {
		int imageId = nextImageID();
		IPart imagePart = null;
		if (data != null) {
			try {
				ImagePart imgPart = imageManager.getImagePart(part, uri, data);
				imagePart = imgPart.getPart();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		if (inlineFlag == InlineFlag.FIRST_INLINE || inlineFlag == InlineFlag.BLOCK) {
			writer.openTag("w:p"); //$NON-NLS-1$
		}

		openHyperlink(hyper);
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:pict"); //$NON-NLS-1$
		drawImageShapeType(imageId);
		drawImageShape(height, width, style, altText, imageId, imagePart);
		writer.closeTag("w:pict"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
		closeHyperlink(hyper);

		if (inlineFlag == InlineFlag.BLOCK) {
			writer.closeTag("w:p"); //$NON-NLS-1$
		}
	}

	private void drawImageShape(double height, double width, IStyle style, String altText, int imageId,
			IPart imagePart) {
		writer.openTag("v:shape"); //$NON-NLS-1$
		writer.attribute("id", "_x0000_i10" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("type", "#_x0000_t" + imageId); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("alt", altText); //$NON-NLS-1$
		writer.attribute("style", "width:" + width + "pt;height:" + height + "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		drawImageBordersColor(style);
		writer.openTag("v:imagedata"); //$NON-NLS-1$
		if (imagePart != null) {
			writer.attribute("r:id", imagePart.getRelationshipId()); //$NON-NLS-1$
			writer.attribute("r:href", part.getExternalImageId("ooxWord:/" + imagePart.getAbsoluteUri())); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			writer.attribute("r:id", part.getExternalImageId("wordml://" + imageId + ".png")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		writer.closeTag("v:imagedata"); //$NON-NLS-1$
		drawImageBordersStyle(style);
		writer.closeTag("v:shape"); //$NON-NLS-1$
	}

	@Override
	protected void openHyperlink(HyperlinkInfo info) {
		if (info == null) {
			return;
		}
		writer.openTag("w:hyperlink"); //$NON-NLS-1$

		if (HyperlinkInfo.BOOKMARK == info.getType()) {
			writer.attribute("w:anchor", info.getUrl()); //$NON-NLS-1$
		} else if (HyperlinkInfo.HYPERLINK == info.getType()) {
			if (info.getUrl() != null) {
				String url = info.getUrl().replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				writer.attribute("r:id", part.getHyperlinkId(url)); //$NON-NLS-1$
			}
			if (info.getBookmark() != null) {
				writer.attribute("w:anchor", info.getBookmark()); //$NON-NLS-1$
			}
		}
		if (info.getTooltip() != null) {
			writer.attribute("w:tooltip", info.getTooltip()); //$NON-NLS-1$
		}
	}

	@Override
	protected void closeHyperlink(HyperlinkInfo info) {
		if ((info == null) || (info.getType() == HyperlinkInfo.DRILL)) {
			return;
		}
		writer.closeTag("w:hyperlink"); //$NON-NLS-1$
	}

	@Override
	protected void writeTableLayout() {
		writer.openTag("w:tblLayout"); //$NON-NLS-1$
		writer.attribute("w:type", "fixed"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:tblLayout"); //$NON-NLS-1$
	}

	@Override
	protected void writeFontSize(IStyle style) {
		CSSValue fontSize = style.getProperty(StyleConstants.STYLE_FONT_SIZE);
		int size = WordUtil.parseFontSize(PropertyUtil.getDimensionValue(fontSize));
		writeAttrTag("w:sz", size); //$NON-NLS-1$
		writeAttrTag("w:szCs", size); //$NON-NLS-1$
	}

	@Override
	protected void writeFont(String fontFamily) {
		writer.openTag("w:rFonts"); //$NON-NLS-1$
		writer.attribute("w:ascii", fontFamily); //$NON-NLS-1$
		writer.attribute("w:eastAsia", fontFamily); //$NON-NLS-1$
		writer.attribute("w:hAnsi", fontFamily); //$NON-NLS-1$
		writer.attribute("w:cs", fontFamily); //$NON-NLS-1$
		writer.closeTag("w:rFonts"); //$NON-NLS-1$
	}

	@Override
	protected void writeFontStyle(IStyle style) {
		String val = WordUtil.removeQuote(style.getFontStyle());
		if (!"normal".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:i", "on"); //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:iCs", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	protected void writeFontWeight(IStyle style) {
		String val = WordUtil.removeQuote(style.getFontWeight());
		if (!"normal".equalsIgnoreCase(val)) { //$NON-NLS-1$
			writeAttrTag("w:b", "on"); //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:bCs", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void writeTOC(String tocText, int level) {
		writeTOC(tocText, null, level, false);
	}

	protected void writeTOC(String tocText, String color, int level, boolean middleInline) {
		if (!middleInline) {
			writer.openTag("w:p"); //$NON-NLS-1$
		}
		if (color != null && color.length() != 0) {
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
			writer.openTag("w:rPr"); //$NON-NLS-1$
			writer.openTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:vanish"); //$NON-NLS-1$
			writer.closeTag("w:rPr"); //$NON-NLS-1$
		}

		writer.openTag("w:bookmarkStart"); //$NON-NLS-1$
		writer.attribute("w:id", bookmarkId); //$NON-NLS-1$
		writer.attribute("w:name", "_Toc" + tocText); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:bookmarkStart"); //$NON-NLS-1$
		writer.openTag("w:bookmarkEnd"); //$NON-NLS-1$
		writer.attribute("w:id", bookmarkId); //$NON-NLS-1$
		writer.closeTag("w:bookmarkEnd"); //$NON-NLS-1$

		writeField(true);
		writeTocText(tocText, level);
		writeField(false);
		if (!middleInline) {
			writer.closeTag("w:p"); //$NON-NLS-1$
		}
	}

	@Override
	protected void writeVmerge(SpanInfo spanInfo) {
		if (spanInfo.isStart()) {
			writeAttrTag("w:vMerge", "restart"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			writer.openTag("w:vMerge"); //$NON-NLS-1$
			writer.closeTag("w:vMerge"); //$NON-NLS-1$
		}
	}

	protected void writeBookmark(String bm) {
		String bookmark = WordUtil.validBookmarkName(bm);

		writer.openTag("w:bookmarkStart"); //$NON-NLS-1$
		writer.attribute("w:id", bookmarkId); //$NON-NLS-1$
		writer.attribute("w:name", bookmark); //$NON-NLS-1$
		writer.closeTag("w:bookmarkStart"); //$NON-NLS-1$

		writer.openTag("w:bookmarkEnd"); //$NON-NLS-1$
		writer.attribute("w:id", bookmarkId); //$NON-NLS-1$
		writer.closeTag("w:bookmarkEnd"); //$NON-NLS-1$

		bookmarkId++;
	}

	protected void writeForeign(IForeignContent foreignContent) {
		if (foreignContent.getRawValue() != null) {
			String uri = "mhtText" + nextMhtTextId() + ".mht"; //$NON-NLS-1$ //$NON-NLS-2$
			MimeType type = MimeType.MHT;
			String relationshipType = RelationshipTypes.AFCHUNK;
			IPart mhtPart = part.getPart(uri, type, relationshipType);
			handle = foreignContent.getReportContent().getDesign().getReportDesign();
			writeMhtPart(mhtPart, foreignContent);
			writer.openTag("w:altChunk"); //$NON-NLS-1$
			writer.attribute("r:id", mhtPart.getRelationshipId()); //$NON-NLS-1$
			writer.closeTag("w:altChunk"); //$NON-NLS-1$
		}
	}

	private void writeMhtPart(IPart mhtPart, IForeignContent foreignContent) {
		try {
			mhtPartWriter = mhtPart.getWriter();
			mhtPartWriter.println("From:"); //$NON-NLS-1$
			mhtPartWriter.println("Subject:"); //$NON-NLS-1$
			mhtPartWriter.println("Date:"); //$NON-NLS-1$
			mhtPartWriter.println("MIME-Version: 1.0"); //$NON-NLS-1$
			mhtPartWriter.println("Content-Type: multipart/related; type=\"text/html\"; boundary=\"" + BOUNDARY + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			writeHtmlText(foreignContent);
			writeImages();
		} catch (IOException | EncoderException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (mhtPartWriter != null) {
				mhtPartWriter.close();
				mhtPartWriter = null;
			}
		}
	}

	private void buildHtmlBody(IForeignContent foreignContent, String foreignText, IStyle style,
			StringBuffer htmlBuffer) throws EncoderException, UnsupportedEncodingException {
		htmlBuffer.append("<body>"); //$NON-NLS-1$
		DimensionType x = foreignContent.getX();
		DimensionType y = foreignContent.getY();
		DimensionType width = foreignContent.getWidth();
		DimensionType height = foreignContent.getHeight();
		int display = getElementType(x, y, width, height, style);
		String tagName = getTagByType(display, DISPLAY_FLAG_ALL);
		if (null != tagName) {
			htmlBuffer.append("<div"); //$NON-NLS-1$
			if (tagName.equalsIgnoreCase("span")) { //$NON-NLS-1$
				htmlBuffer.append(" style=\"display: inline\" "); //$NON-NLS-1$
			}
		}
		if (style != null && !style.isEmpty()) {
			htmlBuffer.append(" class=\"styleForeign\""); //$NON-NLS-1$
		}
		StringBuffer foreignStyles = new StringBuffer();
		buildForeignStyles(foreignContent, foreignStyles, display);
		if (foreignStyles.length() > 0) {
			htmlBuffer.append(" style =\""); //$NON-NLS-1$
			htmlBuffer.append(foreignStyles + "\""); //$NON-NLS-1$
		}
		htmlBuffer.append(">"); //$NON-NLS-1$

		Map appContext = foreignContent.getReportContent().getReportContext() == null ? null
				: foreignContent.getReportContent().getReportContext().getAppContext();
		htmlBuffer.append(normalize(foreignText, appContext));
		htmlBuffer.append("</" + tagName + ">"); //$NON-NLS-1$//$NON-NLS-2$
		htmlBuffer.append("</body>"); //$NON-NLS-1$
		String quotedPritableHtml = encodcAsQuotedPrintable(htmlBuffer.toString());
		mhtPartWriter.println(quotedPritableHtml);
	}

	private String encodcAsQuotedPrintable(String normalizedHtml) throws EncoderException {
		return new QuotedPrintableCodec().encode(normalizedHtml);
	}

	private String normalize(String foreignText, Map appContext) throws UnsupportedEncodingException {
		Document doc = new TextParser().parse(foreignText, TextParser.TEXT_TYPE_HTML);
		HTMLProcessor htmlProcessor = new HTMLProcessor(handle, appContext);
		HashMap<String, String> styleMap = new HashMap<String, String>();
		Element body = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		HTMLWriter htmlWriter = new HTMLWriter();
		htmlWriter.setEnableCompactMode(true); // HVB, bug 519375
		htmlWriter.open(byteOut);
		if (doc != null) {
			NodeList bodys = doc.getElementsByTagName("body"); //$NON-NLS-1$
			if (bodys.getLength() > 0) {
				body = (Element) bodys.item(0);
			}
		}
		if (body != null) {
			htmlProcessor.execute(body, styleMap);
			processNodes(body, styleMap, htmlWriter, appContext);
		}

		htmlWriter.close();
		return new String(byteOut.toByteArray(), "UTF-8"); //$NON-NLS-1$
	}

	private void buildStyleClass(IStyle style, StringBuffer htmlBuffer) {
		StringBuffer styleBuffer = new StringBuffer();
		buildStyle(styleBuffer, style);
		if (styleBuffer.length() > 0) {
			htmlBuffer.append("<head>"); //$NON-NLS-1$
			htmlBuffer.append("<style type=" + "\"text/css\"" + ">"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			htmlBuffer.append(".styleForeign"); //$NON-NLS-1$
			htmlBuffer.append('{');
			htmlBuffer.append(styleBuffer.toString());
			htmlBuffer.append('}');
			htmlBuffer.append("</style>"); //$NON-NLS-1$
			htmlBuffer.append("</head>"); //$NON-NLS-1$
		}
	}

	private void buildStyle(StringBuffer styleBuffer, IStyle style) {
		if (style == null || style.isEmpty()) {
			return;
		}

		buildFont(styleBuffer, style);
		buildBox(styleBuffer, style);
		buildText(styleBuffer, style);
		buildVisual(styleBuffer, style);
		buildTextDecoration(styleBuffer, style);
		buildDirection(styleBuffer, style);
	}

	private void buildDirection(StringBuffer styleBuffer, IStyle style) {
		String direction = style.getDirection();
		if (CSSConstants.CSS_RTL_VALUE.equals(style.getDirection())) {
			// set direction to rtl
			styleBuffer.append(" direction:"); //$NON-NLS-1$
			styleBuffer.append(direction);
			styleBuffer.append(";"); //$NON-NLS-1$
			styleBuffer.append("unicode-bidi:didi-override;"); //$NON-NLS-1$
		}
	}

	private int getElementType(DimensionType x, DimensionType y, DimensionType width, DimensionType height,
			IStyle style) {
		int type = 0;
		String display = null;
		if (style != null) {
			display = style.getDisplay();
		}

		if (EngineIRConstants.DISPLAY_NONE.equalsIgnoreCase(display)) {
			type |= DISPLAY_NONE;
		}

		if (x != null || y != null) {
			return type | DISPLAY_BLOCK;
		} else if (EngineIRConstants.DISPLAY_INLINE.equalsIgnoreCase(display)) {
			type |= DISPLAY_INLINE;
			if (width != null || height != null) {
				type |= DISPLAY_INLINE_BLOCK;
			}
			return type;
		}

		return type | DISPLAY_BLOCK;
	}

	private String getTagByType(int display, int mask) {
		int flag = display & mask;
		String tag = null;
		if ((flag & DISPLAY_BLOCK) > 0) {
			tag = HTMLTags.TAG_DIV;
		}

		if ((flag & DISPLAY_INLINE) > 0) {
			tag = HTMLTags.TAG_SPAN;
		}
		return tag;
	}

	private void buildForeignStyles(IForeignContent foreignContent, StringBuffer foreignStyles, int display) {
		IStyle style = foreignContent.getComputedStyle();
		foreignStyles.setLength(0);
		buildTextAlign(foreignStyles, style);
		style = getElementStyle(foreignContent);
		if (style == null) {
			return;
		}
		buildFont(foreignStyles, style);
		buildBox(foreignStyles, style);
		buildText(foreignStyles, style);
		buildVisual(foreignStyles, style);
		buildTextDecoration(foreignStyles, style);
	}

	private IStyle getElementStyle(IContent content) {
		IStyle style = content.getInlineStyle();
		if (style == null || style.isEmpty()) {
			return null;
		}
		return style;
	}

	private void buildTextAlign(StringBuffer foreignStyles, IStyle style) {
		// build the text-align
		String textAlign = style.getTextAlign();
		if (textAlign != null) {
			foreignStyles.append(" text-align:"); //$NON-NLS-1$
			foreignStyles.append(textAlign);
			foreignStyles.append(";"); //$NON-NLS-1$
		}
	}

	private void buildFont(StringBuffer styleBuffer, IStyle style) {
		buildProperty(styleBuffer, HTMLTags.ATTR_FONT_FAMILY, style.getFontFamily());

		buildProperty(styleBuffer, HTMLTags.ATTR_FONT_STYLE, style.getFontStyle());

		buildProperty(styleBuffer, HTMLTags.ATTR_FONT_VARIANT, style.getFontVariant());

		buildProperty(styleBuffer, HTMLTags.ATTR_FONT_WEIGTH, style.getFontWeight());

		buildProperty(styleBuffer, HTMLTags.ATTR_FONT_SIZE, style.getFontSize());

		buildProperty(styleBuffer, HTMLTags.ATTR_COLOR, style.getColor());
	}

	private void buildProperty(StringBuffer styleBuffer, String name, String value) {
		if (value != null) {
			addPropName(styleBuffer, name);
			addPropValue(styleBuffer, value);
			styleBuffer.append(';');
		}
	}

	private void addPropName(StringBuffer styleBuffer, String name) {
		styleBuffer.append(' ');
		styleBuffer.append(name);
		styleBuffer.append(':');
	}

	private void addPropValue(StringBuffer styleBuffer, String value) {
		if (value != null) {
			styleBuffer.append(' ');
			styleBuffer.append(value);
		}
	}

	private void buildBox(StringBuffer styleBuffer, IStyle style) {
		buildMargins(styleBuffer, style);
		buildPaddings(styleBuffer, style);
	}

	/**
	 * Build the margins.
	 *
	 * @param styleBuffer
	 * @param style
	 */
	private void buildMargins(StringBuffer styleBuffer, IStyle style) {
		// build the margins
		String topMargin = style.getMarginTop();
		String rightMargin = style.getMarginRight();
		String bottomMargin = style.getMarginBottom();
		String leftMargin = style.getMarginLeft();

		if (null != topMargin && null != rightMargin && null != bottomMargin && null != leftMargin) {
			if (rightMargin.equals(leftMargin)) {
				if (topMargin.equals(bottomMargin)) {
					if (topMargin.equals(rightMargin)) {
						// The four margins have the same value
						buildProperty(styleBuffer, HTMLTags.ATTR_MARGIN, topMargin);
					} else {
						// The top & bottom margins have the same value. The
						// right & left margins have the same value.
						addPropName(styleBuffer, HTMLTags.ATTR_MARGIN);
						addPropValue(styleBuffer, topMargin);
						addPropValue(styleBuffer, rightMargin);
						styleBuffer.append(';');
					}
				} else {
					// only the right & left margins have the same value.
					addPropName(styleBuffer, HTMLTags.ATTR_MARGIN);
					addPropValue(styleBuffer, topMargin);
					addPropValue(styleBuffer, rightMargin);
					addPropValue(styleBuffer, bottomMargin);
					styleBuffer.append(';');
				}
			} else {
				// four margins have different values.
				addPropName(styleBuffer, HTMLTags.ATTR_MARGIN);
				addPropValue(styleBuffer, topMargin);
				addPropValue(styleBuffer, rightMargin);
				addPropValue(styleBuffer, bottomMargin);
				addPropValue(styleBuffer, leftMargin);
				styleBuffer.append(';');
			}
		} else {
			// At least one margin has null value.
			buildProperty(styleBuffer, HTMLTags.ATTR_MARGIN_TOP, topMargin);
			buildProperty(styleBuffer, HTMLTags.ATTR_MARGIN_RIGHT, rightMargin);
			buildProperty(styleBuffer, HTMLTags.ATTR_MARGIN_BOTTOM, bottomMargin);
			buildProperty(styleBuffer, HTMLTags.ATTR_MARGIN_LEFT, leftMargin);
		}
	}

	/**
	 * Build the paddings.
	 *
	 * @param styleBuffer
	 * @param style
	 */
	public void buildPaddings(StringBuffer styleBuffer, IStyle style) {
		// build the paddings
		String topPadding = style.getPaddingTop();
		String rightPadding = style.getPaddingRight();
		String bottomPadding = style.getPaddingBottom();
		String leftPadding = style.getPaddingLeft();
		if (null != topPadding && null != rightPadding && null != bottomPadding && null != leftPadding) {
			if (rightPadding.equals(leftPadding)) {
				if (topPadding.equals(bottomPadding)) {
					if (topPadding.equals(rightPadding)) {
						// The four paddings have the same value
						buildProperty(styleBuffer, HTMLTags.ATTR_PADDING, topPadding);
					} else {
						// The top & bottom paddings have the same value. The
						// right & left paddings have the same value.
						addPropName(styleBuffer, HTMLTags.ATTR_PADDING);
						addPropValue(styleBuffer, topPadding);
						addPropValue(styleBuffer, rightPadding);
						styleBuffer.append(';');
					}
				} else {
					// only the right & left paddings have the same value.
					addPropName(styleBuffer, HTMLTags.ATTR_PADDING);
					addPropValue(styleBuffer, topPadding);
					addPropValue(styleBuffer, rightPadding);
					addPropValue(styleBuffer, bottomPadding);
					styleBuffer.append(';');
				}
			} else {
				// four paddings have different values.
				addPropName(styleBuffer, HTMLTags.ATTR_PADDING);
				addPropValue(styleBuffer, topPadding);
				addPropValue(styleBuffer, rightPadding);
				addPropValue(styleBuffer, bottomPadding);
				addPropValue(styleBuffer, leftPadding);
				styleBuffer.append(';');
			}
		} else {
			// At least one paddings has null value.
			buildProperty(styleBuffer, HTMLTags.ATTR_PADDING_TOP, topPadding);
			buildProperty(styleBuffer, HTMLTags.ATTR_PADDING_RIGHT, rightPadding);
			buildProperty(styleBuffer, HTMLTags.ATTR_PADDING_BOTTOM, bottomPadding);
			buildProperty(styleBuffer, HTMLTags.ATTR_PADDING_LEFT, leftPadding);
		}
	}

	private void buildText(StringBuffer styleBuffer, IStyle style) {
		buildProperty(styleBuffer, HTMLTags.ATTR_TEXT_INDENT, style.getTextIndent());

		buildProperty(styleBuffer, HTMLTags.ATTR_LETTER_SPACING, style.getLetterSpacing());
		buildProperty(styleBuffer, HTMLTags.ATTR_WORD_SPACING, style.getWordSpacing());
		buildProperty(styleBuffer, HTMLTags.ATTR_TEXT_TRANSFORM, style.getTextTransform());
		buildProperty(styleBuffer, HTMLTags.ATTR_WHITE_SPACE, style.getWhiteSpace());
	}

	private void buildVisual(StringBuffer styleBuffer, IStyle style) {
		buildProperty(styleBuffer, HTMLTags.ATTR_LINE_HEIGHT, style.getLineHeight()); // $NON-NLS-1$
	}

	private void buildTextDecoration(StringBuffer styleBuffer, IStyle style) {
		CSSValue linethrough = style.getProperty(IStyle.STYLE_TEXT_LINETHROUGH);
		CSSValue underline = style.getProperty(IStyle.STYLE_TEXT_UNDERLINE);
		CSSValue overline = style.getProperty(IStyle.STYLE_TEXT_OVERLINE);

		if (linethrough == IStyle.LINE_THROUGH_VALUE || underline == IStyle.UNDERLINE_VALUE
				|| overline == IStyle.OVERLINE_VALUE) {
			styleBuffer.append(" text-decoration:"); //$NON-NLS-1$
			if (IStyle.LINE_THROUGH_VALUE == linethrough) {
				addPropValue(styleBuffer, "line-through"); //$NON-NLS-1$
			}
			if (IStyle.UNDERLINE_VALUE == underline) {
				addPropValue(styleBuffer, "underline"); //$NON-NLS-1$
			}
			if (IStyle.OVERLINE_VALUE == overline) {
				addPropValue(styleBuffer, "overline"); //$NON-NLS-1$
			}
			styleBuffer.append(';');
		}
	}

	private void writeImages() {
		for (String uri : imageSrc) {
			String imageType = uri.substring(uri.indexOf('.') + 1);
			mhtPartWriter.println();
			mhtPartWriter.println("--" + BOUNDARY); //$NON-NLS-1$
			mhtPartWriter.println("Content-Type: image/" + imageType); //$NON-NLS-1$
			mhtPartWriter.println("Content-Transfer-Encoding: base64"); //$NON-NLS-1$
			mhtPartWriter.println("Content-Location:" + uri); //$NON-NLS-1$
			mhtPartWriter.println();

			try {
				byte[] data = EmitterUtil.getImageData(uri);
				if (data != null && data.length != 0) {
					Base64 base = new Base64();
					String pic2Text = new String(base.encode(data));
					mhtPartWriter.println(pic2Text);
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
		mhtPartWriter.println();
		mhtPartWriter.println("--" + BOUNDARY + "--"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void writeHtmlText(IForeignContent foreignContent) throws EncoderException, UnsupportedEncodingException {
		mhtPartWriter.println();
		mhtPartWriter.println("--" + BOUNDARY); //$NON-NLS-1$
		mhtPartWriter.println("Content-Type: text/html; charset=\"gb2312\""); //$NON-NLS-1$
		mhtPartWriter.println("Content-Transfer-Encoding: quoted-printable"); //$NON-NLS-1$
		mhtPartWriter.println();

		StringBuffer htmlBuffer = new StringBuffer();
		String foreignText = foreignContent.getRawValue().toString();
		String headInformation = null;
		String htmlAttribute = null;
		Pattern pattern = Pattern.compile(validHtml, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(foreignText);
		if (matcher.find() && matcher.group(0).length() == foreignText.length()) {
			headInformation = matcher.group(1);
			htmlAttribute = matcher.group(2);
			foreignText = matcher.group(3);
		}
		mhtPartWriter.print("=EF=BB=BF"); //$NON-NLS-1$
		if (headInformation != null) {
			htmlBuffer.append(headInformation + " "); //$NON-NLS-1$
		}
		htmlBuffer.append("<html"); //$NON-NLS-1$
		if (htmlAttribute != null) {
			htmlBuffer.append(" " + htmlAttribute); //$NON-NLS-1$
		}
		htmlBuffer.append(">"); //$NON-NLS-1$
		IStyle style = foreignContent.getComputedStyle();
		buildStyleClass(style, htmlBuffer);
		buildHtmlBody(foreignContent, foreignText, style, htmlBuffer);
		mhtPartWriter.print("</html>"); //$NON-NLS-1$
	}

	private void processNodes(Element ele, HashMap cssStyles, HTMLWriter writer, Map appContext) {
		for (Node node = ele.getFirstChild(); node != null; node = node.getNextSibling()) {
			// At present we only deal with the text, comment and element nodes
			short nodeType = node.getNodeType();
			if (nodeType == Node.TEXT_NODE) {
				if (isScriptText(node)) {
					writer.cdata(node.getNodeValue());
				} else {
					// bug132213 in text item should only deal with the
					// escape special characters: < > &
					// writer.text( node.getNodeValue( ), false, true );
					writer.text(node.getNodeValue());
				}
			} else if (nodeType == Node.COMMENT_NODE) {
				writer.comment(node.getNodeValue());
			} else if (nodeType == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if ("br".equalsIgnoreCase(node.getNodeName())) { //$NON-NLS-1$
					// <br/> is correct. <br></br> is not correct. The brower
					// will treat the <br></br> as <br><br>
					boolean bImplicitCloseTag = writer.isImplicitCloseTag();
					writer.setImplicitCloseTag(true);
					startNode(node, cssStyles, writer, appContext);
					processNodes((Element) node, cssStyles, writer, appContext);
					endNode(node, writer);
					writer.setImplicitCloseTag(bImplicitCloseTag);
				} else {
					startNode(node, cssStyles, writer, appContext);
					processNodes((Element) node, cssStyles, writer, appContext);
					endNode(node, writer);
				}
			}
		}
	}

	public void startNode(Node node, HashMap cssStyles, HTMLWriter writer, Map appContext) {
		String nodeName = node.getNodeName();
		HashMap cssStyle = (HashMap) cssStyles.get(node);
		writer.openTag(nodeName);
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String attrName = attribute.getNodeName();
				String attrValue = attribute.getNodeValue();

				if (attrValue != null) {
					if ("img".equalsIgnoreCase(nodeName) && "src".equalsIgnoreCase(attrName)) { //$NON-NLS-1$ //$NON-NLS-2$
						String attrValueTrue = handleStyleImage(attrValue, appContext);
						if (attrValueTrue != null) {
							attrValue = attrValueTrue;
						}
					}
					writer.attribute(attrName, attrValue);
				}
			}
		}
		if (cssStyle != null) {
			StringBuilder buffer = new StringBuilder();
			Iterator ite = cssStyle.entrySet().iterator();
			while (ite.hasNext()) {
				Map.Entry entry = (Map.Entry) ite.next();
				Object keyObj = entry.getKey();
				Object valueObj = entry.getValue();
				if (keyObj == null || valueObj == null) {
					continue;
				}
				String key = keyObj.toString();
				String value = valueObj.toString();
				buffer.append(key);
				buffer.append(":"); //$NON-NLS-1$
				if ("background-image".equalsIgnoreCase(key)) { //$NON-NLS-1$
					String valueTrue = handleStyleImage(value, appContext);
					if (valueTrue != null) {
						value = valueTrue;
					}
					buffer.append("url("); //$NON-NLS-1$
					buffer.append(value);
					buffer.append(")"); //$NON-NLS-1$
				} else {
					buffer.append(value);
				}
				buffer.append(";"); //$NON-NLS-1$
			}
			if (buffer.length() != 0) {
				writer.attribute("style", buffer.toString()); //$NON-NLS-1$
			}
		}
	}

	public String handleStyleImage(String uri, Map appContext) {
		if (uri != null) {
			if (FileUtil.isLocalResource(uri)) {
				URL url = handle.findResource(uri, IResourceLocator.IMAGE, appContext);
				if (url != null) {
					uri = url.toString();
				}
			}
			imageSrc.add(uri);
		}
		return uri;
	}

	public void endNode(Node node, HTMLWriter writer) {
		writer.closeTag(node.getNodeName());
	}

	/**
	 * test if the text node is in the script
	 *
	 * @param node text node
	 * @return true if the text is a script, otherwise, false.
	 */
	private boolean isScriptText(Node node) {
		Node parent = node.getParentNode();
		if (parent != null) {
			if (parent.getNodeType() == Node.ELEMENT_NODE) {
				String tag = parent.getNodeName();
				if (HTMLTags.TAG_SCRIPT.equalsIgnoreCase(tag)) {
					return true;
				}
			}
		}
		return false;
	}

	public String validHtmlText(String foreignText) {
		Pattern pattern = Pattern.compile(validHtml, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(foreignText);
		if (matcher.matches()) {
			return foreignText;
		} else
			return "<html>" + foreignText + "</html>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String getRelationshipId() {
		return part.getRelationshipId();
	}

	@Override
	public void startTableRow(double height, boolean isHeader, boolean repeatHeader, boolean fixedLayout,
			boolean cantSplit) {
		writer.openTag("w:tr"); //$NON-NLS-1$

		// write the row height, unit: twips
		writer.openTag("w:trPr"); //$NON-NLS-1$

		if (height != -1) {
			writer.openTag("w:trHeight"); //$NON-NLS-1$
			if (fixedLayout) {
				writer.attribute("w:hRule", "exact"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.attribute("w:val", height); //$NON-NLS-1$
			writer.closeTag("w:trHeight"); //$NON-NLS-1$
		}

		// if value is "off",the header will be not repeated
		if (isHeader) {
			String headerOnOff = repeatHeader ? "on" : "off"; //$NON-NLS-1$ //$NON-NLS-2$
			writeAttrTag("w:tblHeader", headerOnOff); //$NON-NLS-1$
		}
		writer.closeTag("w:trPr"); //$NON-NLS-1$
	}

	@Override
	protected void writeIndent(int textIndent) {
		writer.openTag("w:ind"); //$NON-NLS-1$
		writer.attribute("w:firstLine", textIndent); //$NON-NLS-1$
		writer.closeTag("w:ind"); //$NON-NLS-1$
	}

	@Override
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
			writer.attribute("w:firstLine", textIndent); //$NON-NLS-1$
		}
		writer.closeTag("w:ind"); //$NON-NLS-1$
	}

	abstract void start();

	abstract void end();

	abstract protected int nextMhtTextId();
}
