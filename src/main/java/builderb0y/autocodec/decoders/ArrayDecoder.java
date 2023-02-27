package builderb0y.autocodec.decoders;

import java.lang.reflect.Array;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ArrayFactory;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.PrimitiveArrayFactory;

public abstract class ArrayDecoder<T_DecodedElement, T_DecodedArray> extends NamedDecoder<T_DecodedArray> {

	public final @NotNull AutoDecoder<T_DecodedElement> componentDecoder;
	public final @NotNull ArrayFactory<T_DecodedArray> arrayFactory;
	public final boolean singleton;

	public ArrayDecoder(
		@NotNull ReifiedType<T_DecodedArray> arrayType,
		@NotNull AutoDecoder<T_DecodedElement> componentDecoder,
		@NotNull ArrayFactory<T_DecodedArray> arrayFactory,
		boolean singleton
	) {
		super(arrayType);
		this.componentDecoder = componentDecoder;
		this.arrayFactory = arrayFactory;
		this.singleton = singleton;
	}

	public static class PrimitiveArrayDecoder<T_DecodedElement, T_DecodedArray> extends ArrayDecoder<T_DecodedElement, T_DecodedArray> {

		public PrimitiveArrayDecoder(
			@NotNull ReifiedType<T_DecodedArray> arrayType,
			@NotNull AutoDecoder<T_DecodedElement> componentDecoder,
			@NotNull PrimitiveArrayFactory<T_DecodedArray> arrayFactory,
			boolean singleton
		) {
			super(arrayType, componentDecoder, arrayFactory, singleton);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable T_DecodedArray decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			List<DecodeContext<T_Encoded>> from = context.forceAsList(this.singleton);
			int length = from.size();
			T_DecodedArray to = this.arrayFactory.apply(length);
			for (int index = 0; index < length; index++) {
				Array.set(to, index, from.get(index).decodeWith(this.componentDecoder));
			}
			return to;
		}
	}

	public static class ObjectArrayDecoder<T_DecodedElement> extends ArrayDecoder<T_DecodedElement, T_DecodedElement[]> {

		public ObjectArrayDecoder(
			@NotNull ReifiedType<T_DecodedElement[]> arrayType,
			@NotNull AutoDecoder<T_DecodedElement> componentDecoder,
			@NotNull ObjectArrayFactory<T_DecodedElement> arrayFactory,
			boolean singleton
		) {
			super(arrayType, componentDecoder, arrayFactory, singleton);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> T_DecodedElement @Nullable [] decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			List<DecodeContext<T_Encoded>> from = context.forceAsList(this.singleton);
			int length = from.size();
			T_DecodedElement[] to = this.arrayFactory.apply(length);
			for (int index = 0; index < length; index++) {
				to[index] = from.get(index).decodeWith(this.componentDecoder);
			}
			return to;
		}
	}

	public static class Factory extends NamedDecoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> componentType = context.type.getArrayComponentType();
			if (componentType != null) {
				Class<?> componentClass = componentType.getBoundOrSelf().getRawClass();
				if (componentClass != null) {
					AutoDecoder<?> componentDecoder = context.type(componentType).forceCreateDecoder();
					boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
					if (componentClass.isPrimitive()) {
						return new PrimitiveArrayDecoder(context.type, componentDecoder, PrimitiveArrayFactory.forComponentType(componentClass), singleton);
					}
					else {
						return new ObjectArrayDecoder(context.type, componentDecoder, new ObjectArrayFactory(componentClass), singleton);
					}
				}
				else {
					throw new FactoryException("Failed to get array component class of " + componentType + " (array type: " + context.type + ')');
				}
			}
			return null;
		}
	}
}