package builderb0y.autocodec.imprinters;

import builderb0y.autocodec.decoders.DecodeException;

public class ImprintException extends DecodeException {

	public ImprintException() {}

	public ImprintException(String message) {
		super(message);
	}

	public ImprintException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImprintException(Throwable cause) {
		super(cause);
	}
}