package builderb0y.autocodec.fixers;

import java.util.function.Supplier;

/** thrown when an error occurs while fixing data. */
public class DataFixException extends RuntimeException {

	public Supplier<String> messageSupplier;
	public String message;

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DataFixException(String message) {
		this.message = message;
	}

	public DataFixException(Supplier<String> messageSupplier) {
		this.messageSupplier = messageSupplier;
	}

	/** @deprecated it will likely be more efficient to use ths Supplier-based constructor. */
	@Deprecated
	public DataFixException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}

	public DataFixException(Supplier<String> messageSupplier, Throwable cause) {
		super(cause);
		this.messageSupplier = messageSupplier;
	}

	public DataFixException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		if (this.message != null) return this.message;
		if (this.messageSupplier != null) return this.message = this.messageSupplier.get();
		return null;
	}
}