package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.fixers.AutoFixer;
import builderb0y.autocodec.fixers.DataAppendContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class FixingCoder<T_Decoded> extends NamedCoder<T_Decoded> {

	public final @NotNull AutoCoder<T_Decoded> coder;
	public final @NotNull AutoFixer<T_Decoded> fixer;

	public FixingCoder(
		@NotNull ReifiedType<T_Decoded> handledType,
		@NotNull AutoCoder<T_Decoded> coder,
		@NotNull AutoFixer<T_Decoded> fixer
	) {
		super(handledType);
		this.coder = coder;
		this.fixer = fixer;
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		return this.coder.getKeys();
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return new DecodeContext<>(context.fixDataWith(this.fixer)).decodeWith(this.coder);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull Data<T_Encoded> encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return new DataAppendContext<>(context, context.encodeWith(this.coder)).appendDataWith(this.fixer).data;
	}

	@Override
	public String toString() {
		return super.toString() + ": { coder: " + this.coder + ", fixer: " + this.fixer + " }";
	}
}