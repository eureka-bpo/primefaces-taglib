package eu.eurekabpo.primefaces.component.premiumschedule;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.context.FacesContext;

import java.util.Locale;

import org.primefaces.component.schedule.Schedule;
import org.primefaces.util.LocaleUtils;

@ResourceDependencies({
	@ResourceDependency(library = "primefaces", name = "premiumschedule/premiumschedule.css"),
	@ResourceDependency(library = "primefaces", name = "components.css"),
	@ResourceDependency(library = "primefaces", name = "core.js"),
	@ResourceDependency(library = "primefaces", name = "components.js"),
	@ResourceDependency(library = "primefaces", name = "premiumschedule/premiumschedule.js")
})
public class PremiumSchedule extends Schedule {

	public static final String COMPONENT_FAMILY = "org.primefaces.component";
	public static final String COMPONENT_TYPE = PremiumSchedule.class.getName();
	public static final String DEFAULT_RENDERER = "eu.eurekabpo.primefaces.component.premiumschedule.PremiumScheduleRenderer";
	public static final String LICENSE_KEY = "primefaces.SCHEDULE_LICENSE_KEY";

	public enum PremiumPropertyKeys {

		datesAboveResources,
		
		resourceGroupField,
		resourceAreaWidth,
		resourceLabelText,
		resourcesInitiallyExpanded,
		slotMinWidth,
		eventMinWidth
	}

	public PremiumSchedule() {
		setRendererType(DEFAULT_RENDERER);
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public String getView() {
		return (String) getStateHelper().eval(PropertyKeys.view, "resourceTimeGridDay");
	}

	public String getRightHeaderTemplate() {
		return (String) getStateHelper().eval(PropertyKeys.rightHeaderTemplate, "resourceTimeGridDay,resourceTimeGridWeek");
	}

	public boolean isDatesAboveResources() {
		return (Boolean) getStateHelper().eval(PremiumPropertyKeys.datesAboveResources, false);
	}

	public void setDatesAboveResources(boolean datesAboveResources) {
		getStateHelper().put(PremiumPropertyKeys.datesAboveResources, datesAboveResources);
	}

	public String getResourceGroupField() {
		return (String) getStateHelper().eval(PremiumPropertyKeys.resourceGroupField);
	}

	public void setResourceGroupField(String resourceGroupField) {
		getStateHelper().put(PremiumPropertyKeys.resourceGroupField, resourceGroupField);
	}

	public String getResourceAreaWidth() {
		return (String) getStateHelper().eval(PremiumPropertyKeys.resourceAreaWidth, "30%");
	}

	public void setResourceAreaWidth(String resourceAreaWidth) {
		getStateHelper().put(PremiumPropertyKeys.resourceAreaWidth, resourceAreaWidth);
	}

	public String getResourceLabelText() {
		return (String) getStateHelper().eval(PremiumPropertyKeys.resourceLabelText, "Resources");
	}

	public void setResourceLabelText(String resourceLabelText) {
		getStateHelper().put(PremiumPropertyKeys.resourceLabelText, resourceLabelText);
	}

	public boolean isResourcesInitiallyExpanded() {
		return (Boolean) getStateHelper().eval(PremiumPropertyKeys.resourcesInitiallyExpanded, true);
	}

	public void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded) {
		getStateHelper().put(PremiumPropertyKeys.resourcesInitiallyExpanded, resourcesInitiallyExpanded);
	}

	public Integer getSlotMinWidth() {
		return (Integer) getStateHelper().eval(PremiumPropertyKeys.slotMinWidth);
	}

	public void setSlotMinWidth(Integer slotMinWidth) {
		getStateHelper().put(PremiumPropertyKeys.slotMinWidth, slotMinWidth);
	}

	public Integer getEventMinWidth() {
		return (Integer) getStateHelper().eval(PremiumPropertyKeys.eventMinWidth, 30);
	}

	public void setEventMinWidth(Integer eventMinWidth) {
		getStateHelper().put(PremiumPropertyKeys.eventMinWidth, eventMinWidth);
	}

	Locale calculateLocale(FacesContext facesContext) {
		return LocaleUtils.resolveLocale(facesContext, getLocale(), getClientId(facesContext));
	}
}