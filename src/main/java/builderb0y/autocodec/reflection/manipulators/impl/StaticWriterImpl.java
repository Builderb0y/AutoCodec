package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.StaticWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.AutoCodecUtil;

public interface StaticWriterImpl<T_Member> extends StaticWriter<T_Member> {

	public static final MethodType SETTER_TYPE = MethodType.methodType(void.class, Object.class);

	public abstract @NotNull MethodHandle getWriterMethodHandle();

	@Override
	public default void set(T_Member value) {
		try {
			this.getWriterMethodHandle().invokeExact(value);
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	public static <T_Member> StaticWriterImpl<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull MethodHandle setter
	) {
		MethodHandle coercedSetter = setter.asType(SETTER_TYPE);
		return new StaticWriterImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public @NotNull MethodHandle getWriterMethodHandle() {
				return coercedSetter;
			}

			@Override
			public String toString() {
				return "StaticWriterImpl: { member: " + member + ", setter: " + setter + " }";
			}
		};
	}
}