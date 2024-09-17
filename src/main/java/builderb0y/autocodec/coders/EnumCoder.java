package builderb0y.autocodec.coders;

import java.util.LinkedHashMap;
import java.util.Map;

import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.EnumName;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumCoder<T_DecodedEnum extends Enum<T_DecodedEnum>> extends NamedCoder<T_DecodedEnum> {

	public final @NotNull EnumName enumName;
	public final @NotNull T_DecodedEnum @NotNull [] valueArray;
	public final @N Map<@NotNull String, @NotNull T_DecodedEnum> valueMap;

	public EnumCoder(@NotNull Class<T_DecodedEnum> enumClass, @NotNull EnumName enumName) {
		super(ReifiedType.from(enumClass));
		this.enumName = enumName;
		this.valueArray = enumClass.getEnumConstants();
		this.valueMap = new LinkedHashMap<>(this.valueArray.length);
		for (@NotNull T_DecodedEnum value : this.valueArray) {
			String name = enumName.getEnumName(value);
			if (this.valueMap.putIfAbsent(name, value) != null) {
				throw new IllegalArgumentException("Duplicate enum: " + name);
			}
		}
	}

	@Override
	public <T_Encoded> @Nullable T_DecodedEnum decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		//note: check ordinal first, as some ops will implicitly convert numbers to strings.
		Number ordinal = context.tryAsNumber();
		if (ordinal != null) {
			int actualOrdinal = ordinal.intValue();
			int length = this.valueArray.length;
			if (actualOrdinal >= 0 && actualOrdinal < length) {
				return this.valueArray[actualOrdinal];
			}
			else {
				throw new DecodeException(() -> "Ordinal out of bounds: " + ordinal + " (there are only " + length + " enums to choose from)");
			}
		}
		String name = context.tryAsString();
		if (name != null) {
			T_DecodedEnum value = this.valueMap.get(name);
			if (value != null) return value;
			else throw new DecodeException(() -> "Invalid name: " + name + " (valid names are: " + this.valueMap.keySet() + ')');
		}
		throw context.notA("string or number");
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_DecodedEnum> context) throws EncodeException {
		if (context.object == null) return context.empty();
		return (
			context.isCompressed()
			? context.createInt(context.object.ordinal())
			: context.createString(this.enumName.getEnumName(context.object))
		);
	}

	@Override
	public String toString() {
		return super.toString() + ": { enumName: " + this.enumName + " }";
	}

	public static class Factory extends NamedCoderFactory {

		public @NotNull EnumName nameGetter;

		public Factory(@NotNull EnumName nameGetter) {
			this.nameGetter = nameGetter;
		}

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<?> rawClass = context.type.getRawClass();
			if (rawClass != null && rawClass.isEnum()) {
				return new EnumCoder(rawClass, this.nameGetter);
			}
			return null;
		}
	}
}