package builderb0y.autocodec.encoders;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.WrapperSpec;
import builderb0y.autocodec.decoders.WrapperDecoderFactory;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoderFactory;

public class WrapperEncoderFactory extends NamedEncoderFactory {

	/**
	the problem:
	when a wrapper is annotated with {@link VerifyNullable},
	the wrapped type may not be. it may be desired to decode
	null data into a null wrapper, but this requires getting
	a null wrapped object first, which would fail verification.

	the solution:
	the VerifyNullable annotation is spoofed on the wrapped type
	to allow it to be null always. null checking is then performed
	on the wrapper type instead of the wrapped type.

	this field can be added to by overriding {@link EncoderFactoryList#setup()}
	to add any other annotations necessary to disable any verification which
	would normally be applied to both the wrapper type and the wrapped type,
	when only the wrapper type should be verified at runtime.
	note that when modifying this list,
	{@link WrapperDecoderFactory#annotationsToDisableVerification}
	should be kept in sync with it.
	*/
	public List<Annotation> annotationsToDisableVerification = new ArrayList<>(2);

	public WrapperEncoderFactory() {
		this.annotationsToDisableVerification.add(VerifyNullable.INSTANCE);
	}

	@Override
	public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		WrapperSpec<T_HandledType, ?> spec = WrapperSpec.find(context, this.annotationsToDisableVerification);
		return spec != null ? spec.createEncoder(context) : null;
	}
}