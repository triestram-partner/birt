/*************************************************************************************
 * Copyright (c) 2011, 2012, 2013 James Talbut.
 *  jim-emitters@spudsoft.co.uk
 *
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Talbut - Initial implementation.
 ************************************************************************************/

package uk.co.spudsoft.birt.emitters.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.dom.AreaStyle;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSValueConstants;
import org.eclipse.birt.report.engine.ir.DimensionType;
import org.eclipse.birt.report.model.api.util.ColorUtil;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.w3c.dom.css.CSSValue;

import uk.co.spudsoft.birt.emitters.excel.framework.Logger;

/**
 * StyleManagerXUtils is an extension of the StyleManagerUtils to provide XSSFWorkbook specific functionality.
 * @author Jim Talbut
 *
 */
@SuppressWarnings("nls")
public class StyleManagerXUtils extends StyleManagerUtils {

	private static Factory factory = new StyleManagerUtils.Factory() {
		public StyleManagerUtils create(Logger log) {
			return new StyleManagerXUtils(log);
		}
	};

	/**
	 * Get the factory of the style manager
	 *
	 * @return Return factory of style manager
	 */
	public static Factory getFactory() {
		return factory;
	}

	/**
	 * @param log
	 * Logger used by StyleManagerXUtils to record anything of interest.
	 */
	public StyleManagerXUtils(Logger log) {
		super(log);
	}

	@Override
	public RichTextString createRichTextString(String value) {
		XSSFRichTextString result = new XSSFRichTextString(value);
		return result;
	}

	/**
	 * Converts a BIRT border style into a POI BorderStyle.
	 * @param birtBorder
	 * The BIRT border style.
	 * @param width
	 * The width of the border as understood by BIRT.
	 * @return
	 * A POI BorderStyle object.
	 */
	private BorderStyle poiBorderStyleFromBirt( String birtBorder, String width ) {
		if( "none".equals(birtBorder) ) {
			return BorderStyle.NONE;
		}
		double pxWidth = 3.0;
		if( CSSConstants.CSS_THIN_VALUE.equals( width ) ) {
			pxWidth = 1.0;
		} else if( CSSConstants.CSS_MEDIUM_VALUE.equals( width ) ) {
			pxWidth = 3.0;
		} else if( CSSConstants.CSS_THICK_VALUE.equals( width ) ) {
			pxWidth = 4.0;
		} else {
			DimensionType dim = DimensionType.parserUnit( width );
			if( dim != null ) {
				if( "px".equals(dim.getUnits()) ) {
					pxWidth = dim.getMeasure();
				}
			}
		}
		if( "solid".equals(birtBorder) ) {
			if( pxWidth < 2.9 ) {
				return BorderStyle.THIN;
			} else if( pxWidth < 3.1 ) {
				return BorderStyle.MEDIUM;
			} else {
				return BorderStyle.THICK;
			}
		} else if( "dashed".equals(birtBorder) ) {
			if( pxWidth < 2.9 ) {
				return BorderStyle.DASHED;
			}
			return BorderStyle.MEDIUM_DASHED;

		} else if ("dotted".equals(birtBorder)) {
			return BorderStyle.DOTTED;
		} else if( "double".equals(birtBorder) ) {
			return BorderStyle.DOUBLE;
		}

		log.debug( "Border style \"", birtBorder, "\" is not recognised." );
		return BorderStyle.NONE;
	}

	/**
	 * Create e new cell border object
	 *
	 * @param stylesSource original workbook style source
	 * @param cellXf       cell object of the workbook to be styled
	 * @return Return the border object of cell
	 * @since 4.13
	 */
	private CTBorder getCTBorder(StylesTable stylesSource, CTXf cellXf) {
		CTBorder ct;
		if (cellXf.getApplyBorder()) {
			int idx = (int) cellXf.getBorderId();
			XSSFCellBorder cf = stylesSource.getBorderAt(idx);
			ct = (CTBorder) cf.getCTBorder().copy();
		} else {
			ct = CTBorder.Factory.newInstance();
		}
		return ct;
	}

