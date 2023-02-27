package builderb0y.autocodec.decoders;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.EnumName;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumDecoder<T_DecodedEnum extends Enum<T_DecodedEnum>> extends NamedDecoder<T_DecodedEnum> {

	public final @NotNull EnumName enumName;
	public final @NotNull T_DecodedEnum @NotNull [] values;
	public final @NotNull Map<@NotNull String, @NotNull T_DecodedEnum> valueMap;

	public EnumDecoder(@NotNull Class<T_DecodedEnum> enumClass, @NotNull EnumName enumName) {
		super(ReifiedType.from(enumClass));
		this.enumName = enumName;
		this.values = enumClass.getEnumConstants();
		this.valueMap = new LinkedHashMap<>(this.values.length);
		for (T_DecodedEnum value : this.values) {
			String name = enumName.getEnumName(value);
			if (this.valueMap.putIfAbsent(name, value) != null) {
				throw new IllegalArgumentException("Duplicate enum: " + name);
			}
		}
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_DecodedEnum decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		Number ordinal = context.tryAsNumber();
		if (ordinal != null) {
			int actualOrdinal = ordinal.intValue();
			if (actualOrdinal >= 0 && actualOrdinal < this.values.length) {
				return this.values[actualOrdinal];
			}
			else {
				throw new DecodeException("Ordinal out of bounds: " + ordinal + " (there are only " + this.values.length + " enums to choose from)");
			}
		}
		String name = context.tryAsString();
		if (name != null) {
			T_DecodedEnum value = this.valueMap.get(name);
			if (value != null) return value;
			else throw new DecodeException("Invalid name: " + name + " (valid names are: " + this.valueMap.keySet() + ')');
		}
		throw new DecodeException("Not a string or number: " + context.input);
	}

	@Override
	public String toString() {
		return this.toString + ": { enumName: " + this.enumName + " }";
	}

	public static class Factory extends NamedDecoderFactory {

		public @NotNull EnumName nameGetter;

		public Factory(@NotNull EnumName nameGetter) {
			this.nameGetter = nameGetter;
		}

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<?> rawClass = context.type.getBoundOrSelf().getRawClass();
			if (rawClass != null && rawClass.isEnum()) {
				return new EnumDecoder(rawClass, this.nameGetter);
			}
			return null;
		}
	}
}