package procotol;


/** HTTP Status. */
public enum Status {

    OK(200, "OK"),
    CREATED(201, "CREATED"),
    BAD_REQUEST(400, "BAD REQUEST"),
    FORBIDDEN(403, "FORBIDDEN"),
    NOT_FOUND(404, "NOT FOUND"),
    GONE(410, "GONE"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR");

    public final int code;
    public final String codeName;

    private Status(final int code, final String codename) {
        this.code = code;
        codeName = codename;
    }
    
}
