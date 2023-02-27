package builderb0y.autocodec.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

/**
common set of attributes from {@link UseEncoder}, {@link UseDecoder}, {@link UseCoder},
{@link UseConstructor}, {@link UseImprinter}, and {@link UseVerifier}.
*/
public record UseSpec(
	@NotNull String name,
	@NotNull ReifiedType<?> in,
	@NotNull MemberUsage usage,
	boolean strict
) {

	public static final @NotNull ObjectArrayFactory<UseSpec> ARRAY_FACTORY = new ObjectArrayFactory<>(UseSpec.class);

	public static @NotNull ReifiedType<?> inType(@NotNull Class<?> inClass, @NotNull ReifiedType<?> fallback) {
		return inClass == void.class ? fallback : ReifiedType.parameterizeWithWildcards(inClass);
	}

	public static @Nullable UseSpec fromUseEncoder(@NotNull ReifiedType<?> type) {
		UseEncoder encoder = type.getAnnotations().getFirst(UseEncoder.class);
		if (encoder != null) return new UseSpec(encoder.name(), inType(encoder.in(), type), encoder.usage(), encoder.strict());
		UseCoder coder = type.getAnnotations().getFirst(UseCoder.class);
		if (coder != null) return new UseSpec(coder.name(), inType(coder.in(), type), coder.usage(), coder.strict());
		return null;
	}

	public static @Nullable UseSpec fromUseDecoder(@NotNull ReifiedType<?> type) {
		UseDecoder annotation = type.getAnnotations().getFirst(UseDecoder.class);
		if (annotation != null) return new UseSpec(annotation.name(), inType(annotation.in(), type), annotation.usage(), annotation.strict());
		UseCoder coder = type.getAnnotations().getFirst(UseCoder.class);
		if (coder != null) return new UseSpec(coder.name(), inType(coder.in(), type), coder.usage(), coder.strict());
		return null;
	}

	public static @Nullable UseSpec fromUseConstructor(@NotNull ReifiedType<?> type) {
		UseConstructor annotation = type.getAnnotations().getFirst(UseConstructor.class);
		return annotation == null ? null : new UseSpec(annotation.name(), inType(annotation.in(), type), annotation.usage(), annotation.strict());
	}

	public static @Nullable UseSpec fromUseImprinter(@NotNull ReifiedType<?> type) {
		UseImprinter annotation = type.getAnnotations().getFirst(UseImprinter.class);
		return annotation == null ? null : new UseSpec(annotation.name(), inType(annotation.in(), type), annotation.usage(), annotation.strict());
	}

	public static @Nullable UseSpec fromUseVerifier(@NotNull ReifiedType<?> type) {
		UseVerifier annotation = type.getAnnotations().getFirst(UseVerifier.class);
		return annotation == null ? null : new UseSpec(annotation.name(), inType(annotation.in(), type), annotation.usage(), annotation.strict());
	}

	public static @NotNull UseSpec @NotNull [] fromAllUseVerifiers(@NotNull ReifiedType<?> type) {
		UseVerifier[] annotations = type.getAnnotations().getAll(UseVerifier.class);
		int length = annotations.length;
		UseSpec[] specs = ARRAY_FACTORY.apply(length);
		for (int index = 0; index < length; index++) {
			UseVerifier annotation = annotations[index];
			specs[index] = new UseSpec(annotation.name(), inType(annotation.in(), type), annotation.usage(), annotation.strict());
		}
		return specs;
	}
}