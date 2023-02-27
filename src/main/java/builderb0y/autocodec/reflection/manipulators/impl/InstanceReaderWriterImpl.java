package builderb0y.autocodec.reflection.manipulators.impl;

import java.lang.invoke.MethodHandle;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.manipulators.InstanceReaderWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;

public interface
	InstanceReaderWriterImpl<T_Owner, T_Member>
extends
	InstanceReaderImpl<T_Owner, T_Member>,
	InstanceWriterImpl<T_Owner, T_Member>,
	InstanceReaderWriter<T_Owner, T_Member>
{

	public static <T_Owner, T_Member> InstanceReaderWriterImpl<T_Owner, T_Member> of(
		@NotNull FieldLikeMemberView<T_Owner, T_Member> member,
		@NotNull MethodHandle getter,
		@NotNull MethodHandle setter
	) {
		MethodHandle coercedGetter = getter.asType(GETTER_TYPE);
		MethodHandle coercedSetter = setter.asType(SETTER_TYPE);
		return new InstanceReaderWriterImpl<>() {

			@Override
			public @NotNull FieldLikeMemberView<T_Owner, T_Member> getMember() {
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
			public @NotNull String toString() {
				return "InstanceReaderWriterImpl: { member: " + member + ", getter: " + coercedGetter + ", setter: " + coercedSetter + " }";
			}
		};
	}
}