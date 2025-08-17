package eu.eurekabpo.primefaces.component.inputtextarea;

import eu.eurekabpo.primefaces.Util;
import jakarta.el.ValueReference;
import jakarta.faces.context.FacesContext;

public class InputTextarea extends org.primefaces.component.inputtextarea.InputTextarea {

	@Override
	public int getMaxlength() {
		int maxValue = super.getMaxlength();
		if (maxValue != Integer.MAX_VALUE && maxValue != Integer.MIN_VALUE) {
			return maxValue;
		}
		ValueReference reference = this.getValueExpression("value")
				.getValueReference(FacesContext.getCurrentInstance().getELContext());
		Integer supposedMaxlength = Util.getMaxlength(reference);
		if (supposedMaxlength != null) {
			getStateHelper().put(org.primefaces.component.inputtextarea.InputTextareaBase.PropertyKeys.maxlength, supposedMaxlength);
			maxValue = supposedMaxlength;
		}
		return maxValue;
	}
}
