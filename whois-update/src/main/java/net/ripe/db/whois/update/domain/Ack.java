package net.ripe.db.whois.update.domain;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
            switch(updateResult.getStatus()) {
                case SUCCESS:
                    succeeded.add(updateResult);
                    break;
                default:
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
        return Iterables.size(Iterables.filter(succeededUpdates, new Predicate<UpdateResult>() {
            @Override
            public boolean apply(final UpdateResult input) {
                return Action.CREATE.equals(input.getAction()) && UpdateStatus.SUCCESS.equals(input.getStatus());
            }
        }));
    }

    public int getNrUpdate() {
        return Iterables.size(Iterables.filter(succeededUpdates, new Predicate<UpdateResult>() {
            @Override
            public boolean apply(final UpdateResult input) {
                return Action.MODIFY.equals(input.getAction()) && UpdateStatus.SUCCESS.equals(input.getStatus());
            }
        }));
    }

    public int getNrDelete() {
        return Iterables.size(Iterables.filter(succeededUpdates, new Predicate<UpdateResult>() {
            @Override
            public boolean apply(final UpdateResult input) {
                return Action.DELETE.equals(input.getAction()) && UpdateStatus.SUCCESS.equals(input.getStatus());
            }
        }));
    }

    public int getNrNoop() {
        return Iterables.size(Iterables.filter(succeededUpdates, input -> Action.NOOP.equals(input.getAction())));
    }

    public int getNrProcessedErrrors() {
        return failedUpdates.size();
    }

    public int getNrCreateErrors() {
        return Iterables.size(Iterables.filter(failedUpdates, input -> Action.CREATE.equals(input.getAction())));
    }

    public int getNrUpdateErrors() {
        return Iterables.size(Iterables.filter(failedUpdates, input -> Action.MODIFY.equals(input.getAction())));
    }

    public int getNrDeleteErrors() {
        return Iterables.size(Iterables.filter(failedUpdates, input -> Action.DELETE.equals(input.getAction())));
    }

    public UpdateStatus getUpdateStatus() {
        return (succeededUpdates.isEmpty() || !failedUpdates.isEmpty()) ? UpdateStatus.FAILED : UpdateStatus.SUCCESS;
    }
}
