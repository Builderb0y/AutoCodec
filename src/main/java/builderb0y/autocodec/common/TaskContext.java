package builderb0y.autocodec.common;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.util.ObjectArrayFactory;

public abstract class TaskContext {

	public static final @NotNull ObjectArrayFactory<TaskContext> ARRAY_FACTORY = new ObjectArrayFactory<>(TaskContext.class);

	public final @NotNull AutoCodec autoCodec;

	public TaskContext(@NotNull AutoCodec codec) {
		this.autoCodec = codec;
	}

	public @NotNull AutoCodec autoCodec() {
		return this.autoCodec;
	}

	public abstract @NotNull TaskLogger logger();

	@Override
	public abstract String toString();
}