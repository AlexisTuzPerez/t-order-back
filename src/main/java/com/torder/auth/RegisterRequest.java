package com.torder.auth;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.torder.negocioCliente.NegocioCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @JsonProperty("firstname")  // Map JSON key "firstname" to this field
    private String firstname;

    @JsonProperty("lastname")   // Map JSON key "lastname" to this field
    private String lastname;

    private String email;
    private String password;
    private NegocioCliente negocio;

}
