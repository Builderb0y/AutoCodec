package builderb0y.autocodec.encoders;

import java.util.function.Supplier;

public class EncodeException extends RuntimeException {

	public Supplier<String> messageSupplier;
	public String message;

	public EncodeException() {}

	public EncodeException(String message) {
		this.message = message;
	}

	public EncodeException(Supplier<String> messageSupplier) {
		this.messageSupplier = messageSupplier;
	}

	public EncodeException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}

	public EncodeException(Supplier<String> messageSupplier, Throwable cause) {
		super(cause);
		this.messageSupplier = messageSupplier;
	}

	public EncodeException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		if (this.message != null) return this.message;
		if (this.messageSupplier != null) return this.message = this.messageSupplier.get();
		return null;
	}
}