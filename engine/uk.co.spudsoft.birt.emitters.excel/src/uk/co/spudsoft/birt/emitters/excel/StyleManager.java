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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.engine.CSSEngine;
import org.eclipse.birt.report.engine.css.engine.value.FloatValue;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.w3c.dom.css.CSSValue;

import uk.co.spudsoft.birt.emitters.excel.framework.Logger;

/**
 * StyleManager is a cache of POI CellStyles to enable POI CellStyles to be reused based upon their BIRT styles.
 * @author Jim Talbut
 *
 */
public class StyleManager {

	/**
	 * StylePair maintains the relationship between a BIRT style and a POI style.
	 * @author Jim Talbut
	 *
	 *
	private class StylePair {
		public BirtStyle birtStyle;
		public CellStyle poiStyle;

		public StylePair(BirtStyle birtStyle, CellStyle poiStyle) {
			this.birtStyle = birtStyle;
			this.poiStyle = poiStyle;
		}
	}*/

	private Workbook workbook;
	private FontManager fm;
	// private List<StylePair> styles = new ArrayList<StylePair>();
	private Map< BirtStyle, CellStyle > styleMap = new HashMap< BirtStyle, CellStyle >();
	private StyleManagerUtils smu;
	private CSSEngine cssEngine;
	@SuppressWarnings("unused")
	private Logger log;
	private Locale locale;

	/**
	 * @param workbook   The workbook for which styles are being tracked.
	 * @param log        Logger to be used during processing.
	 * @param smu        Set of functions for carrying out conversions between BIRT
	 *                   and POI.
	 * @param cssEngine  BIRT CSS Engine for creating BIRT styles.
	 * @param locale     Locale of the workbook
	 */
	public StyleManager(Workbook workbook, Logger log, StyleManagerUtils smu, CSSEngine cssEngine, Locale locale) {
		this.workbook = workbook;
		this.fm = new FontManager(cssEngine, workbook, smu);
		this.log = log;
		this.smu = smu;
		this.cssEngine = cssEngine;
		this.locale = locale;
	}

	/**
	 * Get the font manager of the style
	 *
	 * @return Return the font manager of the style
	 */
	public FontManager getFontManager() {
		return fm;
	}

	/**
	 * Get the CSS engine of the style
	 *
	 * @return Return the CSS engine of the style
	 */
	public CSSEngine getCssEngine() {
		return cssEngine;
	}


	static int COMPARE_CSS_PROPERTIES[] = {
		StylePropertyIndexes.STYLE_TEXT_ALIGN,
		StylePropertyIndexes.STYLE_BACKGROUND_COLOR,
		StylePropertyIndexes.STYLE_BORDER_TOP_STYLE,
		StylePropertyIndexes.STYLE_BORDER_TOP_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_TOP_COLOR,
		StylePropertyIndexes.STYLE_BORDER_LEFT_STYLE,
		StylePropertyIndexes.STYLE_BORDER_LEFT_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_LEFT_COLOR,
		StylePropertyIndexes.STYLE_BORDER_RIGHT_STYLE,
		StylePropertyIndexes.STYLE_BORDER_RIGHT_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_RIGHT_COLOR,
		StylePropertyIndexes.STYLE_BORDER_BOTTOM_STYLE,
		StylePropertyIndexes.STYLE_BORDER_BOTTOM_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_BOTTOM_COLOR,
		StylePropertyIndexes.STYLE_BORDER_DIAGONAL_NUMBER,
		StylePropertyIndexes.STYLE_BORDER_DIAGONAL_STYLE,
		StylePropertyIndexes.STYLE_BORDER_DIAGONAL_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_DIAGONAL_COLOR,
		StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_NUMBER,
		StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_STYLE,
		StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_WIDTH,
		StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_COLOR,
		StylePropertyIndexes.STYLE_WHITE_SPACE,
		StylePropertyIndexes.STYLE_VERTICAL_ALIGN,
	};

	/**
	 * Test whether two BIRT styles are equivalent, as far as the attributes understood by POI are concerned.
	 * <br/>
	 * Every attribute tested in this method must be used in the construction of the CellStyle in createStyle.
	 * @param style1
	 * The first BIRT style to be compared.
	 * @param style2
	 * The second BIRT style to be compared.
	 * @return
	 * true if style1 and style2 would produce identical CellStyles if passed to createStyle.
	 *
	private boolean stylesEquivalent( BirtStyle style1, BirtStyle style2) {

		// System.out.println( "style1: " + style1 );
		// System.out.println( "style2: " + style2 );

		for( int i = 0; i < COMPARE_CSS_PROPERTIES.length; ++i ) {
			int prop = COMPARE_CSS_PROPERTIES[ i ];
			CSSValue value1 = style1.getProperty( prop );
			CSSValue value2 = style2.getProperty( prop );
			if( ! StyleManagerUtils.objectsEqual( value1, value2 ) ) {
				// System.out.println( "Differ on " + i + " because " + value1 + " != " + value2 );
				return false;
			}
		}
		if( ! StyleManagerUtils.objectsEqual( style1.getProperty( BirtStyle.TEXT_ROTATION ), style2.getProperty( BirtStyle.TEXT_ROTATION ) ) ) {
			// System.out.println( "Differ on " + i + " because " + value1 + " != " + value2 );
			return false;
		}


		// Number format
		if( ! StyleManagerUtils.dataFormatsEquivalent( (DataFormatValue)style1.getProperty( StylePropertyIndexes.STYLE_DATA_FORMAT )
				, (DataFormatValue)style2.getProperty( StylePropertyIndexes.STYLE_DATA_FORMAT ) ) ) {
			// System.out.println( "Differ on DataFormat" );
			return false;
		}

		// Font
		if( !FontManager.fontsEquivalent( style1, style2 ) ) {
			// System.out.println( "Differ on font" );
			return false;
		}
		return true;
	}*/

