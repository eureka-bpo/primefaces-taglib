package eu.eurekabpo.primefaces.component.schedule;

import java.io.IOException;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.primefaces.PrimeFaces;
import org.primefaces.component.outputpanel.OutputPanel;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.LocaleUtils;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.StateHelper;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;

@FacesComponent(value="eu.eurekabpo.primefaces.component.schedule.CronExpressionEditor")
public class CronExpressionEditor extends UIInput implements NamingContainer {

	@Override
	public String getFamily() {
		return UINamingContainer.COMPONENT_FAMILY;
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		String exprParam = (String) getValue();
		if (exprParam != null) {
			setValue(exprParam.trim());
		}
		parseExpression();
		super.encodeBegin(context);
	}

	private void parseExpression() {
		secondsInit();
		minutesInit();
		hoursInit();
		daysInit();
		monthsInit();
		yearsInit();
	}
	
	private String extractExpressionPart(int partIndex) {
		String expression = (String) getValue();
		if (expression == null || expression.isBlank()) {
			return null;
		}
		String[] parts = expression.trim().split(" ");
		return parts.length > partIndex ? parts[partIndex] : null;
	}
	
	private void updateExpressionPart(int partIndex, String value) {
		String[] parts = Stream.generate(() -> "").limit(7).collect(Collectors.toList()).toArray(String[]::new);
		if (getValue() != null) {
			String[] actualParts = ((String) getValue()).split(" ");
			System.arraycopy(actualParts, 0, parts, 0, actualParts.length);
		}
		parts[partIndex] = value;
		setValue(String.join(" ", parts).trim());
	}
	
	private Locale getLocale() {
		return LocaleUtils.getCurrentLocale();
	}
	
	/* seconds */
	private static final int SECONDS_EXPRESSION_PART_INDEX = 0;
	
	private void secondsInit() {
		String part = extractExpressionPart(SECONDS_EXPRESSION_PART_INDEX);
		if (part == null) {
			return;
		}
		if (part.contains("/")) {
			setSecondsMode(SecondsMode.EVERY_N);
			setSecondsEveryN(Integer.parseInt(part.substring(part.indexOf("/") + 1)));
			setSecondsEveryNStarting(Integer.parseInt(part.substring(0, part.indexOf("/"))));
		} else if (part.contains("-")) {
			setSecondsMode(SecondsMode.BETWEEN);
			setSecondsBetweenStart(Integer.parseInt(part.substring(0, part.indexOf("-"))));
			setSecondsBetweenFinish(Integer.parseInt(part.substring(part.indexOf("-") + 1)));
		} else if ("*".equals(part)) {
			return;
		} else {
			setSecondsMode(SecondsMode.SPECIFIC);
			String[] specificParts = part.split(",");
			for (String specificPart : specificParts) {
				getSeconds().put(leftPad(Integer.parseInt(specificPart)), Boolean.TRUE);
			}
		}
	}
	
	public void panelUpdate(AjaxBehaviorEvent event) {
		updateExpression(event);
		String componentId = ComponentTraversalUtils.closest(OutputPanel.class, event.getComponent()).getClientId();
		PrimeFaces.current().ajax().update(componentId);
	}
	
