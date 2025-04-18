package builderb0y.autocodec.coders;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.ForceOrdinal;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.EnumName;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.data.*;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumCoder<T_DecodedEnum extends Enum<T_DecodedEnum>> extends NamedCoder<T_DecodedEnum> {

	public final @NotNull EnumName enumName;
	public final @NotNull T_DecodedEnum @NotNull [] valueArray;
	public final @NotNull Map<@NotNull String, @NotNull T_DecodedEnum> valueMap;
	public final @Nullable Boolean forceOrdinal;

	public EnumCoder(@NotNull Class<T_DecodedEnum> enumClass, @NotNull EnumName enumName, @Nullable Boolean forceOrdinal) {
		super(ReifiedType.from(enumClass));
		this.enumName = enumName;
		this.valueArray = enumClass.getEnumConstants();
		this.valueMap = new LinkedHashMap<>(this.valueArray.length);
		for (T_DecodedEnum value : this.valueArray) {
			String name = enumName.getEnumName(value);
			if (this.valueMap.putIfAbsent(name, value) != null) {
				throw new IllegalArgumentException("Duplicate enum: " + name);
			}
		}
		this.forceOrdinal = forceOrdinal;
	}

	@Override
	public <T_Encoded> @Nullable T_DecodedEnum decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.data.isEmpty()) return null;
		//note: check ordinal first, as some ops will implicitly convert numbers to strings.
		AbstractNumberData ordinal = context.data.tryAsNumber();
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
		StringData name = context.data.tryAsString();
		if (name != null) {
			T_DecodedEnum value = this.valueMap.get(name.value);
			if (value != null) return value;
			else throw new DecodeException(() -> "Invalid name: " + name.value + " (valid names are: " + this.valueMap.keySet() + ')');
		}
		throw context.notA("string or number");
	}

	@Override
	public <T_Encoded> @NotNull Data encode(@NotNull EncodeContext<T_Encoded, T_DecodedEnum> context) throws EncodeException {
		if (context.object == null) return EmptyData.INSTANCE;
		return (
			(this.forceOrdinal != null ? this.forceOrdinal.booleanValue() : context.isCompressed())
			? new NumberData(context.object.ordinal())
			: new StringData(this.enumName.getEnumName(context.object))
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
				ForceOrdinal annotation = context.type.getAnnotations().getFirst(ForceOrdinal.class);
				return new EnumCoder(rawClass, this.nameGetter, annotation != null ? Boolean.valueOf(annotation.value()) : null);
			}
			return null;
		}
	}
}