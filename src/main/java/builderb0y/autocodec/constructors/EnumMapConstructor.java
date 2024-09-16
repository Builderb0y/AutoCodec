package builderb0y.autocodec.constructors;

import java.util.EnumMap;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructor;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumMapConstructor<K extends Enum<K>, V> extends NamedConstructor<EnumMap<K, V>> {

	public final @NotNull Class<K> enumClass;

	public EnumMapConstructor(@NotNull ReifiedType<EnumMap<K, V>> type, @NotNull Class<K> enumClass) {
		super(type);
		this.enumClass = enumClass;
	}

	@Override
	@OverrideOnly
	@Contract("_ -> new")
	public @NotNull <T_Encoded> EnumMap<K, V> construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
		return new EnumMap<>(this.enumClass);
	}

	public static class Factory extends NamedConstructorFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> keyType = context.type.resolveParameter(EnumMap.class);
			if (keyType != null) {
				Class<?> keyClass;
				if ((keyClass = keyType.getRawClass()) == null || !keyClass.isEnum()) {
					throw new FactoryException("EnumMap with non-enum keys: " + keyType);
				}
				return new EnumMapConstructor<>(context.type.uncheckedCast(), keyClass.asSubclass(Enum.class));
			}
			return null; //not an EnumMap.
		}
	}
}