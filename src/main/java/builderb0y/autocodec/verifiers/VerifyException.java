package builderb0y.autocodec.verifiers;

import builderb0y.autocodec.decoders.DecodeException;

public class VerifyException extends DecodeException {

	public VerifyException() {}

	public VerifyException(String message) {
		super(message);
	}

	public VerifyException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerifyException(Throwable cause) {
		super(cause);
	}
}