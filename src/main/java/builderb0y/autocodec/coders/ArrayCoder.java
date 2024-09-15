package builderb0y.autocodec.coders;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ArrayFactory;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.PrimitiveArrayFactory;

public class ArrayCoder<T_DecodedElement, T_DecodedArray> extends NamedCoder<T_DecodedArray> {

	public final @NotNull AutoCoder<T_DecodedElement> elementCoder;
	public final @NotNull ArrayFactory<T_DecodedArray> arrayFactory;
	public final boolean singleton;

	public ArrayCoder(
		@NotNull ReifiedType<T_DecodedArray> handledType,
		@NotNull AutoCoder<T_DecodedElement> elementCoder,
		@NotNull ArrayFactory<T_DecodedArray> arrayFactory,
		boolean singleton
	) {
		super(handledType);
		this.elementCoder = elementCoder;
		this.arrayFactory = arrayFactory;
		this.singleton    = singleton;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_DecodedArray decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		List<DecodeContext<T_Encoded>> from = context.forceAsList(this.singleton);
		int length = from.size();
		T_DecodedArray to = this.arrayFactory.apply(length);
		for (int index = 0; index < length; index++) {
			Array.set(to, index, from.get(index).decodeWith(this.elementCoder));
		}
		return to;
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
			return context.input(decodedElement).encodeWith(this.elementCoder);
		}
		List<T_Encoded> to = new ArrayList<>(length);
		for (int index = 0; index < length; index++) {
			T_DecodedElement decodedElement = (T_DecodedElement)(Array.get(from, index));
			to.add(context.input(decodedElement).encodeWith(this.elementCoder));
		}
		return context.createList(to);
	}

	@Override
	public String toString() {
		return this.toString + ": { elementCoder: " + this.elementCoder + " }";
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> componentType = context.type.getArrayComponentType();
			if (componentType != null) {
				Class<?> componentClass = componentType.getBoundOrSelf().getRawClass();
				if (componentClass == null) {
					throw new FactoryException("Failed to get array component class of " + componentType + " (array type: " + context.type + ')');
				}
				AutoCoder<?> elementCoder = context.type(componentType).forceCreateCoder();
				boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
				return new ArrayCoder(
					context.type,
					elementCoder,
					componentClass.isPrimitive()
					? PrimitiveArrayFactory.forComponentType(componentClass)
					: new ObjectArrayFactory(componentClass),
					singleton
				);
			}
			return null;
		}
	}
}