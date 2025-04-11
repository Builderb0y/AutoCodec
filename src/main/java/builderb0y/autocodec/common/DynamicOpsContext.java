package builderb0y.autocodec.common;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.data.DataFactory;
import builderb0y.autocodec.util.ObjectArrayFactory;

public abstract class DynamicOpsContext<T_Encoded> extends TaskContext implements DataFactory<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<DynamicOpsContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DynamicOpsContext.class).generic();

	public final @NotNull DynamicOps<T_Encoded> ops;

	public DynamicOpsContext(@NotNull AutoCodec codec, @NotNull DynamicOps<T_Encoded> ops) {
		super(codec);
		this.ops = ops;
	}

	@Override
	public @NotNull DynamicOps<T_Encoded> ops() {
		return this.ops;
	}

	public boolean isCompressed() {
		return this.ops.compressMaps();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { ops: " + this.ops + " }";
	}
}