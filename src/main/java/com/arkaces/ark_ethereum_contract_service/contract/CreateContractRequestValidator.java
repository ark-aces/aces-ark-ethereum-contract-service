package com.arkaces.ark_ethereum_contract_service.contract;

import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.common.error.ErrorCodes;
import com.arkaces.aces_server.common.error.FieldError;
import com.arkaces.aces_server.common.error.ValidationError;
import com.arkaces.aces_server.common.error.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateContractRequestValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void validate(CreateContractRequest<Arguments> createContractRequest) {
        List<FieldError> fieldErrors = new ArrayList<>();

        Arguments arguments = createContractRequest.getArguments();
        if (arguments == null) {

        } else {
            String contractAbiJson = arguments.getContractAbiJson();
            if (StringUtils.isBlank(contractAbiJson)) {
                fieldErrors.add(createRequierdFieldError("contractAbiJson"));
            } else {
                if (! isJson(contractAbiJson)) {
                    fieldErrors.add(createInvalidJsonFieldError("contractAbiJson"));
                }
            }

            if (StringUtils.isBlank(arguments.getContractCode())) {
                fieldErrors.add(createRequierdFieldError("contractCode"));
            } else {
                if (! isHexString(arguments.getContractCode())) {
                    FieldError fieldError = new FieldError();
                    fieldError.setCode("invalid");
                    fieldError.setField("contractCode");
                    fieldError.setMessage("Contract code must be a hex string beginning with \"0x\".");
                    fieldErrors.add(fieldError);
                }
            }

            String gasLimit = arguments.getGasLimit();
            if (gasLimit == null) {
                fieldErrors.add(createRequierdFieldError("gasLimit"));
            } else {
                if (! isBigInteger(gasLimit)) {
                    FieldError fieldError = new FieldError();
                    fieldError.setCode("invalid");
                    fieldError.setField("gasLimit");
                    fieldError.setMessage("Gas limit must be an integer.");
                    fieldErrors.add(fieldError);
                } else {
                    BigInteger gasLimitValue = new BigInteger(gasLimit);
                    if (gasLimitValue.compareTo(BigInteger.ZERO) <= 0) {
                        FieldError fieldError = new FieldError();
                        fieldError.setCode("invalid");
                        fieldError.setField("gasLimit");
                        fieldError.setMessage("Gas limit must be greater than 0.");
                        fieldErrors.add(fieldError);
                    }
                }
            }
        }

        if (fieldErrors.size() > 0) {
            ValidationError validationError = new ValidationError();
            validationError.setCode(ErrorCodes.BAD_REQUEST);
            validationError.setMessage("Contract parameters are invalid");
            validationError.setFieldErrors(fieldErrors);
            throw new ValidationException(ErrorCodes.BAD_REQUEST, "Request parameters are invalid", fieldErrors, null);
        }
    }

    private FieldError createInvalidJsonFieldError(String propertyName) {
        FieldError fieldError = new FieldError();
        fieldError.setCode("invalid");
        fieldError.setMessage("Must contain valid JSON.");
        fieldError.setField(propertyName);
        return fieldError;
    }

    private FieldError createRequierdFieldError(String propertyName) {
        FieldError fieldError = new FieldError();
        fieldError.setCode("valueRequired");
        fieldError.setMessage("Value is required.");
        fieldError.setField(propertyName);
        return fieldError;
    }

    private boolean isJson(String input) {
        try {
            objectMapper.readValue(input, JsonNode.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isBigInteger(String input) {
        return input.matches("[0-9]+");
    }

    private boolean isHexString(String input) {
        return input.matches("0x[a-fA-F0-9]+");
    }

}
