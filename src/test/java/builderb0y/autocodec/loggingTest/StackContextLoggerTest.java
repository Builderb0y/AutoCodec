package builderb0y.autocodec.loggingTest;

import org.junit.Test;

import builderb0y.autocodec.logging.LoggableTask;
import builderb0y.autocodec.logging.Printer;
import builderb0y.autocodec.logging.StackContextLogger;
import builderb0y.autocodec.logging.TaskLogger;

public class StackContextLoggerTest {

	public static TaskLogger LOGGER = new StackContextLogger(Printer.SYSTEM, true);

	@Test
	public void testBasic() {
		try {
			LOGGER.runTask(new RecursiveTask(() -> {
				try {
					LOGGER.logMessage("Starting first error");
					LOGGER.runTask(new RecursiveTask(() -> {
						throw new RuntimeException("1");
					}));
				}
				catch (RuntimeException expected) {
					LOGGER.logMessage("Caught first error");
				}

				try {
					LOGGER.logMessage("Starting second error");
					LOGGER.runTask(new RecursiveTask(() -> {
						throw new RuntimeException("2");
					}));
				}
				catch (RuntimeException expected) {
					LOGGER.logMessage("caught second error");
				}

				LOGGER.logError("some");
				LOGGER.logError("more");
				LOGGER.logError("errors");
				LOGGER.logError("here");
				LOGGER.logMessage("last message which shouldn't be shown anywhere.");
			}));
		}
		catch (RuntimeException expected) {}
		//assertNull(LOGGER.firstFrame);
		//assertNull(LOGGER.currentFrame);
		//assertNull(LOGGER.errorFrame);
	}

	public static class RecursiveTask extends LoggableTask<Void, RuntimeException> {

		public Runnable whenDone;

		public RecursiveTask(Runnable whenDone) {
			this.whenDone = whenDone;
		}

		@Override
		public Void run() {
			this.whenDone.run();
			return null;
		}

		@Override
		public String toString() {
			return "RecursiveTask";
		}
	}
}