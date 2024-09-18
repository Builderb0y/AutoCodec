package builderb0y.autocodec.encoders;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class CollectionEncoder<T_Element, T_Collection extends Collection<T_Element>> extends NamedEncoder<T_Collection> {

	public final @NotNull AutoCoder<T_Element> elementCoder;
	public final boolean singleton;

	public CollectionEncoder(
		@NotNull ReifiedType<T_Collection> type,
		@NotNull AutoCoder<T_Element> elementCoder,
		boolean singleton
	) {
		super(type);
		this.elementCoder = elementCoder;
		this.singleton = singleton;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Collection> context) throws EncodeException {
		if (context.object == null) return context.empty();
		AutoCoder<T_Element> coder = this.elementCoder;
		return context.createList(context.object.stream().map((T_Element element) -> context.object(element).encodeWith(coder)));
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> elementType = context.type.resolveParameter(Collection.class);
			if (elementType != null) {
				AutoCoder<?> elementEncoder = context.type(elementType).forceCreateCoder();
				boolean singleton = context.type.getAnnotations().has(SingletonArray.class);
				return new CollectionEncoder<>(context.type.uncheckedCast(), elementEncoder, singleton);
			}
			return null;
		}
	}
}