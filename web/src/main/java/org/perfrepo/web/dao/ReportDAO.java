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
package org.perfrepo.web.dao;

import org.perfrepo.model.report.Report;

import javax.inject.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO layer for report entity.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
@Named
public class ReportDAO extends DAO<Report, Long> {

	public List<Report> getByUser(String username) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);
		return findByNamedQuery(Report.GET_BY_USERNAME, params);
	}

	public List<Report> getByGroupPermission(Long userId, List<Long> groupIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupIds", groupIds);
		params.put("userId", userId);
		return findByNamedQuery(Report.GET_BY_GROUP_PERMISSION, params);
	}

	public List<Report> getByAnyPermission(Long userId, List<Long> groupIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupIds", groupIds);
		params.put("userId", userId);
		return findByNamedQuery(Report.GET_BY_ANY_PERMISSION, params);
	}
}