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
import org.eclipse.birt.report.engine.extension.IReportEvent;
import org.eclipse.birt.report.engine.script.internal.instance.ReportItemInstance;
import org.eclipse.birt.report.engine.script.internal.instance.RunningState;

abstract class ReportEvent extends ReportItemInstance implements IReportEvent {

	protected int eventType;

	// See IReportEvent constants
	static final RunningState translateState[] = { RunningState.CREATE, RunningState.CREATE, RunningState.CREATE,
			RunningState.PAGEBREAK, RunningState.RENDER };

	public ReportEvent(IContent content, ExecutionContext context, int eventType) {
		super(content, context, translateState[eventType]);
		this.eventType = eventType;
	}

	@Override
	public int getEventType() {
		return eventType;
	}
}
