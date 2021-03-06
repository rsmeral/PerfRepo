/**
 *
 * PerfRepo
 *
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.perfrepo.web.controller;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.perfrepo.model.report.Report;
import org.perfrepo.model.userproperty.ReportFilter;
import org.perfrepo.web.service.ReportService;
import org.perfrepo.web.service.exceptions.ServiceException;
import org.perfrepo.web.session.UserSession;
import org.perfrepo.web.viewscope.ViewScoped;

/**
 * Controller for /reports
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 * @author Jiri Holusa (jholusa@redhat.com)
 */
@Named("reportList")
@ViewScoped
public class ReportListController extends BaseController {

	private static final long serialVersionUID = 724797862273365319L;

	private static final Logger log = Logger.getLogger(ReportListController.class);

	@Inject
	private ReportService reportService;

	@Inject
	private UserSession userSession;

	private List<Report> savedReports;

	public List<Report> getSavedReports() {
		return savedReports;
	}

	/**
	 * called on preRenderView
	 */
	public void preRender() throws Exception {
		reloadSessionMessages();
		if (savedReports == null) {
			updateSavedReports();
		}
	}

	public void setAllReports() {
		try {
			userSession.setReportFilter(ReportFilter.ALL);
			updateSavedReports();
		} catch(ServiceException se) {
			addMessage(se);
		}
	}

	public void setMyReports() {
		try {
			userSession.setReportFilter(ReportFilter.MY);
			updateSavedReports();
		} catch(ServiceException se) {
			addMessage(se);
		}
	}

	public void setTeamReports() {
		try {
			userSession.setReportFilter(ReportFilter.TEAM);
			updateSavedReports();
		} catch(ServiceException se) {
			addMessage(se);
		}
	}

	private void updateSavedReports() {
		switch (userSession.getReportFilter()) {
			case MY:
				savedReports = reportService.getAllUsersReports();
				break;
			case TEAM:
				savedReports = reportService.getAllGroupReports();
				break;
			case ALL :
				savedReports = reportService.getAllReports();
				break;
			default:
				savedReports = reportService.getAllGroupReports();
				break;
		}
		Collections.sort(savedReports);
	}

	public void remove(Report itemToRemove) {
		if (itemToRemove == null) {
			throw new IllegalStateException("Item to remove is null");
		}
		try {
			reportService.removeReport(itemToRemove);
		} catch (ServiceException e) {
			log.error("Error while removing report " + itemToRemove.getId(), e);
			addMessage(e);
		}
		updateSavedReports();
	}
}
