package com.example.jasper;

import net.sf.jasperreports.functions.AbstractFunctionSupport;
import net.sf.jasperreports.functions.annotations.Function;
import net.sf.jasperreports.functions.annotations.FunctionCategories;
import net.sf.jasperreports.functions.annotations.FunctionParameter;
import net.sf.jasperreports.functions.annotations.FunctionParameters;
import net.sf.jasperreports.functions.standard.TextCategory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.swing.text.MaskFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static org.apache.commons.lang3.StringUtils.*;
import static org.springframework.util.StringUtils.isEmpty;


/**
 * Кастомные функции для использования в отчетах
 */
@FunctionCategories({TextCategory.class})
@SuppressWarnings("all")
public class TextFunctions extends AbstractFunctionSupport {
    private static final Locale RU_LOCALE = new Locale("ru");
    private static final Integer MONTH_IN_YEARS = 12;

    private static final int CARD_NUMBER_18_CHARS = 18;
    private static final int CARD_NUMBER_POSITION_MASK_START = 5;
    private static final int CARD_NUMBER_POSITION_MASK_END = 12;
    private static final String CARD_NUMBER_CHAR_MASK = "*";
    private static final char DEFAULT_GROUPING_SEPARATOR = ' ';
    private static final Pattern MOBILE_REG = Pattern.compile("\\+?[7|8]?(\\d{3})(\\d{3})(\\d{2})(\\d{2})$");
    private static final String TWO_SCALE_STRANGE = "#,##0.00";
    private static final String DASH = "-";
    private static final String DEFAULT_DATE = localDateCustomFormat(LocalDate.of(1990, 1, 1).toString(), "dd.MM.yyyy");

    private static final List<String> TITLES_MONTHS = new ArrayList<>(Arrays.asList("месяц", "месяца", "месяцев"));
    private static final List<String> TITLES_YEARS = new ArrayList<>(Arrays.asList("год", "года", "лет"));

    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    private static final String HTML_SPACE = "&nbsp";

    private static final Pattern splitRegex = Pattern.compile("\\, ");

