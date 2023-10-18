package net.ripe.db.whois.api.rdap.domain;

public class PreparedParentRedaction {

    private RdapObject parentObject;

    private String handle;
    private String subFolder;


    public PreparedParentRedaction(final RdapObject parentObject, final String handle, final String subFolder){
        this.parentObject = parentObject;
        this.handle = handle;
        this.subFolder = subFolder;
    }

    public RdapObject getParentObject() {
        return parentObject;
    }

    public void setParentObject(RdapObject parentObject) {
        this.parentObject = parentObject;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
