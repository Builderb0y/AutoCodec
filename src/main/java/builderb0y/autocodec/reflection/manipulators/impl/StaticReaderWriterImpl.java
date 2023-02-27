package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.StaticReaderWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;

public interface StaticReaderWriterImpl<T_Member> extends StaticReaderImpl<T_Member>, StaticWriterImpl<T_Member>, StaticReaderWriter<T_Member> {

	public static <T_Member> StaticReaderWriterImpl<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull MethodHandle getter,
		@NotNull MethodHandle setter
	) {
		MethodHandle coercedGetter = getter.asType(GETTER_TYPE);
		MethodHandle coercedSetter = setter.asType(SETTER_TYPE);
		return new StaticReaderWriterImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public @NotNull MethodHandle getReaderMethodHandle() {
				return coercedGetter;
			}

			@Override
			public @NotNull MethodHandle getWriterMethodHandle() {
				return coercedSetter;
			}

			@Override
			public String toString() {
				return "StaticReaderWriterImpl: { member: " + member + ", getter: " + coercedGetter + ", setter: " + coercedSetter + " }";
			}
		};
	}
}