	private void secondsRecalculate() {
		String part = null;
		if (getSecondsMode() == SecondsMode.EVERY_N) {
			part = getSecondsEveryNStarting() + "/" + getSecondsEveryN();
		} else if (getSecondsMode() == SecondsMode.SPECIFIC) {
			SortedSet<Integer> items = new TreeSet<>();
			items.addAll(getSeconds().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(entry -> Integer.valueOf(entry.getKey())).sorted().distinct().collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(0);
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getSecondsMode() == SecondsMode.BETWEEN) {
			part = getSecondsBetweenStart() != getSecondsBetweenFinish() ?
					getSecondsBetweenStart() + "-" + getSecondsBetweenFinish() : 
						Integer.toString(getSecondsBetweenStart());
		} else {
			part = "*";
		}
		this.updateExpressionPart(SECONDS_EXPRESSION_PART_INDEX, part);
	}

	private void secondsReset(SecondsMode currentMode) {
		if (currentMode != SecondsMode.SPECIFIC) {
			getSeconds().clear();
		}
		if (currentMode != SecondsMode.EVERY_N) {
			getStateHelper().remove("secondsEveryN");
			getStateHelper().remove("secondsEveryNStarting");
		}
		if (currentMode != SecondsMode.BETWEEN) {
			getStateHelper().remove("secondsBetweenStart", 0);
			getStateHelper().remove("secondsBetweenFinish", SECONDS_BETWEEN_FINISH_INITIAL);
		}
	}

	private String leftPad(int number) {
		if (number >= 10) {
			return Integer.toString(number);
		} else {
			return "0" + Integer.toString(number);
		}
	}

	public List<String> getSecondsVariants() {
		return IntStream.range(0, 60).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	public Map<String, Boolean> getSeconds() {
		return evalAndPut(getStateHelper(), "seconds");
	}

	private class ModifiedHashMap extends HashMap<String, Boolean> {
		@Override
		public Boolean put(String key, Boolean value) {
			if (Boolean.TRUE.equals(value)) {
				return super.put(key, value);
			} else {
				return super.remove(key);
			}
		}
	}

	private Map<String, Boolean> evalAndPut(StateHelper stateHelper, Serializable key) {
		Map<String, Boolean> value = (Map<String, Boolean>) stateHelper.get(key);
		if (value == null) {
			value = new ModifiedHashMap();
		}
		stateHelper.put(key, value);
		return value;
	}

	public enum SecondsMode {
		EVERY,
		EVERY_N,
		SPECIFIC,
		BETWEEN
	}

	public void setSecondsModeOrdinal(int ordinal) {
		setSecondsMode(SecondsMode.values()[ordinal]);
	}
	
	public int getSecondsModeOrdinal() {
		return getSecondsMode().ordinal();
	}
	
	private SecondsMode getSecondsMode() {
		SecondsMode mode = (SecondsMode) getStateHelper().eval("secondsMode", SecondsMode.EVERY);
		return mode;
	}

	private void setSecondsMode(SecondsMode value) {
		if (getSecondsMode() != value) {
			getStateHelper().put("secondsMode", value);
			secondsReset(value);
		}
	}
	
	private static final int SECONDS_EVERY_N_INITIAL = 1;
	
	public int getSecondsEveryN() {
		return (Integer) getStateHelper().eval("secondsEveryN", SECONDS_EVERY_N_INITIAL);
	}

	public void setSecondsEveryN(int value) {
		getStateHelper().put("secondsEveryN", value);
	}

	public List<String> getSecondsEveryNVariants() {
		return IntStream.rangeClosed(1, 60).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	public int getSecondsEveryNStarting() {
		return (Integer) getStateHelper().eval("secondsEveryNStarting", 0);
	}

	public void setSecondsEveryNStarting(int value) {
		getStateHelper().put("secondsEveryNStarting", value);
	}

	public int getSecondsBetweenStart() {
		return (Integer) getStateHelper().eval("secondsBetweenStart", 0);
	}

	public void setSecondsBetweenStart(int value) {
		getStateHelper().put("secondsBetweenStart", value);
		if (getSecondsBetweenFinish() < value) {
			setSecondsBetweenFinish(value);
		}
	}

	private static final int SECONDS_BETWEEN_FINISH_INITIAL = 59;

	public int getSecondsBetweenFinish() {
		return (Integer) getStateHelper().eval("secondsBetweenFinish", SECONDS_BETWEEN_FINISH_INITIAL);
	}

	public void setSecondsBetweenFinish(int value) {
		getStateHelper().put("secondsBetweenFinish", value);
	}

	public List<String> getSecondsBetweenFinishVariants() {
		return IntStream.rangeClosed(getSecondsBetweenStart(), 60).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	/* minutes */
	private static final int MINUTES_EXPRESSION_PART_INDEX = 1;
	
	private void minutesInit() {
		String part = extractExpressionPart(MINUTES_EXPRESSION_PART_INDEX);
		if (part == null) {
			return;
		}
		if (part.contains("/")) {
			setMinutesMode(MinutesMode.EVERY_N);
			setMinutesEveryN(Integer.parseInt(part.substring(part.indexOf("/") + 1)));
			setMinutesEveryNStarting(Integer.parseInt(part.substring(0, part.indexOf("/"))));
		} else if (part.contains("-")) {
			setMinutesMode(MinutesMode.BETWEEN);
			setMinutesBetweenStart(Integer.parseInt(part.substring(0, part.indexOf("-"))));
			setMinutesBetweenFinish(Integer.parseInt(part.substring(part.indexOf("-") + 1)));
		} else if ("*".equals(part)) {
			return;
		} else {
			setMinutesMode(MinutesMode.SPECIFIC);
			String[] specificParts = part.split(",");
			for (String specificPart : specificParts) {
				getMinutes().put(leftPad(Integer.parseInt(specificPart)), Boolean.TRUE);
			}
		}
	}

	private void minutesRecalculate() {
		String part = null;
		if (getMinutesMode() == MinutesMode.EVERY_N) {
			part = getMinutesEveryNStarting() + "/" + getMinutesEveryN();
		} else if (getMinutesMode() == MinutesMode.SPECIFIC) {
			SortedSet<Integer> items = new TreeSet<>();
			items.addAll(getMinutes().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(entry -> Integer.valueOf(entry.getKey())).sorted().distinct().collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(0);
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getMinutesMode() == MinutesMode.BETWEEN) {
			part = getMinutesBetweenStart() != getMinutesBetweenFinish() ?
					getMinutesBetweenStart() + "-" + getMinutesBetweenFinish() :
						Integer.toString(getMinutesBetweenStart());
		} else {
			part = "*";
		}
		this.updateExpressionPart(MINUTES_EXPRESSION_PART_INDEX, part);
	}
	
	private void minutesReset(MinutesMode currentMode) {
		if (currentMode != MinutesMode.SPECIFIC) {
			getMinutes().clear();
		}
		if (currentMode != MinutesMode.EVERY_N) {
			getStateHelper().remove("minutesEveryN");
			getStateHelper().remove("minutesEveryNStarting");
		}
		if (currentMode != MinutesMode.BETWEEN) {
			getStateHelper().remove("minutesBetweenStart");
			getStateHelper().remove("minutesBetweenFinish");
		}
	}
	
	public List<String> getMinutesVariants() {
		return IntStream.range(0, 60).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	public Map<String, Boolean> getMinutes() {
		return evalAndPut(getStateHelper(), "minutes");
	}
	
	public enum MinutesMode {
		EVERY,
		EVERY_N,
		SPECIFIC,
		BETWEEN
	}
	
	public void setMinutesModeOrdinal(int ordinal) {
		setMinutesMode(MinutesMode.values()[ordinal]);
	}
	
	public int getMinutesModeOrdinal() {
		return getMinutesMode().ordinal();
	}

	private void setMinutesMode(MinutesMode value) {
		if (getMinutesMode() != value) {
			getStateHelper().put("minutesMode", value);
			minutesReset(value);
		}
	}
	
	private MinutesMode getMinutesMode() {
		return (MinutesMode) getStateHelper().eval("minutesMode", MinutesMode.EVERY);
	}
	
	private static final int MINUTES_EVERY_N_INITIAL = 1;
	
	public int getMinutesEveryN() {
		return (Integer) getStateHelper().eval("minutesEveryN", MINUTES_EVERY_N_INITIAL);
	}

	public void setMinutesEveryN(int value) {
		getStateHelper().put("minutesEveryN", value);
	}

	public List<String> getMinutesEveryNVariants() {
		return IntStream.rangeClosed(1, 60).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	public int getMinutesEveryNStarting() {
		return (Integer) getStateHelper().eval("minutesEveryNStarting", 0);
	}

	public void setMinutesEveryNStarting(int value) {
		getStateHelper().put("minutesEveryNStarting", value);
	}

	public int getMinutesBetweenStart() {
		return (Integer) getStateHelper().eval("minutesBetweenStart", 0);
	}

	public void setMinutesBetweenStart(int value) {
		getStateHelper().put("minutesBetweenStart", value);
		if (getMinutesBetweenFinish() < value) {
			setMinutesBetweenFinish(value);
		}
	}

	private static final int MINUTES_BETWEEN_FINISH_INITIAL = 59;

	public int getMinutesBetweenFinish() {
		return (Integer) getStateHelper().eval("minutesBetweenFinish", MINUTES_BETWEEN_FINISH_INITIAL);
	}

	public void setMinutesBetweenFinish(int value) {
		getStateHelper().put("minutesBetweenFinish", value);
	}

	public List<String> getMinutesBetweenFinishVariants() {
		return IntStream.rangeClosed(getMinutesBetweenStart(), 60).mapToObj(this::leftPad).collect(Collectors.toList());
	}
	
	/* hours */
	private static final int HOURS_EXPRESSION_PART_INDEX = 2;
	
	private void hoursInit() {
		String part = extractExpressionPart(HOURS_EXPRESSION_PART_INDEX);
		if (part == null) {
			return;
		}
		if (part.contains("/")) {
			setHoursMode(HoursMode.EVERY_N);
			setHoursEveryN(Integer.parseInt(part.substring(part.indexOf("/") + 1)));
			setHoursEveryNStarting(Integer.parseInt(part.substring(0, part.indexOf("/"))));
		} else if (part.contains("-")) {
			setHoursMode(HoursMode.BETWEEN);
			setHoursBetweenStart(Integer.parseInt(part.substring(0, part.indexOf("-"))));
			setHoursBetweenFinish(Integer.parseInt(part.substring(part.indexOf("-") + 1)));
		} else if ("*".equals(part)) {
			return;
		} else {
			setHoursMode(HoursMode.SPECIFIC);
			String[] specificParts = part.split(",");
			for (String specificPart : specificParts) {
				getHours().put(leftPad(Integer.parseInt(specificPart)), Boolean.TRUE);
			}
		}
	}
	
	private void hoursRecalculate() {
		String part = null;
		if (getHoursMode() == HoursMode.EVERY_N) {
			part = getHoursEveryNStarting() + "/" + getHoursEveryN();
		} else if (getHoursMode() == HoursMode.SPECIFIC) {
			SortedSet<Integer> items = new TreeSet<>();
			items.addAll(getHours().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(entry -> Integer.valueOf(entry.getKey())).sorted().distinct().collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(0);
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getHoursMode() == HoursMode.BETWEEN) {
			part = getHoursBetweenStart() != getHoursBetweenFinish() ?
					getHoursBetweenStart() + "-" + getHoursBetweenFinish() :
						Integer.toString(getHoursBetweenStart());
		} else {
			part = "*";
		}
		this.updateExpressionPart(HOURS_EXPRESSION_PART_INDEX, part);
	}
	
	private void hoursReset(HoursMode currentMode) {
		if (currentMode != HoursMode.SPECIFIC) {
			getHours().clear();
		}
		if (currentMode != HoursMode.EVERY_N) {
			getStateHelper().remove("hoursEveryN");
			getStateHelper().remove("hoursEveryNStarting");
		}
		if (currentMode != HoursMode.BETWEEN) {
			getStateHelper().remove("hoursBetweenStart");
			getStateHelper().remove("hoursBetweenFinish");
		}
	}
	
	public List<String> getHoursVariants() {
		return IntStream.range(0, 24).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	public Map<String, Boolean> getHours() {
		return evalAndPut(getStateHelper(), "hours");
	}
	
	public enum HoursMode {
		EVERY,
		EVERY_N,
		SPECIFIC,
		BETWEEN
	}
	
	public int getHoursModeOrdinal() {
		return getHoursMode().ordinal();
	}

	public void setHoursModeOrdinal(int ordinal) {
		setHoursMode(HoursMode.values()[ordinal]);
	}
	
	private void setHoursMode(HoursMode value) {
		if (getHoursMode() != value) {
			getStateHelper().put("hoursMode", value);
			hoursReset(value);
		}
	}
	
	private HoursMode getHoursMode() {
		return (HoursMode) getStateHelper().eval("hoursMode", HoursMode.EVERY);
	}
	
	private static final int HOURS_EVERY_N_INITIAL = 1;
	
	public int getHoursEveryN() {
		return (Integer) getStateHelper().eval("hoursEveryN", HOURS_EVERY_N_INITIAL);
	}

	public void setHoursEveryN(int value) {
		getStateHelper().put("hoursEveryN", value);
	}

	public List<String> getHoursEveryNVariants() {
		return IntStream.rangeClosed(1, 24).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	public int getHoursEveryNStarting() {
		return (Integer) getStateHelper().eval("hoursEveryNStarting", 0);
	}

	public void setHoursEveryNStarting(int value) {
		getStateHelper().put("hoursEveryNStarting", value);
	}

	public int getHoursBetweenStart() {
		return (Integer) getStateHelper().eval("hoursBetweenStart", 0);
	}

	public void setHoursBetweenStart(int value) {
		getStateHelper().put("hoursBetweenStart", value);
		if (getHoursBetweenFinish() < value) {
			setHoursBetweenFinish(value);
		}
	}

	private static final int HOURS_BETWEEN_FINISH_INITIAL = 23;

	public int getHoursBetweenFinish() {
		return (Integer) getStateHelper().eval("hoursBetweenFinish", HOURS_BETWEEN_FINISH_INITIAL);
	}

	public void setHoursBetweenFinish(int value) {
		getStateHelper().put("hoursBetweenFinish", value);
	}

	public List<String> getHoursBetweenFinishVariants() {
		return IntStream.range(getHoursBetweenStart(), 24).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	/* days of month */
	private static final int DAYS_OF_MONTH_EXPRESSION_PART_INDEX = 3;

	private void daysInit() {
		String partDoM = extractExpressionPart(DAYS_OF_MONTH_EXPRESSION_PART_INDEX);
		String partDoW = extractExpressionPart(DAYS_OF_WEEK_EXPRESSION_PART_INDEX);
		if (partDoM == null || partDoW == null) {
			return;
		}
		if ("*".equals(partDoM)) {
			setDaysMode(DaysMode.EVERY);
		} else if (partDoM.contains("/")) {
			setDaysMode(DaysMode.EVERY_N_OF_MONTH);
			setDaysEveryNOfMonthStarting(Integer.parseInt(partDoM.substring(0, partDoM.indexOf("/"))));
			setDaysEveryNOfMonth(Integer.parseInt(partDoM.substring(partDoM.indexOf("/") + 1)));
		} else if (partDoM.contains(",")) {
			setDaysMode(DaysMode.SPECIFIC_OF_MONTH);
			Stream.of(partDoM.split(",")).forEach(dom -> getDaysOfMonth().put(dom, Boolean.TRUE));
		} else if ("LW".equals(partDoM)) {
			setDaysMode(DaysMode.LAST_WEEKDAY);
		} else if ("L".equals(partDoM)) {
			setDaysMode(DaysMode.LAST);
		} else if (partDoM.startsWith("L-")) {
			setDaysMode(DaysMode.BEFORE_N_OF_MONTH);
			setDaysBeforeNOfMonth(Integer.parseInt(partDoM.substring("L-".length())));
		} else if (partDoM.endsWith("W")) {
			setDaysMode(DaysMode.NEAREST_WEEKDAY_N);
			setDaysNearestWeekdayN(Integer.parseInt(partDoM.substring(0, partDoM.length()-1)));
		} else if (partDoM.contains("-")) {
			setDaysMode(DaysMode.BETWEEN);
			setDaysBetweenStart(Integer.parseInt(partDoM.substring(0, partDoM.indexOf("-"))));
			setDaysBetweenFinish(Integer.parseInt(partDoM.substring(partDoM.indexOf("-") + 1)));
		} else if (partDoW.contains("/")) {
			setDaysMode(DaysMode.EVERY_N_OF_WEEK);
			int selectedDOW = Integer.parseInt(partDoW.substring(0, partDoW.indexOf("/")));
			setDaysEveryNOfWeekStarting(selectedDOW != 1 ? DayOfWeek.values()[selectedDOW - 2] : DayOfWeek.SUNDAY);
			setDaysEveryNOfWeek(Integer.parseInt(partDoW.substring(partDoW.indexOf("/") + 1)));
		} else if (partDoW.contains(",")) {
			setDaysMode(DaysMode.SPECIFIC_OF_WEEK);
			Stream.of(partDoW.split(",")).forEach(dom -> getDaysOfWeek().put(dom, Boolean.TRUE));
		} else if (partDoW.endsWith("L")) {
			setDaysMode(DaysMode.LAST_WEEKDAY_OF_MONTH);
			int selectedDOW = Integer.parseInt(partDoW.substring(0, partDoW.length()-1));
			setDaysLastWeekDay(selectedDOW != 1 ? DayOfWeek.values()[selectedDOW - 2] : DayOfWeek.SUNDAY);
		} else if (partDoW.contains("#")) {
			setDaysMode(DaysMode.N_WEEKDAY);
			setDaysNWeekdayOrdinal(partDoW.substring(partDoW.indexOf("#")+1));
			int selectedDOW = Integer.parseInt(partDoW.substring(0, partDoW.indexOf("#")));
			setDaysNWeekdayWeekday(selectedDOW != 1 ? DayOfWeek.values()[selectedDOW - 2] : DayOfWeek.SUNDAY);
		} 
	}

	private void dayOfMonthRecalculate() {
		String part = null;
		if (getDaysMode() == DaysMode.EVERY_N_OF_MONTH) {
			part = getDaysEveryNOfMonthStarting() + "/" + getDaysEveryNOfMonth();
		} else if (getDaysMode() == DaysMode.EVERY_N_OF_WEEK) {
			part = "?";
		} else if (getDaysMode() == DaysMode.SPECIFIC_OF_MONTH) {
			SortedSet<Integer> items = new TreeSet<>();
			items.addAll(getDaysOfMonth().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(entry -> Integer.valueOf(entry.getKey())).sorted().distinct().collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(0);
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getDaysMode() == DaysMode.SPECIFIC_OF_WEEK) {
			part = "?";
		} else if (getDaysMode() == DaysMode.BETWEEN) {
			part = getDaysBetweenStart() != getDaysBetweenFinish() ?
					getDaysBetweenStart() + "-" + getDaysBetweenFinish() :
						Integer.toString(getDaysBetweenStart());
		} else if (getDaysMode() == DaysMode.LAST) {
			part = "L";
		} else if (getDaysMode() == DaysMode.LAST_WEEKDAY) {
			part = "LW";
		} else if (getDaysMode() == DaysMode.LAST_WEEKDAY_OF_MONTH) {
			part = "?";
		} else if (getDaysMode() == DaysMode.BEFORE_N_OF_MONTH) {
			part = "L-" + getDaysBeforeNOfMonth();
		} else if (getDaysMode() == DaysMode.NEAREST_WEEKDAY_N) {
			part = getDaysNearestWeekdayN() + "W";
		} else if (getDaysMode() == DaysMode.N_WEEKDAY) {
			part = "?";
		} else {
			part = "*";
		}
		this.updateExpressionPart(DAYS_OF_MONTH_EXPRESSION_PART_INDEX, part);
	}
	
	private void daysReset(DaysMode currentMode) {
		if (currentMode != DaysMode.SPECIFIC_OF_MONTH) {
			this.getDaysOfMonth().clear();
		}
		if (currentMode != DaysMode.SPECIFIC_OF_WEEK) {
			this.getDaysOfWeek().clear();
		}
		if (currentMode != DaysMode.EVERY_N_OF_MONTH) {
			getStateHelper().remove("daysEveryNOfMonth");
			getStateHelper().remove("daysEveryNOfMonthStarting");
		}
		if (currentMode != DaysMode.EVERY_N_OF_WEEK) {
			getStateHelper().remove("daysEveryNOfWeek");
			getStateHelper().remove("daysEveryNOfWeekStarting");
		}
		if (currentMode != DaysMode.LAST_WEEKDAY) {
			getStateHelper().remove("daysLastWeekDay");
		}
		if (currentMode != DaysMode.BEFORE_N_OF_MONTH) {
			getStateHelper().remove("daysBeforeNOfMonth");
		}
		if (currentMode != DaysMode.NEAREST_WEEKDAY_N) {
			getStateHelper().remove("daysNearestWeekdayN");
		}
		if (currentMode != DaysMode.N_WEEKDAY) {
			getStateHelper().remove("daysNWeekdayOrdinal");
			getStateHelper().remove("daysNWeekdayWeekday");
		}
		if (currentMode != DaysMode.BETWEEN) {
			getStateHelper().remove("daysBetweenStart");
			getStateHelper().remove("daysBetweenFinish");
		}
	}
	
	public List<String> getDaysOfMonthVariants() {
		return IntStream.rangeClosed(1, 31).mapToObj(this::leftPad).collect(Collectors.toList());
	}

	public enum DaysMode {
		EVERY,
		EVERY_N_OF_MONTH,
		EVERY_N_OF_WEEK,
		SPECIFIC_OF_MONTH,
		SPECIFIC_OF_WEEK,
		LAST,
		LAST_WEEKDAY,
		LAST_WEEKDAY_OF_MONTH,
		BEFORE_N_OF_MONTH,
		NEAREST_WEEKDAY_N,
		N_WEEKDAY,
		BETWEEN
	}

	public Map<String, Boolean> getDaysOfMonth() {
		return evalAndPut(getStateHelper(), "daysOfMonth");
	}

	public void setDaysModeOrdinal(int ordinal) {
		setDaysMode(DaysMode.values()[ordinal]);
	}
	
	public int getDaysModeOrdinal() {
		return getDaysMode().ordinal();
	}
	
	private void setDaysMode(DaysMode value) {
		if (getDaysMode() != value) {
			getStateHelper().put("daysMode", value);
			daysReset(value);
		}
	}
	
	private DaysMode getDaysMode() {
		return (DaysMode)getStateHelper().eval("daysMode", DaysMode.EVERY);
	}
	
	private static final int DAYS_EVERY_N_OF_MONTH_INITIAL = 1;
	
	public int getDaysEveryNOfMonth() {
		return (Integer) getStateHelper().eval("daysEveryNOfMonth", DAYS_EVERY_N_OF_MONTH_INITIAL);
	}

	public void setDaysEveryNOfMonth(int value) {
		getStateHelper().put("daysEveryNOfMonth", value);
	}

	public List<String> getDaysEveryNOfMonthVariants() {
		return IntStream.rangeClosed(1, 31).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	private static final int DAYS_EVERY_N_OF_MONTH_STARTING_INITIAL = 1;

	public int getDaysEveryNOfMonthStarting() {
		return (Integer) getStateHelper().eval("daysEveryNOfMonthStarting", DAYS_EVERY_N_OF_MONTH_STARTING_INITIAL);
	}

	public void setDaysEveryNOfMonthStarting(int value) {
		getStateHelper().put("daysEveryNOfMonthStarting", value);
	}

	private static final int DAYS_EVERY_N_OF_WEEK_INITIAL = 1;
	
	public int getDaysEveryNOfWeek() {
		return (Integer) getStateHelper().eval("daysEveryNOfWeek", DAYS_EVERY_N_OF_WEEK_INITIAL);
	}

	public void setDaysEveryNOfWeek(int value) {
		getStateHelper().put("daysEveryNOfWeek", value);
	}

	public List<String> getDaysEveryNOfWeekVariants() {
		return IntStream.rangeClosed(1, 7).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	private final DayOfWeek DAYS_EVERY_N_OF_WEEK_STARTING_INITIAL = WeekFields.of(getLocale()).getFirstDayOfWeek();
	
	public DayOfWeek getDaysEveryNOfWeekStarting() {
		return (DayOfWeek) getStateHelper().eval("daysEveryNOfWeekStarting", DAYS_EVERY_N_OF_WEEK_STARTING_INITIAL);
	}

	public void setDaysEveryNOfWeekStarting(DayOfWeek value) {
		getStateHelper().put("daysEveryNOfWeekStarting", value);
	}

	private static final int DAYS_BETWEEN_START_INITIAL = 1;
	
	public int getDaysBetweenStart() {
		return (Integer) getStateHelper().eval("daysBetweenStart", DAYS_BETWEEN_START_INITIAL);
	}

	public void setDaysBetweenStart(int value) {
		getStateHelper().put("daysBetweenStart", value);
		if (getDaysBetweenFinish() < value) {
			setDaysBetweenFinish(value);
		}
	}

	private static final int DAYS_BETWEEN_FINISH_INITIAL = 31;

	public int getDaysBetweenFinish() {
		return (Integer) getStateHelper().eval("daysBetweenFinish", DAYS_BETWEEN_FINISH_INITIAL);
	}

	public void setDaysBetweenFinish(int value) {
		getStateHelper().put("daysBetweenFinish", value);
	}

	public List<String> getDaysBetweenFinishVariants() {
		return IntStream.rangeClosed(getDaysBetweenStart(), 31).mapToObj(this::leftPad).collect(Collectors.toList());
	}
	
	private final DayOfWeek DAYS_LAST_WEEK_DAY_INITIAL = WeekFields.of(getLocale()).getFirstDayOfWeek();
	
	public DayOfWeek getDaysLastWeekDay() {
		return (DayOfWeek) getStateHelper().eval("daysLastWeekDay", DAYS_LAST_WEEK_DAY_INITIAL);
	}

	public void setDaysLastWeekDay(DayOfWeek value) {
		getStateHelper().put("daysLastWeekDay", value);
	}
	
	private static final int DAYS_BEFORE_N_OF_MONTH_INITIAL = 1;

	public Integer getDaysBeforeNOfMonth() {
		return (Integer) getStateHelper().eval("daysBeforeNOfMonth", DAYS_BEFORE_N_OF_MONTH_INITIAL);
	}

	public void setDaysBeforeNOfMonth(Integer daysBeforeNOfMonth) {
		getStateHelper().put("daysBeforeNOfMonth", daysBeforeNOfMonth);
	}
	
	private static final int DAYS_NEAREST_WEEKDAY_N_INITIAL = 1;

	public int getDaysNearestWeekdayN() {
		return (Integer) getStateHelper().eval("daysNearestWeekdayN", DAYS_NEAREST_WEEKDAY_N_INITIAL);
	}

	public void setDaysNearestWeekdayN(int daysNearestWeekdayN) {
		getStateHelper().put("daysNearestWeekdayN", daysNearestWeekdayN);
	}
	
	private static final String DAYS_N_WEEKDAY_ORDINAL_INITIAL = "1";

	public String getDaysNWeekdayOrdinal() {
		return (String) getStateHelper().eval("daysNWeekdayOrdinal", DAYS_N_WEEKDAY_ORDINAL_INITIAL);
	}

	public void setDaysNWeekdayOrdinal(String daysNWeekdayOrdinal) {
		getStateHelper().put("daysNWeekdayOrdinal", daysNWeekdayOrdinal);
	}
	
	public List<String> getWeekdayOrdinalVariants() {
		return IntStream.rangeClosed(1, 5).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	private final DayOfWeek DAYS_N_WEEKDAY_WEEKDAY_INITIAL = WeekFields.of(getLocale()).getFirstDayOfWeek();
	
	public DayOfWeek getDaysNWeekdayWeekday() {
		return (DayOfWeek) getStateHelper().eval("daysNWeekdayWeekday", DAYS_N_WEEKDAY_WEEKDAY_INITIAL);
	}

	public void setDaysNWeekdayWeekday(DayOfWeek daysNWeekdayWeekday) {
		getStateHelper().put("daysNWeekdayWeekday", daysNWeekdayWeekday);
	}

	/* months */
	private static final int MONTH_EXPRESSION_PART_INDEX = 4;
	
	private void monthsInit() {
		String part = extractExpressionPart(MONTH_EXPRESSION_PART_INDEX);
		if (part == null) {
			return;
		}
		if (part.contains("/")) {
			setMonthsMode(MonthsMode.EVERY_N);
			setMonthsEveryN(Integer.parseInt(part.substring(part.indexOf("/") + 1)));
			setMonthsEveryNStarting(Month.values()[Integer.parseInt(part.substring(0, part.indexOf("/")))-1]);
		} else if (part.contains("-")) {
			setMonthsMode(MonthsMode.BETWEEN);
			setMonthsBetweenStart(monthFromCronExpressionFormat(part.substring(0, part.indexOf("-"))));
			setMonthsBetweenFinish(monthFromCronExpressionFormat(part.substring(part.indexOf("-") + 1)));
		} else if ("*".equals(part)) {
			return;
		} else {
			setMonthsMode(MonthsMode.SPECIFIC);
			Stream.of(part.split(",")).filter(mo -> Objects.equals(mo, monthFromCronExpressionFormat(mo)))
					.forEach(mo -> getMonths().put(mo, Boolean.TRUE));
		}
	}
	
	private void monthsRecalculate() {
		String part = null;
		if (getMonthsMode() == MonthsMode.EVERY_N) {
			part = (getMonthsEveryNStarting().ordinal() + 1) + "/" + getMonthsEveryN();
		} else if (getMonthsMode() == MonthsMode.SPECIFIC) {
			List<String> items = new ArrayList<>();
			items.addAll(getMonths().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(Map.Entry<String, Boolean>::getKey).map(Month::valueOf).distinct()
					.sorted().map(this::toCronExpressionFormat).collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(toCronExpressionFormat(Month.JANUARY));
			}
			part = String.join(",", items);
		} else if (getMonthsMode() == MonthsMode.BETWEEN) {
			part = getMonthsBetweenStart() != getMonthsBetweenFinish() ?
					toCronExpressionFormat(getMonthsBetweenStart()) + "-" + toCronExpressionFormat(getMonthsBetweenFinish()) :
						toCronExpressionFormat(getMonthsBetweenStart());
		} else {
			part = "*";
		}
		this.updateExpressionPart(MONTH_EXPRESSION_PART_INDEX, part);
	}
	
	private Month monthFromCronExpressionFormat(String monthExpression) {
		return Stream.of(Month.values()).filter(mo -> Objects.equals(toCronExpressionFormat(mo), monthExpression)).findAny().get();
	}

	private String toCronExpressionFormat(Month month) {
		return month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH);
	}
	
	private void monthsReset(MonthsMode currentMode) {
		if (currentMode != MonthsMode.SPECIFIC) {
			getMonths().clear();
		}
		if (currentMode != MonthsMode.EVERY_N) {
			getStateHelper().remove("monthsEveryN");
			getStateHelper().remove("monthsEveryNStarting");
		}
		if (currentMode != MonthsMode.BETWEEN) {
			getStateHelper().remove("monthsBetweenStart");
			getStateHelper().remove("monthsBetweenFinish");
		}
	}
	
	public List<String> getMonthsVariants() {
		return Arrays.stream(Month.values()).map(Month::toString).collect(Collectors.toList());
	}

	public Map<String, Boolean> getMonths() {
		return evalAndPut(getStateHelper(), "months");
	}
	
	public enum MonthsMode {
		EVERY,
		EVERY_N,
		SPECIFIC,
		BETWEEN
	}
	
	public void setMonthsModeOrdinal(int ordinal) {
		setMonthsMode(MonthsMode.values()[ordinal]);
	}
	
	public int getMonthsModeOrdinal() {
		return getMonthsMode().ordinal();
	}
	
	private void setMonthsMode(MonthsMode value) {
		if (getMonthsMode() != value) {
			getStateHelper().put("monthsMode", value);
			monthsReset(value);
		}
	}
	
	private MonthsMode getMonthsMode() {
		return (MonthsMode) getStateHelper().eval("monthsMode", MonthsMode.EVERY);
	}
	
	private static final int MONTHS_EVERY_N_INITIAL = 1;
	
	public int getMonthsEveryN() {
		return (Integer) getStateHelper().eval("monthsEveryN", MONTHS_EVERY_N_INITIAL);
	}

	public void setMonthsEveryN(int value) {
		getStateHelper().put("monthsEveryN", value);
	}

	public List<Integer> getMonthsEveryNVariants() {
		return Arrays.stream(Month.values()).map(month -> month.ordinal() + 1).collect(Collectors.toList());
	}

	private static final Month MONTH_EVERY_N_STARTING_INITIAL = Month.JANUARY;

	public Month getMonthsEveryNStarting() {
		return (Month) getStateHelper().eval("monthsEveryNStarting", MONTH_EVERY_N_STARTING_INITIAL);
	}

	public void setMonthsEveryNStarting(Month value) {
		getStateHelper().put("monthsEveryNStarting", value);
	}

	private static final Month MONTH_BETWEEN_START_INITIAL = Month.JANUARY;
	
	public Month getMonthsBetweenStart() {
		return (Month) getStateHelper().eval("monthsBetweenStart", MONTH_BETWEEN_START_INITIAL);
	}

	public void setMonthsBetweenStart(Month value) {
		getStateHelper().put("monthsBetweenStart", value);
		if (getMonthsBetweenFinish().ordinal() < value.ordinal()) {
			setMonthsBetweenFinish(value);
		}
	}

	private static final Month MONTH_BETWEEN_FINISH_INITIAL = Month.DECEMBER;

	public Month getMonthsBetweenFinish() {
		return (Month) getStateHelper().eval("monthsBetweenFinish", MONTH_BETWEEN_FINISH_INITIAL);
	}

	public void setMonthsBetweenFinish(Month value) {
		getStateHelper().put("monthsBetweenFinish", value);
	}

	public List<Month> getMonthsBetweenFinishVariants() {
		return Arrays.stream(Month.values()).filter(mo -> mo.ordinal() >= getMonthsBetweenStart().ordinal()).collect(Collectors.toList());
	}

	public String getMonthDisplayName(Month month) {
		return month.getDisplayName(TextStyle.FULL_STANDALONE, getLocale());
	}

	/* days of week */
	private static final int DAYS_OF_WEEK_EXPRESSION_PART_INDEX = 5;
	
	private void dayOfWeekRecalculate() {
		String part = null;
		if (getDaysMode() == DaysMode.EVERY_N_OF_WEEK) {
			DayOfWeek selectedDOW = getDaysEveryNOfWeekStarting();
			part = (selectedDOW != DayOfWeek.SUNDAY ? selectedDOW.ordinal() + 2 : 1) + "/" + getDaysEveryNOfWeek();
		} else if (getDaysMode() == DaysMode.SPECIFIC_OF_WEEK) {
			List<String> items = new ArrayList<>();
			items.addAll(getDaysOfWeek().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(Map.Entry<String, Boolean>::getKey).map(DayOfWeek::valueOf).distinct().sorted()
					.map(this::toCronExpressionFormat).collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(toCronExpressionFormat(WeekFields.of(getLocale()).getFirstDayOfWeek()));
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getDaysMode() == DaysMode.EVERY) {
			part = "*";
		} else if (getDaysMode() == DaysMode.LAST_WEEKDAY_OF_MONTH) {
			DayOfWeek selectedDOW = getDaysLastWeekDay();
			part = (selectedDOW != DayOfWeek.SUNDAY ? selectedDOW.ordinal() + 2 : 1) + "L";
		} else if (getDaysMode() == DaysMode.N_WEEKDAY) {
			DayOfWeek selectedDOW = getDaysNWeekdayWeekday();
			part = (selectedDOW != DayOfWeek.SUNDAY ? selectedDOW.ordinal() + 2 : 1) + "#" + getDaysNWeekdayOrdinal();
		} else {
			part = "?";
		}
		updateExpressionPart(DAYS_OF_WEEK_EXPRESSION_PART_INDEX, part);
	}
	
	private DayOfWeek dayOfWeekFromCronExpressionFormat(String dowExpr) {
		return Stream.of(DayOfWeek.values()).filter(dow -> Objects.equals(dowExpr, toCronExpressionFormat(dow))).findFirst().get();
	}
	
	private String toCronExpressionFormat(DayOfWeek dow) {
		return dow.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH).toUpperCase(Locale.ENGLISH);
	}
	
	public List<String> getDaysOfWeekVariants() {
		DayOfWeek first = WeekFields.of(getLocale()).getFirstDayOfWeek();
		return Arrays.asList(first, DayOfWeek.of((first.ordinal() + 1) % 7 + 1), DayOfWeek.of((first.ordinal() + 2) % 7 + 1),
				DayOfWeek.of((first.ordinal() + 3) % 7 + 1), DayOfWeek.of((first.ordinal() + 4) % 7 + 1),
				DayOfWeek.of((first.ordinal() + 5) % 7 + 1), DayOfWeek.of((first.ordinal() + 6) % 7 + 1))
				.stream().map(item -> item.toString()).collect(Collectors.toList());
	}
	
	public String getDayOfWeekDisplayName(String dayOfWeek) {
		return DayOfWeek.valueOf(dayOfWeek).getDisplayName(TextStyle.FULL_STANDALONE, getLocale());
	}
	
	public Map<String, Boolean> getDaysOfWeek() {
		return evalAndPut(getStateHelper(), "daysOfWeek");
	}
	
	/* years */
	private static final int YEARS_EXPRESSION_PART_INDEX = 6;
	
	private void yearsInit() {
		String part = extractExpressionPart(YEARS_EXPRESSION_PART_INDEX);
		if (part == null || part.isBlank()) {
			return;
		}
		if (part.contains("/")) {
			setYearsMode(YearsMode.EVERY_N);
			setYearsEveryN(Integer.parseInt(part.substring(part.indexOf("/") + 1)));
			setYearsEveryNStarting(Integer.parseInt(part.substring(0, part.indexOf("/"))));
		} else if (part.contains("-")) {
			setYearsMode(YearsMode.BETWEEN);
			setYearsBetweenStart(Integer.parseInt(part.substring(0, part.indexOf("-"))));
			setYearsBetweenFinish(Integer.parseInt(part.substring(part.indexOf("-") + 1)));
		} else if ("*".equals(part)) {
			return;
		} else {
			setYearsMode(YearsMode.SPECIFIC);
			String[] specificParts = part.split(",");
			for (String specificPart : specificParts) {
				getYears().put(specificPart, Boolean.TRUE);
			}
		}
	}
	
	private void yearsRecalculate() {
		String part = null;
		if (getYearsMode() == YearsMode.EVERY_N) {
			part = getYearsEveryNStarting() + "/" + getYearsEveryN();
		} else if (getYearsMode() == YearsMode.SPECIFIC) {
			SortedSet<Integer> items = new TreeSet<>();
			items.addAll(getYears().entrySet().stream().filter(entry -> entry.getValue() == Boolean.TRUE)
					.map(entry -> Integer.valueOf(entry.getKey())).sorted().distinct().collect(Collectors.toList()));
			if (items.isEmpty()) {
				items.add(LocalDate.now().getYear());
			}
			part = items.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (getYearsMode() == YearsMode.BETWEEN) {
			part = getYearsBetweenStart() != getYearsBetweenFinish() ?
					getYearsBetweenStart() + "-" + getYearsBetweenFinish() :
						Integer.toString(getYearsBetweenStart());
		} else {
			part = "";
		}
		this.updateExpressionPart(YEARS_EXPRESSION_PART_INDEX, part);
	}
	
	private void yearsReset(YearsMode currentMode) {
		if (currentMode != YearsMode.SPECIFIC) {
			getYears().clear();
		}
		if (currentMode != YearsMode.EVERY_N) {
			getStateHelper().remove("yearsEveryN");
			getStateHelper().remove("yearsEveryNStarting");
		}
		if (currentMode != YearsMode.BETWEEN) {
			getStateHelper().remove("yearsBetweenStart");
			getStateHelper().remove("yearsBetweenFinish");
		}
	}
	
	public List<String> getYearsVariants() {
		return IntStream.rangeClosed(LocalDate.now().getYear(), LocalDate.now().plusYears(20).getYear()).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	public Map<String, Boolean> getYears() {
		return evalAndPut(getStateHelper(), "years");
	}
	
	public enum YearsMode {
		EVERY,
		EVERY_N,
		SPECIFIC,
		BETWEEN
	}
	
	public void setYearsModeOrdinal(int ordinal) {
		setYearsMode(YearsMode.values()[ordinal]);
	}
	
	public int getYearsModeOrdinal() {
		return getYearsMode().ordinal();
	}
	
	private void setYearsMode(YearsMode value) {
		if (getYearsMode() != value) {
			getStateHelper().put("yearsMode", value);
			yearsReset(value);
		}
	}
	
	private YearsMode getYearsMode() {
		return (YearsMode) getStateHelper().eval("yearsMode", YearsMode.EVERY);
	}
	
	private final int YEARS_EVERY_N_INITIAL = 1;
	
	public int getYearsEveryN() {
		return (Integer) getStateHelper().eval("yearsEveryN", YEARS_EVERY_N_INITIAL);
	}

	public void setYearsEveryN(int value) {
		getStateHelper().put("yearsEveryN", value);
	}

	public List<String> getYearsEveryNVariants() {
		return IntStream.rangeClosed(1, 20).mapToObj(Integer::toString).collect(Collectors.toList());
	}

	private static final int YEARS_EVERY_N_STARTING_INITIAL = LocalDate.now().getYear();

	public int getYearsEveryNStarting() {
		return (Integer) getStateHelper().eval("yearsEveryNStarting", YEARS_EVERY_N_STARTING_INITIAL);
	}

	public void setYearsEveryNStarting(int value) {
		getStateHelper().put("yearsEveryNStarting", value);
	}

	private final int YEARS_BETWEEN_START_INITIAL = LocalDate.now().getYear();
	
	public int getYearsBetweenStart() {
		return (Integer) getStateHelper().eval("yearsBetweenStart", YEARS_BETWEEN_START_INITIAL);
	}

	public void setYearsBetweenStart(int value) {
		getStateHelper().put("yearsBetweenStart", value);
		if (getYearsBetweenFinish() < value) {
			setYearsBetweenFinish(value);
		}
	}

	private final int YEARS_BETWEEN_FINISH_INITIAL = YEARS_BETWEEN_START_INITIAL;

	public int getYearsBetweenFinish() {
		return (Integer) getStateHelper().eval("yearsBetweenFinish", YEARS_BETWEEN_FINISH_INITIAL);
	}

	public void setYearsBetweenFinish(int value) {
		getStateHelper().put("yearsBetweenFinish", value);
	}

	public List<String> getYearsBetweenFinishVariants() {
		return IntStream.rangeClosed(getYearsBetweenStart(), getYearsBetweenStart() + 20).mapToObj(Integer::toString).collect(Collectors.toList());
	}
	
	public void updateExpression(AjaxBehaviorEvent event) {
		secondsRecalculate();
		minutesRecalculate();
		hoursRecalculate();
		dayOfMonthRecalculate();
		monthsRecalculate();
		dayOfWeekRecalculate();
		yearsRecalculate();
		
		String componentId = ComponentTraversalUtils.closest(this.getClass(), event.getComponent()).getClientId();
		PrimeFaces.current().ajax().update(":" + componentId + ":expression");
	}
	
	private ResourceBundle messages = ResourceBundle.getBundle("eu.eurekabpo.primefaces.component.schedule.messages", getLocale());
	
	public String messages(String key) {
		return messages.getString(key);
	}
}
