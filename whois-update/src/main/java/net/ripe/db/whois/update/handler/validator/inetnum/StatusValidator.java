package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.Messages.Type.WARNING;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.update.domain.Action.DELETE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;
import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

// TODO [SB] This class is over complex and needs to be split into seperate validators for easier overview/maintenance
// TODO probably something like specific validators for inetnum/inet6num, create/modify/delete combinations and seperate validator(s) for the authorisation checks
@Component
public class StatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(MODIFY, DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(INETNUM, ObjectType.INET6NUM);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public StatusValidator(
            final RpslObjectDao objectDao,
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getAction() == DELETE) {
            if (update.getType().equals(INETNUM)) {
                validateDelete(update, updateContext, ipv4Tree);
            } else {
                validateDelete(update, updateContext, ipv6Tree);
            }
        } else {
            validateModify(update, updateContext);
        }
    }

    private boolean authByRsOrOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER) || hasAuthOverride(subject);
    }

    private boolean hasAuthOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER);
    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString originalStatus = update.getReferenceObject() != null ? update.getReferenceObject().getValueForAttribute(STATUS) : null;
        final CIString updateStatus = update.getUpdatedObject() != null ? update.getUpdatedObject().getValueForAttribute(STATUS) : null;

        if (!Objects.equals(originalStatus, updateStatus)) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.statusChange());
            }
        }

        addHierarchyWarnings(update.getUpdatedObject(), updateContext, update);
    }

    private void validateDelete(final PreparedUpdate update, final UpdateContext updateContext, final IpTree ipTree) {
        InetStatus status;

        if (update.getReferenceObject() == null) {
            return;
        }

        try {
            status = getStatus(update.getReferenceObject());
            if (status == null) {
                // invalid status attribute value
                return;
            }
        } catch (IllegalArgumentException e) {
            // status attribute not found
            return;
        }

        if (status.requiresRsMaintainer()) {
            final Set<CIString> mntBy = update.getReferenceObject().getValuesForAttribute(AttributeType.MNT_BY);
            if (!maintainers.isRsMaintainer(mntBy)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.deleteWithStatusRequiresAuthorization(status.toString()));
                }
            }
        }

        if (update.getReferenceObject().getType().equals(INETNUM)) {
            final IpInterval ipInterval = IpInterval.parse(update.getReferenceObject().getKey());
            final List<IpEntry> parents = ipTree.findFirstLessSpecific(ipInterval);
            if (parents.size() != 1) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipInterval));
                }
                return;
            }
            validateStatusLegacy(update.getReferenceObject(), objectDao.getById(parents.get(0).getObjectId()), update, updateContext);
        }

        addHierarchyWarnings(update.getReferenceObject(), updateContext, update);
    }

    private void validateStatusLegacy(final RpslObject updatedObject, final RpslObject parentObject, final PreparedUpdate update, final UpdateContext updateContext) {
        if (updatedObject.getValueForAttribute(STATUS).equals(InetnumStatus.LEGACY.toString()) &&
                !parentObject.getValueForAttribute(STATUS).equals(InetnumStatus.LEGACY.toString())) {
            if (!authByRsOrOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.inetnumStatusLegacy());
            }
        }
    }

    private void addHierarchyWarnings(final RpslObject rpslObject,
                                      final UpdateContext updateContext,
                                      final PreparedUpdate update) {
        if (rpslObject.getType() == INETNUM) {
            ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(rpslObject.getKey())).stream()
                    .findFirst()
                    .map(entry -> objectDao.getById(entry.getObjectId()))
                    .ifPresent(parent -> {
                        // TODO: [ES] use status index to lookup status of children (don't load entire objects)
                        final List<RpslObject> children = ipv4Tree.findFirstMoreSpecific(Ipv4Resource.parse(rpslObject.getKey())).stream()
                                .map(entry -> objectDao.getById(entry.getObjectId()))
                                .collect(Collectors.toList());

                        if (update.getAction() == MODIFY) {
                            // check modified object against its parent
                            addWarningIfHierarchyInvalid(parent, rpslObject, updateContext, update, true);
                            // checked modified object against its children
                            children.forEach(child -> addWarningIfHierarchyInvalid(rpslObject, child, updateContext, update, false));
                        } else {
                            // check object being deleted's parent against object being deleted's children
                            children.forEach(child -> addWarningIfHierarchyInvalid(parent, child, updateContext, update, false));
                        }
                    });
        }
    }

    private void addWarningIfHierarchyInvalid(final RpslObject parent,
                                              final RpslObject child,
                                              final UpdateContext updateContext,
                                              final PreparedUpdate update,
                                              final boolean parentMessage) {
        if (parent.getType() == INETNUM) {
            final InetnumStatus parentStatus = InetnumStatus.getStatusFor(parent.getValueForAttribute(STATUS));
            final InetnumStatus childStatus = InetnumStatus.getStatusFor(child.getValueForAttribute(STATUS));
            if (!childStatus.worksWithParentStatus(parentStatus, false)) {

                if (parentMessage) {
                    updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(WARNING, child.getType(), parentStatus.toString()));
                } else {
                    updateContext.addMessage(update, UpdateMessages.incorrectChildStatus(WARNING, parentStatus.toString(), childStatus.toString(), child.getKey()));
                }

            }
        }
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

}
