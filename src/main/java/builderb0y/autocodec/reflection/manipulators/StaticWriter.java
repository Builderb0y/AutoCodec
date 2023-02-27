package builderb0y.autocodec.reflection.manipulators;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** provides write access to a static {@link FieldLikeMemberView}. */
public interface StaticWriter<T_Member> extends StaticManipulator<T_Member> {

	public static final @NotNull ObjectArrayFactory<StaticWriter<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(StaticWriter.class).generic();

	public static <T_Member> StaticWriter<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull Consumer<? super T_Member> setter
	) {
		return new StaticWriter<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public void set(T_Member value) {
				setter.accept(value);
			}

			@Override
			public String toString() {
				return "StaticWriter: { member: " + member + ", setter: " + setter + " }";
			}
		};
	}

	public abstract void set(T_Member value);
}