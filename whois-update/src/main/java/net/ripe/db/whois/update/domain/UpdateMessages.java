package net.ripe.db.whois.update.domain;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;

import java.util.Set;

import static net.ripe.db.whois.common.FormatHelper.prettyPrint;
import static net.ripe.db.whois.common.Messages.Type;

public final class UpdateMessages {
    private static final Joiner LIST_JOINED = Joiner.on(", ");

    public static String print(final Message message) {
        return prettyPrint(String.format("***%s: ", message.getType()), message.getFormattedText(), 12, 80);
    }

    private UpdateMessages() {
    }

    public static Message invalidKeywordsFound(final CharSequence subject) {
        return new Message(Type.WARNING, "Invalid keyword(s) found: %s", subject);
    }

    public static Message allKeywordsIgnored() {
        return new Message(Type.WARNING, "All keywords were ignored");
    }

    public static Message unexpectedError() {
        return new Message(Type.ERROR, "Unexpected error occurred");
    }

    public static Message filteredNotAllowed() {
        return new Message(Type.ERROR, "Cannot submit filtered whois output for updates");
    }

    public static Message unrecognizedSource(final CharSequence source) {
        return new Message(Type.ERROR, "Unrecognized source: %s", source);
    }

    public static Message operationNotAllowedForKeyword(final Keyword keyword, final Operation operation) {
        return new Message(Type.ERROR, "%s is not allowed when keyword %s is specified", operation.name(), keyword.name());
    }

    public static Message objectNotFound(final CharSequence key) {
        return new Message(Type.ERROR, "Object %s does not exist in the database", key);
    }

