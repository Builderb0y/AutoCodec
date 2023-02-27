package builderb0y.autocodec.encoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.EnumName;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class EnumEncoder<T_DecodedEnum extends Enum<T_DecodedEnum>> extends NamedEncoder<T_DecodedEnum> {

	public final @NotNull EnumName enumName;

	public EnumEncoder(@NotNull ReifiedType<T_DecodedEnum> enumType, @NotNull EnumName enumName) {
		super(enumType);
		this.enumName = enumName;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_DecodedEnum> context) throws EncodeException {
		if (context.input == null) return context.empty();
		return (
			context.isCompressed()
			? context.createInt(context.input.ordinal())
			: context.createString(this.enumName.getEnumName(context.input))
		);
	}

	@Override
	public String toString() {
		return this.toString + ": { enumName: " + this.enumName + " }";
	}

	public static class Factory extends NamedEncoderFactory {

		public @NotNull EnumName nameGetter;

		public Factory(@NotNull EnumName nameGetter) {
			this.nameGetter = nameGetter;
		}

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<?> rawClass = context.type.getBoundOrSelf().getRawClass();
			if (rawClass != null && rawClass.isEnum()) {
				return new EnumEncoder(context.type, this.nameGetter);
			}
			return null;
		}
	}
}