    /**
     * Форматирование даты в виде строки по шаблону
     *
     * @param date   дата
     * @param format шаблон форматирования
     * @return форматированнная строка
     */
    @Function("dateCustomFormat")
    @FunctionParameters({@FunctionParameter("date")})
    public static String dateCustomFormat(Date date, String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format, RU_LOCALE).format(date);
    }

    /**
     * Форматирование даты в виде строки по шаблону
     *
     * @param date    дата
     * @param pattern шаблон форматирования
     * @return форматированнная строка
     */
    @Function("localDateCustomFormat")
    @FunctionParameters({
            @FunctionParameter("date"),
            @FunctionParameter("pattern")
    })
    public static String localDateCustomFormat(String date, String pattern) {
        if (date == null) {
            return "";
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofPattern(pattern, RU_LOCALE));
    }


    /**
     * Функция проставляет значение по умолчанию, если значение не задано
     *
     * @param value - Значение
     * @return непустое значение
     */
    @Function("defaultIfEmpty")
    @FunctionParameters({@FunctionParameter("value")})
    public static String defaultIfEmpty(Object value) {
        return defaultIfEmpty(value, "-");
    }

    /**
     * Функция проставляет значение по умолчанию, если значение не задано
     *
     * @param value        - Значение
     * @param defaultValue - Значение по умолчанию
     * @return непустое значение
     */
    @Function("defaultIfEmpty")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("defaultValue")})
    public static String defaultIfEmpty(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return isEmpty(value.toString()) ? defaultValue : value.toString();
    }

    /**
     * Функция проставляет значение Да/Нет
     *
     * @param value - Значение
     * @return Значение
     */
    @Function("yesNo")
    @FunctionParameters({@FunctionParameter("value")})
    public static String yesNo(Boolean value) {
        if (value == null) {
            return "<b> - </b>";
        }

        return Boolean.TRUE.equals(value) ? ("<b> Да </b>") : ("<b> Нет </b>");
    }


    /**
     * Форматирование даты в виде строки «DD» месяц yyyyг.
     *
     * @param date дата
     * @return строка вида «DD» месяц YYYYг.
     */
    @Function("dateFullFormat")
    @FunctionParameters({@FunctionParameter("date")})
    public static String dateFullFormat(Date date) {
        return new SimpleDateFormat("«d» MMMM yyyyг.", RU_LOCALE).format(date);
    }

    /**
     * функция склонения из выборки
     *
     * @param number количество
     * @param titles выборка
     * @return склонение
     */
    @SuppressWarnings({"squid:S109"})
    public static String declOfNum(Integer number, List<String> titles) {
        int[] cases = new int[]{2, 0, 1, 1, 1, 2};
        return titles.get
                (number % 100 > 4 && number % 100 < 20
                        ?
                        2
                        :
                        cases[Math.min(number % 10, 5)]);
    }

    /**
     * форматирование в месяцах
     *
     * @param number количество (в месяцах)
     * @return формат
     */
    @Function("formatMonths")
    @FunctionParameters({@FunctionParameter("number")})
    public static String formatMonths(Integer number) {
        Integer months = number % MONTH_IN_YEARS;
        Integer years = (number - months) / MONTH_IN_YEARS;
        if (years == 0) {
            return months + SPACE + declOfNum(months, TITLES_MONTHS);
        }

        if (months == 0) {
            return years + SPACE + declOfNum(years, TITLES_YEARS);
        }

        return years + SPACE + declOfNum(years, TITLES_YEARS) + SPACE + months + SPACE + declOfNum(months, TITLES_MONTHS);
    }


    /**
     * форматирование мобильного телефона по маске для РФ
     *
     * @param phoneNumber номер телефона
     * @return форматированный номер
     */
    @Function("getRussianPhone")
    @FunctionParameters({@FunctionParameter("phoneNumber")})
    public static String getRussianPhone(String phoneNumber) {
        if (isEmpty(phoneNumber)) {
            return phoneNumber;
        }

        Matcher mobMatch = MOBILE_REG.matcher(phoneNumber);
        if (!mobMatch.matches()) {
            return phoneNumber;
        }

        return mobMatch.replaceAll("+7 $1 $2-$3-$4");
    }

    /**
     * Функция вывода чекбокса с серым фоном
     *
     * @param checked взведен ли чекбокс
     * @return чекбокс
     */
    @Function("grayCheckbox")
    @FunctionParameters({@FunctionParameter("checked")})
    public static String grayCheckbox(boolean checked) {
        return "<span style=\"font-family:'Symbols'; background-color:'#C2C2C2';font-weight:'bold'\">" + (checked ? "&#x1F5F9;" : "&#9744;") + "</span>";
    }

    /**
     * Получить число в виде строки в формате 23 323.02
     *
     * @param amount - число
     * @return строка в формате 23 323.02
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormat(BigDecimal amount) {
        return decimalFormat(amount, TWO, "#,##0.00", '.');
    }


    /**
     * Получить число в виде строки в формате 23 323[разделитель]02
     *
     * @param amount    - число
     * @param separator - разделитель
     * @return строка в формате 23 323[разделитель]02
     */
    @Function("amountInDecimalFormatWithSeparator")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("separator")})
    public static String amountInDecimalFormatWithSeparator(BigDecimal amount, char separator) {
        return decimalFormat(amount, TWO, "#,##0.00", separator);
    }

    /**
     * Получить число в виде строки в формате 23 323[разделитель]02
     *
     * @param amount       - число
     * @param separator    - разделитель
     * @param defaultValue - значение по умолчанию
     * @return строка в формате 23 323[разделитель]02
     */
    @Function("amountInDecimalFormatWithSeparator")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("separator"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormatWithSeparator(BigDecimal amount, char separator, String defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amountInDecimalFormatWithSeparator(amount, separator == 0 ? '.' : separator);
    }

    /**
     * Функция, формирующая сумму строкой
     *
     * @param amount       сумма
     * @param defaultValue значение по умолчанию
     * @return сумма строкой
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormat(BigDecimal amount, String defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amountInDecimalFormat(amount);
    }

    /**
     * Получить число в виде строки в формате 23 323.022
     *
     * @param amount - число
     * @return строка в формате 23 323.022
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormatScaleThree(BigDecimal amount) {
        return decimalFormat(amount, THREE, "#,##0.000", '.');
    }

    /**
     * Функция, формирующая сумму строкой
     *
     * @param amount       сумма
     * @param defaultValue значение по умолчанию
     * @return сумма строкой
     */
    @Function("amountInDecimalFormatScaleThree")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormatScaleThree(BigDecimal amount, String defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amountInDecimalFormatScaleThree(amount);
    }

    private static String decimalFormat(BigDecimal amount, Integer scale, String strange, char separator) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(RU_LOCALE);
        symbols.setDecimalSeparator(separator);
        symbols.setGroupingSeparator(' ');
        amount = amount.setScale(scale, BigDecimal.ROUND_HALF_UP);
        DecimalFormat decimalFormat = new DecimalFormat(strange, symbols);
        return decimalFormat.format(amount);
    }

    /**
     * Возвращает символ по позиции или ' '
     *
     * @param string строка
     * @param index  позиция
     * @return символ
     */
    @Function("getCharAtPos")
    @FunctionParameters({@FunctionParameter("string"), @FunctionParameter("index")})
    public static Character getCharAtPos(String string, Integer index) {
        return string != null && string.length() > index ? string.charAt(index) : ' ';
    }

    /**
     * Функция возвращает строку из нескольких значений с заданым разделителем
     *
     * @param separator - разделитель значений
     * @param format    - формат значения
     * @param values    - список значений
     * @return строка из переданных значений
     */
    @SuppressWarnings("all")
    @Function("valuesFormatWithIndex")
    @FunctionParameters({@FunctionParameter("format"), @FunctionParameter("separator"), @FunctionParameter("values")})
    public static String valuesFormatWithIndex(String format, String separator, Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return "";
        }

        AtomicInteger index = new AtomicInteger(1);
        return values.stream()
                .filter(v -> !StringUtils.isEmpty(v))
                .map(value -> String.format(format, index.getAndIncrement(), value))
                .collect(Collectors.joining(separator));
    }

    /**
     * Функция возвращает строку из нескольких значений с заданым разделителем
     *
     * @param separator - разделитель значений
     * @param format    - формат значения
     * @param values    - список значений
     * @return строка из переданных значений
     */
    @Function("valuesFormatWithIndex")
    @FunctionParameters({@FunctionParameter("format"), @FunctionParameter("separator"), @FunctionParameter("values")})
    public static String valuesFormatWithIndex(String format, String separator, String values) {
        return valuesFormatWithIndex(format, separator, Arrays.asList(values.split("\\|")));
    }

    /**
     * Функция возвращает строку из нескольких значений
     *
     * @param separator - разделитель значений
     * @param values    - масив значений
     * @return срока из переданных значений
     */
    @Function("valuesArray")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("defaultValue")})
    public static String valuesArray(String separator, String... values) {
        return buildStringFromStream(Arrays.stream(values), separator, "", "");
    }

    /**
     * Функция возвращает строку из нескольких значений
     *
     * @param separator - разделитель значений
     * @param values    - масив значений
     * @return срока из переданных значений
     */
    @Function("valuesArray")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("defaultValue")})
    public static String valuesArrayIfExists(String separator, String... values) {
        boolean anyNotExists = Arrays.stream(values).anyMatch(StringUtils::isEmpty);
        return anyNotExists ? null : buildStringFromStream(Arrays.stream(values), separator, "", "");
    }

    private static String buildStringFromStream(Stream<String> stream, String separator, String prefix, String suffix) {
        return stream
                .filter(e -> !StringUtils.isEmpty(e))
                .filter(e -> !StringUtils.isEmpty(e.trim()))
                .collect(Collectors.joining(separator, prefix, suffix));
    }

    /**
     * Разбить строку на две части и получить первую(с полным переносом слов)
     *
     * @param original - строка
     * @param length   - длинна
     * @return первая часть строки
     */
    @Function("getHeadOfString")
    @FunctionParameters({@FunctionParameter("original"), @FunctionParameter("length")})
    public static String getHeadOfString(String original, int length) {
        return original.substring(0, findSplitIndex(original, length)).trim();
    }

    /**
     * Разбить строку на две части и получить вторую(с полным переносом слов)
     *
     * @param original - строка
     * @param length   - длинна
     * @return вторая часть строки
     */
    @Function("getTailOfString")
    @FunctionParameters({@FunctionParameter("original"), @FunctionParameter("length")})
    public static String getTailOfString(String original, int length) {
        return original.substring(findSplitIndex(original, length), original.length()).trim();
    }


    private static Integer findSplitIndex(String original, int length) {
        int index = 0;
        if (length == 0) {
            return 0;
        }
        if (original.length() <= length) {
            return original.length();
        }
        for (int i = 0; i < original.length(); i++) {
            if (original.charAt(i) == ' ') {
                if (i - 1 < length) {
                    index = i;
                } else {
                    return index;
                }
            }
        }
        return index;
    }

    /**
     * Функция приводит номер карты к определенному формату
     *
     * @param cardNumber - троковое представление номера карты
     * @return отформатированный номер карты
     */
    @Function("formatCardNumber")
    @FunctionParameter("cardNumber")
    public static String formatCardNumber(String cardNumber) {
        if (isEmpty(cardNumber)) {
            return EMPTY;
        }
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] chars = cardNumber.trim().toCharArray();
        if (chars.length < CARD_NUMBER_18_CHARS) {
            return formatCardNumberLess18Chars(chars, stringBuilder);
        } else {
            return formatCardNumberMore17Chars(chars, stringBuilder);
        }
    }

    @SuppressWarnings("all")
    private static String formatCardNumberLess18Chars(char[] chars, StringBuilder stringBuilder) {
        int num = 0;
        for (char c : chars) {
            if (num == 4 || num == 8 || num == 12) {
                stringBuilder.append(SPACE);
            }
            if (num > CARD_NUMBER_POSITION_MASK_START && num < CARD_NUMBER_POSITION_MASK_END) {
                stringBuilder.append(CARD_NUMBER_CHAR_MASK);
                num++;
                continue;
            }
            stringBuilder.append(c);
            num++;
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("all")
    private static String formatCardNumberMore17Chars(char[] chars, StringBuilder stringBuilder) {
        int num = 0;
        for (char c : chars) {
            if (num == 5 || num == 9 || num == 13) {
                stringBuilder.append(SPACE);
            }
            if (num > (CARD_NUMBER_POSITION_MASK_START + 1) && num < (CARD_NUMBER_POSITION_MASK_END + 1)) {
                stringBuilder.append(CARD_NUMBER_CHAR_MASK);
                num++;
                continue;
            }
            stringBuilder.append(c);
            num++;
        }
        return stringBuilder.toString();
    }

    /**
     * Функция трансформирует строковое представление OffsetDateTime в Date
     *
     * @param date - строковое представление OffsetDateTime
     * @return объект Date
     */
    @Function("formatDate")
    @FunctionParameter("date")
    @SuppressWarnings("all")
    public static Date formatDate(String date) {
        return Date.from((OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant()));
    }

    /**
     * Функция трансформирует строковое представление LocalDateTime в строку заданного формата
     *
     * @param date   - строковое представление LocalDateTime
     * @param format - формат
     * @return строка
     */
    @Function("formatDateFromLocalDateTime")
    @FunctionParameters({@FunctionParameter("date"), @FunctionParameter("format")})
    public static String formatDateFromLocalDateTime(String date, String format) {
        return DateTimeFormatter.ofPattern(format).format(LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Функция трансформирует строковое представление LocalDateTime в строку заданного формата
     *
     * @param date   - строковое представление LocalDateTime
     * @param format - формат
     * @param locale - язык
     * @return строка
     */
    @Function("formatDateFromLocalDateTime")
    @FunctionParameters({@FunctionParameter("date"), @FunctionParameter("format"), @FunctionParameter("locale")})
    public static String formatDateFromLocalDateTime(String date, String format, String locale) {
        return DateTimeFormatter.ofPattern(format, new Locale(locale)).format(LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Сделать текст жирным
     *
     * @param content контент
     * @return жирный контент
     */
    @Function("makeBold")
    @FunctionParameter("content")
    public static String makeBold(Object content) {
        return "<b>" + content + "</b>";
    }

    /**
     * Получить маскированное значение
     *
     * @param value строка
     * @param mask  маска
     * @return маскированная строка
     */
    @Function("maskString")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("mask")})
    public static String maskString(String value, String mask) {
        if (value == null) {
            return "";
        }
        try {
            MaskFormatter maskFormatter = new MaskFormatter(mask);
            maskFormatter.setValueContainsLiteralCharacters(false);
            return maskFormatter.valueToString(value);
        } catch (ParseException e) {
            return value;
        }
    }

    /**
     * Функция приводит номер счета к определенному формату
     *
     * @param cardAccountNumber - строковое представление номера счета
     * @return отформатированный номер счета
     */
    @Function("formatAccountNumber")
    @FunctionParameter("cardAccountNumber")
    public static String formatAccountNumber(String cardAccountNumber) {
        if (isEmpty(cardAccountNumber)) {
            return EMPTY;
        }
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] chars = cardAccountNumber.trim().toCharArray();

        return formatAccountNumberByAddingSpaces(chars, stringBuilder);
    }

    @SuppressWarnings("all")
    private static String formatAccountNumberByAddingSpaces(char[] chars, StringBuilder stringBuilder) {
        int num = 0;
        char s = ' ';
        for (char c : chars) {
            if (num == 3 || num == 5 || num == 8) {
                stringBuilder.append(s);
            }
            stringBuilder.append(c);
            num++;
        }
        return stringBuilder.toString();
    }

    /**
     * форматирование в годах/месяцах
     * если значение не делится без остатка на 12, то только кол-во месяцев (15 иесяцев)
     *
     * @param months количество месяцев
     * @return формат
     */
    @Function("formatOnlyMonths")
    @FunctionParameters({@FunctionParameter("months")})
    public static String formatOnlyMonths(Integer months) {
        return months + SPACE + declOfNum(months, TITLES_MONTHS);
    }

    /**
     * Функция, преобразует 1й символ в заглавный
     *
     * @param value значените
     * @return ставка строкой
     */
    @Function("upperFirst")
    @FunctionParameters({@FunctionParameter("rate")})
    public static String upperFirst(String value) {
        if (isEmpty(value)) {
            return value;
        }

        return String.format("%s%s", value.substring(0, 1).toUpperCase(), value.substring(1));
    }

    /**
     * Увеличение счетчика
     *
     * @param map {@link Map}
     * @param key ключ
     * @return след. значение
     */
    @Function("incCount")
    @FunctionParameters({@FunctionParameter("map"), @FunctionParameter("key")})
    public static String incCount(Map<String, Object> map, String key) {
        Object v = map.getOrDefault(key, 1);
        map.put(key, ((Integer) v) + 1);
        return String.valueOf(v);
    }

    /**
     * Получение значения счетчика
     *
     * @param map {@link Map}
     * @param key ключ
     * @return значение
     */
    @Function("getCount")
    @FunctionParameters({@FunctionParameter("map"), @FunctionParameter("key")})
    public static String getCount(Map<String, Object> map, String key) {
        return map.get(key) != null ? String.valueOf(map.get(key)) : null;
    }

    /**
     * Добавить пробелы
     *
     * @param count количество пробелов
     * @return пробелы
     */
    @Function("addHtmlWhiteSpace")
    @FunctionParameters({
            @FunctionParameter("count")
    })
    public static String addHtmlWhiteSpace(Integer count) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < count; i++) {
            res.append(HTML_SPACE);
        }
        return res.toString();
    }

    /**
     * получить число в виде строки в формате 23 323.02
     *
     * @param amount - число
     * @param scaleRange - количество нулей после запятой
     * @return строка в формате 23 323.02
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormat(BigDecimal amount, Integer scaleRange) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(RU_LOCALE);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        String scale = Arrays.asList(new String[scaleRange]).stream()
                .map(v -> "0")
                .reduce((v1, v2) -> v1 + v2)
                .get();
        String strange = "#,##0" + (isEmpty(scale) ? "" : "." + scale);
        amount = amount.setScale(scaleRange, BigDecimal.ROUND_HALF_UP);
        DecimalFormat decimalFormat = new DecimalFormat(strange, symbols);
        return decimalFormat.format(amount);
    }


    /**
     * Форматирование даты в виде строки по шаблону
     *
     * @param date   дата
     * @param format шаблон форматирования
     * @return форматированнная строка
     */
    @Function("localDateTimeCustomFormat")
    @FunctionParameters({@FunctionParameter("date")})
    public static String localDateTimeCustomFormat(LocalDateTime date, String format) {
        if (date == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern(format, RU_LOCALE).format(date);
    }

    /**
     * Функция вывода чекбокса
     *
     * @param checked взведен ли чекбокс
     * @return чекбокс
     */
    @Function("checkbox")
    @FunctionParameters({@FunctionParameter("checked")})
    @SuppressWarnings("squid:S2301")
    public static String checkbox(boolean checked) {
        return "<span style=\"font-family:'Symbols'\">" + (checked ? "&#x1f5f7;" : "&#9744;") + "</span>";
    }

    /**
     * Функция вывода чекбокса
     *
     * @param checked взведен ли чекбокс
     * @return чекбокс
     */
    @Function("checkmark")
    @FunctionParameters({@FunctionParameter("checked")})
    @SuppressWarnings("squid:S2301")
    public static String checkmark(boolean checked) {
        return "<span style=\"font-family:'Symbols'\">" + (checked ? "&#9745;" : "&#9744;") + "</span>";
    }

    /**
     * Функция вывода чекбокса или пустой строки
     *
     * @param checked взведен ли чекбокс
     * @return чекбокс
     */
    @Function("checkmarkOrEmpcheckty")
    @FunctionParameters({@FunctionParameter("checked")})
    @SuppressWarnings("squid:S2301")
    public static String checkmarkOrEmpty(boolean checked) {
        return "<span style=\"font-family:'Symbols'\">" + (checked ? "&#10004;" : "") + "</span>";
    }

    /**
     * Функция возвращает строку из нескольких значений с заданым разделителем
     *
     * @param separator - разделитель значений
     * @param values    - список значений
     * @return строка из переданных значений
     */
    @Function("valuesList")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("values")})
    public static String valuesList(String separator, Collection<String> values) {
        return buildStringFromStream(values.stream(), separator, "", "");
    }

    /**
     * Функция возвращает строку из нескольких значений с заданым разделителем
     *
     * @param separator - разделитель значений
     * @param prefix    - строка, которая выводится перед списком
     * @param suffix    - строка, которая выводится после списка
     * @param values    - список значений
     * @return строка из переданных значений
     */
    @Function("valuesList")
    @FunctionParameters({@FunctionParameter("value"), @FunctionParameter("values")})
    public static String valuesList(String separator, String prefix, String suffix, Collection<String> values) {
        return buildStringFromStream(values.stream(), separator, prefix, suffix);
    }

    /**
     * Функция проверки равенства двух дат
     *
     * @param firstDate  - первая дата
     * @param secondDate - вторая дата
     * @return true - даты равны
     */
    @Function("equalsDate")
    @FunctionParameters({@FunctionParameter("firstDate"), @FunctionParameter("secondDate")})
    public static boolean equalsDate(Object firstDate, Object secondDate) {
        return convertToLocalDate(firstDate).equals(convertToLocalDate(secondDate));
    }

    private static LocalDate convertToLocalDate(Object obj) {
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        }
        if (obj instanceof Date) {
            return ((Date) obj).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalDate();
        }
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        throw new RuntimeException(String.format("не удалось сконвертировать %s к LocalDate", obj.getClass().toString()));
    }

    /**
     * Функция формирующая сумму строкой
     * Формат: 12 руб. 12 коп.
     *
     * @param amount - сумма
     * @return - сформированная строка
     */
    @Function("amountInRurCurrencyStr")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInRurCurrencyStr(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(RU_LOCALE);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        String strange = "#,##0";
        amount = amount.setScale(TWO, BigDecimal.ROUND_HALF_UP);
        DecimalFormat decimalFormat = new DecimalFormat(strange, symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        StringBuilder stringBuilder = new StringBuilder(decimalFormat.format(amount));

        stringBuilder.append(" руб. ");

        int coins = amount.remainder(BigDecimal.ONE).movePointRight(TWO).setScale(0, ROUND_HALF_UP).intValue();
        stringBuilder.append(String.format("%02d", coins));
        stringBuilder.append(" коп.");

        return stringBuilder.toString();
    }

    /**
     * Функция формирующая сумму строкой
     * Если сумма 20.00(без копеек), то преобразует к 20-00
     * Если сумма 20.01(с копейками), то преобразует к 20-01
     *
     * @param amount - число
     * @return - сформированная строка
     */
    @Function("amountInString")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInString(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(RU_LOCALE);
        symbols.setDecimalSeparator('-');
        symbols.setGroupingSeparator(' ');
        amount = amount.setScale(TWO, BigDecimal.ROUND_HALF_UP);
        DecimalFormat decimalFormat =
                new DecimalFormat("###,##0.00", symbols);
        return decimalFormat.format(amount);
    }

    /**
     * Вставка подстроки в строку на указаную позицию
     *
     * @param destination строка в которую вставляется значение
     * @param value       добавляемая строка
     * @param position    позиция вставки
     * @param skipIfExist если значение = true - в случае наличия добавляемого значения
     *                    в строке (destination) на указанной позиции, повторно добавляться не будет
     * @return соединенная строка
     */
    @Function("paste")
    @FunctionParameters({
            @FunctionParameter("destination"),
            @FunctionParameter("value"),
            @FunctionParameter("position"),
            @FunctionParameter("skipIfExist")
    })
    public static String paste(String destination, String value, int position, boolean skipIfExist) {
        if (isEmpty(destination) || isEmpty(value) || destination.length() < position) {
            return destination;
        }

        if (skipIfExist && destination.indexOf(value) == position) {
            return destination;
        }

        return destination.substring(0, position) + value + destination.substring(position);
    }

    /**
     * Получить число в виде строки в формате 23 323.02
     *
     * @param amount - число
     * @return строка в формате 23 323.02
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormat(String amount) {
        return amountInDecimalFormat(new BigDecimal(amount));
    }

    /**
     * Функция, формирующая сумму строкой
     *
     * @param amount       сумма
     * @param defaultValue значение по умолчанию
     * @return сумма строкой
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormat(String amount, String defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amountInDecimalFormat(amount);
    }

    /**
     * Функция, формирующая сумму строкой
     *
     * @param amount         сумма
     * @param defaultValue   значение по умолчанию
     * @param groupSeparator разделитель групп разрядов
     * @return сумма строкой
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormat(BigDecimal amount, String defaultValue, char groupSeparator) {
        if (amount == null) {
            return defaultValue;
        }
        return decimalFormat(amount, TWO, TWO_SCALE_STRANGE, groupSeparator);
    }

    /**
     * форматирование в годах/месяцах
     * если значение не делится без остатка на 12, то только кол-во месяцев (15 месяцев)
     *
     * @param months количество месяцев
     * @return формат
     */
    @Function("formatYearOrMonths")
    @FunctionParameters({@FunctionParameter("months")})
    public static String formatYearOrMonths(Integer months) {
        if (months % MONTH_IN_YEARS != 0) {
            return months + SPACE + declOfNum(months, TITLES_MONTHS);
        }

        return formatMonths(months);
    }

    /**
     * Полчить название города из полного адреса
     *
     * @param address адрес
     * @return горож
     */
    @Function("extractCity")
    @FunctionParameters({@FunctionParameter("address")})
    @SuppressWarnings({"squid:S109"})
    public static String extractCity(String address) {
        int startIndex = address.indexOf("г.");
        int endIndex = address.indexOf(',', startIndex);
        if (endIndex == -1) {
            return address;
        }

        return address.substring(startIndex + 2, endIndex);
    }

    /**
     * Функция преобразует 1-й символ строки в прописной
     *
     * @param str Строка, которую нужно преобразовать
     * @return Преобразованная строка
     */
    @Function("lowerFirst")
    @FunctionParameters({@FunctionParameter("str")})
    public static String lowerFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.substring(0, 1).toLowerCase().concat(str.substring(1));
    }


    /**
     * Получить число в виде строки в формате 23 323.0222
     *
     * @param amount - число
     * @return строка в формате 23 323.0222
     */
    @Function("amountInDecimalFormat")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormatScaleFour(BigDecimal amount) {
        return decimalFormat(amount, FOUR, "#,##0.0000", '.');
    }

    /**
     * Функция, формирующая сумму строкой
     *
     * @param amount       сумма
     * @param defaultValue значение по умолчанию
     * @return сумма строкой
     */
    @Function("amountInDecimalFormatScaleFour")
    @FunctionParameters({@FunctionParameter("amount"), @FunctionParameter("defaultValue")})
    public static String amountInDecimalFormatScaleFour(BigDecimal amount, String defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amountInDecimalFormatScaleFour(amount);
    }

    /**
     * Получить число в виде строки в формате 23 323.0200
     *
     * @param amount - число
     * @return строка в формате 23 323.0200
     */
    @Function("amountInDecimalFormatScaleFour")
    @FunctionParameters({@FunctionParameter("amount")})
    public static String amountInDecimalFormatScaleFour(String amount) {
        return amountInDecimalFormatScaleFour(new BigDecimal(amount));
    }

    /**
     * заменить входную строку прочерком, если она равна 01.01.1990
     *
     * @param inputString входная строка
     * @return входная строка или прочерк
     */
    @Function("replaceDefaultDateWithADash")
    @FunctionParameters({
            @FunctionParameter("inputString")
    })
    public static String replaceDefaultDateWithADash(String inputString) {
        return inputString.equals(DEFAULT_DATE)
                ? DASH
                : inputString;
    }

    /**
     * Вывести код подразделения ДУЛ по маске ХХХ-ХХХ
     *
     * @param inputString входная строка
     * @return строка по маске ХХХ-ХХХ
     */
    @Function("maskClientDocIssuerCode")
    @FunctionParameters({
            @FunctionParameter("inputString")
    })
    public static String maskClientDocIssuerCode(String inputString) {
        if (Objects.isNull(inputString)) {
            return null;
        }

        if (Pattern.matches("\\d{3}-\\d{3}", inputString)) {
            return inputString;
        } else if (Pattern.matches("^\\d{6}$", inputString)) {
            return new StringBuilder(inputString).insert(inputString.length() - 3, DASH).toString();
        }
        return inputString;
    }

    /**
     * Строит ФИО по формату Фамилия И.О.
     *
     * @param lastName   Фамилия
     * @param firstName  Имя
     * @param middleName Отчество
     * @return Фамилия И.О.
     */
    @Function("buildFio")
    @FunctionParameters({@FunctionParameter("lastName"), @FunctionParameter("firstName"), @FunctionParameter("middleName")})
    public static String buildFio(String lastName, String firstName, String middleName) {
        if (isEmpty(middleName)) {
            return lastName + " " + firstName.charAt(0) + ".";
        }
        return lastName + " " + firstName.charAt(0) + "." + middleName.charAt(0) + ".";
    }
}
