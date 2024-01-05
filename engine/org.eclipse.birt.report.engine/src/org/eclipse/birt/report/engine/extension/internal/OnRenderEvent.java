
package org.eclipse.birt.report.engine.extension.internal;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.extension.IOnRenderEvent;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;

/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

public class OnRenderEvent extends ReportEvent implements IOnRenderEvent {

	DesignElementHandle handle;
	IContent content;
	ExecutionContext context;

	public OnRenderEvent(ExecutionContext context, DesignElementHandle handle, IContent content) {
		super(content, context, ON_RENDER_EVENT);

		this.context = context;
		this.handle = handle;
		this.content = content;
	}

	@Override
	public DesignElementHandle getHandle() {
		return this.handle;
	}

	public void setHandle(ReportElementHandle handle) {
		this.handle = handle;
	}

	@Override
	public IContent getContent() {
		return this.content;
	}

	public void setHandle(IContent content) {
		this.content = content;
	}

	@Override
	public ExecutionContext getContext() {
		return this.context;
	}

	public void setContext(ExecutionContext context) {
		this.context = context;
	}
}
