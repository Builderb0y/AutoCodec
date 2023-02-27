package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.InstanceWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.AutoCodecUtil;

public interface InstanceWriterImpl<T_Owner, T_Member> extends InstanceWriter<T_Owner, T_Member> {

	public static final MethodType SETTER_TYPE = MethodType.methodType(void.class, Object.class, Object.class);

	public abstract @NotNull MethodHandle getWriterMethodHandle();

	@Override
	public default void set(@NotNull T_Owner obj, T_Member value) {
		try {
			this.getWriterMethodHandle().invokeExact(obj, value);
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	public static <T_Owner, T_Member> InstanceWriterImpl<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull MethodHandle setter
	) {
		MethodHandle coercedSetter = setter.asType(SETTER_TYPE);
		return new InstanceWriterImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
				return member;
			}

			@Override
			public @NotNull MethodHandle getWriterMethodHandle() {
				return coercedSetter;
			}

			@Override
			public @NotNull String toString() {
				return "InstanceWriterImpl: { member: " + member + ", setter: " + coercedSetter + " }";
			}
		};
	}
}