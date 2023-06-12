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

package org.eclipse.birt.report.engine.emitter.docx.writer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.emitter.EmitterUtil;
import org.eclipse.birt.report.engine.emitter.wpml.WordUtil;
import org.eclipse.birt.report.engine.ooxml.IPart;
import org.eclipse.birt.report.engine.ooxml.ImageManager.ImagePart;
import org.eclipse.birt.report.engine.ooxml.constants.ContentTypes;
import org.eclipse.birt.report.engine.ooxml.constants.NameSpaces;
import org.eclipse.birt.report.engine.ooxml.constants.RelationshipTypes;
import org.eclipse.birt.report.engine.ooxml.writer.OOXmlWriter;

/**
 * Representation of document
 *
 * @since 3.3
 *
 */
public class Document extends BasicComponent {

	private static Logger logger = Logger.getLogger(Document.class.getName());

	private String backgroundColor;

	private String backgroundImageImgUrl;

	private String backgroundHeight;
	private String backgroundWidth;

	private int headerId = 1;

	private int footerId = 1;
	private int mhtId = 1;

	private int wordVersion;

	Document(IPart part, String backgroundColor, String backgroundImageUrl, String backgroundHeight,
			String backgroundWidth, boolean rtl, int wordVersion) throws IOException {
		super(part);
		this.backgroundColor = backgroundColor;
		this.backgroundImageImgUrl = backgroundImageUrl;
		this.backgroundHeight = backgroundHeight;
		this.backgroundWidth = backgroundWidth;
		this.wordVersion = wordVersion;
		this.rtl = rtl;
		writeStylesPart();
		writeSettingsPart();
	}

	@Override
	void start() {
		writer.startWriter();
		writer.openTag("w:document"); //$NON-NLS-1$
		writeXmlns();
		drawDocumentBackground();
		writer.openTag("w:body"); //$NON-NLS-1$
	}

