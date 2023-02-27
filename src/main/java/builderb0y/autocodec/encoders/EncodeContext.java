package builderb0y.autocodec.encoders;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class EncodeContext<T_Encoded, T_Decoded> extends DynamicOpsContext<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<EncodeContext<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(EncodeContext.class).generic();

	public final @Nullable T_Decoded input;

	public EncodeContext(
		@NotNull AutoCodec codec,
		@Nullable T_Decoded input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(codec, ops);
		this.input = input;
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.encodeLogger;
	}

	@SuppressWarnings("unchecked")
	public <T_NewDecoded> EncodeContext<T_Encoded, T_NewDecoded> input(@Nullable T_NewDecoded newInput) {
		if (this.input == newInput) return (EncodeContext<T_Encoded, T_NewDecoded>)(this);
		return new EncodeContext<>(this.autoCodec, newInput, this.ops);
	}

	//////////////// other ////////////////

	public @NotNull T_Encoded encodeWith(@NotNull AutoEncoder<T_Decoded> encoder) throws EncodeException {
		return this.logger().encode(encoder, this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { input: " + this.input + ", ops: " + this.ops + " }";
	}
}