package builderb0y.autocodec.coders;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.DefaultValue;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DefaultDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DefaultCoder<T_Decoded> extends DefaultDecoder<T_Decoded> implements AutoCoder<T_Decoded> {

	public final @NotNull AutoEncoder<T_Decoded> encoder;

	public DefaultCoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoEncoder<T_Decoded> encoder,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull DefaultValue defaultValue
	) {
		super(type, decoder, defaultValue);
		this.encoder = encoder;
	}

	public DefaultCoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoCoder<T_Decoded> coder,
		@NotNull DefaultValue defaultValue
	) {
		this(type, coder, coder, defaultValue);
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return context.encodeWith(this.encoder);
	}

	@Override
	public String toString() {
		return (
			this.encoder == this.decoder
			? this.toString + ": { coder: " + this.encoder + ", default: " + this.defaultValue + " }"
			: this.toString + ": { encoder: " + this.encoder + ", decoder: " + this.decoder + ", default: " + this.defaultValue + " }"
		);
	}
}