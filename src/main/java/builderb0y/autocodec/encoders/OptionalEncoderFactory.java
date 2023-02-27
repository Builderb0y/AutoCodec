package builderb0y.autocodec.encoders;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.AutoHandler.HandlerMapper;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class OptionalEncoderFactory extends NamedEncoderFactory {

	public static final @NotNull OptionalEncoderFactory INSTANCE = new OptionalEncoderFactory();

	@Override
	@OverrideOnly
	public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		ReifiedType<?> optionalType = context.type.getUpperBoundOrSelf();
		Class<?> rawClass = optionalType.getRawClass();
		if (rawClass == Optional.class) {
			ReifiedType<?>[] parameters = optionalType.getParameters();
			if (parameters == null) throw new FactoryException("Raw Optional");
			return make(context.type(parameters[0]));
		}
		return null;
	}

	public static <T> @NotNull AutoEncoder<Optional<T>> make(@NotNull FactoryContext<T> context) {
		ReifiedType<T> wrappedType = context.type.addAnnotations(VerifyNullable.INSTANCE);
		ReifiedType<Optional<T>> optionalType = ReifiedType.parameterize(Optional.class, context.type);
		AutoEncoder<T> wrapped = context.type(wrappedType).forceCreateEncoder();
		if (wrapped instanceof AutoCoder<T> coder) {
			return coder.mapCoder(optionalType, "Optional::orElse(null)", HandlerMapper.nullSafe((Optional<T> optional) -> optional.orElse(null)), "Optional::ofNullable", Optional::ofNullable);
		}
		else {
			return wrapped.mapEncoder(optionalType, "Optional::orElse(null)", HandlerMapper.nullSafe((Optional<T> optional) -> optional.orElse(null)));
		}
	}
}