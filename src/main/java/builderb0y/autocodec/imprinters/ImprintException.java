package builderb0y.autocodec.imprinters;

import java.util.function.Supplier;

import builderb0y.autocodec.decoders.DecodeException;

public class ImprintException extends DecodeException {

	public ImprintException() {}

	public ImprintException(String message) {
		super(message);
	}

	public ImprintException(Supplier<String> messageSupplier) {
		super(messageSupplier);
	}

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