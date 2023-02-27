package builderb0y.autocodec.reflection.manipulators;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides write access to a non-static {@link FieldLikeMemberView}. */
public interface InstanceWriter<T_Owner, T_Member> extends InstanceManipulator<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<InstanceWriter<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(InstanceWriter.class).generic();

	public static <T_Owner, T_Member> @NotNull InstanceWriter<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull BiConsumer<? super @NotNull T_Owner, ? super T_Member> setter
	) {
		return new InstanceWriter<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
				return member;
			}

			@Override
			public void set(@NotNull T_Owner obj, T_Member value) {
				setter.accept(obj, value);
			}

			@Override
			public String toString() {
				return "InstanceWriter: { member: " + member + ", setter: " + setter + " }";
			}
		};
	}

	public abstract void set(@NotNull T_Owner obj, T_Member value);
}