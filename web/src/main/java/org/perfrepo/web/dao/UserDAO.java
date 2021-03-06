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

import org.perfrepo.model.user.User;

import javax.inject.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for {@link User}
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 */
@Named
public class UserDAO extends DAO<User, Long> {

   public User findByUsername(String name) {
		List<User> users = getAllByProperty("username", name);
		if (users.size() > 0) {
			return users.get(0);
		}
		return null;
	}

   public List<User> findSubscribersForTest(Long testId) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("testId", testId);
      return findByNamedQuery(User.GET_SUBSCRIBERS_FOR_TEST, params);
   }
}