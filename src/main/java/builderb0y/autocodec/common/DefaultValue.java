package builderb0y.autocodec.common;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.manipulators.StaticReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public interface DefaultValue {

	public abstract boolean alwaysEncode();

	public abstract <T_Encoded, T_Decoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context, AutoDecoder<T_Decoded> decoder) throws DecodeException;

	public abstract <T_Encoded, T_Decoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context, AutoEncoder<T_Decoded> encoder) throws EncodeException;

	public static @NotNull DefaultValue forType(@NotNull ReflectContextProvider reflectContext, @NotNull ReifiedType<?> type, boolean encoding) {
		Annotation annotation = type.getAnnotations().getFirst(
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
		if (annotation instanceof DefaultByte defaultByte) {
			if (encoding && defaultByte.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new ByteDefaultValue(defaultByte.value(), defaultByte.alwaysEncode());
		}
		else if (annotation instanceof DefaultShort defaultShort) {
			if (encoding && defaultShort.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new ShortDefaultValue(defaultShort.value(), defaultShort.alwaysEncode());
		}
		else if (annotation instanceof DefaultInt defaultInt) {
			if (encoding && defaultInt.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new IntDefaultValue(defaultInt.value(), defaultInt.alwaysEncode());
		}
		else if (annotation instanceof DefaultLong defaultLong) {
			if (encoding && defaultLong.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new LongDefaultValue(defaultLong.value(), defaultLong.alwaysEncode());
		}
		else if (annotation instanceof DefaultFloat defaultFloat) {
			if (encoding && defaultFloat.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new FloatDefaultValue(defaultFloat.value(), defaultFloat.alwaysEncode());
		}
		else if (annotation instanceof DefaultDouble defaultDouble) {
			if (encoding && defaultDouble.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new DoubleDefaultValue(defaultDouble.value(), defaultDouble.alwaysEncode());
		}
		else if (annotation instanceof DefaultString defaultString) {
			if (encoding && defaultString.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new StringDefaultValue(defaultString.value(), defaultString.alwaysEncode());
		}
		else if (annotation instanceof DefaultBoolean defaultBoolean) {
			if (encoding && defaultBoolean.alwaysEncode()) return NullDefaultValue.INSTANCE;
			return new BooleanDefaultValue(defaultBoolean.value(), defaultBoolean.alwaysEncode());
		}
		else if (annotation instanceof DefaultObject defaultObject) {
			if (encoding && defaultObject.alwaysEncode()) return NullDefaultValue.INSTANCE;
			try {
				return handleObject(reflectContext, type, defaultObject);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException("@DefaultObject targets member that is not accessible. Consider making the target public, or add a Lookup to the ReflectionManager.", exception);
			}
		}
		else {
			return NullDefaultValue.INSTANCE;
		}
	}

	@Internal
	public static @NotNull DefaultValue handleObject(@NotNull ReflectContextProvider reflectContext, @NotNull ReifiedType<?> type, @NotNull DefaultObject annotation) throws IllegalAccessException {
		Class<?> inClass = annotation.in();
		ReifiedType<?> inType = inClass == void.class ? type : ReifiedType.parameterizeWithWildcards(inClass);
		return switch (annotation.mode()) {
			case FIELD -> {
				FieldLikeMemberView<?, ?> field = reflectContext.reflect(inType).searchFields(
					false,
					new FieldPredicate()
					.name(annotation.name())
					.isStatic()
					.applyConditional(
						annotation.strict(),
						predicate -> predicate.type(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, type),
						UnaryOperator.identity()
					),
					MemberCollector.forceUnique()
				);
				StaticReader<?> reader = field.createStaticReader(reflectContext);
				yield new FieldObjectDefaultValue(reader, annotation.alwaysEncode());
			}
			case METHOD_WITHOUT_CONTEXT -> {
				MethodLikeMemberView<?, ?> method = reflectContext.reflect(inType).searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.name())
					.isStatic()
					.parameterCount(0)
					.applyConditional(
						annotation.strict(),
						predicate -> predicate.returnType(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, type),
						MethodPredicate::returnsNotVoid
					),
					MemberCollector.forceUnique()
				);
				MethodHandle handle = method.createMethodHandle(reflectContext);
				yield new MethodWithoutContextObjectDefaultValue(handle, annotation.alwaysEncode());
			}
			case METHOD_WITH_CONTEXT -> {
				MethodLikeMemberView<?, ?> method = reflectContext.reflect(inType).searchMethods(
					false,
					new MethodPredicate()
					.name(annotation.name())
					.isStatic()
					.applyConditional(
						annotation.strict(),
						predicate -> (
							predicate
							.actualMember(annotatedElement -> {
								Executable actualMethod = (Executable)(annotatedElement);
								if (actualMethod.getParameterCount() != 1) return false;
								TypeVariable<?>[] typeParameters = actualMethod.getTypeParameters();
								if (typeParameters.length != 1) return false;
								TypeVariable<?> variable = typeParameters[0];
								Type parameterType = actualMethod.getGenericParameterTypes()[0];
								if (parameterType instanceof ParameterizedType parameterized) {
									if (parameterized.getRawType() == DynamicOpsContext.class) {
										Type[] expectVariable = parameterized.getActualTypeArguments();
										if (expectVariable.length == 1 && expectVariable[0].equals(variable)) {
											return true;
										}
									}
								}
								return false;
							})
							.returnType(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, type)
						),
						predicate -> (
							predicate
							.parameterCount(1)
							.parameterType(0, ReifiedType.RAW_TYPE_STRATEGY, ReifiedType.parameterizeWithWildcards(DynamicOpsContext.class))
						)
					),
					MemberCollector.forceUnique()
				);
				MethodHandle handle = method.createMethodHandle(reflectContext);
				yield new MethodWithContextObjectDefaultValue(handle, annotation.alwaysEncode());
			}
		};
	}

	public static interface PrimitiveDefaultValue extends DefaultValue {

		public abstract <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops);

		@Override
		public default <T_Encoded, T_Decoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context, AutoDecoder<T_Decoded> decoder) throws DecodeException {
			if (context.isEmpty()) context = context.input(this.getInput(context));
			return context.decodeWith(decoder);
		}

		@Override
		public default <T_Encoded, T_Decoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context, AutoEncoder<T_Decoded> encoder) throws EncodeException {
			T_Encoded result = context.encodeWith(encoder);
			if (result.equals(this.getInput(context))) result = context.empty();
			return result;
		}
	}

	public static enum NullDefaultValue implements PrimitiveDefaultValue {

		INSTANCE;

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.empty();
		}

		@Override
		public boolean alwaysEncode() {
			return false;
		}
	}

	public static record ByteDefaultValue(byte value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createByte(this.value);
		}
	}

	public static record ShortDefaultValue(short value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createShort(this.value);
		}
	}

	public static record IntDefaultValue(int value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createInt(this.value);
		}
	}

	public static record LongDefaultValue(long value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createLong(this.value);
		}
	}

	public static record FloatDefaultValue(float value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createFloat(this.value);
		}
	}

	public static record DoubleDefaultValue(double value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createDouble(this.value);
		}
	}

	public static record StringDefaultValue(@NotNull String value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createString(this.value);
		}
	}

	public static record BooleanDefaultValue(boolean value, boolean alwaysEncode) implements PrimitiveDefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOpsContext<T_Encoded> ops) {
			return ops.createBoolean(this.value);
		}
	}

	public static interface ObjectDefaultValue extends DefaultValue {

		public abstract <T_Encoded> Object getDefaultObject(DynamicOpsContext<T_Encoded> context) throws Throwable;

		@Override
		@SuppressWarnings("unchecked")
		public default <T_Encoded, T_Decoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context, AutoDecoder<T_Decoded> decoder) throws DecodeException {
			if (context.isEmpty()) try {
				return (T_Decoded)(this.getDefaultObject(context));
			}
			catch (Error | DecodeException decodeException) {
				throw decodeException;
			}
			catch (Throwable throwable) {
				throw new DecodeException(throwable);
			}
			return context.decodeWith(decoder);
		}

		@Override
		@SuppressWarnings("unchecked")
		public default <T_Encoded, T_Decoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context, AutoEncoder<T_Decoded> encoder) throws EncodeException {
			T_Encoded actualValue = context.encodeWith(encoder);
			T_Decoded defaultObject;
			try {
				defaultObject = (T_Decoded)(this.getDefaultObject(context));
			}
			catch (Error | EncodeException exception) {
				throw exception;
			}
			catch (Throwable throwable) {
				throw new EncodeException(throwable);
			}
			T_Encoded defaultValue = context.object(defaultObject).encodeWith(encoder);
			if (actualValue.equals(defaultValue)) return context.empty();
			return actualValue;
		}
	}

	public static class FieldObjectDefaultValue implements ObjectDefaultValue {

		public final StaticReader<?> field;
		public final boolean alwaysEncode;

		public FieldObjectDefaultValue(StaticReader<?> field, boolean alwaysEncode) {
			this.field = field;
			this.alwaysEncode = alwaysEncode;
		}

		@Override
		public boolean alwaysEncode() {
			return this.alwaysEncode;
		}

		@Override
		public <T_Encoded> Object getDefaultObject(DynamicOpsContext<T_Encoded> context) {
			return this.field.get();
		}
	}

	public static class MethodWithoutContextObjectDefaultValue implements ObjectDefaultValue {

		public final MethodHandle handle;
		public final boolean alwaysEncode;

		public MethodWithoutContextObjectDefaultValue(MethodHandle handle, boolean alwaysEncode) {
			this.handle = handle.asType(MethodType.methodType(Object.class));
			this.alwaysEncode = alwaysEncode;
		}

		@Override
		public boolean alwaysEncode() {
			return this.alwaysEncode;
		}

		@Override
		public <T_Encoded> Object getDefaultObject(DynamicOpsContext<T_Encoded> context) throws Throwable {
			return this.handle.invokeExact();
		}
	}

	public static class MethodWithContextObjectDefaultValue implements ObjectDefaultValue {

		public final MethodHandle handle;
		public final boolean alwaysEncode;

		public MethodWithContextObjectDefaultValue(MethodHandle handle, boolean alwaysEncode) {
			this.handle = handle.asType(MethodType.methodType(Object.class, DynamicOpsContext.class));
			this.alwaysEncode = alwaysEncode;
		}

		@Override
		public boolean alwaysEncode() {
			return this.alwaysEncode;
		}

		@Override
		public <T_Encoded> Object getDefaultObject(DynamicOpsContext<T_Encoded> context) throws Throwable {
			return this.handle.invokeExact(context);
		}
	}
}