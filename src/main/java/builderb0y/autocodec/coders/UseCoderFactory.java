package builderb0y.autocodec.coders;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.CoderFactory;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.UseHandlerFactory0;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.decoders.UseDecoderFactory;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.encoders.UseEncoderFactory;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;

public class UseCoderFactory extends UseHandlerFactory0<AutoCoder<?>> implements CoderFactory {

	public static final @NotNull UseCoderFactory INSTANCE = new UseCoderFactory();

	public UseCoderFactory() {
		super(AutoCoder.class, CoderFactory.class);
	}

	@Override
	public @Nullable <T_HandledType> UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseCoder(context.type);
	}

	@Override
	public @NotNull AutoCoder<?> createMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) throws Throwable {
		MethodLikeMemberView<?, ?>
			encoder = UseEncoderFactory.findMethodBeingEncoder(context, spec),
			decoder = UseDecoderFactory.findMethodBeingDecoder(context, spec);
		MethodHandle
			encoderHandle = encoder.createMethodHandle(context).asType(MethodType.methodType(Object.class, EncodeContext.class)),
			decoderHandle = decoder.createMethodHandle(context).asType(MethodType.methodType(Object.class, DecodeContext.class));
		return new NamedCoder<>("UseCoder: { encoder: " + encoder + ", decoder: " + decoder + " }") {

			@Override
			@OverrideOnly
			@SuppressWarnings("unchecked")
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Object> context) throws EncodeException {
				try {
					return (T_Encoded)(encoderHandle.invokeExact(context));
				}
				catch (EncodeException | Error normal) {
					throw normal;
				}
				catch (Throwable throwable) {
					throw new EncodeException(throwable);
				}
			}

			@Override
			@OverrideOnly
			public @Nullable <T_Encoded> Object decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				try {
					return decoderHandle.invokeExact(context);
				}
				catch (DecodeException | Error normal) {
					throw normal;
				}
				catch (Throwable throwable) {
					throw new DecodeException(throwable);
				}
			}
		};
	}
}