package builderb0y.autocodec.decoders;

import java.util.function.Supplier;

/** thrown when an error occurs while decoding an object. */
public class DecodeException extends Exception {

	public Supplier<String> messageSupplier;
	public String message;

	public DecodeException() {}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DecodeException(String message) {
		this.message = message;
	}

	public DecodeException(Supplier<String> messageSupplier) {
		this.messageSupplier = messageSupplier;
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DecodeException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}

	public DecodeException(Supplier<String> messageSupplier, Throwable cause) {
		super(cause);
		this.messageSupplier = messageSupplier;
	}

	public DecodeException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		if (this.message != null) return this.message;
		if (this.messageSupplier != null) return this.message = this.messageSupplier.get();
		return null;
	}
}