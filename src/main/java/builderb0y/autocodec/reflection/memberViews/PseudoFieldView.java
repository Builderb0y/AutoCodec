package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class PseudoFieldView<T_Owner, T_Member> extends FieldLikeMemberView<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<PseudoFieldView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(PseudoFieldView.class).generic();

	public final @NotNull PseudoField field;

	public PseudoFieldView(@NotNull ReifiedType<T_Owner> declaringType, @NotNull PseudoField field) {
		super(declaringType);
		this.field = field;
	}

	@Override
	public @NotNull MethodHandle createInstanceReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		return context.lookup().unreflect(this.field.getter);
	}

	@Override
	public @NotNull MethodHandle createInstanceWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		if (this.field.setter == null) throw new IllegalAccessException("Pseudo-field has no setter: " + this.field);
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		return context.lookup().unreflect(this.field.setter);
	}

	@Override
	public int getModifiers() {
		return this.field.getModifiers();
	}

	@Override
	public @NotNull String getName() {
		return this.field.name;
	}

	@Override
	public @NotNull PseudoField getActualMember() {
		return this.field;
	}

	@Override
	public @NotNull ReifiedType<T_Member> getType() {
		ReifiedType<T_Member> type = this.type;
		if (type == null) {
			this.type = type = (
				this
				.getDeclaringType()
				.resolveAncestor(this.field.getter.getDeclaringClass())
				.resolveDeclaration(this.field.getter.getAnnotatedReturnType())
				.uncheckedCast()
			);
		}
		return type;
	}
}