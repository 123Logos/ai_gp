package com.aigp.demo.domain.user;

/** Values match MySQL ENUM on {@code user_identities.identity_type}. */
public enum IdentityType {
	phone,
	email,
	wx,
	apple,
	google
}
