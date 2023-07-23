package builderb0y.autocodec.constructors;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

/** thrown when an error occurs while constructing an object. */
public class ConstructException extends DecodeException {

	public ConstructException() {}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public ConstructException(String message) {
		super(message);
	}

	public ConstructException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
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