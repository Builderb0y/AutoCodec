package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.annotations.UseGetter;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class RecordComponentView<T_Owner, T_Member> extends FieldLikeMemberView<T_Owner, T_Member> {

	public static final @NotNull ObjectArrayFactory<RecordComponentView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(RecordComponentView.class).generic();

	public final @NotNull RecordComponent recordComponent;

	public RecordComponentView(@NotNull ReifiedType<T_Owner> declaringType, @NotNull RecordComponent recordComponent) {
		super(declaringType);
		this.recordComponent = recordComponent;
	}

	@Override
	public @NotNull MethodHandle createInstanceReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		ReflectContext<T_Owner> context = provider.reflect(this.getDeclaringType());
		UseGetter annotation = this.recordComponent.getDeclaredAnnotation(UseGetter.class);
		if (annotation != null) {
			return (
				context.searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.value())
					.parameterTypes(ReifiedType.GENERIC_TYPE_STRATEGY, this.getDeclaringType())
					.returnType(ReifiedType.GENERIC_TYPE_STRATEGY, this.getType())
					.notStatic(),
					MemberCollector.forceUnique()
				)
				.createMethodHandle(provider)
			);
		}
		else {
			return context.lookup().unreflect(this.recordComponent.getAccessor());
		}
	}

	@Override
	public @NotNull MethodHandle createInstanceWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		throw new IllegalAccessException("Cannot set a record component.");
	}

	@Override
	public int getModifiers() {
		return Modifier.PUBLIC | Modifier.FINAL;
	}

	@Override
	public @NotNull RecordComponent getActualMember() {
		return this.recordComponent;
	}

	@Override
	public @NotNull String getName() {
		return this.recordComponent.getName();
	}

	@Override
	@Deprecated
	protected @NotNull AnnotatedType _getAnnotatedType() {
		/**
		workaround for a bug in javac:
		if the record component's type is an annotated *inner* class,
		then the record component's annotated type reports no annotations.
		example: {@code
			public record Example(
				@Foo Outer outer,
				Outer.@Foo Inner inner
			) {}
		}
		the outer record component's annotated type reports @Foo,
		but the inner record component's annotated type doesn't.

		I know this is an issue with javac, not the reflection API,
		because in the bytecode, @Foo for inner is present
		for the backing field and the accessor method,
		but not for the record component itself.
		by contrast, @Foo for outer is present on the field,
		the accessor method, and the record component itself.
		*/
		return this.recordComponent.getAccessor().getAnnotatedReturnType();
	}

	@Override
	public int hashCode() {
		/**
		{@link RecordComponent} does not override this, so we're doing it ourselves.
		implementation matches {@link Field#hashCode()}.
		*/
		return this.getDeclaringType().requireRawClass().getName().hashCode() ^ this.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		/**
		{@link RecordComponent} does not override this, so we're doing it ourselves.
		implementation matches {@link Field#equals(Object)}.
		*/
		return (
			obj instanceof RecordComponentView<?, ?> that &&
			this.getDeclaringType().requireRawClass() == that.getDeclaringType().requireRawClass() &&
			this.getName().equals(that.getName()) &&
			this.getType() == that.getType()
		);
	}
}