package br.com.escrevaevolua.identity.adapter.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escrevaevolua.identity.application.IdentityOperationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityOperationExceptionMapperTest {

    private final IdentityOperationExceptionMapper mapper = new IdentityOperationExceptionMapper();

    @Test
    void mapsRejectedOperationsAndInternalFailuresToGenericResponses() {
        ResponseEntity<IdentityOperationExceptionMapper.ErrorResponse> rejected = mapper.toResponse(IdentityOperationException.rejected());
        ResponseEntity<IdentityOperationExceptionMapper.ErrorResponse> failed = mapper.toResponse(IdentityOperationException.internalFailure());

        assertEquals(400, rejected.getStatusCode().value());
        assertEquals(500, failed.getStatusCode().value());
        assertEquals(rejected.getBody(), failed.getBody());
        assertEquals("Não foi possível concluir a operação de identidade.",
                failed.getBody().message());
    }
}
