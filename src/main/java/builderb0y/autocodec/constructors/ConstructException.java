package builderb0y.autocodec.constructors;

import builderb0y.autocodec.decoders.DecodeException;

public class ConstructException extends DecodeException {

	public ConstructException() {}

	public ConstructException(String message) {
		super(message);
	}

	public ConstructException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstructException(Throwable cause) {
		super(cause);
	}
}