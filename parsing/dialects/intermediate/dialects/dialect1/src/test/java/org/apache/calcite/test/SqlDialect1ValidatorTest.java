/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.test;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.dialect1.Dialect1ParserImpl;
import org.apache.calcite.sql.validate.SqlValidator;

import org.junit.jupiter.api.Test;

import static org.apache.calcite.sql.parser.SqlParser.configBuilder;

/**
 * Concrete child class of {@link SqlValidatorTestCase}, containing lots of unit
 * tests.
 *
 * <p>If you want to run these same tests in a different environment, create a
 * derived class whose {@link #getTester} returns a different implementation of
 * {@link org.apache.calcite.sql.test.SqlTester}.
 */
class SqlDialect1ValidatorTest extends SqlValidatorTestCase {

  @Test public void testScopeInfo() throws SqlParseException {
    String sql = "select (select deptno from dept) as b";
    final SqlParser.Config config = configBuilder().setParserFactory(Dialect1ParserImpl.FACTORY).build();
    final SqlParser sqlParserReader = SqlParser.create(sql, config);
    final SqlNode node = sqlParserReader.parseQuery();
    final SqlValidator validator = tester.getValidator();
    final SqlNode x = validator.validate(node);
  }
}
