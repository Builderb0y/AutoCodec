package builderb0y.autocodec.reflection.manipulators;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides read access to a non-static {@link FieldLikeMemberView}. */
public interface InstanceReader<T_Owner, T_Member> extends InstanceManipulator<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<InstanceReader<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(InstanceReader.class).generic();

	public static <T_Owner, T_Member> @NotNull InstanceReader<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull Function<? super @NotNull T_Owner, ? extends T_Member> getter
	) {
		return new InstanceReader<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
				return member;
			}

			@Override
			public T_Member get(@NotNull T_Owner obj) {
				return getter.apply(obj);
			}

			@Override
			public String toString() {
				return "InstanceReader: { member: " + member + ", getter: " + getter + " }";
			}
		};
	}

	public abstract T_Member get(@NotNull T_Owner obj);
}