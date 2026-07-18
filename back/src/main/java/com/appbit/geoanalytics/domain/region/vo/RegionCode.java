package com.appbit.geoanalytics.domain.region.vo;

import com.appbit.geoanalytics.domain.region.exception.MissingRegionCodeException;
import com.appbit.geoanalytics.domain.region.exception.RegionDomainException;

import java.text.Normalizer;
import java.util.Locale;

public record RegionCode(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 80;

    public RegionCode {
        if (value == null) {
            throw new MissingRegionCodeException("RegionCode cannot be null");
        }

        value = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toUpperCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new MissingRegionCodeException("RegionCode cannot be blank");
        }

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new RegionDomainException(
                    "RegionCode length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
            );
        }

        validateFormat(value);
    }

    private void validateFormat(String value) {
        if (!isAlphaNumeric(value.charAt(0))) {
            throw new RegionDomainException("RegionCode must start with an uppercase letter or digit");
        }

        if (!isAlphaNumeric(value.charAt(value.length() - 1))) {
            throw new RegionDomainException("RegionCode must end with an uppercase letter or digit");
        }

        boolean previousWasSeparator = false;

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);

            if (isAlphaNumeric(current)) {
                previousWasSeparator = false;
                continue;
            }

            if (isSeparator(current)) {
                if (previousWasSeparator) {
                    throw new RegionDomainException("RegionCode cannot contain consecutive separators");
                }

                previousWasSeparator = true;
                continue;
            }

            throw new RegionDomainException(
                    "RegionCode can only contain uppercase letters, digits, hyphen or underscore"
            );
        }
    }

    private boolean isAlphaNumeric(char value) {
        return (value >= 'A' && value <= 'Z') || (value >= '0' && value <= '9');
    }

    private boolean isSeparator(char value) {
        return value == '-' || value == '_';
    }
}