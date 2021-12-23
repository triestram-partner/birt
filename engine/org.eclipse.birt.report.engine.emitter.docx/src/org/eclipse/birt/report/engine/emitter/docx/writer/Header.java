/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.docx.writer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.content.IImageContent;
import org.eclipse.birt.report.engine.emitter.EmitterUtil;
import org.eclipse.birt.report.engine.emitter.wpml.WordUtil;
import org.eclipse.birt.report.engine.layout.emitter.Image;
import org.eclipse.birt.report.engine.ooxml.IPart;
import org.eclipse.birt.report.engine.ooxml.ImageManager.ImagePart;

public class Header extends BasicComponent {

	private static Logger logger = Logger.getLogger(Header.class.getName());

	Document document;
	int headerHeight;
	int headerWidth;

	Header(IPart part, Document document, int headerHeight, int headerWidth) throws IOException {
		super(part);
		this.document = document;
		this.headerHeight = headerHeight;
		this.headerWidth = headerWidth;
	}

	void start() {
		writer.startWriter();
		writer.openTag("w:hdr"); //$NON-NLS-1$
		writeXmlns();
		startHeaderFooterContainer(headerHeight, headerWidth, true);
	}

	void end() {
		endHeaderFooterContainer();
		writer.closeTag("w:hdr"); //$NON-NLS-1$
		writer.endWriter();
		writer.close();
	}

	protected int nextImageID() {
		return document.nextImageID();
	}

	protected int nextMhtTextId() {
		return document.nextMhtTextId();
	}

	public void drawDocumentBackgroundImageWithSize(String backgroundImageUrl, String backgroundHeight,
			String backgroundWidth, double topMargin, double leftMargin, double pageHeight, double pageWidth) {
		int imageId = nextImageID();
		IPart imagePart = null;
		if (backgroundImageUrl != null) {
			try {
				byte[] backgroundImageData = EmitterUtil.getImageData(backgroundImageUrl);

				ImagePart imgPart = imageManager.getImagePart(part, backgroundImageUrl, backgroundImageData);
				imagePart = imgPart.getPart();
				Image imageInfo = EmitterUtil.parseImage(null, IImageContent.IMAGE_URL, backgroundImageUrl, null, null);
				int imageWidth = imageInfo.getWidth();
				int imageHeight = imageInfo.getHeight();
				String[] realSize = WordUtil.parseBackgroundSize(backgroundHeight, backgroundWidth, imageWidth,
						imageHeight, pageWidth, pageHeight);
				drawBackgroundImageShape(realSize, topMargin, leftMargin, imageId, imagePart);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private void drawBackgroundImageShape(String[] size, double topMargin, double leftMargin, int imageId,
			IPart imagePart) {
		writer.openTag("w:sdt"); //$NON-NLS-1$
		writer.openTag("w:sdtPr"); //$NON-NLS-1$
		writer.openTag("w:id"); //$NON-NLS-1$
		writer.attribute("w:val", "90701258"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:id"); //$NON-NLS-1$
		writer.openTag("w:docPartObj"); //$NON-NLS-1$
		writer.openTag("w:docPartGallery"); //$NON-NLS-1$
		writer.attribute("w:val", "Cover Pages"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.closeTag("w:docPartGallery"); //$NON-NLS-1$
		writer.openTag("w:docPartUnique"); //$NON-NLS-1$
		writer.closeTag("w:docPartUnique"); //$NON-NLS-1$
		writer.closeTag("w:docPartObj"); //$NON-NLS-1$
		writer.closeTag("w:sdtPr"); //$NON-NLS-1$
		writer.openTag("w:sdtContent"); //$NON-NLS-1$
		writer.openTag("w:p"); //$NON-NLS-1$
		writer.attribute("w:rsidR", "00182958"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:rsidRDefault", "00182958"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("w:rsidP", "00182958"); //$NON-NLS-1$//$NON-NLS-2$
		writer.openTag("w:r"); //$NON-NLS-1$
		writer.openTag("w:rPr"); //$NON-NLS-1$
		writer.openTag("w:noProof"); //$NON-NLS-1$
		writer.closeTag("w:noProof"); //$NON-NLS-1$
//		writer.openTag("w:lang");
//		writer.attribute("w:eastAsia", "zh-TW");
//		writer.closeTag("w:lang");
		writer.closeTag("w:rPr"); //$NON-NLS-1$
		writer.openTag("w:pict"); //$NON-NLS-1$
		writer.openTag("v:rect"); //$NON-NLS-1$
		writer.attribute("id", "_x0000_s1041"); //$NON-NLS-1$ //$NON-NLS-2$
		String attrValue = "position:absolute;left:0;text-align:left;margin-left:" + 0// Seems leftMargin //$NON-NLS-1$
																						// should not be
																						// used here.
				+ "pt;margin-top:" + 0// Seems topMargin should not be used here. //$NON-NLS-1$
				+ "pt;width:" + size[1] + ";height:" + size[0] //$NON-NLS-1$ //$NON-NLS-2$
				+ ";z-index:-1;mso-width-percent:1000;mso-position-horizontal-relative:page;mso-position-vertical-relative:page;mso-width-percent:1000"; //$NON-NLS-1$
		writer.attribute("style", attrValue); //$NON-NLS-1$
		writer.attribute("o:allowincell", "f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("stroked", "f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.openTag("v:fill"); //$NON-NLS-1$
		writer.attribute("r:id", imagePart.getRelationshipId()); //$NON-NLS-1$
		writer.attribute("o:title", "exposure"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("size", "0,0"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("aspect", "atLeast"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("origin", "-32767f,-32767f"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("position", "-32767f,-32767f"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("recolor", "t"); //$NON-NLS-1$//$NON-NLS-2$
		writer.attribute("rotate", "t"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("type", "frame"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("v:fill"); //$NON-NLS-1$
		writer.openTag("w10:wrap"); //$NON-NLS-1$
		writer.attribute("anchorx", "page"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.attribute("anchory", "page"); //$NON-NLS-1$//$NON-NLS-2$
		writer.closeTag("w10:wrap"); //$NON-NLS-1$
		writer.closeTag("v:rect"); //$NON-NLS-1$
		writer.closeTag("w:pict"); //$NON-NLS-1$
		writer.closeTag("w:r"); //$NON-NLS-1$
		writer.closeTag("w:p"); //$NON-NLS-1$
		writer.closeTag("w:sdtContent"); //$NON-NLS-1$
		writer.closeTag("w:sdt"); //$NON-NLS-1$
	}
}
