package builderb0y.autocodec.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.AutoCodecUtil;

/**
basic TaskLogger implementation which handles some of the common tasks loggers need to do.
for example, actually printing things, and filtering stack traces of exceptions.
*/
public abstract class AbstractTaskLogger extends TaskLogger {

	/**
	since some loggers have an internal state,
	it is important to not let that internal
	state be corrupted by multiple threads.
	every TaskLogger instance can only work on one task at a time.
	except for {@link DisabledTaskLogger}, which has no internal state,
	and can work on any number of tasks at once.
	*/
	public final @NotNull ReentrantLock lock;

	/** the Printer to delegate to. */
	public @NotNull Printer printer;
	/**
	when true, stack traces will be filtered to not include
	stack frames in the "builderb0y.autocodec.logging" package.
	this can be useful, as the logging system typically takes
	up a few stack frames for every {@link LoggableTask}
	that gets run {@link #runTask(LoggableTask)},
	and these frames are typically not useful for debugging
	whatever caused the exception in the first place.
	*/
	public boolean filterStackTraces;
	/**
	set of Throwable's which have already been filtered/printed/etc...
	we do not want to print a Throwable multiple times.
	*/
	public final @NotNull WeakHashMap<Throwable, Unit> seenThrowables = new WeakHashMap<>(8);

	public AbstractTaskLogger(@NotNull Printer printer) {
		this(printer, false);
	}

	public AbstractTaskLogger(@NotNull Printer printer, boolean filterStackTraces) {
		this(new ReentrantLock(), printer, filterStackTraces);
	}

	public AbstractTaskLogger(@NotNull ReentrantLock lock, @NotNull Printer printer, boolean filterStackTraces) {
		this.lock = lock;
		this.printer = printer;
		this.filterStackTraces = filterStackTraces;
	}

	/**
	prints a message to our delegate {@link #printer}.
	note that the message could be multiple lines long,
	so if this TaskLogger wants to apply per-line formatting,
	it should handle that in this method.

	this method is protected as {@link #logMessage(Object)}
	should be used instead in almost all cases.
	subclasses may perform other state-modification operations
	when logging which are not performed when printing,
	and that could break things.

	if you *really* want to intentionally bypass state-modification operations,
	consider calling {@link #printer}.{@link Printer#print(String)} instead.
	*/
	protected abstract void doPrint(@NotNull String message);

	/**
	prints an error to our delegate {@link #printer}.
	note that the error could be multiple lines long,
	so if this TaskLogger wants to apply per-line formatting,
	it should handle that in this method.

	this method is protected as {@link #logError(Object)} (Object)}
	should be used instead in almost all cases.
	subclasses may perform other state-modification operations
	when logging which are not performed when printing,
	and that could break things.

	if you *really* want to intentionally bypass state-modification operations,
	consider calling {@link #printer}.{@link Printer#printError(String)} instead.
	*/
	protected abstract void doPrintError(@NotNull String error);

	public void printStackTrace(@NotNull Throwable throwable) {
		if (this.maybeFilterStackTrace(throwable)) {
			this.doPrintError(stackTraceToString(throwable));
		}
	}

	@Override
	public void logMessage(@NotNull Object message) {
		this.doPrint(AutoCodecUtil.deepToString(message));
	}

	@Override
	public void logError(@NotNull Object message) {
		if (message instanceof Throwable throwable) {
			this.printStackTrace(throwable);
		}
		else {
			this.doPrintError(AutoCodecUtil.deepToString(message));
		}
	}

	/**
	invoked by {@link #runTask(LoggableTask)}, this method indicates
	to the logger that the provided task is about to be run.
	the default implementation here simply prints the task.
	subclasses may perform additional actions,
	or may choose not to print the task.
	*/
	public <R, X extends Throwable> void beginTask(@NotNull LoggableTask<R, X> task) {
		this.doPrint(task.toString());
	}

