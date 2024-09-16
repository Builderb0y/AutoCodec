package builderb0y.autocodec.imprinters;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.imprinters.AutoImprinter.NamedImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class CollectionImprinter<T_Element, T_Collection extends Collection<T_Element>> extends NamedImprinter<T_Collection> {

	public final @NotNull AutoDecoder<T_Element> elementDecoder;
	public final boolean singleton;

	public CollectionImprinter(
		@NotNull ReifiedType<T_Collection> type,
		@NotNull AutoDecoder<T_Element> elementDecoder,
		boolean singleton
	) {
		super(type);
		this.elementDecoder = elementDecoder;
		this.singleton = singleton;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Collection> context) throws ImprintException {
		try {
			List<DecodeContext<T_Encoded>> array = context.forceAsList(this.singleton);
			for (DecodeContext<T_Encoded> encodedElement : array) {
				context.object.add(encodedElement.decodeWith(this.elementDecoder));
			}
		}
		catch (ImprintException exception) {
			throw exception;
		}
		catch (DecodeException exception) {
			throw new ImprintException(exception);
		}
	}

	public static class Factory extends NamedImprinterFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> elementType = context.type.resolveParameter(Collection.class);
			if (elementType != null) {
				AutoDecoder<?> elementDecoder = context.type(elementType).forceCreateDecoder();
				boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
				return new CollectionImprinter<>(context.type.uncheckedCast(), elementDecoder, singleton);
			}
			return null;
		}
	}
}