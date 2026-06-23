package com.taskflow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB infrastructure configuration.
 *
 * <p>Enables Spring Data auditing so {@code @CreatedDate}/{@code @LastModifiedDate}
 * fields (e.g. {@code User.createdAt}/{@code updatedAt}) are populated on insert
 * and update — see {@code docs/DATABASE.md} §8.
 *
 * <p>TODO (future phases): custom converters and explicit index/$jsonSchema
 * registration as described in {@code docs/ARCHITECTURE.md} §3.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
