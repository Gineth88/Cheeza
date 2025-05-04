package com.cheeza.Cheeza.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "email is required")
    @Email
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6,message = "password must at least be 6 characters")
    private String password;

    @NotBlank(message = "full name is required")
    private String fullName;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String password;
        private String fullName;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public RegisterRequest build() {
            RegisterRequest request = new RegisterRequest();
            request.email = this.email;
            request.password = this.password;
            request.fullName = this.fullName;
            return request;
        }
    }

}
