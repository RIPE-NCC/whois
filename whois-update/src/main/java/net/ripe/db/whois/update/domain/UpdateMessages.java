package net.ripe.db.whois.update.domain;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.MessageWithAttribute;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.mail.EmailStatusType;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;

import java.util.Collection;
import java.util.Set;

import static net.ripe.db.whois.common.FormatHelper.prettyPrint;
import static net.ripe.db.whois.common.Messages.Type;

public final class UpdateMessages {
    private static final Joiner LIST_JOINED = Joiner.on(", ");

    public static String print(final Message message) {
        return prettyPrint(String.format("***%s: ", message.getType()), message.getFormattedText(), 12, 80);
    }

    private UpdateMessages() {
        // do not instantiate
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

    public static Message httpSyncupdate(){
        return new Message(Type.WARNING, "This Syncupdates request used insecure HTTP, which will be removed in a future release. Please switch to HTTPS.");
    }

    public static Message invalidReference(final ObjectType objectType, final CharSequence key) {
        return new Message(Type.ERROR, "Invalid reference to [%s] %s", objectType.getName(), key);
    }

    public static Message updateIsIdentical() {
        return new Message(Type.WARNING, "Submitted object identical to database object");
    }

    public static Message unknownObjectReferenced(final RpslAttribute attribute, final CharSequence value) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Unknown object referenced %s", value);
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

    public static Message invalidOauthAudience(final String authType) {
        return new Message(Type.WARNING, "The %s cannot be used because it was created for a different application or environment", authType);
    }

    public static Message apiKeyGettingExpired(final String apiKeyId, final String expiresAt) {
        return new Message(Type.WARNING, "API KeyId %s is due to expire on %s", apiKeyId, expiresAt);
    }

    public static Message invalidIpv4Address(final RpslAttribute attribute, final CharSequence value) {
        return new MessageWithAttribute(Type.ERROR, attribute, "%s is not a valid IPv4 address", value);
    }

    public static Message invalidIpv6Address(final RpslAttribute attribute, final CharSequence value) {
        return new MessageWithAttribute(Type.ERROR, attribute,"%s is not a valid IPv6 address", value);
    }

    public static Message invalidIpv4Address(final CharSequence value) {
        return new Message(Type.ERROR, "%s is not a valid IPv4 address", value);
    }

    public static Message invalidIpv6Address(final CharSequence value) {
        return new Message(Type.ERROR,"%s is not a valid IPv6 address", value);
    }

    public static Message invalidRouteRange(final RpslAttribute attribute, final CharSequence value) {
        return new MessageWithAttribute(Type.ERROR, attribute, "%s is outside the range of this object", value);
    }

    public static Message invalidRoutePrefix(final CharSequence type) {
        return new Message(Type.ERROR, "Automatic creation of %s objects of this size in not allowed, please contact lir-help@ripe.net for further information.", type);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authenticationFailed(final RpslObject object, final AttributeType attributeType, final Iterable<RpslObject> candidates) {
        final CharSequence joined = LIST_JOINED.join(
                Iterables.transform(candidates,input -> input == null ? "" : input.getKey()));

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
                Iterables.transform(candidates, input -> input == null ? "" : input.getKey()));

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

    public static Message reservedNameUsed(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Reserved name used");
    }

    public static Message reservedNameUsed(final CharSequence name) {
        return new Message(Type.ERROR, "Reserved name '%s' used", name);
    }

    public static Message reservedPrefixUsed(final CharSequence prefix, final ObjectType type) {
        return new Message(Type.ERROR, "Names starting with '%s' are reserved for '%s'.", prefix, type.getName());
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message newKeywordAndObjectExists() {
        return new Message(Type.ERROR, "Enforced new keyword specified, but the object already exists in the database");
    }

    public static Message invalidMaintainerForOrganisationType(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Value '%s' can only be set by the RIPE NCC for this organisation.", attribute.getCleanValue());
    }

    public static Message invalidMaintainerName(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR,attribute, "When creating a MNTNER the name must end with an -MNT suffix");
    }

    public static Message cantChangeOrgAttribute(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Referenced organisation can only be changed by the RIPE NCC for this resource.\n" +
                "Please contact \"ncc@ripe.net\" to change this reference.");
    }

    public static Message cantRemoveOrgAttribute() {
        return new Message(Type.ERROR, "Referenced organisation can only be removed by the RIPE NCC for this resource.\n" +
                "Please contact \"ncc@ripe.net\" to remove this reference.");
    }

    public static Message cantAddorRemoveRipeNccRemarks() {
        return new Message(Type.ERROR, "The \"remarks\" attribute can only be added or removed by the RIPE NCC");
    }

    public static Message cantCreateShortFormatAsName() {
        return new Message(Type.ERROR, "Cannot create AS-SET object with a short format name. Only hierarchical " +
                "AS-SET creation is allowed, i.e. at least one ASN must be referenced");
    }

    public static Message countryNotRecognised(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Country not recognised: %s", attribute.getCleanValue());
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
    public static Message authorisationRequiredForAttrChange(final AttributeType attributeType) {
        return new Message(Type.ERROR, "Changing \"%s:\" value requires administrative authorisation", attributeType.getName());
    }

    public static Message attributeNotAllowedWithStatus(final AttributeType attributeType, final CIString statusValue) {
        return new Message(Type.ERROR, "\"%s:\" attribute not allowed for resources with \"%s:\" status", attributeType.getName(), statusValue);
    }

    public static Message canOnlyBeChangedByRipeNCC(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Attribute \"%s:\" can only be changed by the RIPE NCC for this object.\n" +
                "Please contact \"ncc@ripe.net\" to change it.", attribute.getType().getName());
    }

    public static Message canOnlyBeChangedByRipeNCC(final AttributeType attributeType) {
        return new Message(Type.ERROR,"Attribute \"%s:\" can only be changed by the RIPE NCC for this object.\n" +
                "Please contact \"ncc@ripe.net\" to change it.", attributeType.getName());
    }

    public static Message canNotAddCommentsInManagedAttr(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Comments are not allowed on RIPE NCC managed Attribute \"%s:\"" , attribute.getType().getName());
    }

