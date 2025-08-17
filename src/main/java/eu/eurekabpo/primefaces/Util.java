package eu.eurekabpo.primefaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jakarta.el.ValueReference;

public class Util {
	private static Class<? extends Annotation> persistenceColumnClass;
	private static Class<? extends Annotation> validationMaxClass;
	static {
		try {
			persistenceColumnClass = (Class<? extends Annotation>) Class.forName("jakarta.persistence.Column");
		} catch (ClassNotFoundException e) {
		}
		try {
			validationMaxClass = (Class<? extends Annotation>) Class.forName("jakarta.validation.constraints.Max");
		} catch (ClassNotFoundException e) {
		}
	}

	public static Integer getMaxlength(ValueReference reference) {
		if (reference == null) {
			return null;
		}
		Field propertyField = null;
		try {
			propertyField = reference.getBase().getClass().getDeclaredField((String) reference.getProperty());
		} catch (NoSuchFieldException | SecurityException | ClassCastException | NullPointerException e) {
			return null;
		}
		if (persistenceColumnClass != null) {
			try {
				Annotation columnAnnotation = propertyField.getAnnotation(persistenceColumnClass);
				if (columnAnnotation != null) {
					int length = (int) persistenceColumnClass.getDeclaredMethod("length").invoke(columnAnnotation);
					return length;
				}
			} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
			}
		}
		if (validationMaxClass != null) {
			try {
				Annotation maxAnnotation = propertyField.getAnnotation(validationMaxClass);
				if (maxAnnotation != null) {
					int maxValue = Long
							.valueOf((long) validationMaxClass.getDeclaredMethod("value").invoke(maxAnnotation))
							.intValue();
					return maxValue;
				}
			} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
			}
		}
		return null;
	}
}