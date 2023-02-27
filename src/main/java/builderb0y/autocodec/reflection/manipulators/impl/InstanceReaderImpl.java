package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.AutoCodecUtil;

public interface InstanceReaderImpl<T_Owner, T_Member> extends InstanceReader<T_Owner, T_Member> {

	public static final MethodType GETTER_TYPE = MethodType.methodType(Object.class, Object.class);

	public abstract @NotNull MethodHandle getReaderMethodHandle();

	@Override
	@SuppressWarnings("unchecked")
	public default T_Member get(@NotNull T_Owner obj) {
		try {
			return (T_Member)(this.getReaderMethodHandle().invokeExact(obj));
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	public static <T_Owner, T_Member> InstanceReaderImpl<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull MethodHandle getter
	) {
		MethodHandle coercedGetter = getter.asType(GETTER_TYPE);
		return new InstanceReaderImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
				return member;
			}

			@Override
			public @NotNull MethodHandle getReaderMethodHandle() {
				return coercedGetter;
			}

			@Override
			public @NotNull String toString() {
				return "InstanceReaderImpl: { member: " + member + ", getter: " + coercedGetter + " }";
			}
		};
	}
}