    public static Message canOnlyBeChangedinLirPortal(final AttributeType attributeType) {
        return new Message(Type.ERROR,
            "Attribute \"%s:\" can only be changed via the LIR portal.\n" +
            "Please login to https://lirportal.ripe.net and select\n" +
            "\"LIR Account\" under \"My LIR\" to change it.", attributeType.getName());
    }

    public static Message orgAttributeMissing() {
        return new Message(Type.ERROR, "Missing required \"org:\" attribute");
    }

    public static Message wrongOrgType(final Set<OrgType> allowedOrgTypes) {
        return new Message(Type.ERROR, "" +
                "Referenced organisation has wrong \"org-type\".\n" +
                "Allowed values are %s", allowedOrgTypes);
    }

    public static Message incorrectParentStatus(final Messages.Type messageType, final ObjectType type, final CharSequence parentStatus) {
        return new Message(messageType, "%s parent has incorrect status: %s", type.getName(), parentStatus);
    }

    public static Message incorrectChildStatus(final Messages.Type messageType, final CharSequence givenStatus, final CharSequence childStatus, final CharSequence moreSpecificObject) {
        return new Message(messageType, "Status %s not allowed when more specific object '%s' has status %s", givenStatus, moreSpecificObject, childStatus);
    }

    public static Message intersectingRange(final Interval<?> intersectingRange) {
        return new Message(Type.ERROR, "This range overlaps with %s", intervalToString(intersectingRange));
    }

    public static Message intersectingDomain(final CIString domainKey) {
        return new Message(Type.ERROR, "This domain overlaps with %s", domainKey);
    }

    public static Message inetnumStatusLegacy() {
        return new Message(Type.ERROR, "Only RIPE NCC can create/delete a top level object with status 'LEGACY'\nContact legacy@ripe.net for more info");
    }

    private static CharSequence intervalToString(final Interval<?> interval) {
        return switch (interval) {
            case Ipv4Resource ipv4Resource -> ipv4Resource.toRangeString();
            case Ipv6Resource ipv6Resource -> ipv6Resource.toString();
        };
    }

