package edu.uob.command;

import java.util.*;
import java.util.function.Predicate;

public class Conditions {
    private boolean hasCond = false;
    private List<String[]> conds = new ArrayList<>();
    private Predicate<Map<String, String>> predicate;

    public Predicate<Map<String, String>> getPredicate() { return this.predicate; }
    public List<String[]> getConds() { return this.conds; }

    public boolean hasCondition() { return this.hasCond; }

    // conds: [{"0","age",">","20"},{"1","weight","<","70"},...], the 1st number indicates priority level
    // wholeConds: [{"0","age",">","20"},{"0","AND","BOOL"},{"1","weight","<","70"},{"1","OR","BOOL"}...],
    // the 1st number indicates priority level
    public void createPredicates(List<String[]> conds, List<String[]> wholeConds) {
        this.hasCond = true;
        this.conds = conds;
        this.predicate = row -> {
            List<String[]> copiedConds = deepCopy(wholeConds);
            int highest = getHighestPriority(copiedConds);
            boolean result = false;
            for (int h=highest; h >= 0; h--) {
                // 1. get start and end idx of the segments with same priority
                List<int[]> segments = findSegment(copiedConds, h);
                // 2. process every segment with same priority (prepare -> execute -> update)
                for (int[] segment: segments) {
                    List<List<String[]>> segmentConds = prepareSegment(copiedConds, segment);
                    result = executeSegment(row, segmentConds.get(0), segmentConds.get(1));
                    updateCondition(copiedConds, segment[0], segment[1], result, h);
                }
                // 3. remove segments that have already been processed
                removeCondition(copiedConds, segments);
            }
            return result;
        };
    }

    private int getHighestPriority(List<String[]> wholeConds) {
        int result = 0;
        for (String[] wholeCond: wholeConds) {
            if (Integer.parseInt(wholeCond[0]) > result) {
                result = Integer.parseInt(wholeCond[0]);
            }
        }
        return result;
    }

    // Get segments in conditions that have same priority h
    private List<int[]> findSegment(List<String[]> wholeConds, int h) {
        List<int[]> result = new ArrayList<>();
        boolean hasStarted = false;
        int[] segment = {-1, -1};
        for (int i = 0; i < wholeConds.size(); i++) {
            if (!hasStarted && wholeConds.get(i)[0].equals(String.valueOf(h))) {
                hasStarted = true;
                segment[0] = i;
            }
            if (hasStarted) {
                if (i+1>=wholeConds.size()) {
                    segment[1] = i+1;
                    result.add(segment.clone());
                } else if (!wholeConds.get(i)[0].equals(String.valueOf(h))) {
                    segment[1] = i;
                    result.add(segment.clone());
                    segment[0] = -1; segment[1] = -1;
                    hasStarted = false;
                }
            }
        }
        return result;
    }

    // store compare statement and bool operator in the segment into two separate lists
    // Eg. [{Age > 18}, {and}, {height > 170}, {or}, {weight < 80}] -->
    //     return [
    //         [{Age > 18}, {height > 170}, {weight < 80}],
    //         [{and}, {or}]
    //     ]
    private List<List<String[]>> prepareSegment(List<String[]> copiedConds, int[] segment) {
        List<List<String[]>> result = new ArrayList<>();
        List<String[]> tmpConds = new ArrayList<>();
        List<String[]> tmpBoolConds = new ArrayList<>();
        for (int i=segment[0]; i < segment[1]; i++) {
            if (copiedConds.get(i).length == 4) { tmpConds.add(copiedConds.get(i)); }
            if (copiedConds.get(i).length == 3) { tmpConds.add(copiedConds.get(i)); }
            if (copiedConds.get(i).length == 2) { tmpBoolConds.add(copiedConds.get(i)); }
        }
        result.add(tmpConds); result.add(tmpBoolConds);
        return result;
    }

