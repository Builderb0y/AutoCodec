package builderb0y.autocodec.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.AutoCodecUtil;

/**
quite possibly the most useful TaskLogger implementation,
and the one I'm most proud of:
when an error happens (triggered by {@link #logError(Object)}),
this logger will print out the error and all tasks that led up to that error.
if no errors happen, this logger will not print anything.
this logger also tries to be conservative about calling {@link Object#toString()}
on random things unless there is something to actually print.
as such, it should be decently fast when no errors are occurring.
*/
public class StackContextLogger extends AbstractTaskLogger {

	public @Nullable TaskFrame firstFrame, currentFrame;

	public StackContextLogger(@NotNull Printer printer) {
		super(printer);
	}

	public StackContextLogger(@NotNull Printer printer, boolean filterStackTraces) {
		super(printer, filterStackTraces);
	}

	public StackContextLogger(@NotNull ReentrantLock lock, @NotNull Printer printer, boolean filterStackTraces) {
		super(lock, printer, filterStackTraces);
	}

	@Override
	protected void doPrint(@NotNull String message) {
		throw new UnsupportedOperationException("Call logMessage() instead.");
	}

	@Override
	protected void doPrintError(@NotNull String error) {
		throw new UnsupportedOperationException("Call logError() instead.");
	}

	@Override
	public <R, X extends Throwable> void beginTask(@NotNull LoggableTask<R, X> task) {
		if (this.currentFrame != null) {
			this.currentFrame = this.currentFrame.pushNew(task);
		}
		else {
			this.firstFrame = this.currentFrame = new TaskFrame(task, 0);
		}
	}

	public @NotNull TaskFrame currentFrame() {
		if (this.currentFrame != null) return this.currentFrame;
		else throw new IllegalStateException("No task started.");
	}

	@Override
	public <R, X extends Throwable> void endTask(@NotNull LoggableTask<R, X> task, @Nullable R result, @Nullable Throwable throwable) {
		TaskFrame current = this.currentFrame();
		try {
			if (throwable != null) {
				this.logError(throwable);
			}
			else {
				this.logMessage(String.valueOf(result));
			}
		}
		finally {
			this.currentFrame = current.popOld(task);
			if (this.currentFrame == null) {
				this.firstFrame = null;
				this.seenThrowables.clear();
			}
		}
	}

	@Override
	public void logMessage(@NotNull Object message) {
		this.currentFrame().addMessage(message);
	}

	@Override
	public void logError(@NotNull Object message) {
		for (
			TaskFrame frame = this.firstFrame, fence = this.currentFrame().next;
			frame != fence;
			frame = frame.next
		) {
			frame.print(this.printer);
		}
		String toPrint;
		if (message instanceof Throwable throwable && this.maybeFilterStackTrace(throwable)) {
			toPrint = stackTraceToString(throwable);
		}
		else {
			toPrint = AutoCodecUtil.deepToString(message);
		}
		printErrorWithIndentation(this.printer, toPrint, this.currentFrame().depth + 1);
	}

	public static class TaskFrame {

		public @Nullable LoggableTask<?, ?> task;
		public int depth;
		public @Nullable List<@NotNull Object> messages;
		public boolean printed;
		public @Nullable TaskFrame prev, next;

		public TaskFrame(@NotNull LoggableTask<?, ?> task, int depth) {
			this.task = task;
			this.depth = depth;
		}

		public @NotNull TaskFrame pushNew(@NotNull LoggableTask<?, ?> task) {
			TaskFrame frame = this.next;
			if (frame != null) {
				frame.push(task);
			}
			else {
				frame = new TaskFrame(task, this.depth + 1);
				frame.prev = this;
				this.next = frame;
			}
			return frame;
		}

		public void push(@NotNull LoggableTask<?, ?> task) {
			if (this.task != null) {
				throw new IllegalStateException("TaskFrame already in use by " + this.task + " while pushing " + task);
			}
			this.task = task;
		}

		public @Nullable TaskFrame popOld(@NotNull LoggableTask<?, ?> task) {
			this.pop(task);
			return this.prev;
		}

		public void pop(@NotNull LoggableTask<?, ?> task) {
			if (task != this.task) {
				throw new IllegalStateException("Task stack corrupted: expected " + this.task + ", got " + task);
			}
			this.task = null;
			if (this.messages != null) this.messages.clear();
			this.printed = false;
		}

		public void addMessage(@NotNull Object message) {
			if (this.messages == null) this.messages = new ArrayList<>(4);
			this.messages.add(message);
		}

		public void print(@NotNull Printer printer) {
			if (!this.printed) {
				printWithIndentation(printer, String.valueOf(this.task), this.depth);
				this.printed = true;
			}
			List<Object> messages = this.messages;
			if (messages != null) {
				String prefix = "\t".repeat(this.depth + 1);
				int size = messages.size();
				for (int index = 0; index < size; index++) {
					printWithPrefix(printer, AutoCodecUtil.deepToString(messages.get(index)), prefix);
				}
				messages.clear();
			}
		}

		@Override
		public @NotNull String toString() {
			return String.valueOf(this.task);
		}
	}
}