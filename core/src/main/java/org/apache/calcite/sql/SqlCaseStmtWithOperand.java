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
package org.apache.calcite.sql;

import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;
import java.util.Objects;
/**
 * Parse tree for a {@code SqlCaseStmtWithOperand}.
 */
public class SqlCaseStmtWithOperand extends SqlCaseStmt {
  public final SqlNode firstOperand;

  /**
   * Creates a {@code SqlCaseStmtWithOperand}.
   * @param pos                       Parser position, must not be null.
   * @param firstOperand              SqlNode representing the base operand for
   *                                  comparison.
   * @param conditionalStmtListPairs  List of SqlNode pairs
   *                                  with StatementList, must not be null.
   * @param elseStmtList              List of statements in the else clause,
   *                                  must not be null.
   */
  public SqlCaseStmtWithOperand(final SqlParserPos pos,
      final SqlNode firstOperand,
      final SqlNodeList conditionalStmtListPairs,
      final SqlNodeList elseStmtList) {
    super(pos, conditionalStmtListPairs, elseStmtList);
    this.firstOperand = Objects.requireNonNull(firstOperand);
  }

  @Override public List<SqlNode> getOperandList() {
    ImmutableNullableList.Builder<SqlNode> builder =
        ImmutableNullableList.builder();
    builder.add(firstOperand);
    builder.addAll(super.getOperandList());
    return builder.build();
  }

  @Override public void unparse(final SqlWriter writer, final int leftPrec,
      final int rightPrec) {
    writer.keyword("CASE");
    firstOperand.unparse(writer, leftPrec, rightPrec);
    super.unparse(writer, leftPrec, rightPrec);
  }
}