    // execute conditions of same level from left to right
    // tmpConds:     [{Age > 18}, {height > 170}, {weight < 80}]
    // tmpBoolConds: [{and}, {or}]
    private boolean executeSegment(Map<String, String> row, List<String[]> tmpConds, List<String[]> tmpBoolConds) {
        boolean result = tester(row, tmpConds.get(0));
        int condIdx = 1;
        for (String[] tmpBoolCond : tmpBoolConds) {
            switch (tmpBoolCond[1]) {
                case "AND" -> { result = result && tester(row, tmpConds.get(condIdx)); }
                case "OR" -> { result = result || tester(row, tmpConds.get(condIdx)); }
                default -> result = false;
            }
            condIdx++;
        }
        return result;
    }

    // replace segment whose priority is h with result, and decrease the priority level of the segment
    // Eg. startIdx = 0, endIdx = 3, result = true
    //     [{Age > 18}, {and}, {height > 170}, {or}, {weight < 80}] -->
    //     [{true}, {true}, {true}, {or}, {weight < 80}]
    private void updateCondition(List<String[]> wholeConds, int startIdx, int endIdx, boolean result, int h) {
        String[] updatedResult = {String.valueOf(h-1), String.valueOf(result), "BOOL"};
        wholeConds.set(startIdx, updatedResult);
        for (int i=startIdx+1; i<endIdx; i++) {
            wholeConds.set(i, new String[]{String.valueOf(h-1), String.valueOf(result), "BOOL"});
        }
    }

    // remove segments that have already been processed
    // Eg. startIdx = 0, endIdx = 3
    //     [{true}, {true}, {true}, {or}, {weight < 80}] -->
    //     [{true}, {or}, {weight < 80}]
    private void removeCondition(List<String[]> copiedConds, List<int[]> segments) {
        List<String[]> toBeRemoved = new ArrayList<>();
        for (int[] seg: segments) {
            for (String[] cond: copiedConds) {
                if (copiedConds.indexOf(cond) > seg[0] && copiedConds.indexOf(cond) < seg[1]) {
                    toBeRemoved.add(cond);
                }
            }
        }
        copiedConds.removeIf(toBeRemoved::contains);
    }

    public boolean tester(Map<String, String> row, String[] cond) {
        if (cond.length == 3) {
            return Boolean.parseBoolean(cond[1]);
        }
        String attributeName = cond[1];
        String comparator = cond[2];
        String value = cond[3];
        switch (comparator) {
            case "!=" -> {
                if (!row.containsKey(attributeName)) { return false; }
                return !row.get(attributeName).equals(value);
            }
            case "==" -> {
                if (!row.containsKey(attributeName)) { return false; }
                return row.get(attributeName).equals(value);
            }
            case ">=" -> {
                try {
                    double attVal = Double.parseDouble(row.get(attributeName));
                    double tarVal = Double.parseDouble(value);
                    return attVal >= tarVal;
                } catch (Exception e) { return false; }
            }
            case "<=" -> {
                try {
                    double attVal = Double.parseDouble(row.get(attributeName));
                    double tarVal = Double.parseDouble(value);
                    return attVal <= tarVal;
                } catch (Exception e) { return false; }
            }
            case "<" -> {
                try {
                    double attVal = Double.parseDouble(row.get(attributeName));
                    double tarVal = Double.parseDouble(value);
                    return attVal < tarVal;
                } catch (Exception e) { return false; }
            }
            case ">" -> {
                try {
                    double attVal = Double.parseDouble(row.get(attributeName));
                    double tarVal = Double.parseDouble(value);
                    return attVal > tarVal;
                } catch (Exception e) { return false; }
            }
            case "LIKE" -> {
                try {
                    Double.parseDouble(row.get(attributeName));
                    Double.parseDouble(value);
                    return false;
                } catch (Exception e){
                    return row.get(attributeName).contains(value);
                }
            }
            default -> { return false; }
        }
    }

    private List<String[]> deepCopy(List<String[]> source) {
        List<String[]> copied = new ArrayList<>();
        for (String[] src: source) {
            String[] dst = new String[src.length];
            int idx = 0;
            for (String str: src) {
                StringBuilder dstStr = new StringBuilder();
                for (int i=0; i< str.length(); i++) {
                    dstStr.append(str.charAt(i));
                }
                dst[idx] = dstStr.toString();
                idx++;
            }
            copied.add(dst);
        }
        return copied;
    }
}
