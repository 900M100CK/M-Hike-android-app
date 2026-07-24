package com.example.m_hikeapp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable value object that carries the outcome of a validation run.
 *
 * <p>A {@code ValidationResult} is considered valid when {@link #isValid()}
 * returns {@code true}.  Individual field errors are accessible via
 * {@link #getErrors()}, keyed by the field name constants defined in
 * {@link ValidationUtils}.</p>
 */
public class ValidationResult {

    private final boolean            valid;
    private final Map<String, String> errors; // fieldKey -> human-readable message

    /** Creates a passing (valid) result with no errors. */
    public static ValidationResult success() {
        return new ValidationResult(true, new HashMap<>());
    }

    /**
     * Creates a failing result with the supplied error map.
     *
     * @param errors Map of field-name -> error message.
     */
    public static ValidationResult failure(Map<String, String> errors) {
        return new ValidationResult(false, errors);
    }

    private ValidationResult(boolean valid, Map<String, String> errors) {
        this.valid  = valid;
        this.errors = errors;
    }

    /** @return {@code true} if all validation rules passed. */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return Map of field names to their error messages.
     *         Empty map when {@link #isValid()} is {@code true}.
     */
    public Map<String, String> getErrors() {
        return errors;
    }

    /**
     * Returns the error message for a specific field.
     *
     * @param fieldKey One of the constants from {@link ValidationUtils}.
     * @return Error message string or {@code null} if field passed validation.
     */
    public String getError(String fieldKey) {
        return errors.get(fieldKey);
    }

    /** @return {@code true} if the given field has an associated error. */
    public boolean hasError(String fieldKey) {
        return errors.containsKey(fieldKey);
    }
}
