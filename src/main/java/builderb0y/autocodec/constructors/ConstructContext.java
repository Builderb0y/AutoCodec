package builderb0y.autocodec.constructors;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class ConstructContext<T_Encoded> extends DecodeContext<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<ConstructContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ConstructContext.class).generic();

	public ConstructContext(@NotNull DecodeContext<T_Encoded> context) {
		super(context);
	}

	@Override
	public <T_Decoded> @NotNull T_Decoded constructWith(@NotNull AutoConstructor<T_Decoded> constructor) throws ConstructException {
		return this.logger().construct(constructor, this);
	}
}