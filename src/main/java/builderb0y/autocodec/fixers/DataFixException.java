package builderb0y.autocodec.fixers;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

/** thrown when an error occurs while fixing data. */
public class DataFixException extends DecodeException {

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DataFixException(String message) {
		super(message);
		this.message = message;
	}

	public DataFixException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DataFixException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataFixException(Supplier<String> messageSupplier, Throwable cause) {
		super(messageSupplier, cause);
	}

	public DataFixException(Throwable cause) {
		super(cause);
	}
}