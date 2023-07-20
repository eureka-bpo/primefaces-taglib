package eu.eurekabpo.primefaces.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;

public class DefaultPremiumScheduleModel extends DefaultScheduleModel implements PremiumScheduleModel {
	private static final long serialVersionUID = -7164411821077003614L;

	private Map<Object, List<ScheduleEvent<?>>> events;
	private boolean eventLimit = false;
	
	public DefaultPremiumScheduleModel() {
		events = new HashMap<>();
	}
	
	public DefaultPremiumScheduleModel(Map<Object, List<ScheduleEvent<?>>> events) {
		this.events = events;
	}
	
	@Override
	public Set<Object> getKeys() {
		return events.keySet();
	}
	
	@Override
	public void addEvent(Object key, ScheduleEvent<?> event) {
		event.setId(UUID.randomUUID().toString());
		List<ScheduleEvent<?>> lEvents = events.get(key);
		if (lEvents == null) {
			lEvents = events.put(key, new ArrayList<ScheduleEvent<?>>());
		}
		events.get(key).add(event);
	}

	@Override
	public boolean deleteEvent(ScheduleEvent<?> event) {
		boolean result = false;
		for (List<ScheduleEvent<?>> lEvents : events.values()) {
			result = result | lEvents.remove(event);
		}
		return result;
	}
	
	@Override
	public List<ScheduleEvent<?>> getEvents() {
		List<ScheduleEvent<?>> result = new ArrayList<ScheduleEvent<?>>();
		for (List<ScheduleEvent<?>> lEvents : events.values()) {
			result.addAll(lEvents);
		}
		return result;
	}
	
	@Override
	public List<ScheduleEvent<?>> getEvents(Object key) {
		return events.get(key);
	}

	@Override
	public ScheduleEvent<?> getEvent(String id) {
		for (List<ScheduleEvent<?>> lEvents : events.values()) {
			for (ScheduleEvent<?> event : lEvents) {
				if (event.getId() != null && event.getId().equals(id)) {
					return event;
				}
			}
		}
		return null;
	}
	
	@Override
	public void updateEvent(ScheduleEvent<?> event) {
		Object key = null;
		int index = -1;

		outer: for (Map.Entry<Object, List<ScheduleEvent<?>>> entry : events.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				if (entry.getValue() .get(i).getId().equals(event.getId())) {
					key = entry.getKey();
					index = i;

					break outer;
				}
			}
		}

		if (key != null && index >= 0) {
			events.get(key).set(index, event);
		}
	}
	
	@Override
	public int getEventCount() {
		int count = 0;
		for (List<ScheduleEvent<?>> lEvents : events.values()) {
			count += lEvents.size();
		}
		return count;
	}
	
	@Override
	public int getEventCount(Object key) {
		return events.get(key).size();
	}

	@Override
	public void clear() {
		events = new HashMap<>();
	}
	
	@Override
	public void clear(Object key) {
		events.put(key, new ArrayList<>());
	}

	@Override
	public boolean isEventLimit() {
		return eventLimit;
	}

	public void setEventLimit(boolean eventLimit) {
		this.eventLimit = eventLimit;
	}
}