	/**
	 * Create a new POI CellStyle based upon a BIRT style.
	 * @param birtStyle
	 * The BIRT style to base the CellStyle upon.
	 * @return
	 * The CellStyle whose attributes are described by the BIRT style.
	 */
	private CellStyle createStyle( BirtStyle birtStyle ) {
		CellStyle poiStyle = workbook.createCellStyle();
		// Font
		Font font = fm.getFont(birtStyle);
		if( font != null ) {
			poiStyle.setFont(font);
		}

		// Alignment
		poiStyle.setAlignment(smu.poiAlignmentFromBirtAlignment(birtStyle.getString( StylePropertyIndexes.STYLE_TEXT_ALIGN )));
		// Background colour
		smu.addBackgroundColourToStyle(workbook, poiStyle, birtStyle.getString( StylePropertyIndexes.STYLE_BACKGROUND_COLOR ));
		// Top border
		smu.applyBorderStyle(workbook, poiStyle, BorderSide.TOP, birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_COLOR), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_STYLE), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_TOP_WIDTH));
		// Left border
		smu.applyBorderStyle(workbook, poiStyle, BorderSide.LEFT, birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_COLOR), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_STYLE), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_LEFT_WIDTH));
		// Right border
		smu.applyBorderStyle(workbook, poiStyle, BorderSide.RIGHT, birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_COLOR), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_STYLE), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_RIGHT_WIDTH));
		// Bottom border
		smu.applyBorderStyle(workbook, poiStyle, BorderSide.BOTTOM, birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_COLOR), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_STYLE), birtStyle.getProperty(StylePropertyIndexes.STYLE_BORDER_BOTTOM_WIDTH));
		// Number format
		smu.applyNumberFormat(workbook, birtStyle, poiStyle, locale);
		// Whitespace/wrap
		if( CSSConstants.CSS_PRE_VALUE.equals( birtStyle.getString( StylePropertyIndexes.STYLE_WHITE_SPACE ) ) ) {
			poiStyle.setWrapText( true );
		}
		// Vertical alignment
		if( CSSConstants.CSS_TOP_VALUE.equals( birtStyle.getString( StylePropertyIndexes.STYLE_VERTICAL_ALIGN ) ) ) {
			poiStyle.setVerticalAlignment(VerticalAlignment.TOP);
		} else if ( CSSConstants.CSS_MIDDLE_VALUE.equals( birtStyle.getString( StylePropertyIndexes.STYLE_VERTICAL_ALIGN ) ) ) {
			poiStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		} else if ( CSSConstants.CSS_BOTTOM_VALUE.equals( birtStyle.getString( StylePropertyIndexes.STYLE_VERTICAL_ALIGN ) ) ) {
			poiStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
		}
		// Rotation
		CSSValue rotation = birtStyle.getProperty( BirtStyle.TEXT_ROTATION );
		if( rotation instanceof FloatValue ) {
			poiStyle.setRotation( (short) ((FloatValue)rotation).getFloatValue() );
		}

		styleMap.put( birtStyle, poiStyle );
		return poiStyle;
	}

	public CellStyle getStyle( BirtStyle birtStyle ) {
		CellStyle poiStyle = styleMap.get(birtStyle);
		if( poiStyle == null ) {
			poiStyle = createStyle( birtStyle );
		}
		return poiStyle;
	}

	private BirtStyle birtStyleFromCellStyle( CellStyle source ) {
		for( Entry< BirtStyle, CellStyle > stylePair : styleMap.entrySet() ) {
			if( source.equals(stylePair.getValue()) ) {
				return stylePair.getKey().clone();
			}
		}

		return new BirtStyle(cssEngine);
	}

	/**
	 * Given a POI CellStyle, add border definitions to it and obtain a CellStyle
	 * (from the cache or newly created) based upon that.
	 * 
	 * @param source                   The POI CellStyle to form the base style.
	 * @param borderStyleBottom        The BIRT style of the bottom border.
	 * @param borderWidthBottom        The BIRT with of the bottom border.
	 * @param borderColourBottom       The BIRT colour of the bottom border.
	 * @param borderStyleLeft          The BIRT style of the left border.
	 * @param borderWidthLeft          The BIRT width of the left border.
	 * @param borderColourLeft         The BIRT colour of the left border.
	 * @param borderStyleRight         The BIRT style of the right border.
	 * @param borderWidthRight         The BIRT width of the right border.
	 * @param borderColourRight        The BIRT color of the right border.
	 * @param borderStyleTop           The BIRT style of the top border.
	 * @param borderWidthTop           The BIRT width of the top border.
	 * @param borderColourTop          The BIRT colour of the top border.
	 * @param borderStyleDiagonal      The BIRT style of the diagonal.
	 * @param borderWidthDiagonal      The BIRT width of the diagonal.
	 * @param borderColourDiagonal     The BIRT colour of the diagonal.
	 * @param borderStyleAntidiagonal  The BIRT style of the antidiagonal.
	 * @param borderWidthAntidiagonal  The BIRT width of the antidiagonal.
	 * @param borderColourAntidiagonal The BIRT colour of the antidiagonal.
	 * @return A POI CellStyle equivalent to the source CellStyle with all the
	 * defined borders added to it.
	 */
	public CellStyle getStyleWithBorders(CellStyle source, CSSValue borderStyleBottom, CSSValue borderWidthBottom,
			CSSValue borderColourBottom, CSSValue borderStyleLeft, CSSValue borderWidthLeft, CSSValue borderColourLeft,
			CSSValue borderStyleRight, CSSValue borderWidthRight, CSSValue borderColourRight, CSSValue borderStyleTop,
			CSSValue borderWidthTop, CSSValue borderColourTop, CSSValue borderStyleDiagonal,
			CSSValue borderWidthDiagonal, CSSValue borderColourDiagonal, CSSValue borderStyleAntidiagonal,
			CSSValue borderWidthAntidiagonal, CSSValue borderColourAntidiagonal) {

		BirtStyle birtStyle = birtStyleFromCellStyle( source );
		if( ( borderStyleBottom != null ) && ( borderWidthBottom != null ) && ( borderColourBottom != null ) ){
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_BOTTOM_STYLE, borderStyleBottom );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_BOTTOM_WIDTH, borderWidthBottom );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_BOTTOM_COLOR, borderColourBottom );
		}
		if( ( borderStyleLeft != null ) && ( borderWidthLeft != null ) && ( borderColourLeft != null ) ){
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_LEFT_STYLE, borderStyleLeft );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_LEFT_WIDTH, borderWidthLeft );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_LEFT_COLOR, borderColourLeft );
		}
		if( ( borderStyleRight != null ) && ( borderWidthRight != null ) && ( borderColourRight != null ) ){
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_RIGHT_STYLE, borderStyleRight );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_RIGHT_WIDTH, borderWidthRight );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_RIGHT_COLOR, borderColourRight );
		}
		if( ( borderStyleTop != null ) && ( borderWidthTop != null ) && ( borderColourTop != null ) ){
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_TOP_STYLE, borderStyleTop );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_TOP_WIDTH, borderWidthTop );
			birtStyle.setProperty( StylePropertyIndexes.STYLE_BORDER_TOP_COLOR, borderColourTop );
		}
		if ((borderStyleDiagonal != null) && (borderWidthDiagonal != null) && (borderColourDiagonal != null)) {
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_STYLE, borderStyleDiagonal);
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_WIDTH, borderWidthDiagonal);
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_DIAGONAL_COLOR, borderColourDiagonal);
		}
		if ((borderStyleAntidiagonal != null) && (borderWidthAntidiagonal != null)
				&& (borderColourAntidiagonal != null)) {
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_STYLE, borderStyleAntidiagonal);
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_WIDTH, borderWidthAntidiagonal);
			birtStyle.setProperty(StylePropertyIndexes.STYLE_BORDER_ANTIDIAGONAL_COLOR, borderColourAntidiagonal);
		}

		CellStyle newStyle = getStyle( birtStyle );
		return newStyle;
	}

	/**
	 * Return a POI style created by combining a POI style with a BIRT style, where the BIRT style overrides the values in the POI style.
	 * @param source
	 * The POI style that represents the base style.
	 * @param birtExtraStyle
	 * The BIRT style to overlay on top of the POI style.
	 * @return
	 * A POI style representing the combination of source and birtExtraStyle.
	 */
	public CellStyle getStyleWithExtraStyle( CellStyle source, IStyle birtExtraStyle ) {

		BirtStyle birtStyle = birtStyleFromCellStyle( source );

		for(int i = 0; i < BirtStyle.NUMBER_OF_STYLES; ++i ) {
			CSSValue value = birtExtraStyle.getProperty( i );
			if( value != null ) {
				birtStyle.setProperty( i , value );
			}
		}

		CellStyle newStyle = getStyle( birtStyle );
		return newStyle;
	}

}
