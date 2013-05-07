package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class Ack {
    private final List<UpdateResult> succeededUpdates;
    private final List<UpdateResult> failedUpdates;
    private final List<Paragraph> ignoredParagraphs;

    public Ack(final List<UpdateResult> updateResults, final List<Paragraph> ignoredParagraphs) {
        final List<UpdateResult> succeeded = Lists.newArrayList();
        final List<UpdateResult> failed = Lists.newArrayList();

        for (final UpdateResult updateResult : updateResults) {
            if (UpdateStatus.SUCCESS.equals(updateResult.getStatus())) {
                succeeded.add(updateResult);
            } else {
                failed.add(updateResult);
            }
        }

        this.succeededUpdates = Collections.unmodifiableList(succeeded);
        this.failedUpdates = Collections.unmodifiableList(failed);
        this.ignoredParagraphs = Collections.unmodifiableList(ignoredParagraphs);
    }

    public List<UpdateResult> getSucceededUpdates() {
        return succeededUpdates;
    }

    public List<UpdateResult> getFailedUpdates() {
        return failedUpdates;
    }

    public List<Paragraph> getIgnoredParagraphs() {
        return ignoredParagraphs;
    }

    public int getNrFound() {
        return getNrProcessedsuccessfully() + getNrProcessedErrrors();
    }

    public int getNrProcessedsuccessfully() {
        return succeededUpdates.size();
    }

    public int getNrCreate() {
        return getCount(Action.CREATE, succeededUpdates);
    }

    public int getNrUpdate() {
        return getCount(Action.MODIFY, succeededUpdates);
    }

    public int getNrDelete() {
        return getCount(Action.DELETE, succeededUpdates);
    }

    public int getNrNoop() {
        return getCount(Action.NOOP, succeededUpdates);
    }

    public int getNrProcessedErrrors() {
        return failedUpdates.size();
    }

    public int getNrCreateErrors() {
        return getCount(Action.CREATE, failedUpdates);
    }

    public int getNrUpdateErrors() {
        return getCount(Action.MODIFY, failedUpdates);
    }

    public int getNrDeleteErrors() {
        return getCount(Action.DELETE, failedUpdates);
    }

    private int getCount(final Action action, final List<UpdateResult> updateResults) {
        int count = 0;

        for (final UpdateResult updateResult : updateResults) {
            if (action.equals(updateResult.getAction())) {
                count++;
            }
        }

        return count;
    }

    public UpdateStatus getUpdateStatus() {
        if (succeededUpdates.isEmpty() || !failedUpdates.isEmpty()) {
            return UpdateStatus.FAILED;
        }

        return UpdateStatus.SUCCESS;
    }
}
