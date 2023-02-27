package builderb0y.autocodec.reflection;

import builderb0y.autocodec.common.FactoryException;

public class ReflectException extends FactoryException {

	public ReflectException() {}

	public ReflectException(String message) {
		super(message);
	}

	public ReflectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectException(Throwable cause) {
		super(cause);
	}
}