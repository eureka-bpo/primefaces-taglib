package eu.eurekabpo.primefaces.component.inputtext;

import eu.eurekabpo.primefaces.Util;
import jakarta.el.ValueReference;
import jakarta.faces.context.FacesContext;

public class InputText extends org.primefaces.component.inputtext.InputText {

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
			getStateHelper().put(jakarta.faces.component.html.HtmlInputText.PropertyKeys.maxlength, supposedMaxlength);
			maxValue = supposedMaxlength;
		}
		return maxValue;
	}
}
