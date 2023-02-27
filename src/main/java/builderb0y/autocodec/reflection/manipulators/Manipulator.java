package builderb0y.autocodec.reflection.manipulators;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

public interface Manipulator {

	public static final @NotNull ObjectArrayFactory<Manipulator> ARRAY_FACTORY = new ObjectArrayFactory<>(Manipulator.class);

	public abstract @NotNull FieldLikeMemberView<?, ?> getMember();

	@Override
	public abstract @NotNull String toString();
}