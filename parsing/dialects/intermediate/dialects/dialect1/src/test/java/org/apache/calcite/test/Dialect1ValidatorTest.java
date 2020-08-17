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

import org.apache.calcite.sql.SqlBeginEndCall;
import org.apache.calcite.sql.SqlCreateProcedure;
import org.apache.calcite.sql.SqlLeaveStmt;
import org.apache.calcite.sql.parser.dialect1.Dialect1ParserImpl;
import org.apache.calcite.sql.test.SqlTestFactory;
import org.apache.calcite.sql.test.SqlTester;
import org.apache.calcite.sql.test.SqlValidatorTester;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class Dialect1ValidatorTest extends SqlValidatorTestCase {

  @Override public SqlTester getTester() {
    return new SqlValidatorTester(
        SqlTestFactory.INSTANCE
            .with("parserFactory", Dialect1ParserImpl.FACTORY)
            .with("conformance", SqlConformanceEnum.LENIENT)
            .with("identifierExpansion", true)
            .with("allowUnknownTables", true));
  }

  @Test public void testSel() {
    String sql = "sel a from abc";
    String expected = "SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValue() {
    String sql = "SELECT FIRST_VALUE (foo) OVER (PARTITION BY (foo)) FROM bar";
    String expected = "SELECT FIRST_VALUE(`BAR`.`FOO`) OVER (PARTITION BY "
        + "`BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testLastValue() {
    String sql = "SELECT LAST_VALUE (foo) OVER (PARTITION BY (foo)) FROM bar";
    String expected = "SELECT LAST_VALUE(`BAR`.`FOO`) OVER (PARTITION BY "
        + "`BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValueIgnoreNulls() {
    final String sql = "SELECT FIRST_VALUE (foo IGNORE NULLS) OVER"
        + " (PARTITION BY (foo)) FROM bar";
    final String expected = "SELECT FIRST_VALUE(`BAR`.`FOO` IGNORE NULLS)"
        + " OVER (PARTITION BY `BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testFirstValueRespectNulls() {
    final String sql = "SELECT FIRST_VALUE (foo RESPECT NULLS) OVER"
        + " (PARTITION BY (foo)) FROM bar";
    final String expected = "SELECT FIRST_VALUE(`BAR`.`FOO` RESPECT NULLS)"
        + " OVER (PARTITION BY `BAR`.`FOO`)\n"
        + "FROM `BAR` AS `BAR`";
    sql(sql).rewritesTo(expected);
  }

  // The sql() call removes "^" symbols in the query, so this test calls
  // checkRewrite() which does not remove the caret operator.
  @Test public void testCaretNegation() {
    String sql = "select a from abc where ^a = 1";
    String expected = "SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`\n"
        + "WHERE ^`ABC`.`A` = 1";
    getTester().checkRewrite(sql, expected);
  }

  @Test public void testHostVariable() {
    String sql = "select :a from abc";
    String expected = "SELECT :A\n"
        + "FROM `ABC` AS `ABC`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testInlineModOperatorWithExpressions() {
    String sql = "select (select a from abc) mod (select d from def) from ghi";
    String expected = "SELECT MOD(((SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`)), ((SELECT `DEF`.`D`\n"
        + "FROM `DEF` AS `DEF`)))\n"
        + "FROM `GHI` AS `GHI`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testCreateProcedure() {
    String sql = "create procedure foo() select a from abc";
    String expected = "CREATE PROCEDURE `FOO` ()\n"
        + "SELECT `ABC`.`A`\n"
        + "FROM `ABC` AS `ABC`";
    sql(sql).rewritesTo(expected);
  }

  @Test public void testCreateProcedureBeginEndLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "leave label1;\n"
        + "end";
    SqlValidator validator = getTester().getValidator();
    SqlCreateProcedure node = (SqlCreateProcedure) getTester().parseAndValidate(validator, sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) beginEnd.statements.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedOuterLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label2: begin\n"
        + "leave label1;\n"
        + "end;"
        + "end";
    SqlValidator validator = getTester().getValidator();
    SqlCreateProcedure node = (SqlCreateProcedure) getTester().parseAndValidate(validator, sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(beginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedInnerLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label2: begin\n"
        + "leave label2;\n"
        + "end;"
        + "end";
    SqlValidator validator = getTester().getValidator();
    SqlCreateProcedure node = (SqlCreateProcedure) getTester().parseAndValidate(validator, sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(nestedBeginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNestedSameNameLabel() {
    String sql = "create procedure foo()\n"
        + "label1: begin\n"
        + "label1: begin\n"
        + "leave label1;\n"
        + "end;"
        + "end";
    SqlValidator validator = getTester().getValidator();
    SqlCreateProcedure node = (SqlCreateProcedure) getTester().parseAndValidate(validator, sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlBeginEndCall nestedBeginEnd = (SqlBeginEndCall) beginEnd.statements.get(0);
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) nestedBeginEnd.statements.get(0);
    assertThat(nestedBeginEnd, sameInstance(leaveStmt.labeledBlock));
  }

  @Test public void testCreateProcedureBeginEndNullLabel() {
    String sql = "create procedure foo()\n"
        + "begin\n"
        + "leave label1;\n"
        + "end";
    SqlValidator validator = getTester().getValidator();
    SqlCreateProcedure node = (SqlCreateProcedure) getTester().parseAndValidate(validator, sql);
    SqlBeginEndCall beginEnd = (SqlBeginEndCall) node.statement;
    SqlLeaveStmt leaveStmt = (SqlLeaveStmt) beginEnd.statements.get(0);
    assertThat(leaveStmt.labeledBlock, nullValue());
  }
}
