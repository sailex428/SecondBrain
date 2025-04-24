package me.sailex.secondbrain.exception;

public class LLMServiceException extends RuntimeException {
	public LLMServiceException(String message) {
		super(message);
	}

	public LLMServiceException() {
		super();
	}

	public LLMServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
