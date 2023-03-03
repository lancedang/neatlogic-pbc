/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
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

package neatlogic.module.pbc.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 人行数据值域校验器
 */
public class ValueRangeValidator {
    private static final Pattern commonPattern = Pattern.compile("([fxoce])([\\d]*\\.\\.[\\d]*)(\\([\\d]\\))?", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern datePatten = Pattern.compile("YYYY-MM-DD(\\sHH:MM)?", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private Pattern p;
    private String type;
    private int min;
    private int max;
    private int tailLength;
    private String valueRange;

    private ValueRangeValidator() {

    }

    private ValueRangeValidator(Pattern pattern, String _type, String _valueRange) {
        p = pattern;
        type = _type;
        valueRange = _valueRange;
    }

    private ValueRangeValidator(Pattern pattern, String _type, int _min, int _max, int _tailLength) {
        p = pattern;
        type = _type;
        min = _min;
        max = _max;
        tailLength = _tailLength;
    }

    /*
     * @Description: 验证，如果返回空字符串代表没有异常
     * @Author: chenqiwei
     * @Date: 2021/2/2 3:10 下午
     * @Params: [itemAuditVo]
     * @Returns: java.lang.String
     **/
    public String valid(String value) {
        if (p != null) {
            if ("date".equalsIgnoreCase(type)) {
                if (!p.matcher(value).matches()) {
                    if (StringUtils.isNotBlank(valueRange)) {
                        return "格式必须符合：" + valueRange;
                    } else {
                        return "不明格式错误";
                    }
                }
            } else if ("boolean".equalsIgnoreCase(type)) {
                if (!p.matcher(value).matches()) {
                    if (StringUtils.isNotBlank(valueRange)) {
                        return "格式必须符合：" + valueRange;
                    } else {
                        return "不明格式错误";
                    }
                }
            } else {
                if (!p.matcher(value).matches()) {
                    switch (type) {
                        case "e":
                            if (min > 0 && max > 0) {
                                return String.format("至少%s个（含）且至多%s个(含)大、小写字母自由组合组成的英文字母字符串。", min, max);
                            } else if (min == 0 && max > 0) {
                                return String.format("至多%s个(含)大、小写字母自由组合组成的英文字母字符串。", max);
                            } else if (min > 0 && max == 0) {
                                return String.format("至少%s个（含）大、小写字母自由组合组成的英文字母字符串。", min);
                            }
                        case "f":
                            if (max > 0 && tailLength > 0) {
                                return String.format("至多%s个（含）字符组成十进制整数部分，小数部分定长为%s个字符。", max, tailLength);
                            }
                            return "不明浮点型错误。";
                        case "x":
                            if (min > 0 && max > 0) {
                                return String.format("至少%s个（含）且至多%s个(含)字符组成的十进制字符串。", min, max);
                            } else if (min == 0 && max > 0) {
                                return String.format("至多%s个(含)字符组成的十进制字符串。", max);
                            } else if (min > 0 && max == 0) {
                                return String.format("至少%s个（含）字符组成的十进制字符串。", min);
                            }
                        case "o":
                            if (min > 0 && max > 0) {
                                return String.format("至少%s个（含）且至多%s个(含)字符组成的十进制字符串。", min, max);
                            } else if (min == 0 && max > 0) {
                                return String.format("至多%s个(含)字符组成的十进制字符串。", max);
                            } else if (min > 0 && max == 0) {
                                return String.format("至少%s个（含）字符组成的十进制字符串。", min);
                            }
                        case "c":
                            if (min > 0 && max > 0) {
                                return String.format("至少%s个（含）且至多%s个(含)字符组成的通用字符串。", min, max);
                            } else if (min == 0 && max > 0) {
                                return String.format("至多%s个(含)字符组成的通用字符串。", max);
                            } else if (min > 0 && max == 0) {
                                return String.format("至少%s个（含）字符组成的通用字符串。", min);
                            }
                    }
                }
            }
        }
        return "";
    }

    public static ValueRangeValidator build(String valueRange) {
        if (StringUtils.isNotBlank(valueRange)) {
            Matcher matcher = commonPattern.matcher(valueRange);
            if (matcher.find()) {
                String type = matcher.group(1);
                String length = matcher.group(2);
                String tailLen = matcher.group(3);
                String[] ls = length.split("\\.\\.");
                int min = 0;
                int max = 0;
                if (ls.length > 0 && StringUtils.isNotBlank(ls[0])) {
                    min = Integer.parseInt(ls[0]);
                }
                if (ls.length > 1 && StringUtils.isNotBlank(ls[1])) {
                    max = Integer.parseInt(ls[1]);
                }
                int tailLength = 0;
                if (StringUtils.isNotBlank(tailLen)) {
                    tailLength = Integer.parseInt(tailLen.replace("(", "").replace(")", ""));
                }
                switch (type) {
                    case "e":
                        return new ValueRangeValidator(Pattern.compile("^[a-zA-Z]{" + min + "," + (max > 0 ? max : "") + "}$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), type, min, max, tailLength);
                    case "f":
                        return new ValueRangeValidator(Pattern.compile("^\\d{" + min + "," + (max > 0 ? max : "") + "}" + (tailLength > 0 ? "(\\.\\d{" + tailLength + "})?" : "") + "$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), type, min, max, tailLength);
                    case "x":
                        return new ValueRangeValidator(Pattern.compile("^[0-9A-Z]{" + min + "," + (max > 0 ? max : "") + "}$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), type, min, max, tailLength);
                    case "o":
                        //System.out.println("^[0-9]+((,[0-9]{" + min + "," + (max > 0 ? max : "") +"}){1,})?$");
                        return new ValueRangeValidator(Pattern.compile("^[0-9]+((,[0-9]{" + min + "," + (max > 0 ? max : "") + "}){1,})?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), type, min, max, tailLength);
                    case "c":
                        return new ValueRangeValidator(Pattern.compile("^.{" + min + "," + (max > 0 ? max : "") + "}$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), type, min, max, tailLength);
                }
            } else {
                matcher = datePatten.matcher(valueRange);
                if (matcher.find()) {
                    String p = valueRange.replaceAll("[YyMmDdHhSs]", "\\\\d").replaceAll("-", "\\\\-");
                    return new ValueRangeValidator(Pattern.compile("^" + p + "$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), "date", valueRange);
                } else if (valueRange.equals("True/False")) {
                    String p = valueRange.replaceAll("/", "|");
                    return new ValueRangeValidator(Pattern.compile("^(" + p + ")$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), "boolean", valueRange);

                }
            }
        }
        return new ValueRangeValidator();
    }

}
