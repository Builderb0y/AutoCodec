package builderb0y.autocodec.common;

import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.EncoderDecoderCoder;
import builderb0y.autocodec.coders.VerifyingCoder;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.VerifyingImprinter;
import builderb0y.autocodec.logging.DisabledTaskLogger;
import builderb0y.autocodec.logging.IndentedTaskLogger;
import builderb0y.autocodec.logging.Printer;
import builderb0y.autocodec.logging.TaskLogger;

public class TestCommon {

	public static final AutoCodec
		DEFAULT_CODEC = new AutoCodec(),
		DISABLED_CODEC = new AutoCodec() {

			@Override
			public @NotNull TaskLogger createDefaultLogger(@NotNull ReentrantLock lock) {
				return new DisabledTaskLogger();
			}
		},
		DEBUG_CODEC = new AutoCodec() {

			@Override
			public @NotNull TaskLogger createDefaultLogger(@NotNull ReentrantLock lock) {
				return new IndentedTaskLogger(lock, Printer.SYSTEM, false);
			}
		};

	public static <T_Decoded> AutoEncoder<T_Decoded> encoder(AutoEncoder<T_Decoded> encoder) {
		while (true) {
			if (encoder instanceof EncoderDecoderCoder<T_Decoded> coder) {
				encoder = coder.encoder();
			}
			else if (encoder instanceof VerifyingCoder<T_Decoded> coder) {
				encoder = coder.coder;
			}
			else {
				return encoder;
			}
		}
	}

	public static <T_Decoded> AutoDecoder<T_Decoded> decoder(AutoDecoder<T_Decoded> decoder) {
		while (true) {
			if (decoder instanceof EncoderDecoderCoder<T_Decoded> coder) {
				decoder = coder.decoder();
			}
			else if (decoder instanceof VerifyingCoder<T_Decoded> verifying) {
				decoder = verifying.coder;
			}
			else {
				return decoder;
			}
		}
	}

	public static <T_Decoded> AutoImprinter<T_Decoded> imprinter(AutoImprinter<T_Decoded> imprinter) {
		while (true) {
			if (imprinter instanceof VerifyingImprinter<T_Decoded> verifying) {
				imprinter = verifying.imprinter;
			}
			else {
				return imprinter;
			}
		}
	}
}