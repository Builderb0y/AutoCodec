package builderb0y.autocodec.encoders;

public class EncodeException extends RuntimeException {

	public EncodeException() {}

	public EncodeException(String message) {
		super(message);
	}

	public EncodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncodeException(Throwable cause) {
		super(cause);
	}
}