	/**
	invoked by {@link #runTask(LoggableTask)}, this method indicates
	to the logger that the provided task just finished running.
	if the task threw an exception while running,
	that exception is exposed by the throwable parameter.
	otherwise, the task's return value is exposed by the result parameter,
	and the throwable parameter will be null.
	note: if the task returned null without throwing an exception,
	then the result parameter and the throwable parameter will both be null.

	the default implementation here will perform the following actions:
	if the task returned normally and did not throw an exception,
	then the task's return value is printed as a normal message.
	if the task threw an exception and did not return normally,
	then the exception's stack trace is printed.

	subclasses may choose to print more or less information when a task ends.
	*/
	public <R, X extends Throwable> void endTask(@NotNull LoggableTask<R, X> task, @Nullable R result, @Nullable Throwable throwable) {
		if (throwable != null) {
			this.printStackTrace(throwable);
		}
		else {
			this.doPrint(AutoCodecUtil.deepToString(result));
		}
		if (this.lock.getHoldCount() == 1) {
			this.seenThrowables.clear();
		}
	}

	@Override
	public <R, X extends Throwable> R runTask(@NotNull LoggableTask<R, X> task) throws X {
		this.lock.lock();
		try {
			this.beginTask(task);
			R result;
			Throwable throwable;
			try {
				result = task.run();
				throwable = null;
			}
			catch (Throwable error) {
				result = null;
				throwable = error;
			}

			try {
				this.endTask(task, result, throwable);
			}
			catch (Throwable error) {
				/**
				this block of code indicates that the logger *itself* is broken,
				and we do NOT want to risk this error getting swallowed elsewhere.
				so, we will print its stack trace immediately,
				and we will not delegate to {@link #logError(Object)}.
				*/
				error.printStackTrace();
				if (throwable != null) throwable.addSuppressed(error);
				else throwable = error;
			}

			if (throwable != null) {
				throw AutoCodecUtil.<X>rethrow(throwable);
			}

			return result;
		}
		finally {
			this.lock.unlock();
		}
	}

	/**
	removes all {@link StackTraceElement}'s from the throwable's stack trace
	which correspond to classes in the "builderb0y.autocodec.logging" package.
	*/
	public boolean maybeFilterStackTrace(@Nullable Throwable throwable) {
		if (throwable == null) return false;
		if (this.seenThrowables.put(throwable, Unit.INSTANCE) != null) return false;
		if (this.filterStackTraces) {
			StackTraceElement[] stackTrace = throwable.getStackTrace();
			int length = stackTrace.length;
			int readIndex = 0, writeIndex = 0;
			for (; readIndex < length; readIndex++) {
				StackTraceElement element = stackTrace[readIndex];
				if (!element.getClassName().startsWith("builderb0y.autocodec.logging.")) {
					stackTrace[writeIndex++] = element;
				}
			}
			if (writeIndex != length) {
				throwable.setStackTrace(Arrays.copyOf(stackTrace, writeIndex));
			}
			this.maybeFilterStackTrace(throwable.getCause());
			for (Throwable suppressed : throwable.getSuppressed()) {
				this.maybeFilterStackTrace(suppressed);
			}
		}
		return true;
	}

	public static @NotNull String stackTraceToString(@NotNull Throwable throwable) {
		StringWriter writer = new StringWriter(1024);
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	public static @NotNull Stream<@NotNull String> prefixLines(@NotNull String text, @NotNull String prefix) {
		Stream<String> lines = text.lines();
		if (!prefix.isEmpty()) lines = lines.map(prefix::concat);
		return lines;
	}

	public static @NotNull Stream<@NotNull String> indentLines(@NotNull String text, int indentation) {
		return prefixLines(text, "\t".repeat(indentation));
	}

	public static void printWithPrefix(@NotNull Printer printer, @NotNull String text, @NotNull String prefix) {
		prefixLines(text, prefix).forEachOrdered(printer::print);
	}

	public static void printErrorWithPrefix(@NotNull Printer printer, @NotNull String text, @NotNull String prefix) {
		prefixLines(text, prefix).forEachOrdered(printer::printError);
	}

	public static void printWithIndentation(@NotNull Printer printer, @NotNull String text, int indentation) {
		indentLines(text, indentation).forEachOrdered(printer::print);
	}

	public static void printErrorWithIndentation(@NotNull Printer printer, @NotNull String text, int indentation) {
		indentLines(text, indentation).forEachOrdered(printer::printError);
	}
}