package builderb0y.autocodec.verifiers;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

public class VerifyException extends DecodeException {

	public VerifyException() {}

	public VerifyException(String message) {
		super(message);
	}

	public VerifyException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	public VerifyException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerifyException(Supplier<String> messageSupplier, Throwable cause) {
		super(messageSupplier, cause);
	}

	public VerifyException(Throwable cause) {
		super(cause);
	}
}