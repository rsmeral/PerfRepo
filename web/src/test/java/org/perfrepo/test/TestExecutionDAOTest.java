package org.perfrepo.test;

import com.google.common.collect.Lists;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.perfrepo.model.Entity;
import org.perfrepo.model.Metric;
import org.perfrepo.model.MetricComparator;
import org.perfrepo.model.Tag;
import org.perfrepo.model.Test;
import org.perfrepo.model.TestExecution;
import org.perfrepo.model.TestExecutionTag;
import org.perfrepo.model.Value;
import org.perfrepo.model.to.TestExecutionSearchTO;
import org.perfrepo.web.dao.DAO;
import org.perfrepo.web.dao.TagDAO;
import org.perfrepo.web.dao.TestDAO;
import org.perfrepo.web.dao.TestExecutionDAO;
import org.perfrepo.web.dao.TestExecutionTagDAO;
import org.perfrepo.web.util.TagUtils;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link org.perfrepo.web.dao.TestExecutionDAO}
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 *
 */
@RunWith(Arquillian.class)
public class TestExecutionDAOTest {

   @Inject
   private TestExecutionDAO testExecutionDao;

   @Inject
   private TestDAO testDAO;

   @Inject
   private TestExecutionTagDAO testExecutionTagDAO;

   @Inject
   private TagDAO tagDAO;

   @Inject
   private UserTransaction userTransaction;

   private Test test1;
   private Test test2;
   private TestExecution te1;
   private TestExecution te2;
   private TestExecution te3;
   private TestExecution te4;
   private TestExecution te5;
   private Calendar calendar = Calendar.getInstance();

