package builderb0y.autocodec.decoders;

public class DecodeException extends Exception {

	public DecodeException() {}

	public DecodeException(String message) {
		super(message);
	}

	public DecodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecodeException(Throwable cause) {
		super(cause);
	}
}