	/**
	 * Set all border lines to the cell
	 *
	 * @param stylesSource workbook style sheet source
	 * @param cellXf       cell object
	 * @param theme        color theme object
	 * @param birtStyle    birt cell style with all border information
	 * @since 4.13
	 */
	public void setBorderAll(StylesTable stylesSource, CTXf cellXf, ThemesTable theme, BirtStyle birtStyle) {

		String borderTopColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_COLOR).getCssText());
		String borderTopStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_STYLE) == null ? "none"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_STYLE).getCssText());
		String borderTopWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_WIDTH) == null ? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_WIDTH).getCssText());

		String borderBottomColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_COLOR).getCssText());
		String borderBottomStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_STYLE) == null
				? "none"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_STYLE).getCssText());
		String borderBottomWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_WIDTH) == null
				? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_WIDTH).getCssText());

		String borderLeftColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_COLOR).getCssText());
		String borderLeftStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_STYLE) == null ? "none"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_STYLE).getCssText());
		String borderLeftWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_WIDTH) == null ? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_WIDTH).getCssText());

		String borderRightColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_COLOR).getCssText());
		String borderRightStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_STYLE) == null ? "none"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_STYLE).getCssText());
		String borderRightWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_WIDTH) == null
				? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_WIDTH).getCssText());

		String diagonalColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_COLOR).getCssText());
		String diagonalStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_STYLE) == null ? "none"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_STYLE).getCssText());
		String diagonalWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_WIDTH) == null
				? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_WIDTH).getCssText());

		String antidiagonalColor = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_COLOR) == null
				? "rgb(0,0,0)"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_COLOR).getCssText());
		String antidiagonalStyle = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_STYLE) == null
				? "solid"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_STYLE).getCssText());
		String antidiagonalWidth = (birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_WIDTH) == null
				? "medium"
				: birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_WIDTH).getCssText());

		CTBorder ct = getCTBorder(stylesSource, cellXf);
		CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();
		BorderStyle border = null;
		XSSFColor xBorderColour = null;

		// border get the used color map
		IndexedColorMap colorMap = stylesSource.getIndexedColors();
		XSSFCellBorderExtended cb = new XSSFCellBorderExtended(ct, theme, colorMap);

		// border: diagonal & antidiagonal
		BorderStyle borderDiagonal = poiBorderStyleFromBirt(diagonalStyle, diagonalWidth);
		BorderStyle borderAntidiagonal = poiBorderStyleFromBirt(antidiagonalStyle, antidiagonalWidth);

		if (borderDiagonal == BorderStyle.NONE && ct.isSetDiagonalDown())
			ct.unsetDiagonalDown();
		if (borderAntidiagonal == BorderStyle.NONE && ct.isSetDiagonalUp())
			ct.unsetDiagonalUp();

		if (borderDiagonal != BorderStyle.NONE || borderAntidiagonal != BorderStyle.NONE) {
			if (borderDiagonal != BorderStyle.NONE) {
				xBorderColour = getXColour(diagonalColor);
				border = borderDiagonal;
				ct.setDiagonalDown(true);
				cb.setDiagonal(true);
				cb.setDiagonalColor(xBorderColour);
				cb.setDiagonalStyle(border);
			}
			if (borderAntidiagonal != BorderStyle.NONE) {
				if (xBorderColour == null)
					xBorderColour = getXColour(antidiagonalColor);
				if (border == null)
					border = borderAntidiagonal;
				ct.setDiagonalUp(true);
				cb.setAntidiagonal(true);
				cb.setAntidiagonalColor(xBorderColour);
				cb.setAntidiagonalStyle(border);
			}
			ct.setDiagonal(pr);

			// border style & width
			pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));

			// border color
			pr.addNewColor().setRgb(xBorderColour.getRGB());
		}

		// border: top
		border = poiBorderStyleFromBirt(borderTopStyle, borderTopWidth);
		if (border != BorderStyle.NONE) {
			xBorderColour = getXColour(borderTopColor);
			cb.setBorderStyle(BorderSide.TOP, border);
			cb.setBorderColor(BorderSide.TOP, xBorderColour);
		}

		// border: bottom
		border = poiBorderStyleFromBirt(borderBottomStyle, borderBottomWidth);
		if (border != BorderStyle.NONE) {
			xBorderColour = getXColour(borderBottomColor);
			cb.setBorderStyle(BorderSide.BOTTOM, border);
			cb.setBorderColor(BorderSide.BOTTOM, xBorderColour);
		}

		// border: left
		border = poiBorderStyleFromBirt(borderLeftStyle, borderLeftWidth);
		if (border != BorderStyle.NONE) {
			xBorderColour = getXColour(borderLeftColor);
			cb.setBorderStyle(BorderSide.LEFT, border);
			cb.setBorderColor(BorderSide.LEFT, xBorderColour);
		}

		// border: right
		border = poiBorderStyleFromBirt(borderRightStyle, borderRightWidth);
		if (border != BorderStyle.NONE) {
			xBorderColour = getXColour(borderRightColor);
			cb.setBorderStyle(BorderSide.RIGHT, border);
			cb.setBorderColor(BorderSide.RIGHT, xBorderColour);
		}

		// assign border to cell
		int idx = stylesSource.putBorder(cb);
		cellXf.setBorderId(idx);
		cellXf.setApplyBorder(true);
	}

	@Override
	public void applyBorderStyle(Workbook workbook, CellStyle style, BorderSide side, CSSValue colour,
			CSSValue borderStyle, CSSValue width) {
		// method due to compatibility reasons of StyleManagerUtil & StyleManager
	}

	@Override
	public void applyBorderStyle(Workbook workbook, CellStyle style, BirtStyle birtStyle) {
		applyBorderStyleToCell(workbook, style, birtStyle);
	}

	private void applyBorderStyleToCell(Workbook workbook, CellStyle style, BirtStyle birtStyle) {

		if (style instanceof XSSFCellStyle) {
			XSSFCellStyle xStyle = (XSSFCellStyle) style;
			StylesTable stylesSource = null;
			if (stylesSource == null) {
				if (workbook instanceof SXSSFWorkbook) {
					stylesSource = ((SXSSFWorkbook) workbook).getXSSFWorkbook().getStylesSource();
				} else if (workbook instanceof XSSFWorkbook) {
					stylesSource = ((XSSFWorkbook) workbook).getStylesSource();
				}
			}
				// style based on style & width
			if (stylesSource != null) {
				ThemesTable theme = stylesSource.getTheme();
				CTXf cellXf = xStyle.getCoreXf();
				setBorderAll(stylesSource, cellXf, theme, birtStyle);
			}
		}
	}

	private XSSFColor getXColour(String colour) {
		int[] rgbInt = ColorUtil.getRGBs(colour);
		if( rgbInt == null ) {
			return null;
		}
		byte[] rgbByte = { (byte)-1, (byte)rgbInt[0], (byte)rgbInt[1], (byte)rgbInt[2] };
		// System.out.println( "The X colour for " + colour + " is [ " + rgbByte[0] + "," + rgbByte[1] + "," + rgbByte[2] + "," + rgbByte[3] + "]" );
		XSSFColor result = new XSSFColor(rgbByte, new DefaultIndexedColorMap()); // @TODO is it OK to do it that way or
																					// do we have to manage our own
																					// ColorMap?
		return result;
	}

	@Override
	public void addColourToFont(Workbook workbook, Font font, String colour) {
		if ((colour == null) || CSSValueConstants.TRANSPARENT_VALUE.equals(colour)) {
			return;
		}
		if(font instanceof XSSFFont) {
			XSSFFont xFont = (XSSFFont)font;
			XSSFColor xColour = getXColour(colour);

			if(xColour != null) {
				xFont.setColor(xColour);
			}
		}
	}

	@Override
	public void addBackgroundColourToStyle(Workbook workbook, CellStyle style, String colour) {
		if ((colour == null) || CSSValueConstants.TRANSPARENT_VALUE.equals(colour)) {
			return;
		}
		if(style instanceof XSSFCellStyle) {
			XSSFCellStyle cellStyle = (XSSFCellStyle)style;
			XSSFColor xColour = getXColour(colour);
			if(xColour != null) {
				cellStyle.setFillForegroundColor(xColour);
				cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			}
		}
	}

	@Override
	public Font correctFontColorIfBackground( FontManager fm, Workbook wb, BirtStyle birtStyle, Font font ) {
		CSSValue bgColour = birtStyle.getProperty( StylePropertyIndexes.STYLE_BACKGROUND_COLOR );
		int bgRgb[] = parseColour( bgColour == null ? null : bgColour.getCssText(), "white" );

		XSSFColor colour = ((XSSFFont)font).getXSSFColor();
		int fgRgb[] = rgbOnly(colour.getARGB());
		if( ( fgRgb[0] == 255 ) && ( fgRgb[1] == 255 ) && ( fgRgb[2] == 255 ) ) {
			fgRgb[0]=fgRgb[1]=fgRgb[2]=0;
		} else if( ( fgRgb[0] == 0 ) && ( fgRgb[1] == 0 ) && ( fgRgb[2] == 0 ) ) {
			fgRgb[0]=fgRgb[1]=fgRgb[2]=255;
		}

		if( ( bgRgb[ 0 ] == fgRgb[ 0 ] ) && ( bgRgb[ 1 ] == fgRgb[ 1 ] ) && ( bgRgb[ 2 ] == fgRgb[ 2 ] ) ) {

			IStyle addedStyle = new AreaStyle( fm.getCssEngine() );
			addedStyle.setColor( contrastColour( bgRgb ) );

			return fm.getFontWithExtraStyle(font, addedStyle);
		}
		return font;
	}

	@Override
	public int anchorDxFromMM( double widthMM, double colWidthMM ) {
        return (int)(widthMM * 36000);
	}

	@Override
	public int anchorDyFromPoints( float height, float rowHeight ) {
		return (int) (height * Units.EMU_PER_POINT);
	}

}
