package builderb0y.autocodec.logging;

import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
TaskLogger implementation which prints tasks when they are started,
and task results (or exceptions) when tasks finish running.
printouts are given indentation based on the
number of tasks which led up to the current one.
this makes it easy to see at a glance which tasks started which other tasks.
*/
public class IndentedTaskLogger extends AbstractTaskLogger {

	public int indentation;

	public IndentedTaskLogger(@NotNull Printer printer) {
		super(printer);
	}

	public IndentedTaskLogger(@NotNull Printer printer, boolean filterStackTraces) {
		super(printer, filterStackTraces);
	}

	public IndentedTaskLogger(@NotNull ReentrantLock lock, @NotNull Printer printer, boolean filterStackTraces) {
		super(lock, printer, filterStackTraces);
	}

	@Override
	protected void doPrint(@NotNull String message) {
		printWithIndentation(this.printer, message, this.indentation);
	}

	@Override
	protected void doPrintError(@NotNull String error) {
		printErrorWithIndentation(this.printer, error, this.indentation);
	}

	@Override
	public <R, X extends Throwable> void beginTask(@NotNull LoggableTask<R, X> task) {
		super.beginTask(task);
		this.indentation++;
	}

	@Override
	public <R, X extends Throwable> void endTask(@NotNull LoggableTask<R, X> task, @Nullable R result, @Nullable Throwable throwable) {
		super.endTask(task, result, throwable);
		this.indentation--;
	}
}