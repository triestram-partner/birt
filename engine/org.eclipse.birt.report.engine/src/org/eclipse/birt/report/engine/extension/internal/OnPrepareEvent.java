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

package org.eclipse.birt.report.engine.extension.internal;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.executor.ExecutionContext;
import org.eclipse.birt.report.engine.extension.IOnPrepareEvent;
import org.eclipse.birt.report.model.api.DesignElementHandle;

public class OnPrepareEvent extends ReportEvent implements IOnPrepareEvent {

	DesignElementHandle handle;
	ExecutionContext context;

	public OnPrepareEvent(ExecutionContext context, IContent content, DesignElementHandle handle) {
		super(content, context, ON_PREPARE_EVENT);

		this.context = context;
		this.handle = handle;
	}

	@Override
	public DesignElementHandle getHandle() {
		return handle;
	}

	public void setHandle(DesignElementHandle handle) {
		this.handle = handle;
	}

	@Override
	public ExecutionContext getContext() {
		return context;
	}

	public void setContext(ExecutionContext context) {
		this.context = context;
	}
}
