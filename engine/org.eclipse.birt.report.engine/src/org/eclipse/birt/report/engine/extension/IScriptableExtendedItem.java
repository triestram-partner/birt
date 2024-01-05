package org.eclipse.birt.report.engine.extension;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;

/*******************************************************************************
 * Copyright (c) 2024 Henning von Bargen. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Henning von Bargen - initial API and implementation
 ******************************************************************************/

/**
 * This tells BIRT that the extension item provides Javascript support for
 * onCreate and onRender scripts.
 *
 * @since 4.15
 *
 */
public interface IScriptableExtendedItem {

	/*
	 * Creates an event handler for handling Javascript for the extension item.
	 *
	 * See RotatedTextItem for an example.
	 */
	public IReportEvent buildEvent(ExecutionContext context, ExtendedItemHandle handle, IContent content, int type);
}
