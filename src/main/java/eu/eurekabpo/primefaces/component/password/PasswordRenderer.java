package eu.eurekabpo.primefaces.component.password;

import java.io.IOException;

import jakarta.faces.context.ResponseWriter;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;

import jakarta.faces.context.FacesContext;

public class PasswordRenderer extends org.primefaces.component.password.PasswordRenderer {

    @Override
    protected void encodeMarkup(FacesContext context, org.primefaces.component.password.Password component) throws IOException {
        Password password = (Password) component;
        ResponseWriter writer = context.getResponseWriter();
        String clientId = password.getClientId(context);
        boolean toggleMask = password.isToggleMask();

        if (toggleMask) {
            writer.startElement("span", null);
            boolean isRTL = ComponentUtils.isRTL(context, password);
            String positionClass = getStyleClassBuilder(context)
                        .add(Password.STYLE_CLASS)
                        .add(Password.MASKED_CLASS)
                        .add(Password.WRAPPER_CLASS)
                        .add(isRTL, "ui-input-icon-left", "ui-input-icon-right")
                        .build();
            writer.writeAttribute("class", positionClass, null);
        }

        String inputClass = getStyleClassBuilder(context)
                        .add(!toggleMask, Password.STYLE_CLASS)
                        .add(createStyleClass(password, Password.INPUT_CLASS))
                        .build();

        writer.startElement("input", password);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("type", "password", null);
        writer.writeAttribute("class", inputClass, null);
        if (password.getStyle() != null) {
            writer.writeAttribute("style", password.getStyle(), null);
        }
        if (password.isIgnoreLastPass()) {
            writer.writeAttribute("data-lpignore", "true", null);
        }

        String valueToRender = ComponentUtils.getValueToRender(context, password);
        if (LangUtils.isNotBlank(valueToRender) && password.isRedisplay()) {
            writer.writeAttribute("value", LangUtils.isNotBlank(password.getValuePlaceholder()) ? password.getValuePlaceholder() : valueToRender, null);
        }

        renderAccessibilityAttributes(context, password);
        renderRTLDirection(context, password);
        renderPassThruAttributes(context, password, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, password, HTML.INPUT_TEXT_EVENTS);
        renderValidationMetadata(context, password);

        writer.endElement("input");

        if (toggleMask) {
            writer.startElement("i", null);
            writer.writeAttribute("id", clientId + "_mask", "id");
            writer.writeAttribute("class", Password.ICON_CLASS, null);
            writer.endElement("i");

            writer.endElement("span");
        }
    }

}
