package builderb0y.autocodec.reflection.memberViews;

import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.reflection.AnnotationContainer;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.TypeFormatter;
import builderb0y.autocodec.util.TypeFormatter.TypeFormatterAppendable;

/**
common properties of {@link Field}, {@link RecordComponent},
{@link PseudoField}, {@link Method}, and {@link Constructor}.
*/
public abstract class MemberView<T_Owner> implements TypeFormatterAppendable {

	public static final @NotNull ObjectArrayFactory<MemberView<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(MemberView.class).generic();

	public final @NotNull ReifiedType<T_Owner> declaringType;
	public @Nullable AnnotationContainer annotations;

	public MemberView(@NotNull ReifiedType<T_Owner> declaringType) {
		this.declaringType = declaringType;
	}

	/** returns the name of the underlying member. */
	public abstract @NotNull String getName();

	/**
	returns the actual underlying member that this view represents.

	{@link RecordComponent} doesn't implement {@link Member},
	and it also doesn't extend {@link AccessibleObject}.
	so this method has to return {@link AnnotatedElement} instead,
	since that's the most specific common superinterface
	of {@link Field}, {@link RecordComponent}, {@link PseudoField},
	{@link Method}, and {@link Constructor}.

	the concrete subclasses {@link ConstructorView}, {@link MethodView},
	{@link FieldView}, {@link RecordComponentView}, and {@link PseudoFieldView}
	strengthen the return type of this method to the actual type of the underlying member.
	additionally, the abstract subclass {@link MethodLikeMemberView}
	strengthens the return type to {@link Executable}.
	*/
	public abstract @NotNull AnnotatedElement getActualMember();

	public @NotNull AnnotationContainer getAnnotations() {
		AnnotationContainer annotations = this.annotations;
		if (annotations == null) {
			annotations = this.annotations = AnnotationContainer.from(this.getActualMember());
		}
		return annotations;
	}

	public @NotNull ReifiedType<T_Owner> getDeclaringType() {
		return this.declaringType;
	}

	public abstract int getModifiers();

	public boolean isStatic() {
		return Modifier.isStatic(this.getModifiers());
	}

	@Override
	public @NotNull String toString() {
		return new TypeFormatter(128).annotations(true).simplify(false).append(this).toString();
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);
}