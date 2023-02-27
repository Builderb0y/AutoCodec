package builderb0y.autocodec.encoders;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public abstract class ArrayEncoder<T_DecodedElement, T_DecodedArray> extends NamedEncoder<T_DecodedArray> {

	public final @NotNull AutoEncoder<T_DecodedElement> componentEncoder;
	public final boolean singleton;

	public ArrayEncoder(
		@NotNull ReifiedType<T_DecodedArray> arrayType,
		@NotNull AutoEncoder<T_DecodedElement> componentEncoder,
		boolean singleton
	) {
		super(arrayType);
		this.componentEncoder = componentEncoder;
		this.singleton = singleton;
	}

	@Override
	public String toString() {
		return this.toString + ": { componentEncoder: " + this.componentEncoder + " }";
	}

	public static class ObjectArrayEncoder<T_DecodedElement> extends ArrayEncoder<T_DecodedElement, T_DecodedElement[]> {

		public ObjectArrayEncoder(
			@NotNull ReifiedType<T_DecodedElement[]> arrayType,
			@NotNull AutoEncoder<T_DecodedElement> componentEncoder,
			boolean singleton
		) {
			super(arrayType, componentEncoder, singleton);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_DecodedElement[]> context) throws EncodeException {
			T_DecodedElement[] from = context.input;
			if (from == null) return context.empty();
			int length = from.length;
			if (this.singleton && length == 1) {
				return context.input(from[0]).encodeWith(this.componentEncoder);
			}
			else {
				List<T_Encoded> to = new ArrayList<>(length);
				for (int index = 0; index < length; index++) {
					to.add(context.input(from[index]).encodeWith(this.componentEncoder));
				}
				return context.createList(to);
			}
		}
	}

	public static class PrimitiveArrayEncoder<T_DecodedElement, T_DecodedArray> extends ArrayEncoder<T_DecodedElement, T_DecodedArray> {

		public PrimitiveArrayEncoder(
			@NotNull ReifiedType<T_DecodedArray> arrayType,
			@NotNull AutoEncoder<T_DecodedElement> componentEncoder,
			boolean singleton
		) {
			super(arrayType, componentEncoder, singleton);
		}

		@Override
		@OverrideOnly
		@SuppressWarnings("unchecked")
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_DecodedArray> context) throws EncodeException {
			T_DecodedArray from = context.input;
			if (from == null) return context.empty();
			int length = Array.getLength(from);
			if (this.singleton && length == 1) {
				T_DecodedElement decodedElement = (T_DecodedElement)(Array.get(from, 0));
				return context.input(decodedElement).encodeWith(this.componentEncoder);
			}
			List<T_Encoded> to = new ArrayList<>(length);
			for (int index = 0; index < length; index++) {
				T_DecodedElement decodedElement = (T_DecodedElement)(Array.get(from, index));
				to.add(context.input(decodedElement).encodeWith(this.componentEncoder));
			}
			return context.createList(to);
		}
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> componentType = context.type.getArrayComponentType();
			if (componentType != null) {
				AutoEncoder<?> componentEncoder = context.type(componentType).forceCreateEncoder();
				boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
				if (componentType.isPrimitive()) {
					return new PrimitiveArrayEncoder<>(context.type, componentEncoder, singleton);
				}
				else {
					return new ObjectArrayEncoder(context.type, componentEncoder, singleton);
				}
			}
			return null; //not an array.
		}
	}
}