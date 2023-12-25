package eu.eurekabpo.primefaces.component.password;

import java.util.Objects;

import eu.eurekabpo.primefaces.component.premiumschedule.PremiumSchedule.PremiumPropertyKeys;

public class Password extends org.primefaces.component.password.Password {

    public static final String COMPONENT_FAMILY = "org.primefaces.component";
    public static final String COMPONENT_TYPE = Password.class.getName();
    public static final String DEFAULT_RENDERER = "eu.eurekabpo.primefaces.component.password.PasswordRenderer";

    public enum AdditionalPropertyKeys {
        valuePlaceholder,
    }

    public Password() {
        setRendererType(DEFAULT_RENDERER);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getValuePlaceholder() {
        return (String) getStateHelper().eval(AdditionalPropertyKeys.valuePlaceholder, null);
    }

    public void setValuePlaceholder(String valuePlaceholder) {
        getStateHelper().put(AdditionalPropertyKeys.valuePlaceholder, valuePlaceholder);
    }

    @Override
    public void setValue(Object value) {
        if (value == null || getValuePlaceholder() == null || !Objects.equals(value, getValuePlaceholder())) {
            super.setValue(value);
        }
    }
}