	private void writeStylesPart() throws IOException {
		String uri = "styles.xml"; //$NON-NLS-1$
		String type = ContentTypes.WORD_STYLES;
		String relationshipType = RelationshipTypes.STYLES;
		IPart stylesPart = part.getPart(uri, type, relationshipType);
		OOXmlWriter stylesPartWriter = null;
		try {
			stylesPartWriter = stylesPart.getWriter();
			stylesPartWriter.startWriter();
			stylesPartWriter.openTag("w:styles"); //$NON-NLS-1$
			stylesPartWriter.nameSpace("w", NameSpaces.WORD_PROCESSINGML); //$NON-NLS-1$
			stylesPartWriter.openTag("w:docDefaults"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:rPrDefault"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:rPr"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:rFonts"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:ascii", "Times New Roman"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:eastAsia", "Times New Roman"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:hAnsi", "Times New Roman"); //$NON-NLS-1$//$NON-NLS-2$
			stylesPartWriter.attribute("w:cs", "Times New Roman"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:rFonts"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:lang"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", "en-US"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:eastAsia", "zh-CN"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:bidi", "ar-SA"); //$NON-NLS-1$//$NON-NLS-2$
			stylesPartWriter.closeTag("w:lang"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:rPr"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:rPrDefault"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:pPrDefault"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:pPrDefault"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:docDefaults"); //$NON-NLS-1$
//			stylesPartWriter.openTag( "w:style" );
//			stylesPartWriter.attribute( "w:type", "paragraph" );
//			stylesPartWriter.attribute( "w:default", "4" );
//			stylesPartWriter.attribute( "w:styleId", "Normal" );
//			stylesPartWriter.openTag( "w:name" );
//			stylesPartWriter.attribute( "w:val", "Normal" );
//			stylesPartWriter.closeTag( "w:name" );
//			stylesPartWriter.openTag( "w:autoRedefine" );
//			stylesPartWriter.closeTag( "w:autoRedefine" );
//			stylesPartWriter.openTag( "w:semiHidden" );
//			stylesPartWriter.closeTag( "w:semiHidden" );
//			stylesPartWriter.openTag( "w:rsid" );
//			stylesPartWriter.attribute( "w:val", "009B3C8F" );
//			stylesPartWriter.closeTag( "w:rsid" );
//			stylesPartWriter.openTag( "w:pPr" );
//			stylesPartWriter.openTag( "w:pStyle" );
//			stylesPartWriter.attribute( "w:val", "Normal" );
//			stylesPartWriter.closeTag( "w:pStyle" );
//			stylesPartWriter.openTag( "w:bidi" );
//			if ( !rtl )
//			{
//				stylesPartWriter.attribute( "w:val", "off" );
//			}
//			stylesPartWriter.closeTag( "w:bidi" );
//			stylesPartWriter.closeTag( "w:pPr" );
//			stylesPartWriter.closeTag( "w:style" );
			stylesPartWriter.openTag("w:style"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "character"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:styleId", "Hyperlink"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.openTag("w:name"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", "Hyperlink"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:name"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:rPr"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:u"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", "single"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:u"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:color"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", "0000ff"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:color"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:rPr"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:style"); //$NON-NLS-1$

			stylesPartWriter.openTag("w:style"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "table"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.attribute("w:default", 1); //$NON-NLS-1$
			stylesPartWriter.attribute("w:styleId", "TableNormal"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.openTag("w:name"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", "Normal Table"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:name"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:uiPriority"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:val", 99); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:uiPriority"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:semiHidden"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:semiHidden"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:unhidenWhenUsed"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:unhidenWhenUsed"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:qFormat"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:qFormat"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:tblPr"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:tblInd"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:w", 0); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:tblInd"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:tblCellMar"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:top"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:w", 0); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:top"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:left"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:w", 108); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:left"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:bottom"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:w", 0); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:bottom"); //$NON-NLS-1$
			stylesPartWriter.openTag("w:right"); //$NON-NLS-1$
			stylesPartWriter.attribute("w:w", 108); //$NON-NLS-1$
			stylesPartWriter.attribute("w:type", "dxa"); //$NON-NLS-1$ //$NON-NLS-2$
			stylesPartWriter.closeTag("w:right"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:tblCellMar"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:tblPr"); //$NON-NLS-1$
			stylesPartWriter.closeTag("w:style"); //$NON-NLS-1$

			stylesPartWriter.closeTag("w:styles"); //$NON-NLS-1$
			stylesPartWriter.endWriter();
		} finally {
			if (stylesPartWriter != null) {
				stylesPartWriter.close();
			}
		}
	}

	private void writeSettingsPart() throws IOException {
		String uri = "settings.xml"; //$NON-NLS-1$
		String type = ContentTypes.WORD_SETTINGS;
		String relationshipType = RelationshipTypes.SETTINGS;
		IPart settingsPart = part.getPart(uri, type, relationshipType);
		OOXmlWriter settingsPartWriter = null;
		try {
			settingsPartWriter = settingsPart.getWriter();
			settingsPartWriter.startWriter();
			switch (wordVersion) {
			case 2010:
				settingsPartWriter.openTag("w:settings"); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w", NameSpaces.WORD_PROCESSINGML); //$NON-NLS-1$
				settingsPartWriter.nameSpace("o", NameSpaces.OFFICE); //$NON-NLS-1$
				settingsPartWriter.nameSpace("r", NameSpaces.RELATIONSHIPS); //$NON-NLS-1$
				settingsPartWriter.openTag("w:zoom"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:percent", "100"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:zoom"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:displayBackgroundShape"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:displayBackgroundShape"); //$NON-NLS-1$
				// settingsPartWriter.openTag( "w:proofState" );
				// settingsPartWriter.attribute( "w:spelling", "clean" );
				// settingsPartWriter.attribute( "w:grammar", "clean" );
				// settingsPartWriter.closeTag( "w:proofState" );
				settingsPartWriter.openTag("w:view"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:val", "print"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:view"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:compat"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:compatSetting"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:name", "w:compatibilityMode"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.attribute("w:uri", "http://schemas.microsoft.com/office/word"); //$NON-NLS-1$//$NON-NLS-2$
				settingsPartWriter.attribute("w:val", "12"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:compatSetting"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:compat"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:settings"); //$NON-NLS-1$
				break;
			default:
				settingsPartWriter.openTag("w:settings"); //$NON-NLS-1$
				settingsPartWriter.nameSpace("mc", NameSpaces.MARKUP_COMPATIBILITY); //$NON-NLS-1$
				settingsPartWriter.nameSpace("o", NameSpaces.OFFICE); //$NON-NLS-1$
				settingsPartWriter.nameSpace("r", NameSpaces.RELATIONSHIPS); //$NON-NLS-1$
				settingsPartWriter.nameSpace("m", NameSpaces.MATH); //$NON-NLS-1$
				settingsPartWriter.nameSpace("v", NameSpaces.VML); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w10", NameSpaces.W10); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w", NameSpaces.WORD_PROCESSINGML); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w14", NameSpaces.W14); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w15", NameSpaces.W15); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w16cex", NameSpaces.W16CEX); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w16cid", NameSpaces.W16CID); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w16", NameSpaces.W16); //$NON-NLS-1$
				settingsPartWriter.nameSpace("w16se", NameSpaces.W16SE); //$NON-NLS-1$
				settingsPartWriter.nameSpace("sl", NameSpaces.SCHEMA_LIBRARY); //$NON-NLS-1$
				settingsPartWriter.attribute("mc:Ignorable", "w14 w15 w16se w16cid w16 w16cex"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.openTag("w:zoom"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:percent", "100"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:zoom"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:displayBackgroundShape"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:displayBackgroundShape"); //$NON-NLS-1$
				// settingsPartWriter.openTag( "w:proofState" );
				// settingsPartWriter.attribute( "w:spelling", "clean" );
				// settingsPartWriter.attribute( "w:grammar", "clean" );
				// settingsPartWriter.closeTag( "w:proofState" );
				settingsPartWriter.openTag("w:view"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:val", "print"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:view"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:compat"); //$NON-NLS-1$
				settingsPartWriter.openTag("w:compatSetting"); //$NON-NLS-1$
				settingsPartWriter.attribute("w:name", "compatibilityMode"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.attribute("w:uri", "http://schemas.microsoft.com/office/word"); //$NON-NLS-1$//$NON-NLS-2$
				settingsPartWriter.attribute("w:val", "15"); //$NON-NLS-1$ //$NON-NLS-2$
				settingsPartWriter.closeTag("w:compatSetting"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:compat"); //$NON-NLS-1$
				settingsPartWriter.closeTag("w:settings"); //$NON-NLS-1$
			}
			settingsPartWriter.endWriter();
		} finally {
			if (settingsPartWriter != null) {
				settingsPartWriter.close();
			}
		}
	}

	@Override
	void end() {
		writer.closeTag("w:body"); //$NON-NLS-1$
		writer.closeTag("w:document"); //$NON-NLS-1$
		writer.endWriter();
		writer.close();
	}

	private void drawDocumentBackground() {
		if (backgroundImageImgUrl != null && backgroundHeight == null && backgroundWidth == null) {
			try {
				byte[] backgroundImageData = EmitterUtil.getImageData(backgroundImageImgUrl);
				ImagePart imagePart = imageManager.getImagePart(part, backgroundImageImgUrl, backgroundImageData);
				IPart part = imagePart.getPart();
				drawDocumentBackgroundImage(part);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		} else {
			String color = WordUtil.parseColor(backgroundColor);
			if (color != null) {
				writer.openTag("w:background"); //$NON-NLS-1$
				writer.attribute("w:color", color); //$NON-NLS-1$
				writer.closeTag("w:background"); //$NON-NLS-1$
			}
		}
	}

	private void drawDocumentBackgroundImage(IPart imagePart) {
		writer.openTag("w:background"); //$NON-NLS-1$
		writer.attribute("w:color", "FFFFFF"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("v:background"); //$NON-NLS-1$
		writer.attribute("id", ""); //$NON-NLS-1$//$NON-NLS-2$
		writer.openTag("v:fill"); //$NON-NLS-1$
		writer.attribute("r:id", imagePart.getRelationshipId()); //$NON-NLS-1$
		writer.attribute("recolor", "t"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("type", "frame"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("v:fill"); //$NON-NLS-1$
		writer.closeTag("v:background"); //$NON-NLS-1$
		writer.closeTag("w:background"); //$NON-NLS-1$
	}

	void writeHeaderReference(BasicComponent header, String type) {
		writer.openTag("w:headerReference"); //$NON-NLS-1$
		writer.attribute("w:type", type); //$NON-NLS-1$
		writer.attribute("r:id", header.getRelationshipId()); //$NON-NLS-1$
		writer.closeTag("w:headerReference"); //$NON-NLS-1$
	}

	void writeFooterReference(BasicComponent footer, String type) {
		writer.openTag("w:footerReference"); //$NON-NLS-1$
		writer.attribute("w:type", type); //$NON-NLS-1$
		writer.attribute("r:id", footer.getRelationshipId()); //$NON-NLS-1$
		writer.closeTag("w:footerReference"); //$NON-NLS-1$
	}

	Header createHeader(boolean showHeaderOnFirst, int headerHeight, int headerWidth) throws IOException {
		if (!showHeaderOnFirst) {
			// Add an empty header for the first page, which overrides the default header.
			String uri = "header" + nextHeaderID() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
			String type = ContentTypes.WORD_HEADER;
			String relationshipType = RelationshipTypes.HEADER;
			IPart headerPart = part.getPart(uri, type, relationshipType);
			Header firstPageHeader = new Header(headerPart, this, headerHeight, headerWidth);
			firstPageHeader.start();
			firstPageHeader.end();
			writeHeaderReference(firstPageHeader, "first");
		}
		// Start a default header. It will be closed by DocxWriter::endHeader.
		String uri = "header" + nextHeaderID() + ".xml"; //$NON-NLS-1$//$NON-NLS-2$
		String type = ContentTypes.WORD_HEADER;
		String relationshipType = RelationshipTypes.HEADER;
		IPart headerPart = part.getPart(uri, type, relationshipType);
		return new Header(headerPart, this, headerHeight, headerWidth);
	}

	Footer createFooter(int footerHeight, int footerWidth) throws IOException {
		String uri = "footer" + nextFooterID() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
		String type = ContentTypes.WORD_FOOTER;
		String relationshipType = RelationshipTypes.FOOTER;
		IPart footerPart = part.getPart(uri, type, relationshipType);
		return new Footer(footerPart, this, footerHeight, footerWidth);
	}

	private int nextHeaderID() {
		return headerId++;
	}

	private int nextFooterID() {
		return footerId++;
	}

	@Override
	protected int nextImageID() {
		return imageId++;
	}

	@Override
	protected int nextMhtTextId() {
		return mhtId++;
	}

	void writePageBorders(IStyle style, int topMargin, int bottomMargin, int leftMargin, int rightMargin) {
		writer.openTag("w:pgBorders"); //$NON-NLS-1$
		writer.attribute("w:offsetFrom", "page"); //$NON-NLS-1$ //$NON-NLS-2$
		writeBorders(style, topMargin, bottomMargin, leftMargin, rightMargin);
		writer.closeTag("w:pgBorders"); //$NON-NLS-1$
	}

	@Override
	public void writeEmptyElement( String tag )
	{
		writer.openTag( tag );
		writer.closeTag( tag );
	}
}
