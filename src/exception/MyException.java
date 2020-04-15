package exception;

public class MyException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyExceptionCode code;
    private String description;

    public MyException(MyExceptionCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public MyExceptionCode getCode() {
        return code;
    }

    public MyException setCode(MyExceptionCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MyException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "MyException{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
