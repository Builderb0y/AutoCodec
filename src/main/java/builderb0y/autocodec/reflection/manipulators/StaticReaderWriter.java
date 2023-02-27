package builderb0y.autocodec.reflection.manipulators;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides read and write access to a static {@link FieldLikeMemberView}. */
public interface StaticReaderWriter<T_Member> extends StaticReader<T_Member>, StaticWriter<T_Member> {

	public static final @NotNull ObjectArrayFactory<StaticReaderWriter<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(StaticReaderWriter.class).generic();

	public static <T_Member> @NotNull StaticReaderWriter<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull Supplier<? extends T_Member> getter,
		@NotNull Consumer<? super T_Member> setter
	) {
		return new StaticReaderWriter<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public T_Member get() {
				return getter.get();
			}

			@Override
			public void set(T_Member value) {
				setter.accept(value);
			}

			@Override
			public String toString() {
				return "StaticReaderWriter: { member: " + member + ", getter: " + getter + ", setter: " + setter + " }";
			}
		};
	}
}