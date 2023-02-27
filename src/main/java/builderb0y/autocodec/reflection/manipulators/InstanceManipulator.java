package builderb0y.autocodec.reflection.manipulators;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

public interface InstanceManipulator<T_Owner, T_Member> extends Manipulator {

	public static final @NotNull ObjectArrayFactory<InstanceManipulator<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(InstanceManipulator.class).generic();

	@Override
	public abstract @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember();
}