    public static Message objectMismatch(final CharSequence key) {
        return new Message(Type.ERROR, "Object %s doesn't match version in database", key);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message objectInUse(final RpslObject object) {
        return new Message(Type.ERROR, "Object [%s] %s is referenced from other objects", object.getType().getName().toLowerCase(), object.getKey());
    }

    public static Message nonexistantMntRef(final CharSequence organisation, final CharSequence mntRef) {
        return new Message(Type.WARNING, "Referenced organisation %s has mnt-ref attribute %s which does not exist in the database", organisation, mntRef);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message nicHandleNotAvailable(final CharSequence nicHandle) {
        return new Message(Type.ERROR, "The nic-hdl \"%s\" is not available", nicHandle);
    }

    public static Message referenceNotFound(final CharSequence reference) {
        return new Message(Type.ERROR, "Reference \"%s\" not found", reference);
    }

    public static Message invalidReference(final ObjectType objectType, final CharSequence key) {
        return new Message(Type.ERROR, "Invalid reference to [%s] %s", objectType.getName(), key);
    }

    public static Message updateIsIdentical() {
        return new Message(Type.WARNING, "Submitted object identical to database object");
    }

    public static Message unknownObjectReferenced(final CharSequence value) {
        return new Message(Type.ERROR, "Unknown object referenced %s", value);
    }

    public static Message multipleReasonsSpecified(final Operation operation) {
        return new Message(Type.ERROR, "Multiple reasons specified for %s operation", operation.name());
    }

    public static Message referencedObjectMissingAttribute(final ObjectType objectType, final CharSequence objectName, final AttributeType attributeType) {
        return new Message(Type.WARNING, "Referenced %s object %s is missing mandatory attribute \"%s:\"", objectType.getName(), objectName, attributeType.getName());
    }

    public static Message referencedObjectMissingAttribute(final ObjectType objectType, final CharSequence objectName, final ObjectType viaType, final CharSequence viaName, final AttributeType attributeType) {
        return new Message(Type.WARNING, "Referenced %s object %s from %s: %s is missing mandatory attribute \"%s:\"", objectType.getName(), objectName, viaType.getName(), viaName, attributeType.getName());
    }

    public static Message invalidIpv4Address(final CharSequence value) {
        return new Message(Type.ERROR, "%s is not a valid IPv4 address", value);
    }

    public static Message invalidIpv6Address(final CharSequence value) {
        return new Message(Type.ERROR, "%s is not a valid IPv6 address", value);
    }

    public static Message invalidRouteRange(final CharSequence value) {
        return new Message(Type.ERROR, "%s is outside the range of this object", value);
    }

    public static Message invalidRoutePrefix(final CharSequence type) {
        return new Message(Type.ERROR, "Automatic creation of %s objects of this size in not allowed, please contact lir-help@ripe.net for further information.", type);
    }

    public static Message invalidDateFormat() {
        return new Message(Type.ERROR, "Invalid date format, expected YYYYMMDD");
    }

    public static Message invalidDate(final CharSequence date) {
        return new Message(Type.ERROR, "Date is older than the database itself in changed: attribute \"%s\"", date);
    }

    public static Message dateTooFuturistic(final CharSequence date) {
        return new Message(Type.ERROR, "Date is in the future in changed: attribute \"%s\"", date);
    }

    public static Message multipleMissingChangeDates() {
        return new Message(Type.ERROR, "More than one \"changed:\" attribute without date");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authenticationFailed(final RpslObject object, final AttributeType attributeType, final Iterable<RpslObject> candidates) {
        final CharSequence joined = LIST_JOINED.join(
                Iterables.transform(candidates, new Function<RpslObject, CharSequence>() {
                    @Override
                    public CharSequence apply(final RpslObject input) {
                        return input == null ? "" : input.getKey();
                    }
                }));

        return new Message(Type.ERROR, "" +
                "Authorisation for [%s] %s failed\n" +
                "using \"%s:\"\n" +
                (joined.length() == 0 ?
                        "no valid maintainer found\n" :
                        "not authenticated by: %s"),
                object.getType().getName(),
                object.getKey(),
                attributeType.getName(),
                joined);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message parentAuthenticationFailed(final RpslObject object, final AttributeType attributeType, final Iterable<RpslObject> candidates) {
        final CharSequence joined = LIST_JOINED.join(
                Iterables.transform(candidates, new Function<RpslObject, CharSequence>() {
                    @Override
                    public CharSequence apply(final RpslObject input) {
                        return input == null ? "" : input.getKey();
                    }
                }));

        return new Message(Type.ERROR, "" +
                "Authorisation for parent [%s] %s failed\n" +
                "using \"%s:\"\n" +
                (joined.length() == 0 ?
                        "no valid maintainer found\n" :
                        "not authenticated by: %s"),
                object.getType().getName(),
                object.getKey(),
                attributeType.getName(),
                joined);
    }

    public static Message reservedNameUsed() {
        return new Message(Type.ERROR, "Reserved name used");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message newKeywordAndObjectExists() {
        return new Message(Type.ERROR, "Enforced new keyword specified, but the object already exists in the database");
    }

    public static Message invalidMaintainerForOrganisationType() {
        return new Message(Type.ERROR, "This org-type value can only be set by administrative mntners");
    }

    public static Message cantChangeOrgAttribute() {
        return new Message(Type.ERROR, "The org attribute value can only be set by administrative mntners");
    }

    public static Message cantChangeOrgName() {
        return new Message(Type.ERROR, "The org name can only be set by administrative mntners");
    }

    public static Message countryNotRecognised(final CharSequence country) {
        return new Message(Type.ERROR, "Country not recognised: %s", country);
    }

    public static Message asblockIsMaintainedByRipe() {
        return new Message(Type.ERROR, "As-block object are maintained by RIPE NCC");
    }

    public static Message asblockParentAlreadyExists() {
        return new Message(Type.ERROR, "Parent As-block already exists");
    }

    public static Message asblockAlreadyExists() {
        return new Message(Type.ERROR, "Parent As-block already exists");
    }

    public static Message asblockChildAlreadyExists() {
        return new Message(Type.ERROR, "Child As-block already exists");
    }

    public static Message intersectingAsblockAlreadyExists() {
        return new Message(Type.ERROR, "Overlapping As-block already exists");
    }

    public static Message languageNotRecognised(final CharSequence language) {
        return new Message(Type.ERROR, "Language not recognised: %s", language);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message nameChanged() {
        return new Message(Type.ERROR, "" +
                "Person/Role name cannot be changed automatically. " +
                "Please create another Person/Role object and modify any references " +
                "to the old object, then delete the old object");
    }

    public static Message statusChange() {
        return new Message(Type.ERROR, "status value cannot be changed, you must delete and re-create the object");
    }

    public static Message adminMaintainerRemoved() {
        return new Message(Type.ERROR, "Cannot remove the administrative maintainer");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForSetStatus(final CharSequence status) {
        return new Message(Type.ERROR, "Setting status %s requires administrative authorisation", status);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForFirstAttrChange(final AttributeType attributeType) {
        return new Message(Type.ERROR, "Changing first \"%s:\" value requires administrative authorisation", attributeType);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForAttrChange(final AttributeType attributeType) {
        return new Message(Type.ERROR, "Changing \"%s:\" value requires administrative authorisation", attributeType.getName());
    }

    public static Message orgAttributeMissing() {
        return new Message(Type.ERROR, "Missing required \"org:\" attribute");
    }

    public static Message wrongOrgType(final Set<OrgType> allowedOrgTypes) {
        return new Message(Type.ERROR, "" +
                "Referenced organisation has wrong \"org-type\".\n" +
                "Allowed values are %s", allowedOrgTypes);
    }

    // TODO: [DW] this error should specify that this specific parent-child status in not allowed, similar to incorrectChildStatus()
    public static Message incorrectParentStatus(final ObjectType type, final CharSequence parentStatus) {
        return new Message(Messages.Type.ERROR, "%s parent has incorrect status: %s", type.getName(), parentStatus);
    }

    public static Message incorrectChildStatus(final CharSequence givenStatus, final CharSequence childStatus) {
        return new Message(Type.ERROR, "Status %s not allowed when more specific object has status %s", givenStatus, childStatus);
    }

    public static Message objectLacksStatus(final CharSequence familyMember, final CharSequence parentInetnum) {
        return new Message(Type.ERROR, "%s %s does not have \"status:\"", familyMember, parentInetnum);
    }

    public static Message objectHasInvalidStatus(final CharSequence familyMember, final CharSequence parentInetnum, final CharSequence status) {
        return new Message(Type.ERROR, "%s %s has invalid status: %s", familyMember, parentInetnum, status);
    }

    public static Message intersectingRange(final Interval<?> intersectingRange) {
        return new Message(Type.ERROR, "This range overlaps with %s", intervalToString(intersectingRange));
    }

    private static CharSequence intervalToString(final Interval<?> interval) {
        if (interval instanceof Ipv4Resource) {
            return ((Ipv4Resource) interval).toRangeString();
        }

        return interval.toString();
    }

    public static Message rangeTooSmallForStatus(final InetStatus inetStatus, final int maxPrefixLength) {
        return new Message(Type.ERROR, "%s cannot be smaller than /%s", inetStatus.toString(), maxPrefixLength);
    }

    public static Message createFirstPersonMntnerForOrganisation() {
        return new Message(Type.INFO, "To create the first person/mntner pair of objects for an organisation see https://apps.db.ripe.net/startup/");
    }

    public static Message maintainerNotFound(final CharSequence maintainer) {
        return new Message(Type.ERROR, "The maintainer '%s' was not found in the database", maintainer);
    }

    public static Message cantChangeAssignmentSize() {
        return new Message(Type.ERROR, "\"assignment-size:\" value cannot be changed");
    }

    public static Message attributeAssignmentSizeNotAllowed() {
        return new Message(Type.ERROR, "\"assignment-size:\" attribute only allowed with status %s", Inet6numStatus.AGGREGATED_BY_LIR.getLiteralStatus());
    }

    public static Message assignmentSizeTooSmall(final int prefixLength) {
        return new Message(Type.ERROR, "\"assignment-size:\" value must be greater than prefix size %s", prefixLength);
    }

    public static Message assignmentSizeTooLarge(final int prefixLength) {
        return new Message(Type.ERROR, "\"assignment-size:\" value must not be greater than the maximum prefix size %s", prefixLength);
    }

    public static Message tooManyAggregatedByLirInHierarchy() {
        return new Message(Type.ERROR, "Only two levels of hierarchy allowed with status AGGREGATED-BY-LIR");
    }

    public static Message statusRequiresAuthorization(final CharSequence currentStatus) {
        return new Message(Type.ERROR, "Status %s can only be created by the database administrator", currentStatus);
    }

    public static Message deleteWithStatusRequiresAuthorization(final CharSequence currentStatus) {
        return new Message(Type.ERROR, "Object with status %s can only be deleted by the database administrator", currentStatus);
    }

    public static Message invalidChildPrefixLength() {
        return new Message(Type.ERROR, "More specific objects exist that do not match assignment-size");
    }

    public static Message invalidParentEntryForInterval(final IpInterval s) {
        return new Message(Type.ERROR, "Interval %s must have exactly one parent", s);
    }

    public static Message invalidPrefixLength(final IpInterval ipInterval, final int assignmentSize) {
        return new Message(Type.ERROR, "Prefix length for %s must be %s", ipInterval, assignmentSize);
    }

    public static Message membersNotSupportedInReferencedSet(final CharSequence asName) {
        return new Message(Type.ERROR, "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set %s", asName);
    }

    public static Message dnsCheckTimeout() {
        return new Message(Type.ERROR, "Timeout performing DNS check");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForEnumDomain() {
        return new Message(Type.ERROR, "Creating enum domain requires administrative authorisation");
    }

    public static Message lessSpecificDomainFound(final CharSequence existing) {
        return new Message(Type.ERROR, "Existing less specific domain object found %s", existing);
    }

    public static Message moreSpecificDomainFound(final CharSequence existing) {
        return new Message(Type.ERROR, "Existing more specific domain object found %s", existing);
    }

    public static Message hostNameMustEndWith(final CharSequence s) {
        return new Message(Type.ERROR, "Glue records only allowed if hostname ends with %s", s);
    }

    public static Message glueRecordMandatory(final CharSequence s) {
        return new Message(Type.ERROR, "Glue record is mandatory if hostname ends with %s", s);
    }

    public static Message invalidGlueForEnumDomain(final CharSequence s) {
        return new Message(Type.ERROR, "Enum domain has invalid glue %s", s);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForDeleteRsMaintainedObject() {
        return new Message(Type.ERROR, "Deleting this object requires administrative authorisation");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForChangingRipeMaintainer() {
        return new Message(Type.ERROR, "Adding or removing a RIPE NCC maintainer requires administrative authorisation");
    }

    public static Message poemRequiresPublicMaintainer() {
        return new Message(Type.ERROR, "Poem must be maintained by 'LIM-MNT', which has a public password");
    }

    public static Message tooManyPasswordsSpecified() {
        return new Message(Type.ERROR, "Too many passwords specified");
    }

    public static Message overrideIgnored() {
        return new Message(Type.WARNING, "An override password was found not attached to an object and was ignored");
    }

    public static Message noValidUpdateFound() {
        return new Message(Type.ERROR, "No valid update found");
    }

    public static Message multipleOverridePasswords() {
        return new Message(Type.ERROR, "Multiple override passwords used");
    }

    public static Message overrideNotAllowedForOrigin(final Origin origin) {
        return new Message(Type.ERROR, "Override not allowed in %s", origin.getName());
    }

    public static Message overrideOnlyAllowedByDbAdmins() {
        return new Message(Type.ERROR, "Override only allowed by database administrators");
    }

    public static Message overrideAuthenticationUsed() {
        return new Message(Type.INFO, "Authorisation override used");
    }

    public static Message overrideAuthenticationFailed() {
        return new Message(Type.ERROR, "Override authentication failed");
    }

    public static Message overrideOptionInvalid(final CharSequence option) {
        return new Message(Type.ERROR, "Invalid override option: %s", option);
    }

    public static Message overrideOriginalNotFound(final int id) {
        return new Message(Type.ERROR, "Original object with id %s specified in override not found", id);
    }

    public static Message overrideOriginalMismatch(final int id, final ObjectType type, final CharSequence key) {
        return new Message(Type.ERROR, "Original object with id %s does not match supplied key [%s] %s", id, type.getName(), key);
    }

    public static Message ripeMntnerUpdatesOnlyAllowedFromWithinNetwork() {
        return new Message(Type.ERROR, "Authentication by RIPE NCC maintainers only allowed from within the RIPE NCC network");
    }

    public static Message parentObjectNotFound(final CharSequence parent) {
        return new Message(Type.ERROR, "Parent object %s not found", parent);
    }

    public static Message autokeyAlreadyDefined(final CharSequence value) {
        return new Message(Type.ERROR, "Key %s already used (AUTO-nnn must be unique per update message)", value);
    }

    public static Message autokeyForX509KeyCertsOnly() {
        return new Message(Type.ERROR, "AUTO-nnn can only be used with X509 key-cert");
    }

    public static Message noParentAsBlockFound(final CharSequence asNumber) {
        return new Message(Type.ERROR, "No parent as-block found for %s", asNumber);
    }

    public static Message certificateNotYetValid(final CharSequence name) {
        return new Message(Type.WARNING, "Certificate in keycert %s is not yet valid", name);
    }

    public static Message certificateHasExpired(final CharSequence name) {
        return new Message(Type.WARNING, "Certificate in keycert %s has expired", name);
    }

    public static Message publicKeyHasExpired(final CharSequence name) {
        return new Message(Type.WARNING, "Public key in keycert %s has expired", name);
    }

    public static Message messageSignedMoreThanOneWeekAgo() {
        return new Message(Type.WARNING, "Message was signed more than one week ago");
    }

    public static Message eitherSimpleOrComplex(final ObjectType objectType, final CharSequence simple, final CharSequence complex) {
        return new Message(Type.ERROR, "A %s object cannot contain both %s and %s attributes", objectType.getName(), simple, complex);
    }

    public static Message neitherSimpleOrComplex(final ObjectType objectType, final CharSequence simple, final CharSequence complex) {
        return new Message(Type.ERROR, "A %s object must contain either %s or %s attribute", objectType.getName(), simple, complex);
    }

    public static Message diffNotSupported() {
        return new Message(Type.WARNING, "The DIFF keyword is not supported.");
    }

    public static Message abuseMailboxRequired(final CharSequence key) {
        return new Message(Type.ERROR,
                "The \"abuse-c\" ROLE object '%s' has no \"abuse-mailbox:\"\n"
                        + "Add \"abuse-mailbox:\" to the ROLE object, then update the ORGANISATION object", key);
    }

    public static Message abuseCPersonReference() {
        return new Message(Type.ERROR,
                "\"abuse-c:\" references a PERSON object\n"
                        + "This must reference a ROLE object with an \"abuse-mailbox:\"");
    }

    public static Message abuseMailboxReferenced(final CharSequence role) {
        return new Message(Type.ERROR, "There is an organisation referencing role %s's abuse-mailbox", role);
    }

    public static Message keyNotFound(final String keyId) {
        return new Message(Type.WARNING, "The key-cert object %s does not exist", keyId);
    }

    public static Message keyInvalid(final String keyId) {
        return new Message(Type.WARNING, "The public key data held in the key-cert object %s has syntax errors", keyId);
    }

    public static Message abuseCNoLimitWarning() {
        return new Message(Type.WARNING, "There are no limits on queries for ROLE objects containing \"abuse-mailbox:\"");
    }

    public static Message selfReferenceError(final AttributeType attributeType) {
        return new Message(Type.ERROR, "Self reference is not allowed for attribute type \"%s\"", attributeType.getName());
    }

    public static Message commentInSourceNotAllowed() {
        return new Message(Type.ERROR, "End of line comments not allowed on \"source:\" attribute");
    }

    public static Message updatePendingAuthentication() {
        return new Message(Type.WARNING, "This update has only passed one of the two required hierarchical authorisations");
    }

    public static Message updatePendingAuthenticationSaved(final RpslObject rpslObject) {
        return new Message(Type.INFO, "The %s object %s will be saved for one week pending the second authorisation", rpslObject.getType().getName(), rpslObject.getKey());
    }

    public static Message updateAlreadyPendingAuthentication() {
        return new Message(Type.ERROR, "There is already an identical update pending authentication");
    }

    public static Message updateConcludesPendingUpdate(final RpslObject rpslObject) {
        return new Message(Type.INFO, "This update concludes a pending update on %s %s", rpslObject.getType().getName(), rpslObject.getKey());
    }

    public static Message dryRunOnlySupportedOnSingleUpdate() {
        return new Message(Type.ERROR, "Dry-run is only supported when a single update is specified");
    }

    public static Message dryRunNotice() {
        return new Message(Type.INFO, "Dry-run performed, no changes to the database have been made");
    }

    public static Message ripeAccessAccountUnavailable(final CharSequence username) {
        return new Message(Type.ERROR, "No RIPE NCC Access Account found for %s", username);
    }

    public static Message ripeAccessServerUnavailable() {
        return new Message(Type.ERROR, "RIPE NCC Access server is unavailable");
    }

    public static Message statusCannotBeRemoved(final CharSequence status) {
        return new Message(Type.ERROR, "Status %s cannot be removed.", status);
    }

    public static Message statusCannotBeAdded(final CharSequence status) {
        return new Message(Type.ERROR, "Status %s can only be added by a RIPE NCC maintainer.", status);
    }
}