    public static Message createFirstPersonMntnerForOrganisation() {
        return new Message(Type.INFO, "To create the first person/mntner pair of objects for an organisation see\nhttps://apps.db.ripe.net/db-web-ui/webupdates/create/RIPE/person/self");
    }

    public static Message maintainerNotFound(final CharSequence maintainer) {
        return new Message(Type.ERROR, "The maintainer '%s' was not found in the database", maintainer);
    }

    public static Message cantChangeAssignmentSize() {
        return new Message(Type.ERROR, "\"assignment-size:\" value cannot be changed");
    }

    public static Message attributeAssignmentSizeNotAllowed(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"\"assignment-size:\" attribute only allowed with status %s", Inet6numStatus.AGGREGATED_BY_LIR.getLiteralStatus());
    }

    public static Message assignmentSizeTooSmall(final int prefixLength) {
        return new Message(Type.ERROR, "\"assignment-size:\" value must be greater than prefix size %s", prefixLength);
    }

    public static Message assignmentSizeTooLarge(final int prefixLength) {
        return new Message(Type.ERROR, "\"assignment-size:\" value must not be greater than the maximum prefix size %s", prefixLength);
    }

    public static Message prefixTooSmall(final int minimumPrefixLength) {
        return new Message(Type.ERROR, "Minimum prefix size is %s", minimumPrefixLength);
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

    public static Message membersByRefChangedInSet(final Set<String> asName) {
        return new Message(Type.WARNING, "Changing mbrs-by-ref:  may cause updates to %s to fail, because the member-of: reference in %s is no longer protected", asName, asName);
    }

    public static Message dnsCheckTimeout() {
        return new Message(Type.ERROR, "Timeout performing DNS check");
    }

    public static Message dnsCheckMessageParsingError() {
        return new Message(Type.ERROR, "Error parsing response while performing DNS check");
    }

    public static Message dnsCheckError() {
        return new Message(Type.ERROR, "Error from DNS check");
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

    public static Message hostNameMustEndWith(final RpslAttribute attribute, final CharSequence s) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Glue records only allowed if hostname ends with %s", s);
    }

    public static Message glueRecordMandatory(final RpslAttribute attribute, final CharSequence s) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Glue record is mandatory if hostname ends with %s", s);
    }

    public static Message invalidGlueForEnumDomain(final RpslAttribute attribute, final CharSequence s) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Enum domain has invalid glue %s", s);
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForDeleteRsMaintainedObject() {
        return new Message(Type.ERROR, "Deleting this object requires administrative authorisation");
    }

    // NOTE: this errormessage is being used by webupdates.
    public static Message authorisationRequiredForChangingRipeMaintainer() {
        return new Message(Type.ERROR, "You cannot add or remove a RIPE NCC maintainer");
    }

    public static Message poemRequiresPublicMaintainer(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute,"Poem must be maintained by 'LIM-MNT', using a public PGP key-cert");
    }

    public static Message poeticFormRequiresDbmMaintainer(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Poetic-form must only be maintained by 'RIPE-DBM-MNT'");
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

    public static Message autokeyForX509KeyCertsOnly(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "AUTO-nnn can only be used with X509 key-cert");
    }

    public static Message noParentAsBlockFound(final CharSequence asNumber) {
        return new Message(Type.ERROR, "No parent as-block found for %s", asNumber);
    }

    public static Message certificateNotYetValid(final CharSequence name) {
        return new Message(Type.ERROR, "Certificate in keycert %s is not yet valid", name);
    }

    public static Message certificateHasExpired(final CharSequence name) {
        return new Message(Type.ERROR, "Certificate in keycert %s has expired", name);
    }

    public static Message certificateHasWeakHash(final CharSequence name, final CharSequence hash) {
        return new Message(Type.ERROR, "Certificate in keycert %s uses a weak hash algorithm %s", name, hash);
    }

    public static Message publicKeyHasExpired(final CharSequence name) {
        return new Message(Type.ERROR, "Public key in keycert %s has expired", name);
    }

    public static Message publicKeyIsRevoked(final CharSequence name) {
        return new Message(Type.ERROR, "Public key in keycert %s is revoked", name);
    }

    public static Message publicKeyLengthIsWeak(final CharSequence algorithm, final int minimum, final int actual) {
        return new Message(Type.ERROR, "%s public key is %d bits which is less than the minimum %d", algorithm, actual, minimum);
    }

    public static Message cannotCreateOutOfRegionObject(final ObjectType objectType) {
        return new Message(Type.ERROR, "Cannot create out of region %s objects", objectType.getName());
    }

    public static Message sourceNotAllowed(final ObjectType objectType, final CharSequence source) {
        return new Message(Type.ERROR, "Source %s is not allowed for %s objects", source, objectType.getName());
    }

    public static Message cannotUseReservedAsNumber(final Long asNumber) {
        return new Message(Type.ERROR, "Cannot use reserved AS number %d", asNumber);
    }

    public static Message autnumNotFoundInDatabase(final Long asNumber) {
        return new Message(Type.WARNING, "Specified origin AS number %d is allocated to the RIPE region but doesn't exist in the RIPE database", asNumber);
    }

    public static Message messageSignedMoreThanOneHourAgo() {
        return new Message(Type.ERROR, "Message was signed more than one hour ago");
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

    public static Message abuseMailboxRequired(final CharSequence key, final ObjectType objectType) {
        return new Message(Type.ERROR,
                "The \"abuse-c\" ROLE object '%s' has no \"abuse-mailbox:\"\n"
                        + "Add \"abuse-mailbox:\" to the ROLE object, then update the %s object", key, objectType.getName().toUpperCase());
    }

    public static Message abuseCPersonReference() {
        return new Message(Type.ERROR,
                "\"abuse-c:\" references a PERSON object\n"
                        + "This must reference a ROLE object with an \"abuse-mailbox:\"");
    }

    public static Message abuseMailboxReferenced(final CharSequence role, final ObjectType objectType) {
        return new Message(Type.ERROR, "There is an %s referencing role %s's abuse-mailbox", objectType.getName(), role);
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

    public static Message duplicateAbuseC(final CharSequence abuseC, final CharSequence organisation) {
        return new Message(Type.WARNING, "Duplicate abuse-c \"%s\" also found in referenced organisation \"%s\".", abuseC, organisation);
    }

    public static Message abuseContactNotRemovable() {
        return new Message(Type.ERROR, "\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object");
    }

    public static Message selfReferenceError(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Self reference is not allowed for attribute type \"%s\"", attribute.getType().getName());
    }

    public static Message noAbuseContact(final CIString orgId) {
        return new Message(Type.ERROR, "%s must include an \"abuse-c:\" attribute", orgId);
    }

    public static Message commentInSourceNotAllowed() {
        return new Message(Type.ERROR, "End of line comments not allowed on \"source:\" attribute");
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

    public static Message statusCannotBeRemoved() {
        return new Message(Type.WARNING, "\"status:\" attribute cannot be removed");
    }

    public static Message emailCanNotBeSent(final String email, final EmailStatusType emailStatus) {
        return new Message(Type.WARNING, "Not sending notification to %s because it is %s.", email, emailStatus.getValue());
    }

    public static Message sponsoringOrgChanged() {
        return new Message(Type.ERROR, "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC");
    }

    public static Message sponsoringOrgAdded() {
        return new Message(Type.ERROR, "The \"sponsoring-org\" attribute can only be added by the RIPE NCC");
    }

    public static Message sponsoringOrgRemoved() {
        return new Message(Type.ERROR, "The \"sponsoring-org\" attribute can only be removed by the RIPE NCC");
    }

    public static Message sponsoringOrgNotLIR(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Referenced organisation must have org-type: LIR");
    }

    public static Message sponsoringOrgNotAllowedWithStatus(final CharSequence status) {
        return new Message(Type.ERROR, "The \"sponsoring-org:\" attribute is not allowed with status value \"%s\"", status);
    }

    public static Message sponsoringOrgMustBePresent() {
        return new Message(Type.ERROR, "This resource object must be created with a sponsoring-org attribute");
    }

    public static Message valueChangedDueToLatin1Conversion() {
        return new Message(Type.WARNING, "Value changed due to conversion into the ISO-8859-1 (Latin-1) character set");
    }

    public static Message valueChangedDueToLatin1Conversion(final String attributeName) {
        return new Message(Type.WARNING, "Invalid character(s) were substituted in attribute \"%s\" value", attributeName);
    }

    public static Message valueChangedDueToPunycodeConversion() {
        return new Message(Type.WARNING, "Value changed due to conversion of IDN email address(es) into Punycode");
    }

    public static Message oldPasswordsRemoved() {
        return new Message(Type.WARNING, "MD5 passwords older than November 2011 were removed for one or more maintainers of this object, see: https://www.ripe.net/removed2011pw");
    }

    public static Message creatingRipeMaintainerForbidden() {
        return new Message(Type.ERROR, "You cannot create a RIPE NCC maintainer");
    }

    public static Message updatingRipeMaintainerSSOForbidden() {
        return new Message(Type.ERROR, "You cannot update SSO auth attribute(s), because the maintainer is synchronised from the LIR Portal");
    }

    public static Message originIsMissing() {
        return new Message(Type.ERROR, "Origin of the request is missing");
    }

    public static Message netnameCannotBeChanged() {
        return new Message(Type.ERROR, "The \"netname\" attribute can only be changed by the RIPE NCC");
    }

    public static Message multipleUserMntBy(final Collection<CIString> userMntners) {
        return new Message(Type.ERROR, "Multiple user-'mnt-by:' are not allowed, found are: '%s'", Joiner.on(", ").join(userMntners));
    }
    public static Message sourceChanged(final CIString originalSource, final CIString finalSource, final String autnum) {
        return new Message(Messages.Type.WARNING, "The \"source:\" attribute value has been updated from \"%s\" to " +
                "\"%s\" to match the referenced AUT-NUM \"%s\"", originalSource, finalSource, autnum);
    }
    public static Message changedAttributeRemoved() {
        return new Message(Messages.Type.WARNING, "Deprecated attribute \"changed\". This attribute has been removed.");
    }

    public static Message mntRoutesAttributeRemoved() {
        return new Message(Messages.Type.WARNING, "Deprecated attribute \"mnt-routes\". This attribute has been removed.");
    }

    public static Message mntLowerAttributeRemoved() {
        return new Message(Messages.Type.WARNING, "Deprecated attribute \"mnt-lower\". This attribute has been removed.");
    }

    public static Message emailAddressCannotBeUsed(final RpslAttribute attribute, final CIString value) {
        return new MessageWithAttribute(Type.ERROR, attribute,"The email address \"%s\" cannot be used.", value);
    }

    public static Message inconsistentOrgNameFormatting(final RpslAttribute attribute) {
        return new MessageWithAttribute(Type.ERROR, attribute, "Tab characters, multiple lines, or multiple whitespaces are not allowed in the \"org-name:\" value.");
    }

    public static Message shortFormatAttributeReplaced() {
        return new Message(Type.WARNING, "Short format attribute name(s) have been replaced.");
    }

    public static Message bogonPrefixNotAllowed(final String prefix) {
        return new Message(Type.ERROR, "Bogon prefix %s is not allowed.", prefix);
    }

    public static Message maximumObjectSizeExceeded(final long size, final long maximumSize) {
        return new Message(Type.ERROR, "Ignored object with size %d, exceeds maximum object size %d.", size, maximumSize);
    }

    public static Message eitherAttributeOrRemarksIsAllowed(final String attribute) {
        return new Message(Type.ERROR, String.format("Only one between the \"%s:\" and \"remark: %s:\" attributes is allowed.", attribute, attribute));
    }

    public static Message incorrectPrefixForRipeNsServer() {
        return new Message(Type.ERROR, "Prefix length must be /16 for IPv4 or /32 for IPv6 if ns.ripe.net is used as " +
                "a nameserver.");
    }

    public static Message tooManyReferences() {
        return new Message(Type.ERROR, "Too many references");
    }

    public static Message passwordsNotSupported() {
        return new Message(Type.ERROR, "MD5 hashed password authentication is deprecated. Please switch to an alternative authentication method.");
    }
}
