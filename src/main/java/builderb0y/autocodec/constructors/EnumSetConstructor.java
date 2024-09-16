package builderb0y.autocodec.constructors;

import java.util.EnumSet;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructor;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumSetConstructor<E extends Enum<E>> extends NamedConstructor<EnumSet<E>> {

	public final @NotNull Class<E> enumClass;

	public EnumSetConstructor(@NotNull ReifiedType<EnumSet<E>> type, @NotNull Class<E> enumClass) {
		super(type);
		this.enumClass = enumClass;
	}

	@Override
	@OverrideOnly
	@Contract("_ -> new")
	public <T_Encoded> @NotNull EnumSet<E> construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
		return EnumSet.noneOf(this.enumClass);
	}

	public static class Factory extends NamedConstructorFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> elementType = context.type.resolveParameter(EnumSet.class);
			if (elementType != null) {
				Class<?> elementClass;
				if ((elementClass = elementType.getRawClass()) == null || !elementClass.isEnum()) {
					throw new FactoryException("EnumSet with non-enum element type: " + context.type);
				}
				return new EnumSetConstructor<>(context.type.uncheckedCast(), elementClass.asSubclass(Enum.class));
			}
			return null;
		}
	}
}