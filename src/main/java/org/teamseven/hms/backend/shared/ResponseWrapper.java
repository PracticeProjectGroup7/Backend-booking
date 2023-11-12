package org.teamseven.hms.backend.shared;

import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;

public sealed class ResponseWrapper {
    private ResponseWrapper() {
    }

    @NoArgsConstructor(force = true)
    public static final class Success<T> extends ResponseWrapper {
        private final @Nullable T data;

        public Success(T data) {
            super();
            this.data = data;
        }

        @Nullable
        public T getData() {
            return data;
        }
    }

    public static sealed class GenericError extends ResponseWrapper {
        private static final String ERR_DEFAULT_MESSAGE
                = "Something unexpected happened. Please try again later.";
        private final String errorMessage;
        public GenericError(String errorMessage) {
            this.errorMessage = errorMessage != null ? errorMessage : ERR_DEFAULT_MESSAGE;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static final class BadRequest extends GenericError{
        private final @Nullable String[] errorFields;

        public BadRequest(String errorMessage, @Nullable String[] errorFields) {
            super(errorMessage);
            this.errorFields = errorFields;
        }

        @Nullable
        public String[] getErrorFields() {
            return errorFields;
        }
    }
}
