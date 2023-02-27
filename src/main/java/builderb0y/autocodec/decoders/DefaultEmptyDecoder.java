package builderb0y.autocodec.decoders;

import java.lang.reflect.Array;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.reflection.reification.TypeClassification;

public abstract class DefaultEmptyDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public final @NotNull AutoDecoder<T_Decoded> nonEmptyDecoder;

	public DefaultEmptyDecoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> nonEmptyDecoder) {
		super(type);
		this.nonEmptyDecoder = nonEmptyDecoder;
	}

	public abstract <T_Encoded> @NotNull T_Decoded supplyEmpty(@NotNull DecodeContext<T_Encoded> context) throws DecodeException;

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return this.supplyEmpty(context);
		else return context.decodeWith(this.nonEmptyDecoder);
	}

	public static class DefaultEmptySharedDecoder<T_Decoded> extends DefaultEmptyDecoder<T_Decoded> {

		public final @NotNull T_Decoded emptyInstance;

		public DefaultEmptySharedDecoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> nonEmptyDecoder, @NotNull T_Decoded instance) {
			super(type, nonEmptyDecoder);
			this.emptyInstance = instance;
		}

		@Override
		public <T_Encoded> @NotNull T_Decoded supplyEmpty(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			return this.emptyInstance;
		}
	}

	public static class DefaultEmptyConstructedDecoder<T_Decoded> extends DefaultEmptyDecoder<T_Decoded> {

		public final @NotNull AutoConstructor<T_Decoded> constructor;

		public DefaultEmptyConstructedDecoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> nonEmptyDecoder, @NotNull AutoConstructor<T_Decoded> constructor) {
			super(type, nonEmptyDecoder);
			this.constructor = constructor;
		}

		@Override
		public <T_Encoded> @NotNull T_Decoded supplyEmpty(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			return context.constructWith(this.constructor);
		}
	}

	public static class EmptyArrayConstructor<T_Array> extends NamedConstructor<T_Array> {

		public final @NotNull Class<?> componentType;

		public EmptyArrayConstructor(@NotNull ReifiedType<T_Array> type) {
			super(type);
			ReifiedType<?> componentType = type.getArrayComponentType();
			if (componentType == null) throw new IllegalArgumentException("Not an array: " + type);
			Class<?> componentClass = componentType.getRawClass();
			if (componentClass == null) throw new IllegalArgumentException("Unable to get raw class of component type: " + componentType);
			this.componentType = componentClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T_Encoded> @NotNull T_Array construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
			return (T_Array)(Array.newInstance(this.componentType, 0));
		}
	}

	public static class Factory extends NamedDecoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		public @Nullable <T_HandledType> AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultEmpty annotation = context.type.getAnnotations().getFirst(DefaultEmpty.class);
			if (annotation != null) {
				AutoDecoder<T_HandledType> fallback = context.forceCreateFallbackDecoder(this);
				AutoConstructor<T_HandledType> constructor;
				if (context.type.getClassification() == TypeClassification.ARRAY) {
					constructor = new EmptyArrayConstructor<>(context.type);
				}
				else {
					constructor = context.forceCreateConstructor();
				}
				if (annotation.shared()) {
					try {
						@SuppressWarnings("TestOnlyProblems") //one of the rare cases where construction is useful without imprinting.
						T_HandledType object = context.autoCodec.construct(constructor, JsonNull.INSTANCE, JsonOps.INSTANCE);
						return new DefaultEmptySharedDecoder<>(context.type, fallback, object);
					}
					catch (ConstructException exception) {
						throw new FactoryException(exception);
					}
				}
				else {
					return new DefaultEmptyConstructedDecoder<>(context.type, fallback, constructor);
				}
			}
			return null;
		}
	}
}