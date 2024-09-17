package builderb0y.autocodec.common;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.function.UnaryOperator;

import com.google.gson.internal.Primitives;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.NamedPredicate;
import builderb0y.autocodec.util.TypeFormatter;

public record DefaultSpec(
	@Internal
	@NotNull DefaultGetter getter,
	@NotNull DefaultMode mode,
	boolean alwaysEncode
) {

	@SuppressWarnings("unchecked")
	public <T_Encoded> T_Encoded getEncodedDefaultValue(@NotNull DynamicOpsContext<T_Encoded> context) throws Exception {
		if (this.mode == DefaultMode.ENCODED) return (T_Encoded)(this.getter.get(context));
		else throw new IllegalStateException("requested encoded value from non-encoded DefaultSpec");
	}

	@SuppressWarnings("unchecked")
	public <T_Encoded, T_Decoded> T_Decoded getDecodedDefaultValue(@NotNull DynamicOpsContext<T_Encoded> context) {
		if (this.mode == DefaultMode.DECODED) return (T_Decoded)(this.getter.get(context));
		else throw new IllegalStateException("requested decoded value from non-decoded DefaultSpec");
	}

	public static @Nullable DefaultSpec from(@NotNull FactoryContext<?> context) {
		Annotation annotation = context.type.getAnnotations().getFirst(
			DefaultByte   .class,
			DefaultShort  .class,
			DefaultInt    .class,
			DefaultLong   .class,
			DefaultFloat  .class,
			DefaultDouble .class,
			DefaultString .class,
			DefaultBoolean.class,
			DefaultObject .class
		);
		if (annotation == null) return null;
		if (annotation instanceof DefaultObject defaultObject) {
			return handleObject(context, defaultObject);
		}
		Object value;
		DefaultMode mode;
		boolean alwaysEncode;
		if      (annotation instanceof DefaultByte    defaultByte   ) { value = defaultByte   .value(); mode = defaultByte   .mode(); alwaysEncode = defaultByte   .alwaysEncode(); }
		else if (annotation instanceof DefaultShort   defaultShort  ) { value = defaultShort  .value(); mode = defaultShort  .mode(); alwaysEncode = defaultShort  .alwaysEncode(); }
		else if (annotation instanceof DefaultInt     defaultInt    ) { value = defaultInt    .value(); mode = defaultInt    .mode(); alwaysEncode = defaultInt    .alwaysEncode(); }
		else if (annotation instanceof DefaultLong    defaultLong   ) { value = defaultLong   .value(); mode = defaultLong   .mode(); alwaysEncode = defaultLong   .alwaysEncode(); }
		else if (annotation instanceof DefaultFloat   defaultFloat  ) { value = defaultFloat  .value(); mode = defaultFloat  .mode(); alwaysEncode = defaultFloat  .alwaysEncode(); }
		else if (annotation instanceof DefaultDouble  defaultDouble ) { value = defaultDouble .value(); mode = defaultDouble .mode(); alwaysEncode = defaultDouble .alwaysEncode(); }
		else if (annotation instanceof DefaultBoolean defaultBoolean) { value = defaultBoolean.value(); mode = defaultBoolean.mode(); alwaysEncode = defaultBoolean.alwaysEncode(); }
		else if (annotation instanceof DefaultString  defaultString ) { value = defaultString .value(); mode = defaultString .mode(); alwaysEncode = defaultString .alwaysEncode(); }
		else throw new FactoryException("Unhandled annotation: " + annotation);
		if (mode == DefaultMode.DECODED && context.type.getRawClass() != value.getClass() && context.type.getRawClass() != Primitives.unwrap(value.getClass())) {
			throw new FactoryException(
				new TypeFormatter(128)
				.annotations(true)
				.simplify(true)
				.append(annotation)
				.append(" was applied to type ")
				.annotations(false)
				.simplify(false)
				.append(context.type)
				.append(" with mode DECODED")
				.toString()
			);
		}
		return new DefaultSpec(constant(value), mode, alwaysEncode);
	}

	public static @NotNull DefaultSpec handleObject(@NotNull FactoryContext<?> context, @NotNull DefaultObject annotation) {
		Class<?> inClass = annotation.in();
		ReifiedType<?> inType = inClass == void.class ? context.type : ReifiedType.parameterizeWithWildcards(inClass);
		try {
			return switch (annotation.mode()) {
				case FIELD -> {
					FieldLikeMemberView<?, ?> field = context.reflect(inType).searchFields(
						false,
						new FieldPredicate()
						.name(annotation.name())
						.isStatic()
						.applyConditional(
							annotation.strict(),
							(FieldPredicate predicate) -> predicate.type(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, context.type),
							UnaryOperator.identity()
						),
						MemberCollector.forceUnique()
					);
					MethodHandle getter = field.createStaticReaderHandle(context);
					getter = MethodHandles.dropArguments(getter, 1, DynamicOpsContext.class);
					yield new DefaultSpec(
						DefaultGetter.create(getter),
						annotation.mode().defaultMode,
						annotation.alwaysEncode()
					);
				}
				case METHOD_WITH_CONTEXT_ENCODED -> {
					MethodLikeMemberView<?, ?> method = context.reflect(inType).searchMethods(
						false,
						new MethodPredicate()
						.name(annotation.name())
						.isStatic()
						.applyConditional(
							annotation.strict(),
							(MethodPredicate predicate) -> predicate.actualMember(
								new NamedPredicate<>(
									(AnnotatedElement element) -> {
										if (!(element instanceof Method actualMethod)) return false;
										if (actualMethod.getParameterCount() != 1 || actualMethod.getParameterTypes()[0] != DynamicOpsContext.class) return false;
										TypeVariable<?>[] typeParameters = actualMethod.getTypeParameters();
										if (typeParameters.length != 1) return false;
										if (!actualMethod.getGenericReturnType().equals(typeParameters[0])) return false;
										if (!(actualMethod.getGenericParameterTypes()[0] instanceof ParameterizedType parameterized)) return false;
										if (!parameterized.getActualTypeArguments()[0].equals(typeParameters[0])) return false;
										return true;
									},
									"signature matches 'public static <T> T name(DynamicOpsContext<T> context)'"
								)
							),
							(MethodPredicate predicate) -> predicate.actualMember(
								new NamedPredicate<>(
									(AnnotatedElement element) -> {
										if (!(element instanceof Method actualMethod)) return false;
										if (actualMethod.getParameterCount() != 1 || actualMethod.getParameterTypes()[0] != DynamicOpsContext.class) return false;
										if (actualMethod.getReturnType() == void.class) return false;
										return true;
									},
									"signature matches 'public static non-void name(DynamicOpsContext context)'"
								)
							)
						),
						MemberCollector.forceUnique()
					);
					MethodHandle handle = method.createMethodHandle(context);
					yield new DefaultSpec(
						DefaultGetter.create(handle),
						annotation.mode().defaultMode,
						annotation.alwaysEncode()
					);
				}
				case METHOD_WITH_CONTEXT_DECODED -> {
					MethodLikeMemberView<?, ?> method = context.reflect(inType).searchMethods(
						false,
						new MethodPredicate()
						.name(annotation.name())
						.isStatic()
						.applyConditional(
							annotation.strict(),
							(MethodPredicate predicate) -> predicate.returnType(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, context.type).actualMember(
								new NamedPredicate<>(
									(AnnotatedElement element) -> {
										Executable executable = (Executable)(element);
										if (executable.getParameterCount() != 1 || executable.getParameterTypes()[0] != DynamicOpsContext.class) return false;
										TypeVariable<?>[] typeParameters = executable.getTypeParameters();
										if (typeParameters.length != 1) return false;
										Type[] actualParameters = executable.getGenericParameterTypes();
										if (!(actualParameters[0] instanceof ParameterizedType parameterized)) return false;
										if (!typeParameters[0].equals(parameterized.getActualTypeArguments()[0])) return false;
										return true;
									},
									"signature matches 'public static <T_Encoded> T_Decoded name(DynamicOpsContext<T_Encoded> context)'"
								)
							),
							(MethodPredicate predicate) -> predicate.returnsNotVoid().actualMember(
								new NamedPredicate<>(
									(AnnotatedElement element) -> {
										Executable executable = (Executable)(element);
										if (executable.getParameterCount() != 1 || executable.getParameterTypes()[0] != DynamicOpsContext.class) return false;
										return true;
									},
									"signature matches 'public static non-void name(DynamicOpsContext context)'"
								)
							)
						),
						MemberCollector.forceUnique()
					);
					MethodHandle handle = method.createMethodHandle(context);
					yield new DefaultSpec(
						DefaultGetter.create(handle),
						annotation.mode().defaultMode,
						annotation.alwaysEncode()
					);
				}
				case METHOD_WITHOUT_CONTEXT -> {
					MethodLikeMemberView<?, ?> method = context.reflect(inType).searchMethods(
						false,
						new MethodPredicate()
						.name(annotation.name())
						.parameterCount(0)
						.applyConditional(
							annotation.strict(),
							(MethodPredicate predicate) -> predicate.returnType(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, context.type),
							MethodPredicate::returnsNotVoid
						),
						MemberCollector.forceUnique()
					);
					MethodHandle handle = method.createMethodHandle(context);
					yield new DefaultSpec(
						DefaultGetter.create(handle),
						annotation.mode().defaultMode,
						annotation.alwaysEncode()
					);
				}
			};
		}
		catch (FactoryException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new FactoryException(exception);
		}
	}

	@Internal
	public static @NotNull DefaultGetter constant(Object value) {
		return (DynamicOpsContext<?> context) -> value;
	}

	@Internal
	@FunctionalInterface
	public static interface DefaultGetter {

		public static final MethodType IMPL_TYPE = MethodType.methodType(Object.class, DynamicOpsContext.class);

		public abstract Object get(@NotNull DynamicOpsContext<?> context);

		public static DefaultGetter create(MethodHandle impl) {
			return AutoCodecUtil.forceLambda(
				MethodHandles.lookup(),
				"get",
				DefaultGetter.class,
				IMPL_TYPE,
				impl
			);
		}
	}
}