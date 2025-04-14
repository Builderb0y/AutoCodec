package builderb0y.autocodec.fixers;

import java.util.function.Supplier;

import builderb0y.autocodec.encoders.EncodeException;

public class DataAppendException extends EncodeException {

	public DataAppendException() {}

	@Deprecated
	public DataAppendException(String message) {
		super(message);
	}

	public DataAppendException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	@Deprecated
	public DataAppendException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataAppendException(Supplier<String> messageSupplier, Throwable cause) {
		super(messageSupplier, cause);
	}

	public DataAppendException(Throwable cause) {
		super(cause);
	}
}