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

import org.perfrepo.model.TestExecutionAttachment;

import javax.inject.Named;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * DAO for {@link TestExecutionAttachment}
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 */
@Named
public class TestExecutionAttachmentDAO extends DAO<TestExecutionAttachment, Long> {

	public Collection<TestExecutionAttachment> findByExecution(Long testExecutionId) {
		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("exec", testExecutionId);
		return findByNamedQuery(TestExecutionAttachment.FIND_BY_EXECUTION, params);
	}
}