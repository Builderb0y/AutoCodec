package builderb0y.autocodec.reflection.manipulators;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides read and write access to a non-static {@link FieldLikeMemberView}. */
public interface InstanceReaderWriter<T_Owner, T_Member> extends InstanceReader<T_Owner, T_Member>, InstanceWriter<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<InstanceReaderWriter<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(InstanceReaderWriter.class).generic();

	public static <T_Owner, T_Member> @NotNull InstanceReaderWriter<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull Function<? super @NotNull T_Owner, ? extends T_Member> getter,
		@NotNull BiConsumer<? super @NotNull T_Owner, ? super T_Member> setter
	) {
		return new InstanceReaderWriter<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
				return member;
			}

			@Override
			public T_Member get(@NotNull T_Owner obj) {
				return getter.apply(obj);
			}

			@Override
			public void set(@NotNull T_Owner obj, T_Member value) {
				setter.accept(obj, value);
			}

			@Override
			public String toString() {
				return "InstanceReaderWriter: { member: " + member + ", getter: " + getter + ", setter: " + setter + " }";
			}
		};
	}
}