package anon.ecco.experiment.exception;

public class ExperimentException extends RuntimeException{
    public ExperimentException() {
        super();
    }

    public ExperimentException(String message) {
        super(message);
    }

    public ExperimentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExperimentException(Throwable cause) {
        super(cause);
    }
}
