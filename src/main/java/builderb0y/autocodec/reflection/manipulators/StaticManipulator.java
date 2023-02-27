package builderb0y.autocodec.reflection.manipulators;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

public interface StaticManipulator<T_Member> extends Manipulator {

	public static final @NotNull ObjectArrayFactory<StaticManipulator<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(StaticManipulator.class).generic();

	@Override
	public abstract @NotNull FieldLikeMemberView<?, T_Member> getMember();
}