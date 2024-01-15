package com.saicone.settings.parser.impl;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.parser.ExpressionParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MathExpression implements ExpressionParser {

    @Override
    public @Nullable Object parse(@NotNull MapNode root, @NotNull SettingsNode provider, @NotNull Object... args) {
        final EvaluationValue value;
        try {
            value = new Expression(String.valueOf(args[0])).evaluate();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException(e);
        }
        if (args.length == 1) {
            return value.getValue();
        }
        switch (String.valueOf(args[1]).toUpperCase()) {
            case "NUMBER":
                return value.getNumberValue();
            case "BOOLEAN":
                return value.getBooleanValue();
            case "STRING":
                return value.getStringValue();
            case "DATE_TIME":
                return value.getDateTimeValue();
            case "DURATION":
                return value.getDurationValue();
            case "ARRAY":
                return value.getArrayValue();
            case "STRUCTURE":
                return value.getStructureValue();
            case "EXPRESSION_NODE":
                return value.getExpressionNode();
            case "NULL":
                return null;
            default:
                return value.getValue();
        }
    }
}
