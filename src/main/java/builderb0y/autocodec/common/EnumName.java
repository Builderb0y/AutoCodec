package builderb0y.autocodec.common;

import java.lang.reflect.Field;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.UseName;
import builderb0y.autocodec.decoders.EnumDecoder;
import builderb0y.autocodec.encoders.EnumEncoder;

/**
used by {@link EnumEncoder} and {@link EnumDecoder}
when serializing/deserializing enums.
can be used to serialize enums as a string which
differs from the enum's actual {@link Enum#name()}.
for example, the enum may be named with UPPER_SNAKE_CASE
in the source code, but it may be desirable
to serialize it as lower_snake_case instead.
*/
@FunctionalInterface
public interface EnumName {

	public static final @NotNull EnumName
		DEFAULT          = forCase(Case.DEFAULT),
		LOWERCASE        = forCase(Case.LOWERCASE),
		UPPERCASE        = forCase(Case.UPPERCASE),
		LOWER_SNAKE_CASE = forCase(Case.LOWER_SNAKE_CASE),
		UPPER_SNAKE_CASE = forCase(Case.UPPER_SNAKE_CASE),
		LOWER_KEBAB_CASE = forCase(Case.LOWER_KEBAB_CASE),
		UPPER_KEBAB_CASE = forCase(Case.UPPER_KEBAB_CASE),
		LOWER_SPACE_CASE = forCase(Case.LOWER_SPACE_CASE),
		UPPER_SPACE_CASE = forCase(Case.UPPER_SPACE_CASE),
		CAMEL_CASE       = forCase(Case.CAMEL_CASE),
		PASCAL_CASE      = forCase(Case.PASCAL_CASE);

	public abstract @NotNull String getEnumName(@NotNull Enum<?> value);

	public static @NotNull EnumName forCase(@NotNull Case case_) {
		return new EnumName() {

			@Override
			public @NotNull String getEnumName(@NotNull Enum<?> value) {
				String customName = getCustomName(value);
				if (customName != null) return customName;
				return case_.apply(value.name());
			}

			@Override
			public String toString() {
				return case_.toString();
			}
		};
	}

	public static @Nullable String getCustomName(@NotNull Enum<?> value) {
		Class<?> enumClass = value.getDeclaringClass();
		Field field;
		try {
			field = enumClass.getDeclaredField(value.name());
		}
		catch (NoSuchFieldException exception) {
			throw new AssertionError("Enum constant has no associated field: " + value, exception);
		}
		UseName annotation = field.getAnnotatedType().getDeclaredAnnotation(UseName.class);
		return annotation != null ? annotation.value() : null;
	}
}