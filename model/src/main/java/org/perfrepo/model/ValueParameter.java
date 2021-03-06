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
package org.perfrepo.model;

import org.perfrepo.model.auth.EntityType;
import org.perfrepo.model.auth.SecuredEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@javax.persistence.Entity
@Table(name = "value_parameter")
@NamedQueries({
		@NamedQuery(name = ValueParameter.FIND_ALL, query = "SELECT x from ValueParameter x"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_ID_ASC, query = "SELECT x FROM ValueParameter x ORDER BY x.id ASC"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_ID_DESC, query = "SELECT x FROM ValueParameter x ORDER BY x.id DESC"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_NAME_ASC, query = "SELECT x FROM ValueParameter x ORDER BY x.name ASC"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_NAME_DESC, query = "SELECT x FROM ValueParameter x ORDER BY x.name DESC"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_VALUE_ASC, query = "SELECT x FROM ValueParameter x ORDER BY x.paramValue ASC"),
		@NamedQuery(name = ValueParameter.FIND_ALL_SORTED_BY_VALUE_DESC, query = "SELECT x FROM ValueParameter x ORDER BY x.paramValue DESC"),
		@NamedQuery(name = ValueParameter.FIND_BY_ID, query = "SELECT x from ValueParameter x WHERE x.id = :" + ValueParameter.NQ_ID),
		@NamedQuery(name = ValueParameter.GET_TEST, query = "SELECT test from ValueParameter vp inner join vp.value v inner join v.testExecution te inner join te.test test where vp= :entity")})
@XmlRootElement(name = "valueParameter")
@SecuredEntity(type = EntityType.TEST, parent = "value")
public class ValueParameter implements Entity<ValueParameter>, Comparable<ValueParameter> {

	private static final long serialVersionUID = 1673366421709715346L;

	public static final String FIND_ALL = "ValueParameter.findAll";

	public static final String FIND_ALL_SORTED_BY_ID_ASC = "ValueParameter.findAllSortedByIdAsc";
	public static final String FIND_ALL_SORTED_BY_ID_DESC = "ValueParameter.findAllSortedByIdDesc";
	public static final String FIND_ALL_SORTED_BY_NAME_ASC = "ValueParameter.findAllSortedByNameAsc";
	public static final String FIND_ALL_SORTED_BY_NAME_DESC = "ValueParameter.findAllSortedByNameDesc";
	public static final String FIND_ALL_SORTED_BY_VALUE_ASC = "ValueParameter.findAllSortedByValueAsc";
	public static final String FIND_ALL_SORTED_BY_VALUE_DESC = "ValueParameter.findAllSortedByValueDesc";

	public static final String GET_TEST = "ValueParameter.getTest";

	public static final String FIND_BY_ID = "ValueParameter.findById";
	public static final String NQ_ID = "valueParameterId";

	@Id
	@SequenceGenerator(name = "VALUE_PARAMETER_ID_GENERATOR", sequenceName = "VALUE_PARAMETER_SEQUENCE", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VALUE_PARAMETER_ID_GENERATOR")
	private Long id;

	@Column(name = "name")
	@NotNull
	@Size(max = 255)
	private String name;

	@Column(name = "value")
	@NotNull
	@Size(max = 255)
	private String paramValue;

	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "value_id", referencedColumnName = "id")
	private Value value;

	@XmlTransient
	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public ValueParameter() {
		super();
	}

	public ValueParameter(String name, String value) {
		super();
		this.name = name;
		this.paramValue = value;
	}

	@XmlTransient
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return this.name;
	}

	public void setParamValue(String value) {
		this.paramValue = value;
	}

	@XmlAttribute(name = "value")
	public String getParamValue() {
		return this.paramValue;
	}

	@Override
	public ValueParameter clone() {
		try {
			return (ValueParameter) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int compareTo(ValueParameter o) {
		return name.compareTo(o.name);
	}
}