package builderb0y.autocodec.constructors;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

public class ConstructException extends DecodeException {

	public ConstructException() {}

	public ConstructException(String message) {
		super(message);
	}

	public ConstructException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	public ConstructException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstructException(Supplier<String> messageSupplier, Throwable cause) {
		super(messageSupplier, cause);
	}

	public ConstructException(Throwable cause) {
		super(cause);
	}
}