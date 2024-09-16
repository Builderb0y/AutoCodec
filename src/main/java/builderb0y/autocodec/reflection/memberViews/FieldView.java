package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.annotations.UseGetter;
import builderb0y.autocodec.annotations.UseSetter;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class FieldView<T_Owner, T_Member> extends FieldLikeMemberView<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<FieldView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldView.class).generic();

	public final @NotNull Field field;

	public FieldView(@NotNull ReifiedType<T_Owner> declaringType, @NotNull Field field) {
		super(declaringType);
		this.field = field;
	}

	@Override
	public @NotNull MethodHandle createInstanceReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		if (this.isStatic()) throw new IllegalAccessException("Cannot create instance reader for static field.");
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		UseGetter annotation = this.field.getDeclaredAnnotation(UseGetter.class);
		if (annotation != null) {
			return (
				context.searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.value())
					.notStatic()
					.parameterTypes(ReifiedType.GENERIC_TYPE_STRATEGY, this.getDeclaringType())
					.applyConditional(
						annotation.strict(),
						(MethodPredicate predicate) -> predicate.returnType(ReifiedType.GENERIC_TYPE_STRATEGY, this.getType()),
						MethodPredicate::returnsNotVoid
					),
					MemberCollector.forceUnique()
				)
				.createMethodHandle(provider)
			);
		}
		else {
			return context.lookup().unreflectGetter(this.field);
		}
	}

	@Override
	public @NotNull MethodHandle createInstanceWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		if (this.isStatic()) throw new IllegalAccessException("Cannot create instance writer for static field.");
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		UseSetter annotation = this.field.getDeclaredAnnotation(UseSetter.class);
		if (annotation != null) {
			return (
				context.searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.value())
					.parameterCount(2)
					.parameterType(0, ReifiedType.GENERIC_TYPE_STRATEGY, this.getDeclaringType())
					.applyConditional(
						annotation.strict(),
						(MethodPredicate predicate) -> predicate.parameterType(1, ReifiedType.GENERIC_TYPE_STRATEGY, this.getType()),
						UnaryOperator.identity()
					)
					.returnType(ReifiedType.GENERIC_TYPE_STRATEGY, ReifiedType.VOID)
					.notStatic(),
					MemberCollector.forceUnique()
				)
				.createMethodHandle(provider)
			);
		}
		else {
			return context.lookup().unreflectSetter(this.field);
		}
	}

	@Override
	public @NotNull MethodHandle createStaticReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		if (!this.isStatic()) return super.createStaticReaderHandle(provider); //use super error message.
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		MethodHandles.Lookup lookup = context.lookup();
		UseGetter annotation = this.field.getDeclaredAnnotation(UseGetter.class);
		if (annotation != null) {
			return (
				context.searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.value())
					.parameterCount(0)
					.returnType(ReifiedType.GENERIC_TYPE_STRATEGY, this.getType())
					.isStatic(),
					MemberCollector.forceUnique()
				)
				.createMethodHandle(provider)
			);
		}
		else {
			return lookup.unreflectGetter(this.field);
		}
	}

	@Override
	public @NotNull MethodHandle createStaticWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		if (!this.isStatic()) return super.createStaticWriterHandle(provider); //use super error message.
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		MethodHandles.Lookup lookup = context.lookup();
		UseSetter annotation = this.field.getDeclaredAnnotation(UseSetter.class);
		if (annotation != null) {
			return (
				context.searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.value())
					.parameterTypes(ReifiedType.GENERIC_TYPE_STRATEGY, this.getType())
					.returnType(ReifiedType.GENERIC_TYPE_STRATEGY, ReifiedType.VOID)
					.isStatic(),
					MemberCollector.forceUnique()
				)
				.createMethodHandle(provider)
			);
		}
		else {
			return lookup.unreflectSetter(this.field);
		}
	}

	@Override
	public int getModifiers() {
		return this.field.getModifiers();
	}

	@Override
	public @NotNull Field getActualMember() {
		return this.field;
	}

	@Override
	public @NotNull String getName() {
		return this.field.getName();
	}

	@Override
	@Internal
	public @NotNull AnnotatedType getAnnotatedType() {
		return this.field.getAnnotatedType();
	}
}