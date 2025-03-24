package ru.marattim.todolist.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer customize() {
        return openApi -> {
            Schema<?> requestSchema = new ObjectSchema()
                .addProperty("username", new Schema<String>().type("string"))
                .addProperty("password", new Schema<String>().type("string"));

            RequestBody requestBody = new RequestBody()
                .content(new Content()
                    .addMediaType("application/x-www-form-urlencoded",
                        new MediaType().schema(requestSchema)));

            ApiResponses responses = new ApiResponses()
                .addApiResponse("200", new ApiResponse().description("OK"))
                .addApiResponse("403", new ApiResponse().description("Forbidden"));

            Operation operation = new Operation()
                .addTagsItem("login-endpoint")
                .requestBody(requestBody)
                .responses(responses);

            PathItem pathItem = new PathItem().post(operation);

            openApi.getPaths().addPathItem("/login", pathItem);
        };
    }
}

