package builderb0y.autocodec.logging;

import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;

/**
very simple TaskLogger implementation which just logs
whatever it's told to log, with no indentation or formatting.
I don't know why you'd want this, but it's available I guess.
*/
public class BasicTaskLogger extends AbstractTaskLogger {

	public BasicTaskLogger(@NotNull Printer printer) {
		super(printer);
	}

	public BasicTaskLogger(@NotNull Printer printer, boolean filterStackTraces) {
		super(printer, filterStackTraces);
	}

	public BasicTaskLogger(@NotNull ReentrantLock lock, @NotNull Printer printer, boolean filterStackTraces) {
		super(lock, printer, filterStackTraces);
	}

	@Override
	protected void doPrint(@NotNull String message) {
		this.printer.print(message);
	}

	@Override
	protected void doPrintError(@NotNull String error) {
		this.printer.printError(error);
	}
}