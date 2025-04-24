package me.sailex.secondbrain.thread;

import java.util.concurrent.CountDownLatch;

import me.sailex.secondbrain.model.Goal;
import me.sailex.secondbrain.util.LogUtil;

public class GoalThread extends Thread {

    private final Runnable goalTask;
    private final String goalName;
    private final CountDownLatch completionLatch;

    /**
     * Creates a new thread to execute a specific goal
     *
     * @param goal The goal to execute
     */
    public GoalThread(Goal goal) {
        super("NPCGoal-" + goal.name());
        this.goalName = goal.name();
        this.goalTask = goal.task();
        this.completionLatch = new CountDownLatch(1);
        setDaemon(true);
    }

    /**
     * Creates an already completed thread (initial state)
     */
    public GoalThread() {
        super("NPCGoal-Initial");
        this.goalTask = () -> {};
        this.goalName = null;
        this.completionLatch = new CountDownLatch(0);
    }

    @Override
    public void run() {
        try {
            goalTask.run();
        } catch (Exception e) {
            LogUtil.error(("Error executing goal %s").formatted(goalName), e);
        } finally {
            completionLatch.countDown();
        }
    }

    /**
     * Check if the goal has completed execution
     *
     * @return true if the goal has finished executing
     */
    public boolean isCompleted() {
        return completionLatch.getCount() == 0;
    }
}
