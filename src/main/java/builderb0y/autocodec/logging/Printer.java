package builderb0y.autocodec.logging;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.jetbrains.annotations.NotNull;

/**
bridge to other logging platforms. for example, log4j2.
DFU does not have a dependency on log4j2, and I don't want to either.
so instead, this interface specifies the bare minimum
functionality required for actually printing messages somewhere.
users can implement this interface in a way which delegates
to some other logger, and then pass their implementation
into one of the subclasses of {@link AbstractTaskLogger}.
default implementations which delegate to PrintStream's
and PrintWriter's are provided as examples.
*/
public interface Printer {

	public static final @NotNull Printer SYSTEM = forPrintStreams(System.out, System.err);

	/**
	prints a "normal" message.
	normal messages needn't do anything special when printing.
	if the implementing platform supports multiple "levels" of logging,
	implementors are encouraged to use the "info" level here.
	*/
	public abstract void print(@NotNull String message);

	/**
	prints an error message.
	if the implementing platform supports multiple "levels" of logging,
	implementors are encouraged to use the "error" level here.
	*/
	public abstract void printError(@NotNull String error);



	/** creates a Printer which delegates to the provided PrintStream. */
	public static @NotNull Printer forPrintStream(@NotNull PrintStream stream) {
		return forPrintStreams(stream, stream);
	}

	/**
	creates a Printer which delegates to normalStream for
	normal messages, and errorStream for error messages.
	stack traces will also be printed to the errorStream.
	*/
	public static @NotNull Printer forPrintStreams(@NotNull PrintStream normalStream, @NotNull PrintStream errorStream) {
		return new Printer() {
			@Override public void print     (@NotNull String message) { normalStream.println(message); }
			@Override public void printError(@NotNull String error  ) {  errorStream.println(error  ); }
		};
	}

	/** creates a Printer which delegates to the provided PrintStream. */
	public static @NotNull Printer forPrintWriter(@NotNull PrintWriter writer) {
		return forPrintWriters(writer, writer);
	}

	/**
	creates a Printer which delegates to normalWriter for
	normal messages, and errorWriter for error messages.
	stack traces will also be printed to the errorWriter.
	*/
	public static @NotNull Printer forPrintWriters(@NotNull PrintWriter normalWriter, @NotNull PrintWriter errorWriter) {
		return new Printer() {
			@Override public void print     (@NotNull String message) { normalWriter.println(message); }
			@Override public void printError(@NotNull String error  ) {  errorWriter.println(error  ); }
		};
	}
}