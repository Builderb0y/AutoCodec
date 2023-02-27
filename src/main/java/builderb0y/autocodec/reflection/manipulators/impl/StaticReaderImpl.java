package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.StaticReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.util.AutoCodecUtil;

public interface StaticReaderImpl<T_Member> extends StaticReader<T_Member> {

	public static final MethodType GETTER_TYPE = MethodType.methodType(Object.class);

	public abstract @NotNull MethodHandle getReaderMethodHandle();

	@Override
	@SuppressWarnings("unchecked")
	public default T_Member get() {
		try {
			return (T_Member)(this.getReaderMethodHandle().invokeExact());
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	public static <T_Member> StaticReaderImpl<T_Member> of(
		@NotNull FieldLikeMemberView<?, T_Member> member,
		@NotNull MethodHandle getter
	) {
		MethodHandle coercedGetter = getter.asType(GETTER_TYPE);
		return new StaticReaderImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<?, T_Member> getMember() {
				return member;
			}

			@Override
			public @NotNull MethodHandle getReaderMethodHandle() {
				return coercedGetter;
			}

			@Override
			public String toString() {
				return "StaticReaderImpl: { member: " + member + ", getter: " + getter + " }";
			}
		};
	}
}