package builderb0y.autocodec.reflection.manipulators;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides read access to a static {@link FieldLikeMemberView}. */
public interface StaticReader<T_Member> extends StaticManipulator<T_Member> {

	public static final @NotNull ObjectArrayFactory<StaticReader<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(StaticReader.class).generic();

	public static <T_Member> @NotNull StaticReader<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull Supplier<? extends T_Member> getter
	) {
		return new StaticReader<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public T_Member get() {
				return getter.get();
			}

			@Override
			public String toString() {
				return "StaticReader: { member: " + member + ", getter: " + getter + " }";
			}
		};
	}

	public abstract T_Member get();
}