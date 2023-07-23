package builderb0y.autocodec.imprinters;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

/** thrown when an error occurs while imprinting an object. */
public class ImprintException extends DecodeException {

	public ImprintException() {}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public ImprintException(String message) {
		super(message);
	}

	public ImprintException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public ImprintException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImprintException(Supplier<String> messageSupplier, Throwable cause) {
		super(messageSupplier, cause);
	}

	public ImprintException(Throwable cause) {
		super(cause);
	}
}