   @Deployment
   public static Archive<?> createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
      war.addPackages(true, DAO.class.getPackage());
      war.addPackages(true, Entity.class.getPackage());
      war.addClass(TagUtils.class);
      war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
      war.addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
      return war;
   }

   @Before
   public void init() throws Exception {
      userTransaction.begin();

      test1 = testDAO.create(createTest("testuser1", "uid1"));
      test2 = testDAO.create(createTest("testuser1", "uid2"));
      te1 = testExecutionDao.create(createTestExecution1());
      te2 = testExecutionDao.create(createTestExecution2());
      te3 = testExecutionDao.create(createTestExecution3());
      te4 = testExecutionDao.create(createTestExecution4());
      te5 = testExecutionDao.create(createTestExecution5());

      createTestExecutionTag("tag1", te1);
      createTestExecutionTag("tag2", te1);
      createTestExecutionTag("tag3", te1);
      createTestExecutionTag("tag1", te2);
      createTestExecutionTag("tag2", te2);
      createTestExecutionTag("tag1", te3);
      createTestExecutionTag("tag3", te3);
      createTestExecutionTag("tag4", te4);

      assertEquals(5, testExecutionDao.getAll().size());
   }

   /**
    * Clean up
    *
    * @throws Exception
    */
   @After
   public void destroy() throws Exception {
      List<TestExecutionTag> testExecutionTags = testExecutionTagDAO.getAll();
      for(TestExecutionTag testExecutionTag: testExecutionTags) {
         testExecutionTagDAO.remove(testExecutionTag);
      }

      List<Tag> tags = tagDAO.getAll();
      for(Tag tag: tags) {
         tagDAO.remove(tag);
      }

      List<TestExecution> testExecutions = testExecutionDao.getAll();
      for(TestExecution testExecution: testExecutions) {
         testExecutionDao.remove(testExecution);
      }

      assertTrue(testExecutionDao.getAll().isEmpty());

      testDAO.remove(test1);
      testDAO.remove(test2);
      userTransaction.commit();
   }

   @org.junit.Test
   public void testGetAllByPropertyIn() {
      Collection<Object> ids = new ArrayList<>();
      ids.add(te1.getId());
      ids.add(te4.getId());

      List<TestExecution> result = testExecutionDao.getAllByPropertyIn("id", ids);
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te4.getId()) {
            fail("TestExecutionDAO.getAllByPropertyIn returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testGetAllByPropertyIdBetween() {
      Comparable from = te2.getId();
      Comparable to = te4.getId();
      List<TestExecution> result = testExecutionDao.getAllByPropertyBetween("id", from, to);
      assertEquals(3, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te2.getId() && testExecution.getId() != te3.getId() && testExecution.getId() != te4.getId()) {
            fail("TestExecutionDAO.getAllByPropertyIn returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testGetAllByPropertyDateBetween() {
      Comparable from = createStartDate(-7);
      Comparable to = createStartDate(-3);
      List<TestExecution> result = testExecutionDao.getAllByPropertyBetween("started", from, to);
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te2.getId() && testExecution.getId() != te3.getId()) {
            fail("TestExecutionDAO.getAllByPropertyIn returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testGetTestExecutionsByTags() {
      List<String> tags = new ArrayList<>();
      tags.add("tag1");
      tags.add("tag2");
      List<String> testUid = new ArrayList<>();
      testUid.add(test1.getUid());

      List<TestExecution> result = testExecutionDao.getTestExecutions(tags, testUid);
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te2.getId()) {
            fail("TestExecutionDAO.getTestExecutions() returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testGetTestExecutionsByTagsWithLast1() {
      List<String> tags = new ArrayList<>();
      tags.add("tag1");
      List<String> testUid = new ArrayList<>();
      testUid.add(test1.getUid());

      List<TestExecution> result = testExecutionDao.getTestExecutions(tags, testUid, 3, 2);
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te2.getId()) {
            fail("TestExecutionDAO.getTestExecutions() returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testGetTestExecutionsByTagsWithLast2() {
      List<String> tags = new ArrayList<>();
      tags.add("tag1");
      List<String> testUid = new ArrayList<>();
      testUid.add(test1.getUid());

      List<TestExecution> result = testExecutionDao.getTestExecutions(tags, testUid, 1, 1);
      assertEquals(1, result.size());
      assertEquals(te3.getId(), result.get(0).getId());
   }

   @org.junit.Test
   public void testGetTestExecutionsByTagsWithLast3() {
      List<String> tags = new ArrayList<>();
      tags.add("tag1");
      tags.add("tag4");
      List<String> testUid = new ArrayList<>();
      testUid.add(test1.getUid());

      List<TestExecution> result = testExecutionDao.getTestExecutions(tags, testUid, 5, 3);
      assertTrue(result.isEmpty());
   }

   @org.junit.Test
   public void testSearchByTestUID() {
      TestExecutionSearchTO searchCriteria = new TestExecutionSearchTO();
      searchCriteria.setTestUID(test1.getUid());

      assertEquals("Search by test UID retrieved unexpected results.", 4, testExecutionDao.searchTestExecutions(searchCriteria, Arrays.asList(test1.getGroupId())).size());
   }

   @org.junit.Test
   public void testSearchByDate() {
      Date from = createStartDate(-7);
      Date to = createStartDate(-3);

      TestExecutionSearchTO searchCriteria = new TestExecutionSearchTO();
      searchCriteria.setStartedFrom(from);
      searchCriteria.setStartedTo(to);

      List<TestExecution> result = testExecutionDao.searchTestExecutions(searchCriteria, Arrays.asList(test1.getGroupId()));
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te2.getId() && testExecution.getId() != te3.getId()) {
            fail("TestExecutionDAO.searchTestExecutions() returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testSearchByTags() {
      TestExecutionSearchTO searchCriteria = new TestExecutionSearchTO();
      searchCriteria.setTags("tag1 tag2");

      List<TestExecution> result = testExecutionDao.searchTestExecutions(searchCriteria, Arrays.asList(test1.getGroupId()));
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te2.getId()) {
            fail("TestExecutionDAO.searchTestExecutions() returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testSearchByTagsWithLimit() {
      TestExecutionSearchTO searchCriteria = new TestExecutionSearchTO();
      searchCriteria.setTags("tag1");
      searchCriteria.setLimitFrom(3);
      searchCriteria.setLimitHowMany(2);

      List<TestExecution> result = testExecutionDao.searchTestExecutions(searchCriteria, Arrays.asList(test1.getGroupId()));
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te2.getId()) {
            fail("TestExecutionDAO.searchTestExecutions() returned unexpected test execution.");
         }
      }
   }

   @org.junit.Test
   public void testSearchIdsInList() {
      TestExecutionSearchTO searchCriteria = new TestExecutionSearchTO();
      List<Long> ids = Arrays.asList(te1.getId(), te2.getId());
      searchCriteria.setIds(ids);

      List<TestExecution> result = testExecutionDao.searchTestExecutions(searchCriteria, Arrays.asList(test1.getGroupId()));
      assertEquals(2, result.size());

      for(TestExecution testExecution: result) {
         if(testExecution.getId() != te1.getId() && testExecution.getId() != te2.getId()) {
            fail("TestExecutionDAO.searchTestExecutions() returned unexpected test execution.");
         }
      }
   }

   private Test createTest(String groupId, String uid) {
      return Test.builder()
            .name("test1")
            .groupId(groupId)
            .uid(uid)
            .description("this is a test test")
            .metric("metric1", MetricComparator.HB, "this is a test metric 1")
            .build();
   }

   private TestExecution createTestExecution1() {
      Value value = new Value();
      value.setResultValue(10d);
      value.setMetric(createMetric());

      Collection<Value> values = new ArrayList<>();
      values.add(value);

      TestExecution te = new TestExecution();
      te.setStarted(createStartDate(-8));
      te.setName("test execution 1");
      te.setTest(test1);
      te.setValues(values);

      return te;
   }

   private TestExecution createTestExecution2() {
      Value value = new Value();
      value.setResultValue(20d);
      value.setMetric(createMetric());

      Collection<Value> values = new ArrayList<>();
      values.add(value);

      TestExecution te = new TestExecution();
      te.setStarted(createStartDate(-6));
      te.setName("test execution 2");
      te.setTest(test1);
      te.setValues(values);

      return te;
   }

   private TestExecution createTestExecution3() {
      Value value = new Value();
      value.setResultValue(30d);
      value.setMetric(createMetric());

      Collection<Value> values = new ArrayList<>();
      values.add(value);

      TestExecution te = new TestExecution();
      te.setStarted(createStartDate(-4));
      te.setName("test execution 3");
      te.setTest(test1);
      te.setValues(values);

      return te;
   }

   private TestExecution createTestExecution4() {
      Value value = new Value();
      value.setResultValue(40d);
      value.setMetric(createMetric());

      Collection<Value> values = new ArrayList<>();
      values.add(value);

      TestExecution te = new TestExecution();
      te.setStarted(createStartDate(-2));
      te.setName("test execution 4");
      te.setTest(test1);
      te.setValues(values);

      return te;
   }

   private TestExecution createTestExecution5() {
      Value value = new Value();
      value.setResultValue(1000d);
      value.setMetric(createMetric());

      Collection<Value> values = new ArrayList<>();
      values.add(value);

      TestExecution te = new TestExecution();
      te.setStarted(createStartDate(-1));
      te.setName("test execution 5");
      te.setTest(test2);
      te.setValues(values);

      return te;
   }

   private Metric createMetric() {
      Metric metric = new Metric();
      metric.setName("metric1");

      return metric;
   }

   private TestExecutionTag createTestExecutionTag(String tagName, TestExecution storedTestExecution) {
      Tag storedTag = tagDAO.findByName(tagName);
      if (storedTag == null) {
         Tag tag = new Tag();
         tag.setName(tagName);
         storedTag = tagDAO.create(tag);
      }

      TestExecutionTag testExecutionTag = new TestExecutionTag();
      testExecutionTag.setTag(storedTag);
      testExecutionTag.setTestExecution(storedTestExecution);
      TestExecutionTag storedTestExecutionTag = testExecutionTagDAO.create(testExecutionTag);

      Collection<TestExecutionTag> testExecutionTags = storedTestExecution.getTestExecutionTags();
      if(testExecutionTags == null) {
         testExecutionTags = new ArrayList<>();
      }
      testExecutionTags.add(storedTestExecutionTag);
      storedTestExecution.setTestExecutionTags(testExecutionTags);

      return storedTestExecutionTag;
   }

   private Date createStartDate(int daysToShift) {
      calendar.setTime(new Date());
      calendar.add(Calendar.DATE, daysToShift);

      return calendar.getTime();
   }
}
