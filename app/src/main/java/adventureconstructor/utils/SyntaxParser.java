package adventureconstructor.utils;

import adventureconstructor.controllers.GameEngine;
import adventureconstructor.models.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;

public class SyntaxParser {
    private Random rng = new Random();
    private Player player;
    private GameEngine engine;
    private String nav = null;
    private final Map<String, Object> variables = new HashMap<>();

    private static String[] funcs = {
        "clearInventory",
        "log",
        "removeWeapon",
        "removeArmor",
        "itemConsumable",
        "itemWeapon",
        "itemArmor",
        "nextLocation",
        "nextRandom",
        "nextSearch",
        "tryEscape",
        "nextFight",
        "scaleRND",
        "nextNPC",
        "random",
        "setVar",
        "inputVar",
        "inputNumberVar",
        "confirmVar",
        "showMessage",
        "appendVar",
        "getVar",
        "varEquals",
        "varContains",
        "stringLength",
        "upper",
        "lower",
        "replace",
        "concat",
        "trim",
        "hasVar",
        "removeVar",
        "inventorySize",
        "hasWeapon",
        "hasArmor",
        "damage",
        "defend",
        "scale",
        "block",
        "heal",
        "gold",
        "death",
        "item",
        "exp",
        "sp"
    };

    public String execute(String expr, Player p, GameEngine e) {
        this.setPlayer(p);
        this.setEngine(e);
        this.nav = null;

        if (expr == null || expr.trim().isEmpty()) return null;

        for (String part : splitTopLevelExpressions(expr, ';')) {
            part = part.trim();

            if (!part.isEmpty()) execOneExpression(part);
        }

        return nav;
    }

    private void execOneExpression(String expr) {
        int qi = findTopLevelCharacter(expr, '?');

        if (qi >= 0) {
            parseCondition(expr, qi);

            return;
        }

        for (String op : new String[] {"+=", "-=", "*=", "/=", "^=", "="}) {
            int oi = findTopLevelOperator(expr, op);

            if (oi > 0) {
                parseOperator(expr, op, oi);

                return;
            }
        }

        execFunc(expr);
    }

    private void parseCondition(String expr, int qi) {
        String cond = expr.substring(0, qi).trim();
        int ci = findMatchingTernaryColon(expr, qi + 1);
        if (ci < 0) return;
        String t = expr.substring(qi + 1, ci).trim();
        String f = expr.substring(ci + 1).trim();
        execOneExpression(evalCondition(cond) ? t : f);
    }

