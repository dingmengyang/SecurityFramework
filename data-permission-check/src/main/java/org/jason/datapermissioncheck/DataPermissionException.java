package org.jason.datapermissioncheck;


/**
 * @author jason
 */
public class DataPermissionException extends RuntimeException {
    private static final long serialVersionUID = 8158119442100978017L;
    private String code;
    private String errorMessage;

    public DataPermissionException() {
        super();
        this.code = "9527";
        this.errorMessage = "数据无法访问";
    }

    public DataPermissionException(String errorMessage) {
        super(errorMessage);
        this.code = "9527";
        this.errorMessage = errorMessage;
    }

    public DataPermissionException(String code, String errorMessage) {
        super(errorMessage);
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

