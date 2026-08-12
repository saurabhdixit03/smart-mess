
package com.smartmess.backend.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @Size(
                max = 500,
                message = "Remarks cannot exceed 500 characters"
        )
        String remarks
        
) {
}
