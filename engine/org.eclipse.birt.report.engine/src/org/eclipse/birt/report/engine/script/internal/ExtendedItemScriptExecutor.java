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

package org.eclipse.birt.report.engine.script.internal;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.extension.IReportEvent;
import org.eclipse.birt.report.engine.extension.IReportEventHandler;
import org.eclipse.birt.report.engine.extension.IScriptableExtendedItem;
import org.eclipse.birt.report.engine.extension.internal.OnCreateEvent;
import org.eclipse.birt.report.engine.extension.internal.OnPrepareEvent;
import org.eclipse.birt.report.engine.extension.internal.OnRenderEvent;
import org.eclipse.birt.report.engine.ir.ExtendedItemDesign;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.extension.IReportItem;
import org.eclipse.birt.report.model.elements.ExtendedItem;

public class ExtendedItemScriptExecutor extends ScriptExecutor {

	public static void handleOnPrepare(ExtendedItemHandle handle, ExecutionContext context) {
		IReportEventHandler eventHandler = context.getExtendedItemManager().createEventHandler(handle);
		if (eventHandler != null) {
			try {
				OnPrepareEvent event = new OnPrepareEvent(context, null, handle); // We don't have content until now
				eventHandler.handle(event);
			} catch (Exception e) {
				addException(context, e, handle);
			}
		}
	}

	public static void handleOnCreate(ExtendedItemDesign design, IContent content, ExecutionContext context) {
		ExtendedItemHandle handle = (ExtendedItemHandle) design.getHandle();
		if (!needOnCreate(design)) {
			return;
		}

		try {
			IReportEvent event = null;
			ExtendedItem element = (ExtendedItem)handle.getElement();
			IReportItem item = element.getExtensibilityProvider().getExtensionElement();
			if (item instanceof IScriptableExtendedItem) {
				event = ((IScriptableExtendedItem) item).buildEvent(context, handle, content,
						IReportEvent.ON_CREATE_EVENT);
				if (event == null) {
					return;
				}
			} else {
				event = new OnCreateEvent(context, handle, content);
			}
			if (handleScript(event, design.getOnCreate(), context).didRun()) {
				return;
			}
			IReportEventHandler eventHandler = context.getExtendedItemManager().createEventHandler(handle);
			if (eventHandler != null) {
				eventHandler.handle(event);
			}
		} catch (Exception e) {
			addException(context, e, handle);
		}
	}

	public static void handleOnRender(ExtendedItemDesign design, IContent content, ExecutionContext context) {
		ExtendedItemHandle handle = (ExtendedItemHandle) design.getHandle();
		if (!needOnRender(design)) {
			return;
		}

		try {
			IReportEvent event = null;
			ExtendedItem element = (ExtendedItem) handle.getElement();
			IReportItem item = element.getExtensibilityProvider().getExtensionElement();
			if (item instanceof IScriptableExtendedItem) {
				event = ((IScriptableExtendedItem) item).buildEvent(context, handle, content,
						IReportEvent.ON_RENDER_EVENT);
				if (event == null) {
					return;
				}
			} else {
				event = new OnRenderEvent(context, handle, content);
			}
			if (handleScript(event, design.getOnRender(), context).didRun()) {
				return;
			}
			IReportEventHandler eventHandler = context.getExtendedItemManager().createEventHandler(handle);
			if (eventHandler != null) {
				eventHandler.handle(event);
			}
		} catch (Exception e) {
			addException(context, e, handle);
		}
	}
}