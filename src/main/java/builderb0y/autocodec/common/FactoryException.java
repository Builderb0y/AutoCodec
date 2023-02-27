package builderb0y.autocodec.common;

import builderb0y.autocodec.common.AutoHandler.AutoFactory;

/**
typically thrown when an {@link AutoFactory} is unable to
satisfy a request for an {@link AutoHandler} in some way.
*/
public class FactoryException extends RuntimeException {

	public FactoryException() {}

	public FactoryException(String message) {
		super(message);
	}

	public FactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public FactoryException(Throwable cause) {
		super(cause);
	}
}