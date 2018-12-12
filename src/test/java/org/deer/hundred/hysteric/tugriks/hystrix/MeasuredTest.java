package org.deer.hundred.hysteric.tugriks.hystrix;

public class MeasuredTest {

  private final DoStuff toRun;

  private MeasuredTest(DoStuff toRun) {
    this.toRun = toRun;
  }

  public static MeasuredTest measure(DoStuff toRun) {
    return new MeasuredTest(toRun);
  }

  public long run() {
    final long start = System.currentTimeMillis();
    toRun.doStuff();
    return System.currentTimeMillis() - start;
  }

  @FunctionalInterface
  public interface DoStuff {

    void doStuff();
  }
}
