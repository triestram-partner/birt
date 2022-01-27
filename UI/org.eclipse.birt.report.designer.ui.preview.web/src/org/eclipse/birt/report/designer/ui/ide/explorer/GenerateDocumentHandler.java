/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.ide.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.preview.PreviewUtil;
import org.eclipse.birt.report.designer.ui.util.ExceptionUtil;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to generate report document in navigator view
 */
public class GenerateDocumentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PreviewUtil.clearSystemProperties();

		IFile file = ViewHandlerUtil.getSelectedFile(HandlerUtil.getCurrentStructuredSelection(event));
		if (file != null) {
			String url = file.getLocation().toOSString();

			Map<String, Object> options = new HashMap<>();
			options.put(WebViewer.RESOURCE_FOLDER_KEY, ReportPlugin.getDefault().getResourceFolder(file.getProject()));
			options.put(WebViewer.SERVLET_NAME_KEY, WebViewer.VIEWER_DOCUMENT);

			try {
				WebViewer.display(url, options);
			} catch (Exception e) {
				ExceptionUtil.handle(e);
				throw new ExecutionException("Error executing handler", e); //$NON-NLS-1$
			}
		}
		return null;
	}

}
