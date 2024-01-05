/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
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

package org.eclipse.birt.sample.reportitem.rotatedtext;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.extension.IReportEvent;
import org.eclipse.birt.report.engine.extension.IScriptableExtendedItem;
import org.eclipse.birt.report.engine.extension.internal.OnCreateEvent;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.extension.ReportItem;

/**
 * RotatedTextItem
 */
public class RotatedTextItem extends ReportItem implements IScriptableExtendedItem {

	public static final String EXTENSION_NAME = "RotatedText"; //$NON-NLS-1$
	public static final String TEXT_PROP = "text"; //$NON-NLS-1$
	public static final String ROTATION_ANGLE_PROP = "rotationAngle"; //$NON-NLS-1$

	/**
	 * Used for calling Java methods from Javascript in the onCreate handler.
	 *
	 * @since 4.15
	 *
	 */
	public static class ScriptHandler extends OnCreateEvent {

		final RotatedTextItem itm;
		final int type;

		/**
		 * @param context
		 * @param handle
		 * @param content
		 */
		public ScriptHandler(RotatedTextItem itm, ExecutionContext context, DesignElementHandle handle,
				IContent content, int type) {
			super(context, handle, content);
			this.itm = itm;
			this.type = type;
		}

		public void setRotationAngle(int angle) throws EngineException {
			if (type != ON_CREATE_EVENT) {
				throw new EngineException("Calling setRotationAngle only makes sense in onCreate.");
			}
			try {
				itm.setRotationAngle(angle);
			} catch (SemanticException e) {
				throw new EngineException("Calling setRotationAngle failed.", e);
			}
		}

	}

	private ExtendedItemHandle modelHandle;

	RotatedTextItem(ExtendedItemHandle modelHandle) {
		this.modelHandle = modelHandle;
	}

	public ExtendedItemHandle getModelHandle() {
		return modelHandle;
	}

	public String getText() {
		return modelHandle.getStringProperty(TEXT_PROP);
	}

	public int getRotationAngle() {
		return modelHandle.getIntProperty(ROTATION_ANGLE_PROP);
	}

	public void setText(String value) throws SemanticException {
		modelHandle.setProperty(TEXT_PROP, value);
	}

	public void setRotationAngle(int value) throws SemanticException {
		modelHandle.setProperty(ROTATION_ANGLE_PROP, value);
	}

	@Override
	public IReportEvent buildEvent(ExecutionContext context, ExtendedItemHandle handle, IContent content, int type) {
		return new ScriptHandler(this, context, handle, content, type);
	}

}
