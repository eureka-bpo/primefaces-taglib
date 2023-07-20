package eu.eurekabpo.primefaces.model;

import java.util.List;
import java.util.Set;

import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

public interface PremiumScheduleModel extends ScheduleModel {
	Set<Object> getKeys();

	void addEvent(Object key, ScheduleEvent<?> event);

	List<ScheduleEvent<?>> getEvents(Object key);

	int getEventCount(Object key);

	void clear(Object key);
}