    private List<String> splitTopLevelExpressions(String expression, char separator) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && !isEscaped(expression, i)) quote = 0;
            } else if (current == '"' || current == '\'') {
                quote = current;
            } else if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
            } else if (current == separator && depth == 0) {
                parts.add(expression.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(expression.substring(start));
        return parts;
    }

    private int findTopLevelCharacter(String expression, char target) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && !isEscaped(expression, i)) quote = 0;
            } else if (current == '"' || current == '\'') {
                quote = current;
            } else if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
            } else if (current == target && depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private int findTopLevelOperator(String expression, String operator) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i <= expression.length() - operator.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && !isEscaped(expression, i)) quote = 0;
                continue;
            }
            if (current == '"' || current == '\'') {
                quote = current;
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')') {
                depth--;
                continue;
            }
            if (depth != 0 || !expression.startsWith(operator, i)) continue;
            if ("=".equals(operator)
                    && ((i > 0 && "=!<>".indexOf(expression.charAt(i - 1)) >= 0)
                            || (i + 1 < expression.length() && expression.charAt(i + 1) == '='))) continue;
            return i;
        }
        return -1;
    }

    private int findMatchingTernaryColon(String expression, int start) {
        int depth = 0;
        int nestedTernaries = 0;
        char quote = 0;
        for (int i = start; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && !isEscaped(expression, i)) quote = 0;
                continue;
            }
            if (current == '"' || current == '\'') {
                quote = current;
            } else if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
            } else if (depth == 0 && current == '?') {
                nestedTernaries++;
            } else if (depth == 0 && current == ':') {
                if (nestedTernaries == 0) return i;
                nestedTernaries--;
            }
        }
        return -1;
    }

    private boolean isEscaped(String value, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && value.charAt(i) == '\\'; i--) backslashes++;
        return backslashes % 2 != 0;
    }

    private void parseOperator(String expr, String op, int oi) {
        String tgt = expr.substring(0, oi).trim();
        String val = expr.substring(oi + op.length()).trim();
        int v = evalValue(val);
        applyModifier(tgt, op, v);
    }

    public boolean evalCondition(String c) {
        String condition = c.trim();
        if (condition.isEmpty()) return false;

        condition = stripConditionParentheses(condition);

        List<String> parts = splitTopLevelKeyword(condition, "or");
        if (parts.size() > 1) {
            for (String part : parts) if (evalCondition(part)) return true;
            return false;
        }

        parts = splitTopLevelKeyword(condition, "and");
        if (parts.size() > 1) {
            for (String part : parts) if (!evalCondition(part)) return false;
            return true;
        }

        if (startsWithKeyword(condition, "not"))
            return !evalCondition(condition.substring(3).trim());

        int comparison = findTopLevelComparison(condition);
        if (comparison >= 0) {
            String op = comparisonOperatorAt(condition, comparison);
            String l = condition.substring(0, comparison).trim();
            String r = condition.substring(comparison + op.length()).trim();
            int lv = evalValue(l), rv = evalValue(r);

            if (">=".equals(op)) return lv >= rv;
            if ("<=".equals(op)) return lv <= rv;
            if ("==".equals(op)) return lv == rv;
            if ("!=".equals(op)) return lv != rv;
            if (">".equals(op)) return lv > rv;
            if ("<".equals(op)) return lv < rv;
        }

        return evalValue(condition) != 0;
    }

    private String stripConditionParentheses(String condition) {
        String value = condition.trim();
        while (value.startsWith("(") && value.endsWith(")")) {
            int depth = 0;
            char quote = 0;
            boolean wrapsWholeCondition = true;
            for (int i = 0; i < value.length(); i++) {
                char current = value.charAt(i);
                if (quote != 0) {
                    if (current == quote && (i == 0 || value.charAt(i - 1) != '\\')) quote = 0;
                    continue;
                }
                if (current == '"' || current == '\'') quote = current;
                else if (current == '(') depth++;
                else if (current == ')' && --depth == 0 && i < value.length() - 1) {
                    wrapsWholeCondition = false;
                    break;
                }
            }
            if (!wrapsWholeCondition || depth != 0) break;
            value = value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    private List<String> splitTopLevelKeyword(String expression, String keyword) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        char quote = 0;
        for (int i = 0; i <= expression.length() - keyword.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && (i == 0 || expression.charAt(i - 1) != '\\')) quote = 0;
                continue;
            }
            if (current == '"' || current == '\'') {
                quote = current;
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')') {
                depth--;
                continue;
            }
            if (depth == 0
                    && expression.regionMatches(true, i, keyword, 0, keyword.length())
                    && (i == 0 || !isConditionWordCharacter(expression.charAt(i - 1)))
                    && (i + keyword.length() == expression.length()
                            || !isConditionWordCharacter(expression.charAt(i + keyword.length())))) {
                parts.add(expression.substring(start, i).trim());
                i += keyword.length() - 1;
                start = i + 1;
            }
        }
        if (start == 0) return parts;
        parts.add(expression.substring(start).trim());
        return parts;
    }

    private boolean startsWithKeyword(String expression, String keyword) {
        return expression.regionMatches(true, 0, keyword, 0, keyword.length())
                && expression.length() > keyword.length()
                && !isConditionWordCharacter(expression.charAt(keyword.length()));
    }

    private boolean isConditionWordCharacter(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    private int findTopLevelComparison(String expression) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (quote != 0) {
                if (current == quote && (i == 0 || expression.charAt(i - 1) != '\\')) quote = 0;
                continue;
            }
            if (current == '"' || current == '\'') quote = current;
            else if (current == '(') depth++;
            else if (current == ')') depth--;
            else if (depth == 0 && (current == '<' || current == '>' || current == '=' || current == '!')) {
                if (comparisonOperatorAt(expression, i) != null) return i;
            }
        }
        return -1;
    }

    private String comparisonOperatorAt(String expression, int index) {
        for (String op : new String[] {">=", "<=", "==", "!=", "<", ">"})
            if (expression.startsWith(op, index)) return op;
        return null;
    }

    private int getStat(String s) {
        switch (s) {
            case "hp":
            case "char.hp":
                return getPlayer().getHp();
            case "maxHp":
            case "char.maxHp":
                return getPlayer().getMaxHp();
            case "gold":
                return getPlayer().getGold();
            case "level":
            case "char.level":
                return getPlayer().getLevel();
            case "exp":
                return getPlayer().getExp();
            case "score":
                return getPlayer().getScore();
            case "sp":
                return getPlayer().getSp();
            case "str":
            case "char.str":
                return getPlayer().getStr();
            case "agi":
            case "char.agi":
                return getPlayer().getAgi();
            case "intl":
            case "char.intl":
                return getPlayer().getIntl();
            case "end":
            case "char.end":
                return getPlayer().getEnd();
        }
        return 0;
    }

    private void setStatValue(String tgt, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) getPlayer().setHp(v);
        else if ("gold".equals(tgt)) getPlayer().setGold(v);
        else if ("exp".equals(tgt)) getPlayer().setExp(v);
        else if ("score".equals(tgt)) getPlayer().setScore(v);
        else if ("sp".equals(tgt)) getPlayer().setSp(v);
        else if ("str".equals(tgt) || "char.str".equals(tgt)) getPlayer().setStr(v);
        else if ("agi".equals(tgt) || "char.agi".equals(tgt)) getPlayer().setAgi(v);
        else if ("intl".equals(tgt) || "char.intl".equals(tgt)) getPlayer().setIntl(v);
        else if ("end".equals(tgt) || "char.end".equals(tgt)) getPlayer().setEnd(v);
    }

    private int evalValue(String e) {
        e = e.trim();
        if (e.isEmpty()) return 0;

        try {
            return Integer.parseInt(e);
        } catch (Exception ex) {
        }

        int idx = findLastBinaryOp(e, '+', '-');
        if (idx > 0) {
            return evalSumSub(e, idx);
        }

        idx = findLastBinaryOp(e, '*', '/');
        if (idx > 0) {
            return evalMulDiv(e, idx);
        }

        idx = e.indexOf('^');
        if (idx > 0) {
            return evalPow(e, idx);
        }

        for (String fn : funcs) {
            if (e.startsWith(fn)) {
                return evalFunc(e, fn);
            }
        }

        return getStat(e);
    }

    private int evalFunc(String e, String fn) {
        String np = e.substring(fn.length());
        int n = 0;
        if (!np.isEmpty())
            try {
                n = Integer.parseInt(np);
            } catch (Exception x) {
            }
        if ("getVar".equals(fn)) {
            String name = unquote(functionArguments(np).trim());
            Object value = variables.getOrDefault(name, 0);
            if (value instanceof Number) return ((Number) value).intValue();
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        if ("stringLength".equals(fn)) {
            Object value = evaluateStringValue(e);
            return value instanceof Number ? ((Number) value).intValue() : 0;
        }
        if ("hasVar".equals(fn)) {
            String name = unquote(functionArguments(np).trim());
            return variables.containsKey(name) ? 1 : 0;
        }
        if ("varEquals".equals(fn) || "varContains".equals(fn)) {
            List<String> args = splitArguments(functionArguments(np));
            if (args.size() != 2) return 0;
            String name = unquote(args.get(0));
            String expected = String.valueOf(evaluateStringValue(args.get(1)));
            Object stored = variables.get(name);
            if (stored == null) return 0;
            String actual = String.valueOf(stored);
            boolean matches = "varEquals".equals(fn) ? actual.equals(expected) : actual.contains(expected);
            return matches ? 1 : 0;
        }
        if ("inventorySize".equals(fn)) return getPlayer().getInv().size();
        if ("hasWeapon".equals(fn)) return getPlayer().getWeapon() != null ? 1 : 0;
        if ("hasArmor".equals(fn)) return getPlayer().getArmor() != null ? 1 : 0;
        if ("random".equals(fn) || "damage".equals(fn)) return rng.nextInt(n) + 1;
        if ("heal".equals(fn) || "gold".equals(fn) || "exp".equals(fn) || "sp".equals(fn)) return n;
        if ("scale".equals(fn)) return n * getPlayer().getLevel();
        if ("scaleRND".equals(fn)) return rng.nextInt(n * getPlayer().getLevel()) + 1;
        return 0;
    }

    private int evalSumSub(String e, int idx) {
        char op = e.charAt(idx);
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        return op == '+' ? left + right : left - right;
    }

    private int evalMulDiv(String e, int idx) {
        char op = e.charAt(idx);
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        if (op == '*') return left * right;
        return right != 0 ? left / right : 0;
    }

    private int evalPow(String e, int idx) {
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        return (int) Math.pow(left, right);
    }

    private int findLastBinaryOp(String e, char op1, char op2) {
        for (int i = e.length() - 1; i > 0; i--) {
            char c = e.charAt(i);
            if (c == op1 || c == op2) {
                char prev = e.charAt(i - 1);
                if (prev != '+' && prev != '-' && prev != '*' && prev != '/' && prev != '^') {
                    return i;
                }
            }
        }
        return -1;
    }

    private void applyModifier(String tgt, String op, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) {
            if ("+=".equals(op)) {
                getPlayer().heal(v);
                return;
            }
            if ("-=".equals(op)) {
                getPlayer().damage(v);
                return;
            }
        }
        if ("exp".equals(tgt) && "+=".equals(op)) {
            getPlayer().gainExp(v);
            return;
        }

        int cur = getStat(tgt);
        int newVal;
        switch (op) {
            case "+=":
                newVal = cur + v;
                break;
            case "-=":
                if ("gold".equals(tgt) || "sp".equals(tgt)) newVal = Math.max(0, cur - v);
                else newVal = cur - v;
                break;
            case "*=":
                newVal = cur * v;
                break;
            case "/=":
                newVal = v != 0 ? cur / v : 0;
                break;
            case "^=":
                newVal = (int) Math.pow(cur, v);
                break;
            default:
                newVal = v;
                break;
        }
        setStatValue(tgt, newVal);
    }

    private void execFunc(String e) {
        for (String fn : funcs) {
            if (e.startsWith(fn)) {
                String np = e.substring(fn.length());
                int n = 0;
                if (!np.isEmpty())
                    try {
                        n = Integer.parseInt(np);
                    } catch (Exception x) {
                    }
                switch (fn) {
                    case "inputVar": {
                        String name = unquote(functionArguments(np).trim());
                        if (name.isEmpty()) return;
                        String input =
                                JOptionPane.showInputDialog(null, "Введите текст для переменной «" + name + "»:");
                        if (input != null) variables.put(name, input);
                        return;
                    }
                    case "inputNumberVar": {
                        String name = unquote(functionArguments(np).trim());
                        if (name.isEmpty()) return;
                        String input =
                                JOptionPane.showInputDialog(null, "Введите целое число для переменной «" + name + "»:");
                        if (input == null) return;
                        try {
                            variables.put(name, Integer.parseInt(input.trim()));
                        } catch (NumberFormatException x) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Введите целое число. Значение переменной не изменено.",
                                    "Некорректное значение",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        return;
                    }
                    case "confirmVar": {
                        String name = unquote(functionArguments(np).trim());
                        if (name.isEmpty()) return;
                        int answer = JOptionPane.showConfirmDialog(
                                null,
                                "Вы подтверждаете действие?",
                                "Подтверждение",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) variables.put(name, 1);
                        else if (answer == JOptionPane.NO_OPTION) variables.put(name, 0);
                        return;
                    }
                    case "showMessage": {
                        String message = String.valueOf(evaluateStringValue(functionArguments(np)));
                        JOptionPane.showMessageDialog(null, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    case "appendVar": {
                        String args = functionArguments(np);
                        int separator = findTopLevelComma(args);
                        if (separator < 0) return;
                        String name = unquote(args.substring(0, separator).trim());
                        if (name.isEmpty()) return;
                        String current = String.valueOf(variables.getOrDefault(name, ""));
                        String addition = String.valueOf(evaluateStringValue(args.substring(separator + 1)));
                        variables.put(name, current + addition);
                        return;
                    }
                    case "setVar": {
                        String args = functionArguments(np);
                        int separator = findTopLevelComma(args);
                        if (separator < 0) return;
                        String name = unquote(args.substring(0, separator).trim());
                        if (name.isEmpty()) return;
                        String rawValue = args.substring(separator + 1).trim();
                        Object value = evaluateStringValue(rawValue);
                        variables.put(name, value);
                        return;
                    }
                    case "log":
                        String message = np.trim();
                        if (message.startsWith("(") && message.endsWith(")"))
                            message = message.substring(1, message.length() - 1).trim();
                        if (isQuoted(message)) {
                            message = message.substring(1, message.length() - 1);
                        } else {
                            message = String.valueOf(evaluateStringValue(message));
                        }
                        getEngine().getCombatLog().add(getEngine().formatText(message));
                        return;
                    case "removeVar": {
                        String name = unquote(functionArguments(np).trim());
                        variables.remove(name);
                        return;
                    }
                    case "clearInventory":
                        getPlayer().getInv().clear();
                        return;
                    case "removeWeapon":
                        getPlayer().setWeapon(null);
                        return;
                    case "removeArmor":
                        getPlayer().setArmor(null);
                        return;
                    case "nextFight":
                        nav = "nextFight";
                        return;
                    case "nextNPC":
                        nav = "nextNPC";
                        return;
                    case "nextSearch":
                        nav = "nextSearch";
                        return;
                    case "nextRandom":
                        nav = "nextRandom";
                        return;
                    case "nextLocation":
                        String locationId = np.trim();
                        if (locationId.startsWith("(") && locationId.endsWith(")"))
                            locationId = locationId
                                    .substring(1, locationId.length() - 1)
                                    .trim();
                        if (locationId.length() >= 2
                                && ((locationId.startsWith("\"") && locationId.endsWith("\""))
                                        || (locationId.startsWith("'") && locationId.endsWith("'"))))
                            locationId = locationId.substring(1, locationId.length() - 1);
                        if (!locationId.isEmpty()) nav = "nextLocation:" + locationId;
                        return;
                    case "tryEscape":
                        nav = "tryEscape";
                        return;
                    case "death":
                        nav = "death";
                        return;
                    case "item":
                        getEngine().genItem("random");
                        return;
                    case "itemWeapon":
                        getEngine().genItem("weapon");
                        return;
                    case "itemArmor":
                        getEngine().genItem("armor");
                        return;
                    case "itemConsumable":
                        getEngine().genItem("consumable");
                        return;
                    case "damage":
                        getPlayer().damage(rng.nextInt(n) + 1);
                        return;
                    case "heal":
                        getPlayer().heal(n);
                        return;
                    case "gold":
                        getPlayer().setGold(getPlayer().getGold() + n);
                        return;
                    case "exp":
                        getPlayer().gainExp(n);
                        return;
                    case "sp":
                        getPlayer().setSp(getPlayer().getSp() + n);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private String functionArguments(String suffix) {
        String args = suffix.trim();
        if (args.startsWith("(") && args.endsWith(")"))
            return args.substring(1, args.length() - 1).trim();
        return args;
    }

    private String unquote(String value) {
        String result = value.trim();
        if (result.length() >= 2
                && ((result.startsWith("\"") && result.endsWith("\""))
                        || (result.startsWith("'") && result.endsWith("'"))))
            return result.substring(1, result.length() - 1);
        return result;
    }

    private boolean isQuoted(String value) {
        String text = value.trim();
        return text.length() >= 2
                && ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'")));
    }

    private String resolveStringArgument(String argument) {
        return String.valueOf(evaluateStringValue(argument));
    }

    private Object evaluateStringValue(String expression) {
        String value = expression.trim();
        if (isQuoted(value)) return unquote(value);

        int open = value.indexOf('(');
        if (open > 0 && value.endsWith(")")) {
            String name = value.substring(0, open).trim();
            List<String> args = splitArguments(value.substring(open + 1, value.length() - 1));
            switch (name) {
                case "getVar":
                    return args.size() == 1 ? variables.getOrDefault(unquote(args.get(0)), 0) : 0;
                case "concat": {
                    StringBuilder result = new StringBuilder();
                    for (String arg : args) result.append(evaluateStringValue(arg));
                    return result.toString();
                }
                case "upper":
                    return args.size() == 1
                            ? String.valueOf(evaluateStringValue(args.get(0))).toUpperCase(Locale.ROOT)
                            : "";
                case "lower":
                    return args.size() == 1
                            ? String.valueOf(evaluateStringValue(args.get(0))).toLowerCase(Locale.ROOT)
                            : "";
                case "trim":
                    return args.size() == 1
                            ? String.valueOf(evaluateStringValue(args.get(0))).trim()
                            : "";
                case "replace":
                    if (args.size() != 3) return "";
                    return String.valueOf(evaluateStringValue(args.get(0)))
                            .replace(
                                    String.valueOf(evaluateStringValue(args.get(1))),
                                    String.valueOf(evaluateStringValue(args.get(2))));
                case "stringLength":
                    if (args.size() != 1) return 0;
                    String measured = String.valueOf(evaluateStringValue(args.get(0)));
                    return measured.codePointCount(0, measured.length());
                case "varEquals":
                case "varContains":
                    return evalFunc(value, name);
                case "hasVar":
                    return evalFunc(value, name);
                default:
                    break;
            }
        }
        return evalValue(value);
    }

    private List<String> splitArguments(String arguments) {
        List<String> result = new ArrayList<>();
        if (arguments.trim().isEmpty()) return result;
        int start = 0;
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < arguments.length(); i++) {
            char c = arguments.charAt(i);
            if (quote != 0) {
                if (c == quote) quote = 0;
            } else if (c == '\"' || c == '\'') {
                quote = c;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                result.add(arguments.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(arguments.substring(start).trim());
        return result;
    }

    public String formatVariables(String text) {
        String formatted = text;
        for (Map.Entry<String, Object> variable : variables.entrySet()) {
            formatted = formatted.replace("{var:" + variable.getKey() + "}", String.valueOf(variable.getValue()));
        }
        return formatted;
    }

    private int findTopLevelComma(String value) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (quote != 0) {
                if (c == quote) quote = 0;
            } else if (c == '\"' || c == '\'') {
                quote = c;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                return i;
            }
        }
        return -1;
    }

    public void clearVariables() {
        variables.clear();
    }

    public GameEngine getEngine() {
        return engine;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}
