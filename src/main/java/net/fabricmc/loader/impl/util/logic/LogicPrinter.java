/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.loader.impl.util.logic;

import java.util.List;

public class LogicPrinter<T> {
	public static <T> String print(LogicNode<T> node) {
		LogicPrinter<T> printer = new LogicPrinter<>();
		StringBuilder sb = new StringBuilder();
		printer.buildExpression(node, sb);
		return sb.toString();
	}

	private void buildExpression(LogicNode<T> node, StringBuilder sb) {
		switch (node.getType()) {
		case LITERAL:
			// Append the literal value (e.g., "A", "B")
			sb.append(node.getValue());
			break;
		case NOT:
			// Handle NOT: Add "!" before the child expression
			sb.append("!");

			// Ensure parentheses for complex sub-expressions
			if (node.getChildrens().get(0).getType() != LogicType.LITERAL) {
				sb.append("(");
				buildExpression(node.getChildrens().get(0), sb);
				sb.append(")");
			} else {
				buildExpression(node.getChildrens().get(0), sb);
			}

			break;
		case AND:
			// Handle AND: Use " && " as the operator
			appendBinaryOperation(node, sb, " && ");
			break;
		case OR:
			// Handle OR: Use " || " as the operator
			appendBinaryOperation(node, sb, " || ");
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + node.getType());
		}
	}

	private void appendBinaryOperation(LogicNode<T> node, StringBuilder sb, String operator) {
		List<LogicNode<T>> children = node.getChildrens();
		boolean first = true;

		for (LogicNode<T> child : children) {
			if (!first) {
				sb.append(operator); // Add the operator between children
			}

			first = false;

			// Add parentheses for complex sub-expressions
			if (child.getType() == LogicType.OR || child.getType() == LogicType.AND) {
				sb.append("(");
				buildExpression(child, sb);
				sb.append(")");
			} else {
				buildExpression(child, sb);
			}
		}
	}
}
