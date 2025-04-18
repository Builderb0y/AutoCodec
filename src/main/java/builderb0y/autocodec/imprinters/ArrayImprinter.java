package builderb0y.autocodec.imprinters;

import java.lang.reflect.Array;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.data.ListData;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.imprinters.AutoImprinter.NamedImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public abstract class ArrayImprinter<T_DecodedElement, T_DecodedArray> extends NamedImprinter<T_DecodedArray> {

	public final @NotNull AutoCoder<T_DecodedElement> componentCoder;
	public final boolean singleton;

	public ArrayImprinter(
		@NotNull ReifiedType<T_DecodedArray> arrayType,
		@NotNull AutoCoder<T_DecodedElement> componentCoder,
		boolean singleton
	) {
		super(arrayType);
		this.componentCoder = componentCoder;
		this.singleton = singleton;
	}

	public static class PrimitiveArrayImprinter<T_DecodedElement, T_DecodedArray> extends ArrayImprinter<T_DecodedElement, T_DecodedArray> {

		public PrimitiveArrayImprinter(
			@NotNull ReifiedType<T_DecodedArray> arrayType,
			@NotNull AutoCoder<T_DecodedElement> componentDecoder,
			boolean singleton
		) {
			super(arrayType, componentDecoder, singleton);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_DecodedArray> context) throws ImprintException {
			if (context.isEmpty()) return;
			try {
				T_DecodedArray to = context.object;
				int length = Array.getLength(to);
				ListData from = context.forceAsListMaybeSingleton(this.singleton);
				int size = from.value.size();
				if (size != length) {
					throw new ImprintException(() -> context.pathToStringBuilder().append(" should have a length of ").append(length).append(", but it was length ").append(size).toString());
				}
				for (int index = 0; index < length; index++) {
					Array.set(to, index, context.fork(index, from.value.get(index)).decodeWith(this.componentCoder));
				}
			}
			catch (ImprintException exception) {
				throw exception;
			}
			catch (DecodeException exception) {
				throw new ImprintException(exception);
			}
		}
	}

	public static class ObjectArrayImprinter<T_DecodedElement> extends ArrayImprinter<T_DecodedElement, T_DecodedElement[]> {

		public ObjectArrayImprinter(
			@NotNull ReifiedType<T_DecodedElement[]> arrayType,
			@NotNull AutoCoder<T_DecodedElement> componentDecoder,
			boolean singleton
		) {
			super(arrayType, componentDecoder, singleton);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_DecodedElement[]> context) throws ImprintException {
			if (context.isEmpty()) return;
			try {
				T_DecodedElement[] to = context.object;
				int length = to.length;
				ListData from = context.forceAsListMaybeSingleton(this.singleton);
				if (from.value.size() != length) {
					throw new ImprintException(() -> context.pathToStringBuilder().append(" should have a length of ").append(length).append(", but it was length ").append(from.value.size()).toString());
				}
				for (int index = 0; index < length; index++) {
					to[index] = context.fork(index, from.value.get(index)).decodeWith(this.componentCoder);
				}
			}
			catch (ImprintException exception) {
				throw exception;
			}
			catch (DecodeException exception) {
				throw new ImprintException(exception);
			}
		}
	}

	public static class Factory extends NamedImprinterFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> componentType = context.type.getArrayComponentType();
			if (componentType != null) {
				Class<?> componentClass = componentType.getRawClass();
				if (componentClass != null) {
					AutoCoder<?> componentDecoder = context.type(componentType).forceCreateCoder();
					boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
					if (componentClass.isPrimitive()) {
						return new PrimitiveArrayImprinter(context.type, componentDecoder,  singleton);
					}
					else {
						return new ObjectArrayImprinter(context.type, componentDecoder, singleton);
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