package builderb0y.autocodec.verifiers;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

/**
thrown when an object does not meet certain requirements after being decoded.
for example, a common requirement is that the decoded object be non-null,
so if the object is null after being decoded, then a VerifyException would be thrown.
*/
public class VerifyException extends DecodeException {

	public VerifyException() {}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public VerifyException(String message) {
		super(message);
	}

	public VerifyException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
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